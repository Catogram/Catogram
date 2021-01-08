package ua.itaysonlab.catogram

import android.app.Activity
import android.content.SharedPreferences
import com.google.android.exoplayer2.util.Log
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.MessagesController
import ua.itaysonlab.catogram.preferences.ktx.boolean
import ua.itaysonlab.catogram.preferences.ktx.int
import ua.itaysonlab.catogram.ui.CatogramToasts
import ua.itaysonlab.catogram.vkui.icon_replaces.BaseIconReplace
import ua.itaysonlab.catogram.vkui.icon_replaces.NoIconReplace
import ua.itaysonlab.catogram.vkui.icon_replaces.VkIconReplace

object CatogramConfig {
    private val sharedPreferences: SharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE)

    var hideProxySponsor by sharedPreferences.boolean("advanced_hideproxysponsor", true)
    var hidePhoneNumber by sharedPreferences.boolean("advanced_hidephonenumber", true)
    var forceNewYear by sharedPreferences.boolean("advanced_forcenewyear", false)
    var forcePacman by sharedPreferences.boolean("advanced_forcepacman", false)
    var noRounding by sharedPreferences.boolean("advanced_norounding", false)
    var systemFonts by sharedPreferences.boolean("advanced_systemfonts", false)
    var systemFontsTT by sharedPreferences.boolean("advanced_systemfonts_tt", false)
    var noVibration by sharedPreferences.boolean("advanced_novibration", false)
    var forceNewYearDrawer by sharedPreferences.boolean("advanced_nydrawer", false)
    var drawerAvatar by sharedPreferences.boolean("cg_drawer_avatar", false)
    var flatStatusbar by sharedPreferences.boolean("cg_flat_statusbar", true)
    var flatActionbar by sharedPreferences.boolean("cg_flat_actionbar", false)
    var drawerBlur by sharedPreferences.boolean("cg_drawer_blur", false)
    var drawerDarken by sharedPreferences.boolean("cg_drawer_darken", false)
    var controversiveNoReading by sharedPreferences.boolean("cg_ct_read", false)
    var controversiveNoSecureFlag by sharedPreferences.boolean("cg_ct_flag", false)
    var useBiometricPrompt by sharedPreferences.boolean("cg_newbiometric", true)
    var accentNotification by sharedPreferences.boolean("cg_notification_accent", false)
    var contactsNever by sharedPreferences.boolean("contactsNever", false)
    var searchInActionbar by sharedPreferences.boolean("cg_chats_searchbar", false)
    var confirmCalls by sharedPreferences.boolean("cg_confirmcalls", false)
    var profiles_noEdgeTapping by sharedPreferences.boolean("cg_prof_edge", false)
    var profiles_openOnTap by sharedPreferences.boolean("cg_prof_open", false)
    var profiles_alwaysExpand by sharedPreferences.boolean("cg_prof_expand", false)
    var hideKeyboardOnScroll by sharedPreferences.boolean("cg_hidekbd", false)
    var newTabs_noUnread by sharedPreferences.boolean("cg_notabnum", false)
    var newTabs_hideAllChats by sharedPreferences.boolean("cg_ntallchats", false)
    var forceSVDrawer by sharedPreferences.boolean("cg_sv_drawer", false)
    var forceHLDrawer by sharedPreferences.boolean("cg_hl_drawer", false)

    var redesign_messageOption by sharedPreferences.int("cg_messageOption", 3)
    var redesign_iconOption by sharedPreferences.int("cg_iconoption", 0)
    var redesign_SlideDrawer by sharedPreferences.boolean("cg_redesign_slidedrawer", false)
    var redesign_TelegramThemes by sharedPreferences.boolean("cg_redesign_telegramthemes", false)

    var slider_stickerAmplifier by sharedPreferences.int("cg_stickamplifier", 100)
    var archiveOnPull by sharedPreferences.boolean("cg_archonpull", true)
    var syncPins by sharedPreferences.boolean("cg_syncpins", true)
    var rearCam by sharedPreferences.boolean("cg_rearcam", false)
    var useTgxMenuSlide by sharedPreferences.boolean("advanced_tgxslide", false)
    var useTgxMenuSlideSheet by sharedPreferences.boolean("advanced_tgxslide_sheet", false)

    var forwardNoAuthorship by sharedPreferences.boolean("cg_forward_no_authorship", false)
    var msgForwardDate by sharedPreferences.boolean("cg_msg_fwd_date", false)
    var forceLeftFab by sharedPreferences.boolean("cg_fab_invertgravity", false)

    var newTabs_iconsV2_mode by sharedPreferences.int("cg_tabs_v2", 0)

    var iconReplacement by sharedPreferences.int("cg_iconpack", 0)
    var hideUserIfBlocked by sharedPreferences.boolean("cg_hide_msg_if_blocked", false)
    var oldNotificationIcon by sharedPreferences.boolean("cg_old_notification", false)

    var messageSlideAction by sharedPreferences.int("cg_msgslide_action", 0)
    var enableSwipeToPIP by sharedPreferences.boolean("cg_swipe_to_pip", false)

    fun getIconReplacement(): BaseIconReplace {
        return when (iconReplacement) {
            1 -> NoIconReplace()
            else -> VkIconReplace()
        }
    }

    //var flatChannelStyle by sharedPreferences.boolean("cg_flat_channels", false)
    var flatChannelStyle = true

    init {
        CatogramToasts.init(sharedPreferences)
    }

    private fun putBoolean(key: String, value: Boolean) {
        val preferences = MessagesController.getGlobalMainSettings()
        val editor = preferences.edit()
        editor.putBoolean(key, value)
        editor.commit()
    }

    @JvmStatic
    fun shownContactsNever() {
        contactsNever = true
        putBoolean("contactsNever", true)
    }
}
