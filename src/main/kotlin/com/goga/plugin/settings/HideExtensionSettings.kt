package com.goga.plugin.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.BaseState
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.SimplePersistentStateComponent

@Service(Service.Level.APP)
@State(
    name = "com.goga.GogaSettings",
    storages = [Storage("goga.xml")]
)
class HideExtensionSettings : SimplePersistentStateComponent<HideExtensionSettings.State>(State()) {

    class State : BaseState() {
        var enabled by property(false)
        var extensions by list<String>()
        var codemanifestAsYaml by property(false)
    }

    var enabled: Boolean
        get() = state.enabled
        set(value) { state.enabled = value }

    var extensions: List<String>
        get() = state.extensions
        set(value) { state.extensions = value.toMutableList() }

    var codemanifestAsYaml: Boolean
        get() = state.codemanifestAsYaml
        set(value) { state.codemanifestAsYaml = value }

    companion object {
        @JvmStatic
        fun getInstance(): HideExtensionSettings =
            ApplicationManager.getApplication().getService(HideExtensionSettings::class.java)
    }
}
