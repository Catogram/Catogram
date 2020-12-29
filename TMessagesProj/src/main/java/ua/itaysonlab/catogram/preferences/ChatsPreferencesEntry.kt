package ua.itaysonlab.catogram.preferences

import android.view.WindowManager
import androidx.core.util.Pair
import org.telegram.messenger.LocaleController
import org.telegram.messenger.R
import org.telegram.messenger.SharedConfig
import org.telegram.ui.ActionBar.BaseFragment
import org.telegram.ui.ActionBar.Theme
import ua.itaysonlab.catogram.CGControversive
import ua.itaysonlab.catogram.CGFeatureHooks
import ua.itaysonlab.catogram.CGFeatureHooks.getReplyIconDrawable
import ua.itaysonlab.catogram.CatogramConfig
import ua.itaysonlab.catogram.preferences.ktx.*
import ua.itaysonlab.extras.IconExtras
import ua.itaysonlab.tgkit.preference.types.TGKitSliderPreference.TGSLContract

class ChatsPreferencesEntry : BasePreferencesEntry {
    override fun getPreferences(bf: BaseFragment) = tgKitScreen(LocaleController.getString("AS_Header_Chats", R.string.AS_Header_Chats)) {
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

                contract({
                    return@contract CatogramConfig.newTabs_noUnread
                }) {
                    CatogramConfig.newTabs_noUnread = it
                }
            }

            list {
                title = LocaleController.getString("CG_TabIconMode_Title", R.string.CG_TabIconMode_Title)

                contract({
                    return@contract listOf(
                            Pair(0, LocaleController.getString("CG_TabIconMode_Disabled", R.string.CG_TabIconMode_Disabled)),
                            Pair(1, LocaleController.getString("CG_TabIconMode_Append", R.string.CG_TabIconMode_Append)),
                            Pair(2, LocaleController.getString("CG_TabIconMode_Replace", R.string.CG_TabIconMode_Replace))
                    )
                }, {
                    return@contract when (CatogramConfig.newTabs_iconsV2_mode) {
                        1 -> LocaleController.getString("CG_TabIconMode_Append", R.string.CG_TabIconMode_Append)
                        2 -> LocaleController.getString("CG_TabIconMode_Replace", R.string.CG_TabIconMode_Replace)
                        else -> LocaleController.getString("CG_TabIconMode_Disabled", R.string.CG_TabIconMode_Disabled)
                    }
                }) {
                    CatogramConfig.newTabs_iconsV2_mode = it
                    bf.messagesController.loadRemoteFilters(true)
                }
            }

