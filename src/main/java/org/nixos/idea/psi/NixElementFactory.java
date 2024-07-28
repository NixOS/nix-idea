package org.nixos.idea.psi;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.TokenType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nixos.idea.file.NixFile;
import org.nixos.idea.file.NixFileType;

import java.util.Objects;

public final class NixElementFactory {

    private NixElementFactory() {} // Cannot be instantiated

    public static @NotNull NixString createString(@NotNull Project project, @NotNull String code) {
        return createElement(project, NixString.class, "", code, "");
    }

    public static @NotNull NixStringText createStdStringText(@NotNull Project project, @NotNull String code) {
        return createElement(project, NixStringText.class, "\"", code, "\"");
    }

    public static @NotNull NixStringText createIndStringText(@NotNull Project project, @NotNull String code) {
        return createElement(project, NixStringText.class, "''\n", code, "''");
    }

    public static @NotNull NixAttr createAttr(@NotNull Project project, @NotNull String code) {
        return createElement(project, NixAttr.class, "x.", code, "");
    }

    public static @NotNull NixAttrPath createAttrPath(@NotNull Project project, @NotNull String code) {
        return createElement(project, NixAttrPath.class, "x.", code, "");
    }

    public static @NotNull NixBind createBind(@NotNull Project project, @NotNull String code) {
        return createElement(project, NixBind.class, "{", code, "}");
    }

    @SuppressWarnings("unchecked")
    public static <T extends NixExpr> @NotNull T createExpr(@NotNull Project project, @NotNull String code) {
        return (T) createElement(project, NixExpr.class, "", code, "");
    }

    public static <T extends NixPsiElement> @NotNull T createElement(
            @NotNull Project project, @NotNull Class<T> type,
            @NotNull String prefix, @NotNull String text, @NotNull String suffix) {
        return Objects.requireNonNull(
                createElementOrNull(project, type, prefix, text, suffix),
                "Invalid " + type.getSimpleName() + ": " + text);
    }

    private static <T extends NixPsiElement> @Nullable T createElementOrNull(
            @NotNull Project project, @NotNull Class<T> type,
            @NotNull String prefix, @NotNull String text, @NotNull String suffix) {
        NixFile file = createFile(project, prefix + text + suffix);
        ASTNode current = file.getNode().getFirstChildNode();
        int offset = 0;
        while (current != null && offset <= prefix.length()) {
            int length = current.getTextLength();
            // Check if we have found the right element
            if (offset == prefix.length() && length == text.length()) {
                PsiElement psi = current.getPsi();
                if (type.isInstance(psi)) {
                    return containsErrors(current) ? null : type.cast(psi);
                }
            }
            // Check if we should go into or over this element
            if (offset + length <= prefix.length()) {
                offset += length;
                current = current.getTreeNext();
            } else {
                current = current.getFirstChildNode();
            }
        }
        return null;
    }

    private static boolean containsErrors(ASTNode node) {
        ASTNode current = node.getFirstChildNode();
        while (current != null) {
            if (current.getElementType() == TokenType.ERROR_ELEMENT) {
                return true;
            }
            ASTNode next = current.getFirstChildNode();
            if (next == null) {
                next = current.getTreeNext();
                while (next == null && (current = current.getTreeParent()) != node) {
                    next = current.getTreeNext();
                }
            }
            current = next;
        }
        return false;
    }

    public static @NotNull NixFile createFile(@NotNull Project project, @NotNull String code) {
        return (NixFile) PsiFileFactory.getInstance(project).createFileFromText("dummy.nix", NixFileType.INSTANCE, code);
    }
}
