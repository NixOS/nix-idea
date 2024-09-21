package org.nixos.idea.settings;

import com.intellij.openapi.components.RoamingType;
import com.intellij.openapi.components.Storage;

/**
 * Constants to be used for {@link Storage#value()}.
 */
public final class NixStoragePaths {

    /**
     * Location and configuration of external tools.
     * The settings in the file are considered system dependent.
     * This constant must be used with {@link RoamingType#LOCAL}.
     */
    public static final String TOOLS = "nix-idea-tools.xml";

    private NixStoragePaths() {} // Cannot be instantiated

}
