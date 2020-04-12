package ua.itaysonlab.catogram;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Build;

import com.google.android.exoplayer2.util.Log;

import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;

import ua.itaysonlab.catogram.ui.CatogramToasts;

@SuppressWarnings("SpellCheckingInspection")
@SuppressLint("ApplySharedPref")
public class CatogramConfig {
    public static boolean hidePhoneNumber = false;
    public static boolean forceNewYear = false;
    public static boolean forcePacman = false;
    public static boolean noRounding = true;
    public static boolean systemFonts = false;
    public static boolean noVibration = false;
    public static boolean forceNewYearDrawer = false;

    public static boolean hideProxySponsor = false;
    public static boolean noTyping = false;

    public static boolean useCupertinoLib = false;

    public static boolean flatStatusbar = false;
    public static boolean flatActionbar = false;

    public static boolean drawerAvatar = false;
    public static boolean drawerBlur = false;
    public static boolean drawerDarken = false;
    public static boolean drawerUsername = false;

    public static boolean filterChatCount = false;
    public static boolean filterHintFAB = true;
    public static boolean filterHintToolbar = true;
    public static boolean filterInsteadOfWrite = false;

    public static String savedFilter = "all";

    public static boolean controversiveNoReading = false;
    public static boolean controversiveNoSecureFlag = false;

    public static boolean useBiometricPrompt = true;
    public static boolean accentNotification = true;

    public static boolean contactsNever = false;

    public static boolean searchInActionbar = false;
    public static boolean confirmCalls = false;
    public static boolean newRepostUI = false;

    public static boolean profiles_noEdgeTapping = false;
    public static boolean profiles_openOnTap = false;
    public static boolean profiles_alwaysExpand = false;

    public static boolean newMessageAnimation = false;
    public static boolean hideKeyboardOnScroll = false;

    public static boolean newTabs_noUnread = false;
    public static boolean newTabs_hideAllChats = false;

    public static boolean newTabs_emoji_insteadOfName = false;
    public static boolean newTabs_emoji_appendToName = false;

    public static int slider_stickerAmplifier = 100;

    public static void initConfig(SharedPreferences preferences) {
        Log.d("Catogram", "initConfig");
        hidePhoneNumber = preferences.getBoolean("advanced_hidephonenumber", false);
        forceNewYear = preferences.getBoolean("advanced_forcenewyear", false);
        forcePacman = preferences.getBoolean("advanced_forcepacman", false);
        noRounding = preferences.getBoolean("advanced_norounding", true);
        noTyping = preferences.getBoolean("advanced_notyping", false);
        hideProxySponsor = preferences.getBoolean("advanced_hideproxysponsor", false);
        systemFonts = preferences.getBoolean("advanced_systemfonts", false);
        noVibration = preferences.getBoolean("advanced_novibration", false);
        useCupertinoLib = preferences.getBoolean("advanced_cupertino", false);
        forceNewYearDrawer = preferences.getBoolean("advanced_nydrawer", false);

        drawerAvatar = preferences.getBoolean("cg_drawer_avatar", false);
        drawerBlur = preferences.getBoolean("cg_drawer_blur", false);
        drawerDarken = preferences.getBoolean("cg_drawer_darken", false);
        drawerUsername = preferences.getBoolean("cg_drawer_username", false);

        flatActionbar = preferences.getBoolean("cg_flat_actionbar", false);
        flatStatusbar = preferences.getBoolean("cg_flat_statusbar", false);

        filterChatCount = preferences.getBoolean("cg_filters_chatcount", false);
        filterHintFAB = preferences.getBoolean("cg_filters_hint_toolbar", true);
        filterHintToolbar = preferences.getBoolean("cg_filters_hint_fab", true);
        filterInsteadOfWrite = preferences.getBoolean("cg_filters_replacefab", false);

        savedFilter = preferences.getString("cg_filters_saved", "all");

        controversiveNoSecureFlag = preferences.getBoolean("cg_ct_flag", false);
        controversiveNoReading = preferences.getBoolean("cg_ct_read", false);

        useBiometricPrompt = preferences.getBoolean("cg_newbiometric", true);
        accentNotification = preferences.getBoolean("cg_notification_accent", true);

        contactsNever = preferences.getBoolean("contactsNever", false);

        searchInActionbar = preferences.getBoolean("cg_chats_searchbar", false);
        confirmCalls = preferences.getBoolean("cg_confirmcalls", false);
        newRepostUI = preferences.getBoolean("cg_chatrepost_everywhere", false);

        profiles_noEdgeTapping = preferences.getBoolean("cg_prof_edge", false);
        profiles_openOnTap = preferences.getBoolean("cg_prof_open", false);
        profiles_alwaysExpand = preferences.getBoolean("cg_prof_expand", false);

        slider_stickerAmplifier = preferences.getInt("cg_stickamplifier", 100);
        newMessageAnimation = preferences.getBoolean("cg_msganim", false);
        hideKeyboardOnScroll = preferences.getBoolean("cg_hidekbd", false);

        newTabs_noUnread = preferences.getBoolean("cg_notabnum", false);
        newTabs_hideAllChats = preferences.getBoolean("cg_ntallchats", false);
        newTabs_emoji_insteadOfName = preferences.getBoolean("newTabs_emoji_insteadOfName", false);
        newTabs_emoji_appendToName = preferences.getBoolean("newTabs_emoji_appendToName", false);

        CatogramToasts.init(preferences);
    }

