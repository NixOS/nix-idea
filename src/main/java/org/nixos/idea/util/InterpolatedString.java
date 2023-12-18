package org.nixos.idea.util;

import org.jetbrains.annotations.NotNull;
import org.nixos.idea.psi.NixAntiquotation;
import org.nixos.idea.psi.NixExpr;
import org.nixos.idea.psi.NixString;
import org.nixos.idea.psi.NixStringPart;
import org.nixos.idea.psi.NixStringText;

import java.util.ArrayList;
import java.util.List;

public record InterpolatedString(@NotNull List<String> fragments, @NotNull List<NixExpr> expressions) {
    public static @NotNull InterpolatedString parse(@NotNull NixString stringExpression) {
        // TODO: 20.11.2023 Cache?
        StringBuilder fragmentBuilder = new StringBuilder();
        List<String> fragments = new ArrayList<>();
        List<NixExpr> expressions = new ArrayList<>();
        for (NixStringPart part : stringExpression.getStringParts()) {
            if (part instanceof NixStringText text) {
                fragmentBuilder.append(NixStringUtil.parse(text));
            } else if (part instanceof NixAntiquotation antiquotation) {
                fragments.add(fragmentBuilder.toString());
                fragmentBuilder.setLength(0);
                expressions.add(antiquotation.getExpr());
            } else {
                throw new IllegalStateException("Unknown type of NixStringPart: " + part.getClass());
            }
        }
        fragments.add(fragmentBuilder.toString());
        return new InterpolatedString(List.copyOf(fragments), List.copyOf(expressions));
    }
}
