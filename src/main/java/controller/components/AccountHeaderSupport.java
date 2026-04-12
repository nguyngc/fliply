package controller.components;

import model.AppState;
import util.LocaleManager;
import util.I18n;
import view.Navigator;

import java.util.ResourceBundle;

/**
 * Shared helper for account-related screens that all use the same header wiring.
 */
public final class AccountHeaderSupport {
    private AccountHeaderSupport() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Applies a consistent account header setup and falls back to default navigation when no custom back action is provided.
     */
    public static void configure(HeaderController headerController,
                                 ResourceBundle resources,
                                 String titleKey,
                                 Runnable onBack) {
        if (headerController == null) {
            return;
        }

        ResourceBundle effectiveResources = resources != null
                ? resources
                : ResourceBundle.getBundle("Messages", LocaleManager.getLocale());

        headerController.setBackVisible(true);
        headerController.setTitle(I18n.message(effectiveResources, titleKey, ""));
        headerController.setSubtitle("");
        headerController.setOnBack(onBack != null ? onBack : () -> Navigator.go(AppState.Screen.ACCOUNT));
        headerController.applyVariant(AppState.isTeacher()
                ? HeaderController.Variant.TEACHER
                : HeaderController.Variant.STUDENT);
    }
}

