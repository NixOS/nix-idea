package org.nixos.idea.icon;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

public class NixIcons {

    // Documentation
    // -> https://plugins.jetbrains.com/docs/intellij/icons.html
    // Design Guide
    // -> https://jetbrains.design/intellij/principles/icons/

    private static final Icon SNOWFLAKE = IconLoader.getIcon("/icons/nixSnowflake.svg", NixIcons.class);
    public static final Icon FILE = SNOWFLAKE;
}