    public static void setStickerAmplifier(int value) {
        slider_stickerAmplifier = value;

        SharedPreferences preferences = MessagesController.getGlobalMainSettings();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("cg_stickamplifier", value);
        editor.commit();
    }

    public static void toggleProfilesNoEdgeTapping() {
        profiles_noEdgeTapping = !profiles_noEdgeTapping;
        putBoolean("cg_prof_edge", profiles_noEdgeTapping);
    }

    public static void toggleAllChats() {
        newTabs_hideAllChats = !newTabs_hideAllChats;
        putBoolean("cg_ntallchats", newTabs_hideAllChats);
    }

    public static void toggleEmojiIN() {
        newTabs_emoji_insteadOfName = !newTabs_emoji_insteadOfName;
        putBoolean("newTabs_emoji_insteadOfName", newTabs_emoji_insteadOfName);
    }

    public static void toggleEmojiAN() {
        newTabs_emoji_appendToName = !newTabs_emoji_appendToName;
        putBoolean("newTabs_emoji_appendToName", newTabs_emoji_appendToName);
    }

    public static void toggleTabCount() {
        newTabs_noUnread = !newTabs_noUnread;
        putBoolean("cg_notabnum", newTabs_noUnread);
    }

    public static void toggleProfilesOpenOnTap() {
        profiles_openOnTap = !profiles_openOnTap;
        putBoolean("cg_prof_open", profiles_openOnTap);
    }

    public static void toggleNewMessageAnimation() {
        newMessageAnimation = !newMessageAnimation;
        putBoolean("cg_msganim", newMessageAnimation);
    }

    public static void toggleHideKbdOnScroll() {
        hideKeyboardOnScroll = !hideKeyboardOnScroll;
        putBoolean("cg_hidekbd", hideKeyboardOnScroll);
    }

    public static void toggleProfilesAlwaysExpand() {
        profiles_alwaysExpand = !profiles_alwaysExpand;
        putBoolean("cg_prof_expand", profiles_alwaysExpand);
    }

    public static void toggleNewRepostUI() {
        newRepostUI = !newRepostUI;
        putBoolean("cg_chatrepost_everywhere", newRepostUI);
    }

    public static void toggleChatActionbarSearch() {
        searchInActionbar = !searchInActionbar;
        putBoolean("cg_chats_searchbar", searchInActionbar);
    }

    public static void toggleCallConfirm() {
        confirmCalls = !confirmCalls;
        putBoolean("cg_confirmcalls", confirmCalls);
    }

    public static void toggleDrawerUsername() {
        drawerUsername = !drawerUsername;
        putBoolean("cg_drawer_username", drawerUsername);
    }

