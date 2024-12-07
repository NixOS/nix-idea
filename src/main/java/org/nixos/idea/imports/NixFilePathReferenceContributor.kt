package org.nixos.idea.imports

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.PsiReferenceRegistrar
import com.intellij.util.ProcessingContext
import kotlinx.serialization.json.Json
import org.nixos.idea.psi.impl.NixExprPathMixin
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

class NixFilePathReferenceContributor: PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(NixExprPathMixin::class.java),
            object : PsiReferenceProvider() {
                override fun getReferencesByElement(
                    element: PsiElement,
                    context: ProcessingContext
                ): Array<PsiReference> {
                    val it = element as? NixExprPathMixin ?: return emptyArray()
                    return arrayOf(NixImportReferenceImpl(it))
                }
            }
        )
    }
}

private class NixImportReferenceImpl(key: NixExprPathMixin) : PsiReferenceBase<NixExprPathMixin>(key) {
    override fun resolve(): PsiElement? {
        val path = element.containingFile.parent?.virtualFile?.path ?: return null
        val resolvedPath = nixEval(path, element.text) ?: return null

        val file = LocalFileSystem.getInstance().findFileByPath(resolvedPath) ?: return null
        val project = element.project
        val psiFile = PsiManager.getInstance(project).findFile(file)

        return psiFile
    }

    override fun getVariants(): Array<out LookupElement> = LookupElement.EMPTY_ARRAY

    override fun calculateDefaultRangeInElement(): TextRange = TextRange.from(0, element.textLength)
}

fun nixEval(path: String, expr: String): String? {
    val command = listOf("nix", "eval", "--impure", "--expr", expr, "--json")

    try {
        val process = ProcessBuilder(command)
            .directory(File(path))
            .start()

        val stdout = BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
            reader.readText()
        }

        val stderr = BufferedReader(InputStreamReader(process.errorStream)).use { reader ->
            reader.readText()
        }

        val exitCode = process.waitFor()
        if (exitCode == 0) {
            return Json.decodeFromString<String>(stdout)
        } else {
            return null
        }
    } catch (e: Exception) {
        return null
    }
}
