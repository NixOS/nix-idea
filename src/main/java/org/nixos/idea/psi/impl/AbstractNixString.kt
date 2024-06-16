package org.nixos.idea.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiLanguageInjectionHost
import org.nixos.idea.psi.NixString
import org.nixos.idea.psi.NixStringLiteralEscaper


abstract class AbstractNixString(node: ASTNode) : PsiLanguageInjectionHost, AbstractNixPsiElement(node), NixString {

    override fun isValidHost() = true

    override fun updateText(text: String): NixString {
        // TODO implement. This is called when you edit the injected file in
        //   order for the final Nix string to get updated
        //   It is not necessary for syntax highlighting in injections
        //   but is a nice to have
        return this
    }

    override fun createLiteralTextEscaper() = NixStringLiteralEscaper(this)
}

