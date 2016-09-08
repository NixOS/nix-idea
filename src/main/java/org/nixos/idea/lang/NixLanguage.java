package org.nixos.idea.lang;

import com.intellij.lang.Language;

public class NixLanguage extends Language {
    public static final NixLanguage INSTANCE = new NixLanguage();

    private NixLanguage() {
        super("Nix");
    }
}
