package org.nixos.idea.settings;

import com.intellij.openapi.components.RoamingType;
import com.intellij.openapi.components.Storage;

/**
 * Constants to be used for {@link Storage#value()}.
 */
public final class NixStoragePaths {

    /**
     * Storage location of non-system dependent settings for this plugin.
     * This constant must be used with {@link RoamingType#DEFAULT}.
     */
    public static final String DEFAULT = "nix-idea.xml";

    /**
     * Storage location of settings for external tools.
     * The settings in the file are considered system dependent.
     * This constant must be used with {@link RoamingType#LOCAL}.
     */
    public static final String TOOLS = "nix-idea-tools.xml";

    private NixStoragePaths() {} // Cannot be instantiated

}
