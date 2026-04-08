package com.QArium.hidebyextension.actions

import com.QArium.hidebyextension.settings.HideExtensionSettings
import com.intellij.ide.projectView.ProjectView
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.project.ProjectManager

class ToggleHideAction : ToggleAction() {

    override fun isSelected(e: AnActionEvent): Boolean {
        return HideExtensionSettings.getInstance().enabled
    }

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        HideExtensionSettings.getInstance().enabled = state
        refreshProjectView()
    }

    private fun refreshProjectView() {
        for (project in ProjectManager.getInstance().openProjects) {
            ProjectView.getInstance(project).refresh()
        }
    }
}
