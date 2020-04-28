package ua.itaysonlab.catogram

import org.telegram.ui.ActionBar.BaseFragment
import ua.itaysonlab.catogram.preferences.AppearancePreferencesEntry
import ua.itaysonlab.catogram.preferences.ChatsPreferencesEntry
import ua.itaysonlab.catogram.preferences.MainPreferencesEntry
import ua.itaysonlab.catogram.preferences.SecurityPreferencesEntry
import ua.itaysonlab.tgkit.TGKitSettingsFragment

object CatogramPreferencesNavigator {
    @JvmStatic
    fun createMainMenu(): TGKitSettingsFragment {
        return TGKitSettingsFragment(MainPreferencesEntry())
    }

    fun createChats(): BaseFragment {
        return TGKitSettingsFragment(ChatsPreferencesEntry())
    }

    fun createAppearance(): BaseFragment {
        return TGKitSettingsFragment(AppearancePreferencesEntry())
    }

    fun createSecurity(): BaseFragment {
        return TGKitSettingsFragment(SecurityPreferencesEntry())
    }
}