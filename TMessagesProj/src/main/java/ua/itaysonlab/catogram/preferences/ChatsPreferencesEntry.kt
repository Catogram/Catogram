package ua.itaysonlab.catogram.preferences

import androidx.core.util.Pair
import org.telegram.messenger.LocaleController
import org.telegram.messenger.R
import org.telegram.messenger.SharedConfig
import ua.itaysonlab.catogram.CatogramConfig
import ua.itaysonlab.catogram.preferences.ktx.*
import ua.itaysonlab.tgkit.preference.types.TGKitSliderPreference.TGSLContract

class ChatsPreferencesEntry : BasePreferencesEntry {
    override fun getPreferences() = tgKitScreen(LocaleController.getString("AS_Header_Chats", R.string.AS_Header_Chats)) {
        category(LocaleController.getString("AS_RedesignCategory", R.string.AS_RedesignCategory)) {
            switch {
                title = LocaleController.getString("CG_NewDrawer", R.string.CG_NewDrawer)
                summary = LocaleController.getString("CG_NewDrawer_Desc", R.string.CG_NewDrawer_Desc)
                divider = true

                contract({
                    return@contract CatogramConfig.redesign_SlideDrawer
                }) {
                    CatogramConfig.redesign_SlideDrawer = it
                }
            }

            list {
                title = LocaleController.getString("CG_MessageMenuOption", R.string.CG_MessageMenuOption)
                divider = true

                contract({
                    return@contract listOf(
                            Pair(0, LocaleController.getString("CG_MessageMenuOption_Default", R.string.CG_MessageMenuOption_Default)),
                            Pair(1, LocaleController.getString("CG_MessageMenuOption_TGX", R.string.CG_MessageMenuOption_TGX)),
                            Pair(2, LocaleController.getString("CG_MessageMenuOption_CL", R.string.CG_MessageMenuOption_CL))
                    )
                }, {
                    return@contract when (CatogramConfig.redesign_messageOption) {
                        1 -> LocaleController.getString("CG_MessageMenuOption_TGX", R.string.CG_MessageMenuOption_TGX)
                        2 -> LocaleController.getString("CG_MessageMenuOption_CL", R.string.CG_MessageMenuOption_CL)
                        else -> LocaleController.getString("CG_MessageMenuOption_Default", R.string.CG_MessageMenuOption_Default)
                    }
                }) {
                    CatogramConfig.redesign_messageOption = it
                    when (CatogramConfig.redesign_messageOption) {
                        0 -> {
                            CatogramConfig.useCupertinoLib = false
                            CatogramConfig.useTgxMenuSlide = false
                        }
                        1 -> {
                            CatogramConfig.useCupertinoLib = false
                            CatogramConfig.useTgxMenuSlide = true
                        }
                        2 -> {
                            CatogramConfig.useCupertinoLib = true
                            CatogramConfig.useTgxMenuSlide = false
                        }
                    }
                }
            }

            switch {
                title = LocaleController.getString("CG_NewMsgAnim", R.string.CG_NewMsgAnim)

                contract({
                    return@contract CatogramConfig.newMessageAnimation
                }) {
                    CatogramConfig.newMessageAnimation = it
                }
            }
        }

        category(LocaleController.getString("CG_Slider_StickerAmplifier", R.string.CG_Slider_StickerAmplifier)) {
            slider {
                contract = object : TGSLContract {
                    override fun setValue(value: Int) {
                        CatogramConfig.slider_stickerAmplifier = value
                    }

                    override fun getPreferenceValue(): Int {
                        return CatogramConfig.slider_stickerAmplifier
                    }

                    override fun getMin(): Int {
                        return 50
                    }

                    override fun getMax(): Int {
                        return 100
                    }
                }
            }
        }

        category(LocaleController.getString("AS_Filters_Header", R.string.AS_Filters_Header)) {
            switch {
                title = LocaleController.getString("CG_NewTabs_NoCounter", R.string.CG_NewTabs_NoCounter)
                divider = true

                contract({
                    return@contract CatogramConfig.newTabs_noUnread
                }) {
                    CatogramConfig.newTabs_noUnread = it
                }
            }

            switch {
                title = LocaleController.getString("CG_NewTabs_RemoveAllChats", R.string.CG_NewTabs_RemoveAllChats)
                summary = LocaleController.getString("CG_NewTabs_RemoveAllChats_Desc", R.string.CG_NewTabs_RemoveAllChats_Desc)
                divider = true

                contract({
                    return@contract CatogramConfig.newTabs_hideAllChats
                }) {
                    CatogramConfig.newTabs_hideAllChats = it
                }
            }

            switch {
                title = LocaleController.getString("CG_NewTabs_Emoticons", R.string.CG_NewTabs_Emoticons)
                summary = LocaleController.getString("CG_NewTabs_Emoticons_Desc", R.string.CG_NewTabs_Emoticons_Desc)
                divider = true

                contract({
                    return@contract CatogramConfig.newTabs_emoji_insteadOfName
                }) {
                    CatogramConfig.newTabs_emoji_insteadOfName = it
                }
            }

            switch {
                title = LocaleController.getString("CG_NewTabs_EmoticonsAppend", R.string.CG_NewTabs_EmoticonsAppend)
                summary = LocaleController.getString("CG_NewTabs_EmoticonsAppend_Desc", R.string.CG_NewTabs_EmoticonsAppend_Desc)

                contract({
                    return@contract CatogramConfig.newTabs_emoji_appendToName
                }) {
                    CatogramConfig.newTabs_emoji_appendToName = it
                }
            }
        }

        category(LocaleController.getString("AS_Header_Chats", R.string.AS_Header_Chats)) {
            switch {
                title = LocaleController.getString("CG_NewRepostUI", R.string.CG_NewRepostUI)
                summary = LocaleController.getString("CG_NewRepostUI_Desc", R.string.CG_NewRepostUI_Desc)
                divider = true

                contract({
                    return@contract CatogramConfig.newRepostUI
                }) {
                    CatogramConfig.newRepostUI = it
                }
            }

            switch {
                title = LocaleController.getString("CG_SmoothKbd", R.string.CG_SmoothKbd)
                summary = LocaleController.getString("CG_SmoothKbd_Desc", R.string.CG_SmoothKbd_Desc)
                divider = true

                contract({
                    return@contract SharedConfig.smoothKeyboard
                }) {
                    SharedConfig.toggleSmoothKeyboard()
                }
            }

            switch {
                title = LocaleController.getString("CG_HideKbdOnScroll", R.string.CG_HideKbdOnScroll)

                contract({
                    return@contract CatogramConfig.hideKeyboardOnScroll
                }) {
                    CatogramConfig.hideKeyboardOnScroll = it
                }
            }
        }

        category(LocaleController.getString("CG_Header_Profile", R.string.CG_Header_Profile)) {
            switch {
                title = LocaleController.getString("CG_NewAvaHeader_NoEdgeTapping", R.string.CG_NewAvaHeader_NoEdgeTapping)
                summary = LocaleController.getString("CG_NewAvaHeader_NoEdgeTapping_Desc", R.string.CG_NewAvaHeader_NoEdgeTapping_Desc)
                divider = true

                contract({
                    return@contract CatogramConfig.profiles_noEdgeTapping
                }) {
                    CatogramConfig.profiles_noEdgeTapping = it
                }
            }

            switch {
                title = LocaleController.getString("CG_NewAvaHeader_OpenOnTap", R.string.CG_NewAvaHeader_OpenOnTap)
                summary = LocaleController.getString("CG_NewAvaHeader_OpenOnTap_Desc", R.string.CG_NewAvaHeader_OpenOnTap_Desc)
                divider = true

                contract({
                    return@contract CatogramConfig.profiles_openOnTap
                }) {
                    CatogramConfig.profiles_openOnTap = it
                }
            }

            switch {
                title = LocaleController.getString("CG_NewAvaHeader_AlwaysExpand", R.string.CG_NewAvaHeader_AlwaysExpand)
                summary = LocaleController.getString("CG_NewAvaHeader_AlwaysExpand_Desc", R.string.CG_NewAvaHeader_AlwaysExpand_Desc)

                contract({
                    return@contract CatogramConfig.profiles_alwaysExpand
                }) {
                    CatogramConfig.profiles_alwaysExpand = it
                }
            }
        }
    }
}