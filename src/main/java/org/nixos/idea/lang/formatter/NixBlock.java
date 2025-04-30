package org.nixos.idea.lang.formatter;

import com.intellij.formatting.Alignment;
import com.intellij.formatting.Block;
import com.intellij.formatting.Indent;
import com.intellij.formatting.Spacing;
import com.intellij.formatting.SpacingBuilder;
import com.intellij.formatting.Wrap;
import com.intellij.formatting.WrapType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.TokenType;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.formatter.common.AbstractBlock;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nixos.idea.lang.NixLanguage;
import org.nixos.idea.psi.NixBindAttr;
import org.nixos.idea.psi.NixTokenType;
import org.nixos.idea.psi.NixTypes;
import org.nixos.idea.settings.NixCodeStyleSettings;

import java.util.ArrayList;
import java.util.List;

final class NixBlock extends AbstractBlock {

    private final @NotNull Impl myImpl;
    private final @NotNull Settings mySettings;

    private final @Nullable Indent myIndent;
    private @Nullable Alignment myAttributeAlignment;
    private @Nullable NixBindAttr myLastAttributeBinding;

    /**
     * Constructor for root block.
     */
    NixBlock(@NotNull ASTNode node, @NotNull CodeStyleSettings globalSettings) {
        super(node, null, null);
        myImpl = Impl.create(node);
        mySettings = Settings.of(globalSettings);
        myIndent = null;
        myAttributeAlignment = null;
    }

    private NixBlock(
            @NotNull ASTNode node,
            @NotNull ChildConfig childConfig,
            @NotNull NixBlock parent
    ) {
        super(node, childConfig.wrap(), getAlignment(node, parent));
        myImpl = Impl.create(node);
        mySettings = parent.mySettings;
        myIndent = childConfig.indent();
        myAttributeAlignment =
                node.getElementType() == NixTypes.BIND_ATTR ? parent.myAttributeAlignment :
                        node.getElementType() != NixTypes.EXPR_ATTRS ? null :
                                mySettings.attributeAlignmentStrategy().createAlignment(parent.myAttributeAlignment);
    }

    private static @Nullable Alignment getAlignment(@NotNull ASTNode node, @NotNull NixBlock parent) {
        if (node.getElementType() == NixTypes.ASSIGN && parent.myAttributeAlignment != null) {
            NixBindAttr currentBinding = parent.myNode.getPsi(NixBindAttr.class);
            parent.myAttributeAlignment = parent.mySettings.attributeAlignmentStrategy()
                    .updateAlignment(parent.myAttributeAlignment, parent.myLastAttributeBinding, currentBinding);
            parent.myLastAttributeBinding = currentBinding;
            return parent.myAttributeAlignment;
        } else {
            return null;
        }
    }

    @Override
    protected List<Block> buildChildren() {
        List<Block> blocks = new ArrayList<>();
        // todo Didn't I create a util for these iterations somewhere?
        for (ASTNode child = myNode.getFirstChildNode(); child != null; child = child.getTreeNext()) {
            if (child.getElementType() == TokenType.WHITE_SPACE) {
                continue;
            }

            blocks.add(new NixBlock(child, myImpl.configureChild(child), this));
        }
        return blocks;
    }

    @Override
    public @Nullable Indent getIndent() {
        return myIndent;
    }

    @Override
    public @Nullable Spacing getSpacing(@Nullable Block child1, @NotNull Block child2) {
        return mySettings.spacingBuilder().getSpacing(this, child1, child2);
    }

    @Override
    public boolean isLeaf() {
        return myNode.getFirstChildNode() == null;
    }

    @Override
    public @NotNull String getDebugName() {
        return "NixBlock." + myImpl.getClass().getSimpleName();
    }

    private record ChildConfig(@Nullable Wrap wrap, Indent indent) {}

    private record Settings(
            @NotNull CommonCodeStyleSettings commonSettings,
            @NotNull NixCodeStyleSettings nixSettings,
            @NotNull SpacingBuilder spacingBuilder,
            @NotNull AttributeAlignmentStrategy attributeAlignmentStrategy
    ) {
        private static Settings of(@NotNull CodeStyleSettings globalSettings) {
            NixCodeStyleSettings nixSettings = globalSettings.getCustomSettings(NixCodeStyleSettings.class);
            return new Settings(
                    globalSettings.getCommonSettings(NixLanguage.INSTANCE),
                    nixSettings,
                    NixSpacingUtil.spacingBuilder(globalSettings),
                    AttributeAlignmentStrategy.of(nixSettings.ALIGN_ASSIGNMENTS)
            );
        }
    }

    private sealed interface Impl {

        @NotNull ChildConfig configureChild(@NotNull ASTNode node);

        static Impl create(@NotNull ASTNode node) {
            IElementType type = node.getElementType();
            if (type == NixTypes.EXPR_IF) {
                return new If();
            } else if (type == NixTypes.EXPR_ATTRS || type == NixTypes.EXPR_LIST || type == NixTypes.FORMALS) {
                return new Collection();
            } else {
                return new Default();
            }
        }

        final class Default implements Impl {
            private final Wrap myWrap = Wrap.createWrap(WrapType.NORMAL, false);

            @Override
            public @NotNull ChildConfig configureChild(@NotNull ASTNode node) {
                return new ChildConfig(myWrap, Indent.getNoneIndent());
            }
        }

        final class If implements Impl {
            private final Wrap myCaseWrap = Wrap.createWrap(WrapType.CHOP_DOWN_IF_LONG, true);
            private final Wrap myConditionWrap = Wrap.createChildWrap(myCaseWrap, WrapType.CHOP_DOWN_IF_LONG, true);

            @Override
            public @NotNull ChildConfig configureChild(@NotNull ASTNode node) {
                IElementType elementType = node.getElementType();
                if (elementType instanceof NixTokenType) {
                    return new ChildConfig(null, Indent.getNoneIndent());
                } else {
                    PsiElement prev = PsiTreeUtil.skipWhitespacesAndCommentsBackward(node.getPsi());
                    IElementType prevType = PsiUtilBase.getElementType(prev);
                    if (prevType == NixTypes.ELSE && elementType == NixTypes.EXPR_IF) {
                        // TODO Does this work?
                        return new ChildConfig(myConditionWrap, Indent.getNormalIndent());
                    } else if (prevType == NixTypes.IF) {
                        return new ChildConfig(myConditionWrap, Indent.getNormalIndent());
                    } else {
                        return new ChildConfig(myCaseWrap, Indent.getNormalIndent());
                    }
                }
            }
        }

        final class Collection implements Impl {
            private final Wrap myItemWrap = Wrap.createWrap(WrapType.CHOP_DOWN_IF_LONG, true);

            @Override
            public @NotNull ChildConfig configureChild(@NotNull ASTNode node) {
                IElementType elementType = node.getElementType();
                if (elementType instanceof NixTokenType) {
                    return new ChildConfig(null, Indent.getNoneIndent());
                } else {
                    return new ChildConfig(myItemWrap, Indent.getNormalIndent());
                }
            }
        }
    }
}
