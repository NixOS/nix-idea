package org.nixos.idea.lang;

import com.intellij.lang.refactoring.NamesValidator;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiNamedElement;
import org.jetbrains.annotations.NotNull;
import org.nixos.idea.psi.NixTypeUtil;
import org.nixos.idea.psi.NixTypes;

/**
 * Tells the IDEA platform if a given string is a keyword or would be a valid identifier.
 * Since the Nix language integration is not using {@link PsiNamedElement}, this extension might or might not be unused.
 * The documentation refers {@link NamesValidator} in context of the renaming refactoring,
 * but this plugin implements its own rename handling at {@link NixRenamePsiElementProcessor}.
 */
public final class NixNamesValidator implements NamesValidator {
    @Override
    public boolean isKeyword(@NotNull String name, Project project) {
        NixLexer lexer = new NixLexer();
        lexer.start(name);
        return lexer.getTokenEnd() == name.length() && NixTypeUtil.KEYWORDS.contains(lexer.getTokenType());
    }

    /**
     * {@inheritDoc}
     *
     * <h4>Nix Expression Language</h4>
     * At some places of the Nix Expression Language, you may also use the {@code or} keyword as an identifier.
     * In some cases, you may also use strings and string interpolations.
     * This method is rather conservative and only returns true if the string resolves to {@link NixTypes#ID}.
     * Since I am not sure where (or if) IDEA uses this method, I am not sure if that is the optimal solution.
     */
    @Override
    public boolean isIdentifier(@NotNull String name, Project project) {
        NixLexer lexer = new NixLexer();
        lexer.start(name);
        return lexer.getTokenEnd() == name.length() && lexer.getTokenType() == NixTypes.ID;
    }
}
