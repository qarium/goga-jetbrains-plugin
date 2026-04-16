package com.goga.plugin.settings

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class CodemanifestAssociationTest : BasePlatformTestCase() {

    fun testApplySettingCanBeCalledWithoutExistingWriteAction() {
        CodemanifestAssociation.applySetting(true)
        CodemanifestAssociation.applySetting(false)
    }
}
