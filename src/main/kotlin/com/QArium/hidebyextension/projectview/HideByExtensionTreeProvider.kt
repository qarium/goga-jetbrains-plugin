package com.QArium.hidebyextension.projectview

import com.intellij.ide.projectView.TreeStructureProvider
import com.intellij.ide.projectView.ViewSettings
import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import com.QArium.hidebyextension.settings.HideExtensionSettings

fun shouldShowFile(fileName: String, hiddenExtensions: List<String>): Boolean {
    if (hiddenExtensions.isEmpty()) return true
    val dotIndex = fileName.lastIndexOf('.')
    if (dotIndex <= 0) return true
    val ext = fileName.substring(dotIndex + 1).lowercase()
    return ext !in hiddenExtensions.map { it.lowercase() }
}

class HideByExtensionTreeProvider : TreeStructureProvider {

    override fun modify(
        parent: AbstractTreeNode<*>,
        children: MutableCollection<AbstractTreeNode<*>>,
        settings: ViewSettings?
    ): MutableCollection<AbstractTreeNode<*>> {
        val appSettings = HideExtensionSettings.getInstance()
        if (!appSettings.enabled) return children
        val hiddenExtensions = appSettings.extensions
        if (hiddenExtensions.isEmpty()) return children

        return children.filter { node ->
            val file = extractVirtualFile(node)
            file == null || shouldShowFile(file.name, hiddenExtensions)
        }.toMutableList()
    }

    private fun extractVirtualFile(node: AbstractTreeNode<*>): VirtualFile? {
        val value = node.value
        if (value is VirtualFile) return value
        if (value is PsiFile) return value.virtualFile
        if (value is PsiDirectory) return value.virtualFile
        return try {
            val method = node.javaClass.getMethod("getVirtualFile")
            method.invoke(node) as? VirtualFile
        } catch (_: Exception) {
            null
        }
    }
}
