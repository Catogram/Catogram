package ua.itaysonlab.catogram.preferences

import org.telegram.messenger.LocaleController
import org.telegram.messenger.R
import org.telegram.messenger.SharedConfig
import ua.itaysonlab.catogram.CatogramConfig
import ua.itaysonlab.catogram.preferences.ktx.*

class AppearancePreferencesEntry : BasePreferencesEntry {
    override fun getPreferences() = tgKitScreen(LocaleController.getString("AS_Header_Appearance", R.string.AS_Header_Appearance)) {
        category(LocaleController.getString("General", R.string.General)) {
            switch {
                title = LocaleController.getString("AS_HideUserPhone", R.string.AS_HideUserPhone)
                summary = LocaleController.getString("AS_HideUserPhoneSummary", R.string.AS_HideUserPhoneSummary)
                divider = true

                contract({
                    return@contract CatogramConfig.hidePhoneNumber
                }) {
                    CatogramConfig.hidePhoneNumber = it
                }
            }

            switch {
                title = LocaleController.getString("AS_NoRounding", R.string.AS_NoRounding)
                summary = LocaleController.getString("AS_NoRoundingSummary", R.string.AS_NoRoundingSummary)
                divider = true

                contract({
                    return@contract CatogramConfig.noRounding
                }) {
                    CatogramConfig.noRounding = it
                }
            }

            switch {
                title = LocaleController.getString("AS_SystemFonts", R.string.AS_SystemFonts)
                divider = true

                contract({
                    return@contract CatogramConfig.systemFonts
                }) {
                    CatogramConfig.systemFonts = it
                }
            }

            switch {
                title = LocaleController.getString("AS_Vibration", R.string.AS_Vibration)
                divider = true

                contract({
                    return@contract CatogramConfig.noVibration
                }) {
                    CatogramConfig.noVibration = it
                }
            }

            switch {
                title = LocaleController.getString("AS_FlatSB", R.string.AS_FlatSB)
                summary = LocaleController.getString("AS_FlatSB_Desc", R.string.AS_FlatSB_Desc)
                divider = true

                contract({
                    return@contract CatogramConfig.flatStatusbar
                }) {
                    // TODO: do the switch
                    CatogramConfig.flatStatusbar = it
                }
            }

            switch {
                title = LocaleController.getString("AS_FlatAB", R.string.AS_FlatAB)
                divider = true

                contract({
                    return@contract CatogramConfig.flatActionbar
                }) {
                    CatogramConfig.flatActionbar = it
                }
            }

            /*
            general.add(
            new TGKitSwitchPreference(LocaleController.getString("CG_ConfirmCalls", R.string.CG_ConfirmCalls), null, true, new TGKitSwitchPreference.TGSPContract() {
                @Override
                public boolean getPreferenceValue() {
                    return CatogramConfig.confirmCalls;
                }

                @Override
                public void toggleValue() {
                    CatogramConfig.toggleCallConfirm();
                }
            })
            );
             */

            switch {
                title = LocaleController.getString("AS_SysEmoji", R.string.AS_SysEmoji)
                summary = LocaleController.getString("AS_SysEmojiDesc", R.string.AS_SysEmojiDesc)

                contract({
                    return@contract SharedConfig.useSystemEmoji
                }) {
                    SharedConfig.toggleSystemEmoji()
                }
            }
        }

        category(LocaleController.getString("AS_Header_Notification", R.string.AS_Header_Notification)) {
            switch {
                title = LocaleController.getString("AS_AccentNotify", R.string.AS_AccentNotify)

                contract({
                    return@contract CatogramConfig.accentNotification
                }) {
                    CatogramConfig.accentNotification = it
                }
            }
        }

        category(LocaleController.getString("AS_DrawerCategory", R.string.AS_DrawerCategory)) {
            // TODO: create switcher
            switch {
                title = LocaleController.getString("AS_ForceNY_Drawer", R.string.AS_ForceNY_Drawer)
                summary = LocaleController.getString("AS_ForceNYSummary", R.string.AS_ForceNYSummary)
                divider = true

                contract({
                    return@contract CatogramConfig.forceNewYearDrawer
                }) {
                    CatogramConfig.forceNewYearDrawer = it
                }
            }

            switch {
                title = LocaleController.getString("AS_DrawerAvatar", R.string.AS_DrawerAvatar)
                divider = true

                contract({
                    return@contract CatogramConfig.drawerAvatar
                }) {
                    CatogramConfig.drawerAvatar = it
                }
            }

            switch {
                title = LocaleController.getString("AS_DrawerBlur", R.string.AS_DrawerBlur)
                divider = true

                contract({
                    return@contract CatogramConfig.drawerBlur
                }) {
                    CatogramConfig.drawerBlur = it
                }
            }

            switch {
                title = LocaleController.getString("AS_DrawerDarken", R.string.AS_DrawerDarken)

                contract({
                    return@contract CatogramConfig.drawerDarken
                }) {
                    CatogramConfig.drawerDarken = it
                }
            }
        }

        category(LocaleController.getString("AS_Header_Fun", R.string.AS_Header_Fun)) {
            switch {
                title = LocaleController.getString("AS_ForceNY", R.string.AS_ForceNY)
                summary = LocaleController.getString("AS_ForceNYSummary", R.string.AS_ForceNYSummary)
                divider = true

                contract({
                    return@contract CatogramConfig.forceNewYear
                }) {
                    CatogramConfig.forceNewYear = it
                }
            }

            switch {
                title = LocaleController.getString("AS_ForcePacman", R.string.AS_ForcePacman)
                summary = LocaleController.getString("AS_ForcePacmanSummary", R.string.AS_ForcePacmanSummary)
                divider = true

                contract({
                    return@contract CatogramConfig.forcePacman
                }) {
                    CatogramConfig.forcePacman = it
                }
            }
        }
    }
}