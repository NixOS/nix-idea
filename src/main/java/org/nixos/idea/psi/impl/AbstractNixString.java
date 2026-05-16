package org.nixos.idea.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.LiteralTextEscaper;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import org.nixos.idea.psi.NixIndString;
import org.nixos.idea.psi.NixStdString;
import org.nixos.idea.psi.NixString;
import org.nixos.idea.psi.NixStringLiteralEscaper;
import org.nixos.idea.psi.NixStringPart;
import org.nixos.idea.psi.NixTypes;
import org.nixos.idea.util.NixStringUtil;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

public abstract class AbstractNixString extends AbstractNixPsiElement implements NixString {

    private static final Logger LOG = Logger.getInstance(AbstractNixString.class);
    private static final int UNKNOWN = -1;

    private int myLines = UNKNOWN;
    private int myIndent = UNKNOWN;
    private int myBaseIndent = UNKNOWN;

    AbstractNixString(@NotNull ASTNode node) {
        super(node);
        if (!(this instanceof NixStdString) && !(this instanceof NixIndString)) {
            LOG.error("Unknown subclass: " + getClass());
        }
    }

    @Override
    public boolean isValidHost() {
        return true;
    }

    @Override
    public PsiLanguageInjectionHost updateText(@NotNull String s) {
        // TODO Should we implement this method?
        throw new UnsupportedOperationException("NixString doesn't support updateText");
    }

    @Override
    public @NotNull LiteralTextEscaper<? extends PsiLanguageInjectionHost> createLiteralTextEscaper() {
        return new NixStringLiteralEscaper(this);
    }

    @Override
    public void subtreeChanged() {
        super.subtreeChanged();
        // TODO Is that valid, or should I manually preserve the indent while using the fragment editor?
        myLines = UNKNOWN;
        myIndent = UNKNOWN;
        myBaseIndent = UNKNOWN;
    }

    public boolean isSingleLine() {
        return getLines() == 1;
    }

    private int getLines() {
        int lines = myLines;
        if (lines == UNKNOWN) {
            MY_LINES.compareAndSet(this, UNKNOWN, computeLineCount());
            lines = myLines;
        }
        return lines;
    }

    private int computeLineCount() {
        int result = 1;
        if (this instanceof NixIndString) {
            for (NixStringPart stringPart : getStringParts()) {
                for (ASTNode current = stringPart.getNode().getFirstChildNode(); current != null; current = current.getTreeNext()) {
                    if (current.getElementType() == NixTypes.IND_STR_LF) {
                        result += 1;
                    }
                }
            }
        }
        return result;
    }

    public @Range(from = 0, to = Integer.MAX_VALUE) int getIndent() {
        int indent = myIndent;
        if (indent == UNKNOWN) {
            MY_INDENT.compareAndSet(this, UNKNOWN, computeIndent());
            indent = myIndent;
        }
        return indent;
    }

    private int computeIndent() {
        if (isSingleLine()) {
            // TODO Add test!!
            return Math.max(getDesiredIndent(), NixStringUtil.detectIndent(this, () -> 0));
        } else {
            return NixStringUtil.detectIndent(this, this::getDesiredIndent);
        }
    }

    private int getDesiredIndent() {
        // TODO Should use code style configuration, once it exists.
        return getBaseIndent() + 2;
    }

    /// Returns indent of the line containing the opening quotes.
    public @Range(from = 0, to = Integer.MAX_VALUE) int getBaseIndent() {
        int indent = myBaseIndent;
        if (indent == UNKNOWN) {
            MY_BASE_INDENT.compareAndSet(this, UNKNOWN, computeBaseIndent());
            indent = myBaseIndent;
        }
        return indent;
    }

    private int computeBaseIndent() {
        int result = 0;
        for (PsiElement current = PsiTreeUtil.prevLeaf(this); current != null; current = PsiTreeUtil.prevLeaf(current)) {
            if (current instanceof PsiWhiteSpace) {
                if (current.textContains('\n')) {
                    String text = current.getText();
                    int c = text.lastIndexOf('\n');
                    if (c >= 0) {
                        return text.length() - c - 1;
                    }
                    assert false : current + ": " + text;
                }
                result = current.getTextLength();
            } else {
                result = 0;
            }
        }
        return result;
    }

    // VarHandle mechanics
    private static final VarHandle MY_LINES;
    private static final VarHandle MY_INDENT;
    private static final VarHandle MY_BASE_INDENT;
    static {
        try {
            MethodHandles.Lookup l = MethodHandles.lookup();
            MY_LINES = l.findVarHandle(AbstractNixString.class, "myLines", int.class);
            MY_INDENT = l.findVarHandle(AbstractNixString.class, "myIndent", int.class);
            MY_BASE_INDENT = l.findVarHandle(AbstractNixString.class, "myBaseIndent", int.class);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
}
