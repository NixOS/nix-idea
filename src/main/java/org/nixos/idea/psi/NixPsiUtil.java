package org.nixos.idea.psi;

import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nixos.idea.psi.impl.NixStdAttrImpl;
import org.nixos.idea.psi.impl.NixStringAttrImpl;
import org.nixos.idea.util.NixStringUtil;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public final class NixPsiUtil {

    private static final Logger LOG = Logger.getInstance(NixPsiUtil.class);

    private NixPsiUtil() {} // Cannot be instantiated

    public static boolean isRecursive(@NotNull NixExprAttrs attrs) {
        return attrs.getNode().findChildByType(NixTypes.REC) != null ||
                attrs.getNode().findChildByType(NixTypes.LET) != null;
    }

    public static boolean isLegacyLet(@NotNull NixExprAttrs attrs) {
        return attrs.getNode().findChildByType(NixTypes.LET) != null;
    }

    public static @NotNull Collection<NixParameter> getParameters(@NotNull NixExprLambda lambda) {
        NixArgument mainParam = lambda.getArgument();
        NixFormals formalsHolder = lambda.getFormals();
        List<NixFormal> formals = formalsHolder == null ? List.of() : formalsHolder.getFormalList();
        return new AbstractCollection<>() {
            @Override
            public @NotNull Iterator<NixParameter> iterator() {
                return stream().iterator();
            }

            @Override
            public @NotNull Stream<NixParameter> stream() {
                return Stream.concat(
                        Stream.ofNullable(lambda.getArgument()),
                        formals.stream()
                );
            }

            @Override
            public int size() {
                return (mainParam == null ? 0 : 1) + formals.size();
            }
        };
    }

    public static @Nullable NixExpr getDefaultValue(@NotNull NixParameter parameter) {
        if (parameter instanceof NixFormal formal) {
            return formal.getDefaultValue();
        } else if (parameter instanceof NixArgument) {
            return null;
        } else {
            LOG.error("Unexpected NixParameter implementation: " + parameter.getClass());
            return null;
        }
    }

    /**
     * Returns the static name of an attribute.
     * Is {@code null} for dynamic attributes.
     *
     * @param attr the attribute
     * @return the name of the attribute or {@code null}
     */
    public static @Nullable String getAttributeName(@NotNull NixAttr attr) {
        if (attr instanceof NixStdAttrImpl) {
            return attr.getText();
        } else if (attr instanceof NixStringAttrImpl stringAttr) {
            NixStdString string = stringAttr.getStdString();
            List<NixStringPart> stringParts = string == null ? null : string.getStringParts();
            return stringParts != null && stringParts.size() == 1 &&
                    stringParts.get(0) instanceof NixStringText text
                    ? NixStringUtil.parse(text)
                    : null;
        } else {
            LOG.error("Unexpected NixAttr implementation: " + attr.getClass());
            return null;
        }
    }
}
