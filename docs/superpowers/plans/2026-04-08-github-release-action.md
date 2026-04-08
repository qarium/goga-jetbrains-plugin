# GitHub Release Action Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a GitHub Actions workflow that triggers on release tag pushes, builds the IntelliJ plugin ZIP with Gradle, creates a GitHub Release, and uploads the ZIP as the only release asset.

**Architecture:** The implementation is a single workflow file at `.github/workflows/release.yml`. The workflow validates the pushed tag format, builds the plugin via `buildPlugin`, locates the generated ZIP in `build/distributions`, and then creates a release plus uploads the ZIP using GitHub Actions-native steps and the repository `GITHUB_TOKEN`.

**Tech Stack:** GitHub Actions YAML, Bash, Gradle Wrapper, Java 21, JetBrains IntelliJ Platform Gradle Plugin

---

## File Structure

- Create: `.github/workflows/release.yml` — release workflow triggered by tag push; contains tag validation, build, artifact discovery, release creation, and asset upload.
- Reference: `build.gradle.kts` — confirms Java toolchain 21 and the `buildPlugin` task from the IntelliJ Gradle plugin.
- Reference: `docs/superpowers/specs/2026-04-08-github-release-action-design.md` — approved design for this work.

### Task 1: Create the release workflow

**Files:**
- Create: `.github/workflows/release.yml`
- Reference: `build.gradle.kts`
- Reference: `docs/superpowers/specs/2026-04-08-github-release-action-design.md`

- [ ] **Step 1: Write the workflow file with failing-safe validation and release logic**

Create `.github/workflows/release.yml` with exactly this content:

```yaml
name: Release Plugin

on:
  push:
    tags:
      - '*'

permissions:
  contents: write

jobs:
  release:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout repository
        uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '21'

      - name: Set up Gradle cache
        uses: gradle/actions/setup-gradle@v4

      - name: Extract and validate tag
        id: tag
        shell: bash
        run: |
          set -euo pipefail

          TAG_NAME="${GITHUB_REF_NAME}"
          if [[ ! "$TAG_NAME" =~ ^v[0-9]+\.[0-9]+\.[0-9]+(-[A-Za-z0-9]+\.[0-9]+)?$ ]]; then
            echo "Unsupported release tag: $TAG_NAME"
            echo "Expected examples: v1.2.3 or v1.2.3-beta.1"
            exit 1
          fi

          echo "tag_name=$TAG_NAME" >> "$GITHUB_OUTPUT"

      - name: Build plugin distribution
        run: ./gradlew buildPlugin

      - name: Locate plugin ZIP
        id: artifact
        shell: bash
        run: |
          set -euo pipefail

          shopt -s nullglob
          files=(build/distributions/*.zip)

          if [[ ${#files[@]} -eq 0 ]]; then
            echo "No plugin ZIP found in build/distributions"
            exit 1
          fi

          if [[ ${#files[@]} -gt 1 ]]; then
            echo "Multiple ZIP files found in build/distributions:"
            printf ' - %s\n' "${files[@]}"
            exit 1
          fi

          zip_path="${files[0]}"
          zip_name="$(basename "$zip_path")"

          echo "zip_path=$zip_path" >> "$GITHUB_OUTPUT"
          echo "zip_name=$zip_name" >> "$GITHUB_OUTPUT"

      - name: Create GitHub release
        id: create_release
        uses: softprops/action-gh-release@v2
        with:
          tag_name: ${{ steps.tag.outputs.tag_name }}
          name: ${{ steps.tag.outputs.tag_name }}
          files: ${{ steps.artifact.outputs.zip_path }}
          fail_on_unmatched_files: true
          generate_release_notes: true
```

- [ ] **Step 2: Verify the workflow file was written correctly**

Run:

```bash
sed -n '1,240p' .github/workflows/release.yml
```

Expected: output shows the `Release Plugin` workflow with `on.push.tags`, `permissions.contents: write`, `./gradlew buildPlugin`, tag regex validation, ZIP lookup in `build/distributions`, and `softprops/action-gh-release@v2` uploading the ZIP file.

- [ ] **Step 3: Review the validation and artifact rules against the approved spec**

Run:

```bash
grep -nE 'tags:|buildPlugin|build/distributions|action-gh-release|Expected examples' .github/workflows/release.yml
```

Expected: matching lines confirm the workflow triggers on tags, builds with `buildPlugin`, reads `build/distributions`, uses release upload action, and prints the accepted tag examples.

### Task 2: Perform local verification of workflow structure

**Files:**
- Verify: `.github/workflows/release.yml`

- [ ] **Step 1: Check YAML indentation and key structure visually**

Run:

```bash
awk '{printf("%4d  %s\n", NR, $0)}' .github/workflows/release.yml | sed -n '1,240p'
```

Expected: indentation is consistent with YAML nesting; `permissions`, `jobs.release`, and each `steps` entry are aligned correctly.

- [ ] **Step 2: Confirm the workflow contains a single ZIP attachment path and hard-fails on missing or multiple archives**

Run:

```bash
grep -nE 'No plugin ZIP found|Multiple ZIP files found|fail_on_unmatched_files|zip_path=' .github/workflows/release.yml
```

Expected: output shows all four safeguards present so the workflow fails instead of publishing a broken release.

- [ ] **Step 3: Confirm the workflow uses the project's required Java version**

Run:

```bash
grep -nE 'jvmToolchain\(21\)|java-version: '\''21'\''' build.gradle.kts .github/workflows/release.yml
```

Expected: output shows `jvmToolchain(21)` in `build.gradle.kts` and `java-version: '21'` in the workflow.

### Task 3: Document the manual end-to-end verification to run on GitHub

**Files:**
- Verify: `.github/workflows/release.yml`
- Reference: `docs/superpowers/specs/2026-04-08-github-release-action-design.md`

- [ ] **Step 1: Prepare the exact release verification checklist**

Use this checklist after the workflow is merged:

```text
1. Push a tag such as v1.0.1 to GitHub.
2. Open the Actions tab and wait for the "Release Plugin" workflow to finish.
3. Open the Releases page.
4. Verify a Release named v1.0.1 was created automatically.
5. Verify exactly one ZIP asset is attached.
6. Download the ZIP and confirm the filename matches the archive built by Gradle.
```

- [ ] **Step 2: Prepare the exact negative-path verification checklist**

Use this checklist to validate the tag guard:

```text
1. Push a non-release tag such as test-release.
2. Open the Actions tab.
3. Verify the workflow fails in the "Extract and validate tag" step.
4. Verify no GitHub Release is created for that tag.
```

- [ ] **Step 3: Record the operator command to inspect the built ZIP name locally when needed**

Run:

```bash
ls -1 build/distributions/*.zip
```

Expected: when `./gradlew buildPlugin` has been run successfully, the command prints exactly one plugin ZIP path. If nothing matches, the build has not produced the distributable archive yet.
