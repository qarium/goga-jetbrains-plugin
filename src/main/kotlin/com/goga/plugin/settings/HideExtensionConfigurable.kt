package com.goga.plugin.settings

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.panel
import javax.swing.JComponent

class HideExtensionConfigurable : Configurable {

    private val settings = HideExtensionSettings.getInstance()

    private lateinit var enabledCheckBox: JBCheckBox
    private lateinit var extensionsField: JBTextField
    private lateinit var codemanifestCheckBox: JBCheckBox
    private var created = false

    override fun getDisplayName(): String = "Goga"

    override fun createComponent(): JComponent {
        created = true
        return panel {
            group("Hide Files by Extension") {
                row {
                    enabledCheckBox = checkBox("Enable file hiding")
                        .component
                }
                row("Extensions (comma-separated):") {
                    extensionsField = textField()
                        .align(AlignX.FILL)
                        .component
                    comment("e.g. log, tmp, generated")
                }
            }
            group("File Type Association") {
                row {
                    codemanifestCheckBox = checkBox("Associate CODEMANIFEST as YAML")
                        .component
                }
                row {
                    comment("Treats files named exactly <b>CODEMANIFEST</b> (no extension) as YAML files")
                        .component
                }
            }
        }
    }

    override fun isModified(): Boolean {
        if (!created) return false
        return enabledCheckBox.isSelected != settings.enabled ||
                extensionsField.text != settings.extensions.joinToString(", ") ||
                codemanifestCheckBox.isSelected != settings.codemanifestAsYaml
    }

    override fun apply() {
        settings.enabled = enabledCheckBox.isSelected
        settings.extensions = extensionsField.text
            .split(",")
            .map { it.trim().trimStart('.') }
            .map { it.lowercase() }
            .filter { it.isNotEmpty() }

        val codemanifestChanged = settings.codemanifestAsYaml != codemanifestCheckBox.isSelected
        settings.codemanifestAsYaml = codemanifestCheckBox.isSelected
        if (codemanifestChanged) {
            CodemanifestAssociation.applySetting(settings.codemanifestAsYaml)
        }

        refreshProjectView()
    }

    private fun refreshProjectView() {
        for (project in com.intellij.openapi.project.ProjectManager.getInstance().openProjects) {
            val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("Project") ?: continue
            val component = toolWindow.component
            if (component is javax.swing.JComponent) {
                component.updateUI()
            }
            // Force Project View tree to rebuild from scratch
            val projectView = com.intellij.ide.projectView.ProjectView.getInstance(project)
            projectView.refresh()
        }
    }

    override fun reset() {
        if (!created) return
        enabledCheckBox.isSelected = settings.enabled
        extensionsField.text = settings.extensions.joinToString(", ")
        codemanifestCheckBox.isSelected = settings.codemanifestAsYaml
    }

    override fun disposeUIResources() {
        created = false
    }
}
