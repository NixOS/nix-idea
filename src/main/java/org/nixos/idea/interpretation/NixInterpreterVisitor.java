package org.nixos.idea.interpretation;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nixos.idea.psi.NixBind;
import org.nixos.idea.psi.NixElementVisitor;
import org.nixos.idea.psi.NixExpr;
import org.nixos.idea.psi.NixExprApp;
import org.nixos.idea.psi.NixExprAssert;
import org.nixos.idea.psi.NixExprIf;
import org.nixos.idea.psi.NixExprLambda;
import org.nixos.idea.psi.NixExprLet;
import org.nixos.idea.psi.NixExprOpAnd;
import org.nixos.idea.psi.NixExprOpConcat;
import org.nixos.idea.psi.NixExprOpDiv;
import org.nixos.idea.psi.NixExprOpEq;
import org.nixos.idea.psi.NixExprOpGe;
import org.nixos.idea.psi.NixExprOpGt;
import org.nixos.idea.psi.NixExprOpHas;
import org.nixos.idea.psi.NixExprOpImplication;
import org.nixos.idea.psi.NixExprOpLe;
import org.nixos.idea.psi.NixExprOpLt;
import org.nixos.idea.psi.NixExprOpMinus;
import org.nixos.idea.psi.NixExprOpMul;
import org.nixos.idea.psi.NixExprOpNe;
import org.nixos.idea.psi.NixExprOpNeg;
import org.nixos.idea.psi.NixExprOpNot;
import org.nixos.idea.psi.NixExprOpOr;
import org.nixos.idea.psi.NixExprOpPlus;
import org.nixos.idea.psi.NixExprOpUpdate;
import org.nixos.idea.psi.NixExprSelect;
import org.nixos.idea.psi.NixExprWith;
import org.nixos.idea.psi.NixLegacyAppOr;
import org.nixos.idea.psi.NixLegacyLet;
import org.nixos.idea.psi.NixList;
import org.nixos.idea.psi.NixLiteral;
import org.nixos.idea.psi.NixParens;
import org.nixos.idea.psi.NixPath;
import org.nixos.idea.psi.NixSet;
import org.nixos.idea.psi.NixString;
import org.nixos.idea.psi.NixTypes;
import org.nixos.idea.psi.NixVariableAccess;
import org.nixos.idea.util.InterpolatedString;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

final class NixInterpreterVisitor<V> extends NixElementVisitor<V> {

    private static final Logger LOG = Logger.getInstance(NixInterpreterVisitor.class);

    private final NixInterpreter<V> myInterpreter;

    NixInterpreterVisitor(@NotNull NixInterpreter<V> interpreter) {
        myInterpreter = interpreter;
    }

    @Override
    public V visitExprApp(@NotNull NixExprApp o) {
        Iterator<NixExpr> exprIterator = o.getExprList().iterator();
        V value = myInterpreter.resolve(exprIterator.next());
        while (exprIterator.hasNext()) {
            value = myInterpreter.call(value, myInterpreter.resolve(exprIterator.next()));
        }
        return value;
    }

    @Override
    public V visitExprAssert(@NotNull NixExprAssert o) {
        myInterpreter.checkAssertion(resolve(o.getAssertedExpr()));
        return resolve(o.getResultExpr());
    }

    @Override
    public V visitExprIf(@NotNull NixExprIf o) {
        ASTNode thenKeyword = o.getNode().findChildByType(NixTypes.THEN);
        ASTNode elseKeyword = o.getNode().findChildByType(NixTypes.ELSE);
        NixExpr condition = null;
        NixExpr thenExpr = null;
        NixExpr elseExpr = null;
        for (NixExpr expr : o.getExprList()) {
            if (elseKeyword != null && expr.getStartOffsetInParent() > elseKeyword.getStartOffsetInParent()) {
                elseExpr = expr;
            } else if (thenKeyword != null && expr.getStartOffsetInParent() > thenKeyword.getStartOffsetInParent()) {
                thenExpr = expr;
            } else {
                condition = expr;
            }
        }

        Boolean selection = myInterpreter.asBoolean(resolve(condition));
        if (selection == null) {
            return alternatives(thenExpr, elseExpr);
        } else if (selection) {
            return resolve(thenExpr);
        } else {
            return resolve(elseExpr);
        }
    }

    @Override
    public V visitExprLambda(@NotNull NixExprLambda o) {
        return myInterpreter.lambda(o);
    }

    @Override
    public V visitExprLet(@NotNull NixExprLet o) {
        for (NixBind binding : o.getBindList()) {
            // TODO: 20.11.2023 ...
        }
        return resolve(o.getExpr());
    }

    @Override
    public V visitExprOpAnd(@NotNull NixExprOpAnd o) {
        Boolean left = myInterpreter.asBoolean(resolve(o.getLeft()));
        Boolean right = myInterpreter.asBoolean(resolve(o.getRight()));
        if (left != null && !left || right != null && !right) {
            return myInterpreter.bool(false);
        } else if (left != null && right != null) {
            return myInterpreter.bool(true);
        } else {
            return myInterpreter.unknown(NixValueType.BOOLEAN);
        }
    }

    @Override
    public V visitExprOpConcat(@NotNull NixExprOpConcat o) {
        List<V> left = myInterpreter.asList(resolve(o.getLeft()));
        List<V> right = myInterpreter.asList(resolve(o.getRight()));
        return myInterpreter.list(
                Stream.concat(
                        Stream.ofNullable(left),
                        Stream.ofNullable(right)
                ).flatMap(Collection::stream).toList()
        );
    }

