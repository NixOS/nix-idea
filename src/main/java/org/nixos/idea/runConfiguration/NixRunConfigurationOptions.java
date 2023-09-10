package org.nixos.idea.runConfiguration;

import com.intellij.execution.configurations.RunConfigurationOptions;
import com.intellij.openapi.components.StoredProperty;

public final class NixRunConfigurationOptions extends RunConfigurationOptions {
    private final StoredProperty<String> myScriptName = string("").provideDelegate(this, "scriptName");

    public String getScriptName() {
        return myScriptName.getValue(this);
    }

    public void setScriptName(String scriptName) {
        myScriptName.setValue(this, scriptName);
    }
}
