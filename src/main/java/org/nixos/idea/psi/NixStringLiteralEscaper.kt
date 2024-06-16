package org.nixos.idea.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.LiteralTextEscaper
import com.intellij.psi.PsiLanguageInjectionHost
import org.nixos.idea.psi.impl.AbstractNixString
import org.nixos.idea.util.NixIndStringUtil
import org.nixos.idea.util.NixStringUtil

class NixStringLiteralEscaper(host: AbstractNixString) : LiteralTextEscaper<PsiLanguageInjectionHost>(host) {

    override fun isOneLine(): Boolean = false

    override fun decode(rangeInsideHost: TextRange, outChars: StringBuilder): Boolean {
        val subText: String = rangeInsideHost.substring(myHost.text)
        if (myHost is NixIndString) {
            outChars.append(NixIndStringUtil.escape(subText))
        } else {
            NixStringUtil.escape(outChars, subText)
        }
        return true
    }

    override fun getOffsetInHost(offsetInDecoded: Int, rangeInsideHost: TextRange): Int {
        // TODO: Implement proper String back-feed support.
        //  this involves keeping track of text offsets between decoded
        //  and encoded Nix text. See how Terraform does it here:
        //  https://github.com/JetBrains/intellij-plugins/blob/master/terraform/src/org/intellij/terraform/hcl/psi/impl/HCLStringLiteralTextEscaper.kt
        val offsetInHost = offsetInDecoded + rangeInsideHost.startOffset
        return (offsetInHost).coerceIn(rangeInsideHost.startOffset..rangeInsideHost.endOffset)
    }

}