package ua.itaysonlab.catogram.preferences

import android.graphics.Color
import androidx.core.util.Pair
import org.telegram.messenger.LocaleController
import org.telegram.messenger.R
import org.telegram.messenger.SharedConfig
import org.telegram.ui.ActionBar.BaseFragment
import org.telegram.ui.ActionBar.Theme
import org.telegram.ui.LaunchActivity
import ua.itaysonlab.catogram.CGControversive
import ua.itaysonlab.catogram.CatogramConfig
import ua.itaysonlab.catogram.preferences.ktx.*
import ua.itaysonlab.extras.CatogramExtras
import ua.itaysonlab.extras.IconExtras

class AppearancePreferencesEntry : BasePreferencesEntry {
    override fun getPreferences(bf: BaseFragment) = tgKitScreen(LocaleController.getString("AS_Header_Appearance", R.string.AS_Header_Appearance)) {
        category(LocaleController.getString("AS_RedesignCategory", R.string.AS_RedesignCategory)) {
            switch {
                title = LocaleController.getString("CG_TelegramThemes", R.string.CG_TelegramThemes)
                summary = LocaleController.getString("CG_TelegramThemes_Desc", R.string.CG_TelegramThemes_Desc)

                contract({
                    return@contract CatogramConfig.redesign_TelegramThemes
                }) {
                    CatogramConfig.redesign_TelegramThemes = it
                }
            }

            switch {
                title = LocaleController.getString("CG_NewDrawer", R.string.CG_NewDrawer)
                summary = LocaleController.getString("CG_NewDrawer_Desc", R.string.CG_NewDrawer_Desc)

                contract({
                    return@contract CatogramConfig.redesign_SlideDrawer
                }) {
                    CatogramConfig.redesign_SlideDrawer = it
                }
            }

            list {
                title = LocaleController.getString("CG_MessageMenuOption", R.string.CG_MessageMenuOption)

                contract({
                    return@contract listOf(
                            Pair(0, LocaleController.getString("CG_MessageMenuOption_Default", R.string.CG_MessageMenuOption_Default)),
                            Pair(1, LocaleController.getString("CG_MessageMenuOption_TGX", R.string.CG_MessageMenuOption_TGX)),
                            Pair(2, LocaleController.getString("CG_MessageMenuOption_TGXS", R.string.CG_MessageMenuOption_TGXS)),
                    )
                }, {
                    return@contract when (CatogramConfig.redesign_messageOption) {
                        1 -> LocaleController.getString("CG_MessageMenuOption_TGX", R.string.CG_MessageMenuOption_TGX)
                        2 -> LocaleController.getString("CG_MessageMenuOption_TGXS", R.string.CG_MessageMenuOption_TGXS)
                        else -> LocaleController.getString("CG_MessageMenuOption_Default", R.string.CG_MessageMenuOption_Default)
                    }
                }) {
                    CatogramConfig.redesign_messageOption = it
                    when (CatogramConfig.redesign_messageOption) {
                        0 -> {
                            CatogramConfig.useTgxMenuSlide = false
                            CatogramConfig.useTgxMenuSlideSheet = false
                        }
                        1 -> {
                            CatogramConfig.useTgxMenuSlide = true
                            CatogramConfig.useTgxMenuSlideSheet = false
                        }
                        2 -> {
                            CatogramConfig.useTgxMenuSlide = false
                            CatogramConfig.useTgxMenuSlideSheet = true
                        }
                    }
                }
            }

            list {
                title = LocaleController.getString("AS_ChangeIcon", R.string.AS_ChangeIcon)

                contractIcons({
                    return@contractIcons listOf(
                            Triple(0, LocaleController.getString("AS_ChangeIcon_Old", R.string.AS_ChangeIcon_Old), R.mipmap.cg_launcher),
                            Triple(1, LocaleController.getString("AS_ChangeIcon_AltBlue", R.string.AS_ChangeIcon_AltBlue), R.mipmap.cg_launcher_alt_blue),
                            Triple(2, LocaleController.getString("AS_ChangeIcon_AltOrange", R.string.AS_ChangeIcon_AltOrange), R.mipmap.cg_launcher_alt_orange)
                    )
                }, {
                    return@contractIcons when (CatogramConfig.redesign_iconOption) {
                        1 -> LocaleController.getString("AS_ChangeIcon_AltBlue", R.string.AS_ChangeIcon_AltBlue)
                        2 -> LocaleController.getString("AS_ChangeIcon_AltOrange", R.string.AS_ChangeIcon_AltOrange)
                        else -> LocaleController.getString("AS_ChangeIcon_Old", R.string.AS_ChangeIcon_Old)
                    }
                }) {
                    CatogramConfig.redesign_iconOption = it
                    IconExtras.setIcon(it)
                }
            }

            list {
                title = LocaleController.getString("CG_IconReplacements", R.string.CG_IconReplacements)

                contractIcons({
                    return@contractIcons listOf(
                            Triple(0, LocaleController.getString("CG_IconReplacement_VKUI", R.string.CG_IconReplacement_VKUI), R.drawable.settings_outline_28),
                            Triple(1, LocaleController.getString("CG_IconReplacement_Default", R.string.CG_IconReplacement_Default), R.drawable.menu_settings)
                    )
                }, {
                    return@contractIcons when (CatogramConfig.iconReplacement) {
                        1 -> LocaleController.getString("CG_IconReplacement_Default", R.string.CG_IconReplacement_Default)
                        else -> LocaleController.getString("CG_IconReplacement_VKUI", R.string.CG_IconReplacement_VKUI)
                    }
                }) {
                    CatogramConfig.iconReplacement = it
                    (bf.parentActivity as? LaunchActivity)?.reloadResources()
                }
            }
        }

        category(LocaleController.getString("General", R.string.General)) {
            switch {
                title = LocaleController.getString("AS_HideUserPhone", R.string.AS_HideUserPhone)
                summary = LocaleController.getString("AS_HideUserPhoneSummary", R.string.AS_HideUserPhoneSummary)

                contract({
                    return@contract CatogramConfig.hidePhoneNumber
                }) {
                    CatogramConfig.hidePhoneNumber = it
                }
            }

            switch {
                title = LocaleController.getString("CG_FakeUserPhone", R.string.CG_FakeUserPhone)
                summary = LocaleController.getString("CG_FakeUserPhoneSummary", R.string.CG_FakeUserPhoneSummary)

                contract({
                    return@contract CatogramConfig.fakePhoneNumber
                }) {
                    CatogramConfig.fakePhoneNumber = it
                }
            }

            switch {
                title = LocaleController.getString("AS_NoRounding", R.string.AS_NoRounding)
                summary = LocaleController.getString("AS_NoRoundingSummary", R.string.AS_NoRoundingSummary)

                contract({
                    return@contract CatogramConfig.noRounding
                }) {
                    CatogramConfig.noRounding = it
                }
            }

            if (CGControversive.isControversiveFeaturesEnabled()) {
                switch {
                    title = "VK Sans as bold font [alpha]"
                    summary = "enable System Fonts first"

                    contract({
                        return@contract CatogramConfig.systemFontsTT
                    }) {
                        CatogramConfig.systemFontsTT = it
                    }
                }
            }

            switch {
                title = LocaleController.getString("AS_Vibration", R.string.AS_Vibration)

                contract({
                    return@contract CatogramConfig.noVibration
                }) {
                    CatogramConfig.noVibration = it
                }
            }

            switch {
                title = LocaleController.getString("AS_FlatSB", R.string.AS_FlatSB)
                summary = LocaleController.getString("AS_FlatSB_Desc", R.string.AS_FlatSB_Desc)

                contract({
                    return@contract SharedConfig.noStatusBar
                }) {
                    SharedConfig.toggleNoStatusBar()
                    bf.parentActivity.window.statusBarColor = if (Theme.getColor(Theme.key_actionBarDefault, null, true) == Color.WHITE) CatogramExtras.getLightStatusbarColor() else CatogramExtras.getDarkStatusbarColor()
                }
            }

            switch {
                title = LocaleController.getString("AS_FlatAB", R.string.AS_FlatAB)

                contract({
                    return@contract CatogramConfig.flatActionbar
                }) {
                    CatogramConfig.flatActionbar = it
                }
            }

            switch {
                title = LocaleController.getString("CG_ConfirmCalls", R.string.CG_ConfirmCalls)

                contract({
                    return@contract CatogramConfig.confirmCalls
                }) {
                    CatogramConfig.confirmCalls = it
                }
            }

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

            switch {
                title = LocaleController.getString("CG_OldNotification", R.string.CG_OldNotification)

                contract({
                    return@contract CatogramConfig.oldNotificationIcon
                }) {
                    CatogramConfig.oldNotificationIcon = it
                }
            }
        }

        category(LocaleController.getString("AS_DrawerCategory", R.string.AS_DrawerCategory)) {
            list {
                title = LocaleController.getString("AS_ForceIcons", R.string.AS_ForceIcons)

                contract({
                    return@contract listOf(
                            Pair(0, LocaleController.getString("AS_ForceDefault_Drawer", R.string.AS_ForceDefault_Drawer)),
                            Pair(1, LocaleController.getString("AS_ForceSV_Drawer", R.string.AS_ForceSV_Drawer)),
                            Pair(2, LocaleController.getString("AS_ForceNY_Drawer", R.string.AS_ForceNY_Drawer)),
                            Pair(3, "Halloween")
                    )
                }, {
                    return@contract when (CatogramConfig.redesign_forceDrawerIconsOption) {
                        1 -> LocaleController.getString("AS_ForceSV_Drawer", R.string.AS_ForceSV_Drawer)
                        2 -> LocaleController.getString("AS_ForceNY_Drawer", R.string.AS_ForceNY_Drawer)
                        3 -> "Halloween"
                        else -> LocaleController.getString("AS_ForceDefault_Drawer", R.string.AS_ForceDefault_Drawer)
                    }
                }) {
                    CatogramConfig.redesign_forceDrawerIconsOption = it
                    when (CatogramConfig.redesign_forceDrawerIconsOption) {
                        0 -> {
                            CatogramConfig.forceNewYearDrawer = false
                            CatogramConfig.forceSVDrawer = false
                            CatogramConfig.forceHLDrawer = false
                        }
                        1 -> {
                            CatogramConfig.forceNewYearDrawer = false
                            CatogramConfig.forceSVDrawer = true
                            CatogramConfig.forceHLDrawer = false
                        }
                        2 -> {
                            CatogramConfig.forceNewYearDrawer = true
                            CatogramConfig.forceSVDrawer = false
                            CatogramConfig.forceHLDrawer = false
                        }
                        3 -> {
                            CatogramConfig.forceNewYearDrawer = false
                            CatogramConfig.forceSVDrawer = false
                            CatogramConfig.forceHLDrawer = true
                        }
                    }
                }
            }

            switch {
                title = LocaleController.getString("AS_DrawerAvatar", R.string.AS_DrawerAvatar)

                contract({
                    return@contract CatogramConfig.drawerAvatar
                }) {
                    CatogramConfig.drawerAvatar = it
                }
            }

            switch {
                title = LocaleController.getString("AS_DrawerBlur", R.string.AS_DrawerBlur)

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

                contract({
                    return@contract CatogramConfig.forceNewYear
                }) {
                    CatogramConfig.forceNewYear = it
                }
            }

            switch {
                title = LocaleController.getString("AS_ForcePacman", R.string.AS_ForcePacman)
                summary = LocaleController.getString("AS_ForcePacmanSummary", R.string.AS_ForcePacmanSummary)

                contract({
                    return@contract CatogramConfig.forcePacman
                }) {
                    CatogramConfig.forcePacman = it
                }
            }
        }
    }
}