    @Override
    public V visitExprOpDiv(@NotNull NixExprOpDiv o) {
        Number left = myInterpreter.asNumber(resolve(o.getLeft()));
        Number right = myInterpreter.asNumber(resolve(o.getRight()));
        if (left == null || right == null) {
            return myInterpreter.unknown(NixValueType.NUMBER);
        } else if (left instanceof Integer && right instanceof Integer) {
            return myInterpreter.number(left.intValue() / right.intValue());
        } else {
            return myInterpreter.number(left.doubleValue() / right.doubleValue());
        }
    }

    @Override
    public AnticipatedValue visitExprOpEq(@NotNull NixExprOpEq o) {
        return super.visitExprOpEq(o);
    }

    @Override
    public AnticipatedValue visitExprOpGe(@NotNull NixExprOpGe o) {
        return super.visitExprOpGe(o);
    }

    @Override
    public AnticipatedValue visitExprOpGt(@NotNull NixExprOpGt o) {
        return super.visitExprOpGt(o);
    }

    @Override
    public AnticipatedValue visitExprOpHas(@NotNull NixExprOpHas o) {
        return super.visitExprOpHas(o);
    }

    @Override
    public AnticipatedValue visitExprOpImplication(@NotNull NixExprOpImplication o) {
        return super.visitExprOpImplication(o);
    }

    @Override
    public AnticipatedValue visitExprOpLe(@NotNull NixExprOpLe o) {
        return super.visitExprOpLe(o);
    }

    @Override
    public AnticipatedValue visitExprOpLt(@NotNull NixExprOpLt o) {
        return super.visitExprOpLt(o);
    }

    @Override
    public AnticipatedValue visitExprOpMinus(@NotNull NixExprOpMinus o) {
        return super.visitExprOpMinus(o);
    }

    @Override
    public AnticipatedValue visitExprOpMul(@NotNull NixExprOpMul o) {
        return super.visitExprOpMul(o);
    }

    @Override
    public AnticipatedValue visitExprOpNe(@NotNull NixExprOpNe o) {
        return super.visitExprOpNe(o);
    }

    @Override
    public AnticipatedValue visitExprOpNeg(@NotNull NixExprOpNeg o) {
        return super.visitExprOpNeg(o);
    }

    @Override
    public AnticipatedValue visitExprOpNot(@NotNull NixExprOpNot o) {
        return super.visitExprOpNot(o);
    }

    @Override
    public AnticipatedValue visitExprOpOr(@NotNull NixExprOpOr o) {
        return super.visitExprOpOr(o);
    }

    @Override
    public AnticipatedValue visitExprOpPlus(@NotNull NixExprOpPlus o) {
        return super.visitExprOpPlus(o);
    }

    @Override
    public AnticipatedValue visitExprOpUpdate(@NotNull NixExprOpUpdate o) {
        return super.visitExprOpUpdate(o);
    }

    @Override
    public AnticipatedValue visitExprSelect(@NotNull NixExprSelect o) {
        return super.visitExprSelect(o);
    }

    @Override
    public AnticipatedValue visitExprWith(@NotNull NixExprWith o) {
        return super.visitExprWith(o);
    }

    @Override
    public AnticipatedValue visitLegacyAppOr(@NotNull NixLegacyAppOr o) {
        return super.visitLegacyAppOr(o);
    }

    @Override
    public AnticipatedValue visitLegacyLet(@NotNull NixLegacyLet o) {
        return super.visitLegacyLet(o);
    }

    @Override
    public AnticipatedValue visitList(@NotNull NixList o) {
        return super.visitList(o);
    }

    @Override
    public AnticipatedValue visitLiteral(@NotNull NixLiteral o) {
        return super.visitLiteral(o);
    }

    @Override
    public AnticipatedValue visitParens(@NotNull NixParens o) {
        return super.visitParens(o);
    }

    @Override
    public AnticipatedValue visitPath(@NotNull NixPath o) {
        return super.visitPath(o);
    }

    @Override
    public AnticipatedValue visitSet(@NotNull NixSet o) {
        return super.visitSet(o);
    }

    @Override
    public V visitString(@NotNull NixString o) {
        InterpolatedString interpolatedString = InterpolatedString.parse(o);
        List<V> values = interpolatedString.expressions().stream()
                .map(this::resolve)
                .toList();
        return myInterpreter.string(interpolatedString.fragments(), values);
    }

    @Override
    public V visitVariableAccess(@NotNull NixVariableAccess o) {
        return myInterpreter.readVariable(o.getText());
    }

    @Override
    public V visitExpr(@NotNull NixExpr o) {
        LOG.error("Expression not implemented: " + o);
        return myInterpreter.unknown();
    }

    @Override
    public void visitElement(@NotNull PsiElement element) {
        throw new UnsupportedOperationException("Not an expression: " + element);
    }

    private V resolve(@Nullable NixExpr expression) {
        if (expression == null) {
            return myInterpreter.unknown();
        } else {
            return myInterpreter.resolve(expression);
        }
    }

    private V alternatives(@Nullable NixExpr... expressions) {
        List<V> values = Arrays.stream(expressions)
                .filter(Objects::nonNull)
                .map(myInterpreter::resolve)
                .toList();
        if (values.isEmpty()) {
            return myInterpreter.unknown();
        } else if (values.size() == 1) {
            return values.get(0);
        } else {
            return myInterpreter.alternatives(values);
        }
    }
}
