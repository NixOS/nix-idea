package org.nixos.idea.settings;

import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CustomCodeStyleSettings;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public final class NixCodeStyleSettings extends CustomCodeStyleSettings {

    public NixCodeStyleSettings(@NotNull CodeStyleSettings container) {
        super("NixCodeStyleSettings", container);
    }

    public boolean SPACE_AFTER_SET_MODIFIER = true;

    public boolean SPACE_BEFORE_COLON_IN_LAMBDA = false;
    public boolean SPACE_AFTER_COLON_IN_LAMBDA = true;
    public boolean SPACE_BEFORE_AT_SIGN_IN_LAMBDA = false;
    public boolean SPACE_AFTER_AT_SIGN_IN_LAMBDA = false;

    public boolean SPACE_AROUND_CONCAT_OPERATOR = true;
    public boolean SPACE_AROUND_HAS_ATTR_OPERATOR = true;
    public boolean SPACE_AROUND_IMPLICATION_OPERATOR = true;
    public boolean SPACE_AROUND_UPDATE_ATTRS_OPERATOR = true;

    public @AttributeAlignment int ALIGN_ASSIGNMENTS = AttributeAlignment.DO_NOT_ALIGN;

    @Documented
    @Retention(RetentionPolicy.SOURCE)
    @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE, ElementType.METHOD})
    @MagicConstant(valuesFromClass = AttributeAlignment.class)
    public @interface AttributeAlignment {
        int DO_NOT_ALIGN = 0;
        int ALIGN_CONSECUTIVE = 1;
        int ALIGN_SIBLINGS = 2;
        int ALIGN_NESTED = 3;
    }
}
