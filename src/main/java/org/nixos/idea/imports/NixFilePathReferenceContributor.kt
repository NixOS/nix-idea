package org.nixos.idea.imports

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileSystem
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.PsiReferenceRegistrar
import com.intellij.util.ProcessingContext
import org.nixos.idea.psi.impl.NixExprStdPathMixin

class NixFilePathReferenceContributor: PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(NixExprStdPathMixin::class.java),
            object : PsiReferenceProvider() {
                override fun getReferencesByElement(
                    element: PsiElement,
                    context: ProcessingContext
                ): Array<PsiReference> {
                    val it = element as? NixExprStdPathMixin ?: return emptyArray()
                    return arrayOf(NixImportReferenceImpl(it))
                }
            }
        )
    }
}

private class NixImportReferenceImpl(key: NixExprStdPathMixin) : PsiReferenceBase<NixExprStdPathMixin>(key) {
    override fun resolve(): PsiElement? {
        val path = element.containingFile.parent?.virtualFile?.path ?: return null
        val fs = LocalFileSystem.getInstance()
        val file = resolvePath(fs, path, element.text) ?: return null

        val project = element.project
        val psiFile = PsiManager.getInstance(project).findFile(file)

        return psiFile
    }

    override fun getVariants(): Array<out LookupElement> = LookupElement.EMPTY_ARRAY

    override fun calculateDefaultRangeInElement(): TextRange = TextRange.from(0, element.textLength)
}

fun resolvePath(fs: VirtualFileSystem, cwd: String, target: String): VirtualFile? {
    val resolved = FileUtil.join(cwd, target)
    val resolvedFile = fs.findFileByPath(resolved) ?: return null

    if (resolvedFile.isDirectory) {
        return resolvedFile.findChild("default.nix")
    }

    return resolvedFile
}
