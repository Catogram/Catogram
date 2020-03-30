package ua.itaysonlab.catogram.preferences;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.WindowManager;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.voip.VoIPHelper;
import org.telegram.ui.LaunchActivity;

import java.util.ArrayList;
import java.util.List;

import ua.itaysonlab.catogram.CatogramPreferencesNavigator;
import ua.itaysonlab.extras.CatogramExtras;
import ua.itaysonlab.tgkit.preference.TGKitCategory;
import ua.itaysonlab.tgkit.preference.TGKitPreference;
import ua.itaysonlab.tgkit.preference.TGKitSettings;
import ua.itaysonlab.tgkit.preference.types.TGKitTextDetailRow;
import ua.itaysonlab.tgkit.preference.types.TGKitTextIconRow;

public class MainPreferencesEntry implements BasePreferencesEntry {
    @Override
    public TGKitSettings getPreferences() {
        List<TGKitCategory> categories = new ArrayList<>();

        List<TGKitPreference> sections = new ArrayList<>();
        sections.add(new TGKitTextIconRow(LocaleController.getString("AS_Header_Appearance", R.string.AS_Header_Appearance), null, R.drawable.msg_theme, true, (bf) -> bf.presentFragment(CatogramPreferencesNavigator.createAppearance())));
        sections.add(new TGKitTextIconRow(LocaleController.getString("AS_Header_Chats", R.string.AS_Header_Chats), null, R.drawable.menu_chats, true, (bf) -> bf.presentFragment(CatogramPreferencesNavigator.createChats())));
        sections.add(new TGKitTextIconRow(LocaleController.getString("AS_Category_Security", R.string.AS_Category_Security), null, R.drawable.menu_secret, true, (bf) -> bf.presentFragment(CatogramPreferencesNavigator.createSecurity())));
        sections.add(new TGKitTextIconRow(LocaleController.getString("DebugMenu", R.string.DebugMenu), null, R.drawable.group_log, false, MainPreferencesEntry::startDebug));
        categories.add(new TGKitCategory(LocaleController.getString("AS_Header_Categories", R.string.AS_Header_Categories), sections));

        List<TGKitPreference> about = new ArrayList<>();
        about.add(new TGKitTextDetailRow("Catogram " + CatogramExtras.CG_VERSION + " [" + BuildVars.BUILD_VERSION_STRING + "]", LocaleController.getString("CG_AboutDesc", R.string.CG_AboutDesc), true));
        about.add(new TGKitTextIconRow(LocaleController.getString("CG_ToChannel", R.string.CG_ToChannel), "@catogram", -1, true, MainPreferencesEntry::goToChannel));
        about.add(new TGKitTextIconRow(LocaleController.getString("CG_Donate", R.string.CG_Donate), null, -1, false, MainPreferencesEntry::goToDonate));
        categories.add(new TGKitCategory(LocaleController.getString("AS_Header_About", R.string.AS_Header_About), about));

        return new TGKitSettings(LocaleController.getString("AdvancedSettings", R.string.AdvancedSettings), categories);
    }

