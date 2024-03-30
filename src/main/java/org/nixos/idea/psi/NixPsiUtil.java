package org.nixos.idea.psi;

import org.jetbrains.annotations.NotNull;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public final class NixPsiUtil {

    private NixPsiUtil() {} // Cannot be instantiated

    public static boolean isRecursive(@NotNull NixExprAttrs attrs) {
        return attrs.getNode().findChildByType(NixTypes.REC) != null ||
                attrs.getNode().findChildByType(NixTypes.LET) != null;
    }

    public static Collection<NixParameter> getParameters(@NotNull NixExprLambda lambda) {
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
}
