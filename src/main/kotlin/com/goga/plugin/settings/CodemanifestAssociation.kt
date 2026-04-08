package com.goga.plugin.settings

import com.intellij.openapi.fileTypes.FileNameMatcher
import com.intellij.openapi.fileTypes.FileTypeManager
import org.jetbrains.yaml.YAMLFileType

object CodemanifestAssociation {

    private const val CODEMANIFEST = "CODEMANIFEST"

    private val matcher = object : FileNameMatcher {
        override fun accept(fileName: String): Boolean = fileName == CODEMANIFEST
        override fun getPresentableString(): String = CODEMANIFEST
    }

    fun applySetting(enabled: Boolean) {
        val ftm = FileTypeManager.getInstance()
        if (enabled) {
            ftm.associate(YAMLFileType.YML, matcher)
        } else {
            ftm.removeAssociation(YAMLFileType.YML, matcher)
        }
    }
}
