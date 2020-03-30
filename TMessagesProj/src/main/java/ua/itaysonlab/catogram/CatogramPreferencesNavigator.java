package ua.itaysonlab.catogram;

import org.telegram.ui.ActionBar.BaseFragment;

import ua.itaysonlab.catogram.preferences.AppearancePreferencesEntry;
import ua.itaysonlab.catogram.preferences.ChatsPreferencesEntry;
import ua.itaysonlab.catogram.preferences.MainPreferencesEntry;
import ua.itaysonlab.catogram.preferences.SecurityPreferencesEntry;
import ua.itaysonlab.tgkit.TGKitSettingsFragment;

public class CatogramPreferencesNavigator {
    public static TGKitSettingsFragment createMainMenu() {
        return new TGKitSettingsFragment(new MainPreferencesEntry());
    }

    public static BaseFragment createChats() {
        return new TGKitSettingsFragment(new ChatsPreferencesEntry());
    }

    public static BaseFragment createAppearance() {
        return new TGKitSettingsFragment(new AppearancePreferencesEntry());
    }

    public static BaseFragment createSecurity() {
        return new TGKitSettingsFragment(new SecurityPreferencesEntry());
    }
}
