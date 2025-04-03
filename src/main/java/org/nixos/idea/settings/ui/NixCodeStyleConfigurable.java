package org.nixos.idea.settings.ui;

import com.intellij.application.options.CodeStyleAbstractConfigurable;
import com.intellij.application.options.CodeStyleAbstractPanel;
import com.intellij.application.options.TabbedLanguageCodeStylePanel;
import com.intellij.icons.AllIcons;
import com.intellij.ide.DataManager;
import com.intellij.openapi.options.ex.Settings;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.ui.EditorNotificationPanel;
import com.intellij.ui.layout.ComponentPredicate;
import com.intellij.util.ui.JBUI;
import kotlin.Unit;
import org.jetbrains.annotations.NotNull;
import org.nixos.idea.lang.NixLanguage;

import java.awt.BorderLayout;
import java.util.Objects;

public final class NixCodeStyleConfigurable extends CodeStyleAbstractConfigurable {

    public NixCodeStyleConfigurable(
            @NotNull CodeStyleSettings settings,
            @NotNull CodeStyleSettings cloneSettings,
            @NlsContexts.ConfigurableName String displayName
    ) {
        super(settings, cloneSettings, displayName);
    }

    @Override
    protected @NotNull CodeStyleAbstractPanel createPanel(@NotNull CodeStyleSettings settings) {
        return new TabbedLanguageCodeStylePanel(NixLanguage.INSTANCE, getCurrentSettings(), settings) {

            @Override
            protected void initTabs(CodeStyleSettings settings) {
                // InlineBanner might be an alternative to EditorNotificationPanel
                EditorNotificationPanel banner = new EditorNotificationPanel(
                        JBUI.CurrentTheme.Banner.WARNING_BACKGROUND,
                        EditorNotificationPanel.Status.Warning
                );

                banner.icon(AllIcons.General.BalloonWarning);
                banner.text("Your external formatter may overwrite the configuration on this page.");
                banner.createActionLabel("Configure external formatter...", this::goToExternalFormatter);
                banner.setVisible(false);
                updateBanner(banner);

                // TODO Seems a bit hacky.
                //  The fact that TabbedLanguageCodeStylePanel is using a BorderLayout
                //  seems more like an implementation detail.
                Objects.requireNonNull(getPanel()).add(banner, BorderLayout.NORTH);

                addIndentOptionsTab(settings);
                addSpacesTab(settings);
                addWrappingAndBracesTab(settings);
                addBlankLinesTab(settings);
            }

            private static void updateBanner(EditorNotificationPanel banner) {
                DataManager.getInstance().getDataContextFromFocusAsync().onSuccess(context -> {
                    if (context == null) return;
                    Settings settings = Settings.KEY.getData(context);
                    if (settings == null) return;
                    NixLangSettingsConfigurable configurable = settings.find(NixLangSettingsConfigurable.class);
                    if (configurable == null) return;

                    ComponentPredicate formatterEnabled = configurable.isFormatterEnabled();
                    banner.setVisible(formatterEnabled.invoke());
                    formatterEnabled.addListener(enabled -> {
                        banner.setVisible(enabled);
                        return Unit.INSTANCE;
                    });
                });
            }

            private void goToExternalFormatter() {
                DataManager.getInstance().getDataContextFromFocusAsync().onSuccess(context -> {
                    if (context == null) return;
                    Settings settings = Settings.KEY.getData(context);
                    if (settings == null) return;
                    settings.select(settings.find(NixLangSettingsConfigurable.class));
                });
            }
        };
    }
}
