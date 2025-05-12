package org.nixos.idea.lang.formatter;

import com.intellij.formatting.Block;
import com.intellij.formatting.Indent;
import com.intellij.formatting.Spacing;
import com.intellij.formatting.SpacingBuilder;
import com.intellij.lang.ASTNode;
import com.intellij.psi.TokenType;
import com.intellij.psi.formatter.common.AbstractBlock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nixos.idea.lang.formatter.dsl.FormatterDefinition;

import java.util.ArrayList;
import java.util.List;

final class NixBlock extends AbstractBlock {

    private final @NotNull SpacingBuilder mySpacingBuilder;
    private final @NotNull String myDebugName;
    private final @Nullable Indent myIndent;
    private @Nullable FormatterDefinition.Processor myChildProcessor;

    NixBlock(@NotNull ASTNode node, @NotNull SpacingBuilder spacingBuilder, @NotNull FormatterDefinition.Result result) {
        super(node, result.getWrap(), result.getAlignment());
        mySpacingBuilder = spacingBuilder;
        myDebugName = result.getDebugName();
        myIndent = result.getIndent();
        myChildProcessor = result.getChildProcessor();
    }

    @Override
    protected List<Block> buildChildren() {
        FormatterDefinition.Processor childProcessor = myChildProcessor;
        if (childProcessor == null) {
            throw new IllegalStateException("buildChildren() cannot be called twice");
        }

        List<Block> blocks = new ArrayList<>();
        // todo Didn't I create a util for these iterations somewhere?
        for (ASTNode child = myNode.getFirstChildNode(); child != null; child = child.getTreeNext()) {
            if (child.getElementType() == TokenType.WHITE_SPACE) {
                continue;
            }

            blocks.add(new NixBlock(child, mySpacingBuilder, childProcessor.process(child)));
        }

        myChildProcessor = null;
        return blocks;
    }

    @Override
    public @Nullable Indent getIndent() {
        return myIndent;
    }

    @Override
    public @Nullable Spacing getSpacing(@Nullable Block child1, @NotNull Block child2) {
        return mySpacingBuilder.getSpacing(this, child1, child2);
    }

    @Override
    public boolean isLeaf() {
        return myNode.getFirstChildNode() == null;
    }

    @Override
    public @NotNull String getDebugName() {
        return "NixBlock" + '(' + myDebugName + ')';
    }
}
