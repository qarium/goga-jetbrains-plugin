# GitHub Release Action Design

## Summary
Add a GitHub Actions workflow that triggers on release tag pushes, validates the tag format, builds the IntelliJ plugin distribution archive, creates a GitHub Release for that tag, and uploads the generated plugin ZIP as the only release asset.

## Goals
- Start automatically when a tag is pushed.
- Accept only release-like tags such as `v1.2.3` and `v1.2.3-beta.1`.
- Build the plugin using the existing Gradle project setup.
- Fail early if the release ZIP is missing or ambiguous.
- Create the GitHub Release automatically.
- Attach the plugin installation archive to the release.

## Non-Goals
- Synchronizing the Git tag with `pluginVersion` in `gradle.properties` or `plugin.xml`.
- Publishing checksums or additional build artifacts.
- Publishing to JetBrains Marketplace.

## Proposed Approach
Use a single workflow file at `.github/workflows/release.yml`.

The workflow will:
1. Trigger on pushes to tags.
2. Extract the pushed tag name from GitHub Actions context.
3. Validate the tag name against a release regex.
4. Set up JDK 21 and Gradle caching.
5. Run the Gradle task that produces the plugin distribution ZIP.
6. Inspect `build/distributions/` and select the generated ZIP.
7. Fail if no ZIP is found.
8. Create a GitHub Release named from the tag.
9. Upload the ZIP as the release asset.

## Architecture
### Trigger and tag handling
The workflow will use `on.push.tags` so it only reacts to tag pushes. The tag string from the GitHub event becomes the source of truth for the Release tag and title.

### Build execution
The workflow will use the repository's Gradle wrapper and Java 21, matching the project toolchain in `build.gradle.kts`. It will run the plugin distribution build task rather than a generic release script.

### Artifact validation
After the build step, the workflow will inspect `build/distributions/*.zip`. If no ZIP is present, the workflow fails with a clear error. If multiple ZIPs appear, the workflow will choose the first matching distribution ZIP and log its path; if needed later this can be tightened further.

### Release creation
The workflow will use the default `GITHUB_TOKEN` to create the Release in the same repository. The Release body can stay minimal, because the current requirement is only automatic creation and asset attachment.

## Error Handling
- Invalid tag format -> fail before Gradle runs.
- Build failure -> fail the workflow and create no release.
- Missing ZIP -> fail before release creation.
- Release creation/upload failure -> fail the workflow with GitHub Actions logs.

## Testing Strategy
- Validate workflow syntax locally by inspecting YAML carefully.
- Keep the workflow logic simple and shell-based.
- Recommend one manual end-to-end verification using a test tag in GitHub after merge.

## Files to Change
- Create `.github/workflows/release.yml`
- Optionally add a short note to project docs if needed, but not required for the initial change.

## Open Decisions Resolved
- Release is created automatically by CI.
- Trigger is tag push.
- Release version is taken from the tag name only.
- Only the plugin installation ZIP is attached.
- Tag validation is enabled for `v1.2.3` and `v1.2.3-beta.1` style tags.
