package org.nixos.idea.interpretation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nixos.idea.psi.NixAntiquotation;
import org.nixos.idea.psi.NixExpr;
import org.nixos.idea.psi.NixExprLambda;
import org.nixos.idea.psi.NixExprNumber;
import org.nixos.idea.psi.NixExprStdPath;
import org.nixos.idea.psi.NixExprUri;
import org.nixos.idea.psi.NixString;
import org.nixos.idea.psi.NixStringPart;

import java.util.List;

public interface NixInterpreter<V> {

    static <V> V run(@NotNull NixInterpreter<? extends V> interpreter, @NotNull NixExpr expression) {
        return expression.accept(new NixInterpreterVisitor<>(interpreter));
    }

    default V unknown() {
        return null;
    }

    default V unknown(@NotNull NixValueType type) {
        return unknown();
    }

    default V number(@NotNull NixExprNumber literal) {
        return unknown(NixValueType.NUMBER);
    }

    default V uri(@NotNull NixExprUri literal) {
        return unknown(NixValueType.STRING);
    }

    default V string(@NotNull NixString string) {
        for (NixStringPart part : string.getStringParts()) {
            if (part instanceof NixAntiquotation interpolation) {
                NixExpr expr = interpolation.getExpr();
                if (expr != null) {
                    escape(resolve(expr));
                }
            }
        }
        return unknown(NixValueType.STRING);
    }

    default V path(@NotNull NixExprStdPath path) {
        for (NixAntiquotation interpolation : path.getAntiquotationList()) {
            NixExpr expr = interpolation.getExpr();
            if (expr != null) {
                escape(resolve(expr));
            }
        }
        return unknown(NixValueType.PATH);
    }

    default V lambda(@NotNull NixExprLambda lambda) {
        NixExpr code = lambda.getExpr();
        if (code != null) {
            escape(resolve(code));
        }
        return unknown(NixValueType.LAMBDA);
    }

    default V list(@NotNull List<V> items) {
        items.forEach(this::escape);
        return unknown(NixValueType.LIST);
    }

    default V alternatives(List<V> values) {
        values.forEach(this::escape);
        return unknown();
    }

    default V resolve(@NotNull NixExpr expression) {
        return unknown();
    }

    default V readVariable(String variableName) {
        return unknown();
    }

    default V readAttribute(String attribute) {
        return unknown();
    }

    default V readAttribute(V attribute) {
        escape(attribute);
        return unknown();
    }

    default V call(V lambda, V argument) {
        escape(lambda);
        escape(argument);
        return unknown();
    }

    default @Nullable Boolean asBoolean(V value) {
        escape(value);
        return null;
    }

    default void checkAssertion(V value) {
        // Do nothing
    }

    default void escape(V value) {
        // Do nothing
    }
}
