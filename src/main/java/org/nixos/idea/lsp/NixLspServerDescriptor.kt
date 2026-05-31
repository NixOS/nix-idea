package org.nixos.idea.lsp

import com.google.gson.JsonObject
import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.ide.BrowserUtil
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.LspServer
import com.intellij.platform.lsp.api.ProjectWideLspServerDescriptor
import com.intellij.platform.lsp.api.customization.LspCodeActionsSupport
import com.intellij.platform.lsp.api.customization.LspCustomization
import com.intellij.platform.lsp.api.customization.LspIntentionAction
import com.intellij.psi.PsiFile
import com.intellij.util.execution.ParametersListUtil
import org.eclipse.lsp4j.CodeAction
import org.eclipse.lsp4j.WorkspaceEdit
import org.nixos.idea.file.NixFileType

internal class NixLspServerDescriptor(project: Project, private var settings: NixLspSettings) :
    ProjectWideLspServerDescriptor(project, "Nix") {

    @Throws(ExecutionException::class)
    override fun createCommandLine(): GeneralCommandLine {
        val argv = ParametersListUtil.parse(settings.command, false, true)
        return GeneralCommandLine(argv)
    }

    override fun isSupportedFile(file: VirtualFile): Boolean {
        return file.fileType === NixFileType.INSTANCE
    }

    override val lspCustomization: LspCustomization = object : LspCustomization() {
        override val codeActionsCustomizer = object : LspCodeActionsSupport() {
            private fun handleOpenNoogleCodeAction(lspServer: LspServer, codeAction: CodeAction): LspIntentionAction? {
                var noogleUrl = (codeAction.data as? JsonObject)?.get("noogleUrl")?.asString ?: return null
                /* set an empty edit to prevent the IDE from calling
                   the LSP server and automatically opening the url */
                codeAction.edit = WorkspaceEdit()
                return object : LspIntentionAction(lspServer, codeAction) {
                    override fun invoke(project: Project, editor: Editor, psiFile: PsiFile) {
                        BrowserUtil.open(noogleUrl)
                    }
                }
            }

            override fun createQuickFix(lspServer: LspServer, codeAction: CodeAction): LspIntentionAction? {
                /* discard the "open Noogle" codeAction, or it will be shown twice */
                handleOpenNoogleCodeAction(lspServer, codeAction)?.let {return null}
                return super.createQuickFix(lspServer, codeAction)
            }

            override fun createIntentionAction(lspServer: LspServer, codeAction: CodeAction): LspIntentionAction? {
                handleOpenNoogleCodeAction(lspServer, codeAction)?.let {return it}
                return super.createIntentionAction(lspServer, codeAction)
            }
        }
    }
}
