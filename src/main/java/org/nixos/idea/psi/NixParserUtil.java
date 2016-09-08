package org.nixos.idea.psi;

import com.intellij.lang.ASTNode;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.parser.GeneratedParserUtilBase;

import java.lang.CharSequence;

public class NixParserUtil extends GeneratedParserUtilBase {

    public static boolean resetStringInterpolation(PsiBuilder builder, int level, boolean isString) {
        /* do some fancy pants magic */
        return false;
    }

    public static int indentationLevel(CharSequence c) {
        int i = 0;
        while(i < c.length() && c.charAt(i) == ' ') {
            i++;
        }
        return i;
    }

    public static final String getAssignedAttr(NixAttrAssign elm) {
        ASTNode nod = elm.getNode().findChildByType(NixTypes.ATTRS);
        return nod != null ? nod.getText() : null;
    }
}