            switch {
                title = LocaleController.getString("CG_NewTabs_RemoveAllChats", R.string.CG_NewTabs_RemoveAllChats)
                //summary = LocaleController.getString("CG_NewTabs_RemoveAllChats_Desc", R.string.CG_NewTabs_RemoveAllChats_Desc)

                contract({
                    return@contract CatogramConfig.newTabs_hideAllChats
                }) {
                    CatogramConfig.newTabs_hideAllChats = it
                }
            }
        }

        category(LocaleController.getString("AS_Header_Chats", R.string.AS_Header_Chats)) {
            switch {
                title = LocaleController.getString("CG_archiveOnPull", R.string.CG_ArchiveOnPull)
                summary = LocaleController.getString("CG_archiveOnPull_Desc", R.string.CG_ArchiveOnPull_Desc)

                contract({
                    return@contract CatogramConfig.archiveOnPull
                }) {
                    CatogramConfig.archiveOnPull = it
                }
            }

            switch {
                title = LocaleController.getString("CG_syncPins", R.string.CG_SyncPins)
                summary = LocaleController.getString("CG_syncPins_Desc", R.string.CG_SyncPins_Desc)

                contract({
                    return@contract CatogramConfig.syncPins
                }) {
                    CatogramConfig.syncPins = it
                }
            }

            switch {
                title = LocaleController.getString("CG_RearCam", R.string.CG_RearCam)
                summary = LocaleController.getString("CG_RearCam_Desc", R.string.CG_RearCam_Desc)

                contract({
                    return@contract CatogramConfig.rearCam
                }) {
                    CatogramConfig.rearCam = it
                }
            }

            switch {
                title = LocaleController.getString("CG_SmoothKbd", R.string.CG_SmoothKbd)
                summary = LocaleController.getString("CG_SmoothKbd_Desc", R.string.CG_SmoothKbd_Desc)

                contract({
                    return@contract SharedConfig.smoothKeyboard
                }) {
                    SharedConfig.toggleSmoothKeyboard()
                    if (SharedConfig.smoothKeyboard && bf.parentActivity != null) {
                        bf.parentActivity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
                    }
                }
            }

            switch {
                title = LocaleController.getString("CG_FlatChannels", R.string.CG_FlatChannels)

                contract({
                    return@contract CatogramConfig.flatChannelStyle
                }) {
                    CatogramConfig.flatChannelStyle = it
                }
            }

            switch {
                title = LocaleController.getString("CG_FABLeft", R.string.CG_FABLeft)

                contract({
                    return@contract CatogramConfig.forceLeftFab
                }) {
                    CatogramConfig.forceLeftFab = it
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

            switch {
                title = LocaleController.getString("CG_ForwardMsgDate", R.string.CG_ForwardMsgDate)

                contract({
                    return@contract CatogramConfig.msgForwardDate
                }) {
                    CatogramConfig.msgForwardDate = it
                }
            }

            list {
                title = LocaleController.getString("CG_MsgSlideAction", R.string.CG_MsgSlideAction)

                contract({
                    return@contract listOf(
                            Pair(0, LocaleController.getString("CG_MsgSlideAction_Reply", R.string.CG_MsgSlideAction_Reply)),
                            Pair(1, LocaleController.getString("CG_MsgSlideAction_Save", R.string.CG_MsgSlideAction_Save)),
                            Pair(2, LocaleController.getString("CG_MsgSlideAction_Share", R.string.CG_MsgSlideAction_Share))
                    )
                }, {
                    return@contract when (CatogramConfig.messageSlideAction) {
                        1 -> LocaleController.getString("CG_MsgSlideAction_Save", R.string.CG_MsgSlideAction_Save)
                        2 -> LocaleController.getString("CG_MsgSlideAction_Share", R.string.CG_MsgSlideAction_Share)
                        else -> LocaleController.getString("CG_MsgSlideAction_Reply", R.string.CG_MsgSlideAction_Reply)
                    }
                }) {
                    CatogramConfig.messageSlideAction = it
                    Theme.chat_replyIconDrawable = bf.parentActivity.resources.getDrawable(CGFeatureHooks.getReplyIconDrawable())
                    Theme.setDrawableColorByKey(Theme.chat_replyIconDrawable, Theme.key_chat_serviceIcon)
                }
            }

            if (CGControversive.isControversiveFeaturesEnabled()) switch {
                title = LocaleController.getString("CG_HideMsgIfUserBlocked", R.string.CG_HideMsgIfUserBlocked)

                contract({
                    return@contract CatogramConfig.hideUserIfBlocked
                }) {
                    CatogramConfig.hideUserIfBlocked = it
                }
            }
        }

        category(LocaleController.getString("CG_Header_Profile", R.string.CG_Header_Profile)) {
            switch {
                title = LocaleController.getString("CG_NewAvaHeader_NoEdgeTapping", R.string.CG_NewAvaHeader_NoEdgeTapping)
                summary = LocaleController.getString("CG_NewAvaHeader_NoEdgeTapping_Desc", R.string.CG_NewAvaHeader_NoEdgeTapping_Desc)

                contract({
                    return@contract CatogramConfig.profiles_noEdgeTapping
                }) {
                    CatogramConfig.profiles_noEdgeTapping = it
                }
            }

            switch {
                title = LocaleController.getString("CG_EnableSwipePIP", R.string.CG_EnableSwipePIP)
                summary = LocaleController.getString("CG_EnableSwipePIP_Desc", R.string.CG_EnableSwipePIP_Desc)

                contract({
                    return@contract CatogramConfig.enableSwipeToPIP
                }) {
                    CatogramConfig.enableSwipeToPIP = it
                }
            }

            switch {
                title = LocaleController.getString("CG_NewAvaHeader_OpenOnTap", R.string.CG_NewAvaHeader_OpenOnTap)
                summary = LocaleController.getString("CG_NewAvaHeader_OpenOnTap_Desc", R.string.CG_NewAvaHeader_OpenOnTap_Desc)

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
