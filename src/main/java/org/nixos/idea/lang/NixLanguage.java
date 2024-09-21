package org.nixos.idea.lang;

import com.intellij.lang.Language;

public class NixLanguage extends Language {
    public static final NixLanguage INSTANCE = new NixLanguage();
    public static final String NOTIFICATION_GROUP_ID = "NixIDEA";

    private NixLanguage() {
        super("Nix");
    }
}
