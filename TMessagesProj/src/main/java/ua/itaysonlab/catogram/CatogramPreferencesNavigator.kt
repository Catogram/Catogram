package ua.itaysonlab.catogram

import ua.itaysonlab.catogram.preferences.AppearancePreferencesEntry
import ua.itaysonlab.catogram.preferences.ChatsPreferencesEntry
import ua.itaysonlab.catogram.preferences.MainPreferencesEntry
import ua.itaysonlab.catogram.preferences.SecurityPreferencesEntry
import ua.itaysonlab.tgkit.TGKitSettingsFragment

object CatogramPreferencesNavigator {
    @JvmStatic
    fun createMainMenu() = TGKitSettingsFragment(MainPreferencesEntry())

    fun createChats() = TGKitSettingsFragment(ChatsPreferencesEntry())
    fun createAppearance() = TGKitSettingsFragment(AppearancePreferencesEntry())
    fun createSecurity() = TGKitSettingsFragment(SecurityPreferencesEntry())
}