    private static void putBoolean(String key, boolean value) {
        SharedPreferences preferences = MessagesController.getGlobalMainSettings();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    private static void putString(String key, String value) {
        SharedPreferences preferences = MessagesController.getGlobalMainSettings();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static void setSavedFilter(String path) {
        savedFilter = path;
        putString("cg_filters_saved", path);
    }

    public static void shownFABHint() {
        filterHintFAB = false;
        putBoolean("cg_filters_hint_fab", false);
    }

    public static void shownContactsNever() {
        contactsNever = true;
        putBoolean("contactsNever", true);
    }

    public static void toggleToolbarHint() {
        filterHintToolbar = !filterHintToolbar;
        putBoolean("cg_filters_hint_toolbar", filterHintToolbar);
    }

    public static void toggleFilterReplaceFAB() {
        filterInsteadOfWrite = !filterInsteadOfWrite;
        putBoolean("cg_filters_replacefab", filterInsteadOfWrite);
    }

    public static void toggleAccentNotification() {
        accentNotification = !accentNotification;
        putBoolean("cg_notification_accent", accentNotification);
    }

    public static void toggleNewBiometric() {
        useBiometricPrompt = !useBiometricPrompt;
        putBoolean("cg_newbiometric", useBiometricPrompt);
    }

    public static void toggleFlatSB() {
        flatStatusbar = !flatStatusbar;
        putBoolean("cg_flat_statusbar", flatStatusbar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.needCheckSystemBarColors);
        }
    }

    public static void toggleFilterChatCount() {
        filterChatCount = !filterChatCount;
        putBoolean("cg_filters_chatcount", filterChatCount);
    }

    public static void toggleFlatAB() {
        flatActionbar = !flatActionbar;
        putBoolean("cg_flat_actionbar", flatActionbar);
    }

    public static void toggleDrawerAvatar() {
        drawerAvatar = !drawerAvatar;
        putBoolean("cg_drawer_avatar", drawerAvatar);
    }

    public static void toggleDrawerDarken() {
        drawerDarken = !drawerDarken;
        putBoolean("cg_drawer_darken", drawerDarken);
    }

    public static void toggleDrawerBlur() {
        drawerBlur = !drawerBlur;
        putBoolean("cg_drawer_blur", drawerBlur);
    }

    public static void toggleNoVibration() {
        noVibration = !noVibration;
        putBoolean("advanced_novibration", noVibration);
    }

    public static void toggleForceNewYearDrawer() {
        forceNewYearDrawer = !forceNewYearDrawer;
        putBoolean("advanced_nydrawer", forceNewYearDrawer);
    }

    public static void toggleCupertinoLib() {
        useCupertinoLib = !useCupertinoLib;
        putBoolean("advanced_cupertino", useCupertinoLib);
    }

    public static void toggleHideProxySponsor() {
        hideProxySponsor = !hideProxySponsor;
        putBoolean("advanced_hideproxysponsor", hideProxySponsor);
    }

    public static void toggleSystemFonts() {
        systemFonts = !systemFonts;
        putBoolean("advanced_systemfonts", systemFonts);
    }

    public static void toggleNoTyping() {
        noTyping = !noTyping;
        putBoolean("advanced_notyping", noTyping);
    }

    public static void toggleHidePhoneNumber() {
        hidePhoneNumber = !hidePhoneNumber;
        putBoolean("advanced_hidephonenumber", hidePhoneNumber);
    }

    public static void toggleForceNewYear() {
        forceNewYear = !forceNewYear;
        putBoolean("advanced_forcenewyear", forceNewYear);
    }

    public static void toggleForcePacman() {
        forcePacman = !forcePacman;
        putBoolean("advanced_forcepacman", forcePacman);
    }

    public static void toggleNoRounding() {
        noRounding = !noRounding;
        putBoolean("advanced_norounding", noRounding);
    }

    public static void toggleDisableFlagSecure() {
        controversiveNoSecureFlag = !controversiveNoSecureFlag;
        putBoolean("cg_ct_flag", controversiveNoSecureFlag);
    }

    public static void toggleCTNoReading() {
        controversiveNoReading = !controversiveNoReading;
        putBoolean("cg_ct_reading", controversiveNoReading);
    }
}
