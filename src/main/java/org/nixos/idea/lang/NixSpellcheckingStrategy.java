package org.nixos.idea.lang;

import com.intellij.psi.PsiElement;
import com.intellij.spellchecker.inspections.IdentifierSplitter;
import com.intellij.spellchecker.tokenizer.SpellcheckingStrategy;
import com.intellij.spellchecker.tokenizer.Tokenizer;
import com.intellij.spellchecker.tokenizer.TokenizerBase;
import org.jetbrains.annotations.NotNull;
import org.nixos.idea.psi.NixIdentifier;
import org.nixos.idea.psi.NixPsiUtil;
import org.nixos.idea.psi.NixStringText;

/**
 * Enables spell checking for Nix files.
 *
 * @see <a href="https://plugins.jetbrains.com/docs/intellij/spell-checking.html">Spell Checking Documentation</a>
 * @see <a href="https://plugins.jetbrains.com/docs/intellij/spell-checking-strategy.html">Spell Checking Tutorial</a>
 */
public final class NixSpellcheckingStrategy extends SpellcheckingStrategy {

    // TODO: Implement SuppressibleSpellcheckingStrategy
    //  https://plugins.jetbrains.com/docs/intellij/spell-checking.html#suppressing-spellchecking
    // TODO: Suggest rename-refactoring for identifiers (when rename refactoring is supported)

    private static final Tokenizer<NixIdentifier> IDENTIFIER_TOKENIZER = TokenizerBase.create(IdentifierSplitter.getInstance());

    @Override
    public @NotNull Tokenizer<?> getTokenizer(PsiElement element) {
        if (element instanceof NixIdentifier identifier && NixPsiUtil.isDeclaration(identifier)) {
            return IDENTIFIER_TOKENIZER;
        }
        if (element instanceof NixStringText) {
            return TEXT_TOKENIZER;
        }
        return super.getTokenizer(element);
    }
}
