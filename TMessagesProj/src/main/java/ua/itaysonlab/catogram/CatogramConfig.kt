package ua.itaysonlab.catogram

import android.content.SharedPreferences
import com.google.android.exoplayer2.util.Log
import org.telegram.messenger.MessagesController
import ua.itaysonlab.catogram.preferences.ktx.boolean
import ua.itaysonlab.catogram.preferences.ktx.int
import ua.itaysonlab.catogram.ui.CatogramToasts

object CatogramConfig {
    private lateinit var sharedPreferences: SharedPreferences

    var hidePhoneNumber by sharedPreferences.boolean("advanced_hidephonenumber", true)
    var forceNewYear by sharedPreferences.boolean("advanced_forcenewyear", false)
    var forcePacman by sharedPreferences.boolean("advanced_forcepacman", false)
    var noRounding by sharedPreferences.boolean("advanced_norounding", false)
    var systemFonts by sharedPreferences.boolean("advanced_systemfonts", false)
    var noVibration by sharedPreferences.boolean("advanced_novibration", false)
    var forceNewYearDrawer by sharedPreferences.boolean("advanced_nydrawer", false)
    var hideProxySponsor by sharedPreferences.boolean("advanced_hideproxysponsor", false)
    var noTyping by sharedPreferences.boolean("advanced_notyping", false)
    var useCupertinoLib by sharedPreferences.boolean("advanced_cupertino", false)
    var drawerAvatar by sharedPreferences.boolean("cg_drawer_avatar", false)
    var flatStatusbar by sharedPreferences.boolean("cg_flat_statusbar", false)
    var flatActionbar by sharedPreferences.boolean("cg_flat_actionbar", false)
    var drawerBlur by sharedPreferences.boolean("cg_drawer_blur", false)
    var drawerDarken by sharedPreferences.boolean("cg_drawer_darken", false)
    var controversiveNoReading by sharedPreferences.boolean("cg_ct_read", false)
    var controversiveNoSecureFlag by sharedPreferences.boolean("cg_ct_flag", false)
    var useBiometricPrompt by sharedPreferences.boolean("cg_newbiometric", false)
    var accentNotification by sharedPreferences.boolean("cg_notification_accent", false)
    var contactsNever by sharedPreferences.boolean("contactsNever", false)
    var searchInActionbar by sharedPreferences.boolean("cg_chats_searchbar", false)
    var confirmCalls by sharedPreferences.boolean("cg_confirmcalls", false)
    var newRepostUI by sharedPreferences.boolean("cg_chatrepost_everywhere", false)
    var profiles_noEdgeTapping by sharedPreferences.boolean("cg_prof_edge", false)
    var profiles_openOnTap by sharedPreferences.boolean("cg_prof_open", false)
    var profiles_alwaysExpand by sharedPreferences.boolean("cg_prof_expand", false)
    var newMessageAnimation by sharedPreferences.boolean("cg_msganim", false)
    var hideKeyboardOnScroll by sharedPreferences.boolean("cg_hidekbd", false)
    var newTabs_noUnread by sharedPreferences.boolean("cg_notabnum", false)
    var newTabs_hideAllChats by sharedPreferences.boolean("cg_ntallchats", false)
    var newTabs_emoji_insteadOfName by sharedPreferences.boolean("newTabs_emoji_insteadOfName", false)
    var newTabs_emoji_appendToName by sharedPreferences.boolean("newTabs_emoji_appendToName", false)

    var redesign_SlideDrawer by sharedPreferences.boolean("cg_redesign_slidedrawer", false)

    var slider_stickerAmplifier by sharedPreferences.int("cg_stickamplifier", 100)

    @JvmStatic
    fun initConfig(preferences: SharedPreferences) {
        Log.d("Catogram", "initConfig")
        sharedPreferences = preferences
        CatogramToasts.init(preferences)
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