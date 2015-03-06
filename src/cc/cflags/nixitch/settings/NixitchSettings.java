package cc.cflags.nixitch.settings;

import cc.cflags.nixitch.util.NixPathVerifier;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class NixitchSettings implements SearchableConfigurable {
    private String NIXITCH_ID = "Nixitch Settings";
    private PropertiesComponent projectProperties;
    private JPanel nixitchSettings;
    private JTextField nixPath, nixProfiles, nixOtherStores, nixRemote;
    private TextFieldWithBrowseButton nixPkgsConfig, nixConfDir, nixUserProfileDir;

    private List<Setting> settings;

    NixitchSettings(@NotNull Project project) {
        this.projectProperties = PropertiesComponent.getInstance(project);

        settings = Arrays.asList((Setting) new ResettableEnvField("NIX_PATH", (TextFriend) new TextComponent(nixPath))
                , (Setting) new ResettableEnvField("NIX_PROFILES", (TextFriend) new TextComponent(nixProfiles))
                , (Setting) new ResettableEnvField("NIX_OTHER_STORES", (TextFriend) new TextComponent(nixOtherStores))
                , (Setting) new ResettableEnvField("NIX_REMOTE", (TextFriend) new TextComponent(nixRemote))
                , (Setting) new ResettableEnvField("NIXPKGS_CONFIG", (TextFriend) new TextComponent(nixPkgsConfig))
                , (Setting) new ResettableEnvField("NIX_CONF_DIR", (TextFriend) new TextComponent(nixConfDir))
                , (Setting) new ResettableEnvField("NIX_USER_PROFILE_DIR", (TextFriend) new TextComponent(nixUserProfileDir))
        );

        final Color originalBackground = nixPath.getBackground();
        nixPath.setInputVerifier(new InputVerifier() {
            @Override
            public boolean verify(JComponent input) {
                JTextField tf = (JTextField) input;
                NixPathVerifier npv = new NixPathVerifier(tf.getText());
                if (npv.verify()) {
                    input.setBackground(originalBackground);
                    return true;
                } else {
                    //Some parts of the paths are inaccessible
                    //TODO: change to individual path strikeout
                    input.setBackground(JBColor.RED);
                    return false;
                }
            }
        });
    }

    interface Setting {
        public boolean dirty();

        public void store();

        public void reset();
    }

    interface TextFriend {
        public String getText();

        public void setText(String text);
    }

    class TextComponent implements TextFriend {
        Object c;
        public TextComponent(JTextField tf) { c = tf; }
        public TextComponent(TextFieldWithBrowseButton tfwbb) { c = tfwbb; }

        @Override
        public String getText() {
            String txt = "";
            if (c instanceof JTextField) {
                txt = ((JTextField) c).getText();
            } else if (c instanceof TextFieldWithBrowseButton ) {
                txt = ((TextFieldWithBrowseButton) c).getText();
            }
            return txt;
        }

        @Override
        public void setText(String txt) {
            if (c instanceof JTextField) {
                ((JTextField) c).setText(txt);
            } else if (c instanceof TextFieldWithBrowseButton ) {
                ((TextFieldWithBrowseButton) c).setText(txt);
            }
        }
    }

    class ResettableEnvField implements Setting, ChangeListener {

        public final TextFriend tf;
        public String id, env, previous, value;

        ResettableEnvField(@NotNull String id, @NotNull TextFriend tf) {
            this.id = "NIXITCH_" + id;
            this.env = id;
            this.tf = tf;
            this.tf.setText(readStoredEnv(env));
            store();
        }

        @Override
        public boolean dirty() {
            return !tf.getText().equals(previous);
        }

        @Override
        public void store() {
            previous = tf.getText(); // previous is now current..
            projectProperties.setValue(id, previous);
        }

        @Override
        public void reset() {
            tf.setText(previous);
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            if (dirty()) store();
        }

        private String readEnv(String env) {
            try {
                return System.getenv(env);
            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }
        }

        private String readStoredEnv(String env) {
            String nenv = "NIXITCH_" + env;
            if (projectProperties.isValueSet(nenv))
                return projectProperties.getValue(nenv);
            else
                return readEnv(env);
        }

    }

    @NotNull
    @Override
    public String getId() {
        return NIXITCH_ID;
    }

    @Nullable
    @Override
    public Runnable enableSearch(String s) {
        return null;
    }

    @Nls
    @Override
    public String getDisplayName() {
        return NIXITCH_ID;
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        return nixitchSettings;
    }

    @Override
    public boolean isModified() {
        boolean ret = false;
        for (Setting set : settings) { ret |= set.dirty(); }
        return ret;
    }

    @Override
    public void apply() throws ConfigurationException {
        // update the variables
        for (Setting set : settings) { set.store(); }
    }

    @Override
    public void reset() {
        // restore from previously applied variables
        for (Setting set : settings) { set.reset(); }
    }

    @Override
    public void disposeUIResources() {
    }
}
