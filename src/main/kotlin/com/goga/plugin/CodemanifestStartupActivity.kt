package com.goga.plugin

import com.goga.plugin.settings.CodemanifestAssociation
import com.goga.plugin.settings.HideExtensionSettings
import com.intellij.openapi.application.ApplicationActivationListener
import com.intellij.openapi.wm.IdeFrame

class CodemanifestAppListener : ApplicationActivationListener {

    override fun applicationActivated(ideFrame: IdeFrame) {
        applyIfNeeded()
    }

    private var applied = false

    private fun applyIfNeeded() {
        if (applied) return
        applied = true
        val settings = HideExtensionSettings.getInstance()
        if (settings.codemanifestAsYaml) {
            CodemanifestAssociation.applySetting(true)
        }
    }
}
