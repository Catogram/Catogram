package ua.itaysonlab.catogram

import ua.itaysonlab.catogram.preferences.*
import ua.itaysonlab.tgkit.TGKitSettingsFragment

object CatogramPreferencesNavigator {
    @JvmStatic
    fun createMainMenu() = TGKitSettingsFragment(MainPreferencesEntry())

    fun createChats() = TGKitSettingsFragment(ChatsPreferencesEntry())
    fun createAppearance() = TGKitSettingsFragment(AppearancePreferencesEntry())
    fun createSecurity() = TGKitSettingsFragment(SecurityPreferencesEntry())
    fun createDonate() = TGKitSettingsFragment(GPDonateEntry())

    fun createDB() = TGKitSettingsFragment(DoubleBottomPreferencesEntry())
}