    private static void goToChannel(BaseFragment bf) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/catogram"));
        ComponentName componentName = new ComponentName(bf.getParentActivity().getPackageName(), LaunchActivity.class.getName());
        intent.setComponent(componentName);
        intent.putExtra(android.provider.Browser.EXTRA_CREATE_NEW_TAB, true);
        intent.putExtra(android.provider.Browser.EXTRA_APPLICATION_ID, bf.getParentActivity().getPackageName());
        bf.getParentActivity().startActivity(intent);
    }

    private static void goToDonate(BaseFragment bf) {
        bf.getParentActivity().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://qiwi.me/itaysonlab")));
    }

    private static void startDebug(BaseFragment bf) {
        AlertDialog.Builder builder = new AlertDialog.Builder(bf.getParentActivity());
        builder.setTitle(LocaleController.getString("DebugMenu", R.string.DebugMenu));
        CharSequence[] items;
        items = new CharSequence[]{
                LocaleController.getString("DebugMenuImportContacts", R.string.DebugMenuImportContacts),
                LocaleController.getString("DebugMenuReloadContacts", R.string.DebugMenuReloadContacts),
                LocaleController.getString("DebugMenuResetContacts", R.string.DebugMenuResetContacts),
                LocaleController.getString("DebugMenuResetDialogs", R.string.DebugMenuResetDialogs),
                BuildVars.LOGS_ENABLED ? LocaleController.getString("DebugMenuDisableLogs", R.string.DebugMenuDisableLogs) : LocaleController.getString("DebugMenuEnableLogs", R.string.DebugMenuEnableLogs),
                SharedConfig.inappCamera ? LocaleController.getString("DebugMenuDisableCamera", R.string.DebugMenuDisableCamera) : LocaleController.getString("DebugMenuEnableCamera", R.string.DebugMenuEnableCamera),
                LocaleController.getString("DebugMenuClearMediaCache", R.string.DebugMenuClearMediaCache),
                LocaleController.getString("DebugMenuCallSettings", R.string.DebugMenuCallSettings),
                null,
                BuildVars.DEBUG_PRIVATE_VERSION ? "Check for app updates" : null,
                LocaleController.getString("DebugMenuReadAllDialogs", R.string.DebugMenuReadAllDialogs),
                SharedConfig.pauseMusicOnRecord ? LocaleController.getString("DebugMenuDisablePauseMusic", R.string.DebugMenuDisablePauseMusic) : LocaleController.getString("DebugMenuEnablePauseMusic", R.string.DebugMenuEnablePauseMusic),
                BuildVars.DEBUG_VERSION && !AndroidUtilities.isTablet() ? (SharedConfig.smoothKeyboard ? LocaleController.getString("DebugMenuDisableSmoothKeyboard", R.string.DebugMenuDisableSmoothKeyboard) : LocaleController.getString("DebugMenuEnableSmoothKeyboard", R.string.DebugMenuEnableSmoothKeyboard)) : null
        };
        builder.setItems(items, (dialog, which) -> {
            if (which == 0) {
                UserConfig.getInstance(bf.getCurrentAccount()).syncContacts = true;
                UserConfig.getInstance(bf.getCurrentAccount()).saveConfig(false);
                ContactsController.getInstance(bf.getCurrentAccount()).forceImportContacts();
            } else if (which == 1) {
                ContactsController.getInstance(bf.getCurrentAccount()).loadContacts(false, 0);
            } else if (which == 2) {
                ContactsController.getInstance(bf.getCurrentAccount()).resetImportedContacts();
            } else if (which == 3) {
                MessagesController.getInstance(bf.getCurrentAccount()).forceResetDialogs();
            } else if (which == 4) {
                BuildVars.LOGS_ENABLED = !BuildVars.LOGS_ENABLED;
                SharedPreferences sharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("systemConfig", Context.MODE_PRIVATE);
                sharedPreferences.edit().putBoolean("logsEnabled", BuildVars.LOGS_ENABLED).commit();
            } else if (which == 5) {
                SharedConfig.toggleInappCamera();
            } else if (which == 6) {
                MessagesStorage.getInstance(bf.getCurrentAccount()).clearSentMedia();
                SharedConfig.setNoSoundHintShowed(false);
                SharedPreferences.Editor editor = MessagesController.getGlobalMainSettings().edit();
                editor.remove("archivehint").remove("archivehint_l").remove("gifhint").remove("soundHint").remove("themehint").commit();
                SharedConfig.textSelectionHintShows = 0;
            } else if (which == 7) {
                VoIPHelper.showCallDebugSettings(bf.getParentActivity());
            } else if (which == 8) {
                SharedConfig.toggleRoundCamera16to9();
            } else if (which == 9) {
                ((LaunchActivity) bf.getParentActivity()).checkAppUpdate(true);
            } else if (which == 10) {
                MessagesStorage.getInstance(bf.getCurrentAccount()).readAllDialogs(-1);
            } else if (which == 11) {
                SharedConfig.togglePauseMusicOnRecord();
            } else if (which == 12) {
                SharedConfig.toggleSmoothKeyboard();
                if (SharedConfig.smoothKeyboard && bf.getParentActivity() != null) {
                    bf.getParentActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
                }
            }
        });
        builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
        bf.showDialog(builder.create());
    }
}
