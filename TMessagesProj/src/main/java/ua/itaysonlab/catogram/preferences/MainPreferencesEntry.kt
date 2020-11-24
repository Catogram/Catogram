package ua.itaysonlab.catogram.preferences

import android.content.ComponentName
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Browser
import android.view.WindowManager
import org.telegram.messenger.*
import org.telegram.ui.ActionBar.AlertDialog
import org.telegram.ui.ActionBar.BaseFragment
import org.telegram.ui.Components.voip.VoIPHelper
import org.telegram.ui.LaunchActivity
import ua.itaysonlab.catogram.CatogramPreferencesNavigator
import ua.itaysonlab.catogram.preferences.ktx.category
import ua.itaysonlab.catogram.preferences.ktx.textDetail
import ua.itaysonlab.catogram.preferences.ktx.textIcon
import ua.itaysonlab.catogram.preferences.ktx.tgKitScreen
import ua.itaysonlab.extras.CatogramExtras
import ua.itaysonlab.tgkit.preference.types.TGKitTextIconRow

class MainPreferencesEntry : BasePreferencesEntry {
    override fun getPreferences(bf: BaseFragment) = tgKitScreen(LocaleController.getString("AdvancedSettings", R.string.AdvancedSettings)) {
        category(LocaleController.getString("AS_Header_Categories", R.string.AS_Header_Categories)) {
            textIcon {
                title = LocaleController.getString("AS_Header_Appearance", R.string.AS_Header_Appearance)
                icon = R.drawable.msg_theme
                listener = TGKitTextIconRow.TGTIListener {
                    it.presentFragment(CatogramPreferencesNavigator.createAppearance())
                }
            }

            textIcon {
                title = LocaleController.getString("AS_Header_Chats", R.string.AS_Header_Chats)
                icon = R.drawable.menu_chats
                listener = TGKitTextIconRow.TGTIListener {
                    it.presentFragment(CatogramPreferencesNavigator.createChats())
                }
            }

            textIcon {
                title = LocaleController.getString("AS_Category_Security", R.string.AS_Category_Security)
                icon = R.drawable.menu_secret
                listener = TGKitTextIconRow.TGTIListener {
                    it.presentFragment(CatogramPreferencesNavigator.createSecurity())
                }
            }

            textIcon {
                title = LocaleController.getString("DebugMenu", R.string.DebugMenu)
                icon = R.drawable.group_log
                listener = TGKitTextIconRow.TGTIListener {
                    startDebug(it)
                }
            }

            category(LocaleController.getString("AS_Header_About", R.string.AS_Header_About)) {
                textDetail {
                    title = "Catogram " + CatogramExtras.CG_VERSION + " [" + BuildVars.BUILD_VERSION_STRING + "]"
                    detail = LocaleController.getString("CG_AboutDesc", R.string.CG_AboutDesc)
                }

                textIcon {
                    title = LocaleController.getString("CG_ToChannel", R.string.CG_ToChannel)
                    value = "@catogram"
                    listener = TGKitTextIconRow.TGTIListener {
                        goToChannel(it)
                    }
                }
                textIcon {
                    title = LocaleController.getString("CG_ToChat", R.string.CG_ToChat)
                    value = "@catogram_en"
                    listener = TGKitTextIconRow.TGTIListener {
                        goToChat(it)
                    }
                }
                textIcon {
                    title = LocaleController.getString("CG_GooglePlay_Donate", R.string.CG_GooglePlay_Donate)
                    divider = true
                    value = LocaleController.getString("CG_DonatDesc", R.string.CG_DonatDesc)
                    listener = TGKitTextIconRow.TGTIListener {
                        it.presentFragment(CatogramPreferencesNavigator.createDonate())
                    }
                }
            }
        }
    }

    companion object {
        private fun goToChannel(bf: BaseFragment) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/catogram"))
            val componentName = ComponentName(bf.parentActivity.packageName, LaunchActivity::class.java.name)
            intent.component = componentName
            intent.putExtra(Browser.EXTRA_CREATE_NEW_TAB, true)
            intent.putExtra(Browser.EXTRA_APPLICATION_ID, bf.parentActivity.packageName)
            bf.parentActivity.startActivity(intent)
        }

        private fun goToChat(bf: BaseFragment) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/catogram_en"))
            val componentName = ComponentName(bf.parentActivity.packageName, LaunchActivity::class.java.name)
            intent.component = componentName
            intent.putExtra(Browser.EXTRA_CREATE_NEW_TAB, true)
            intent.putExtra(Browser.EXTRA_APPLICATION_ID, bf.parentActivity.packageName)
            bf.parentActivity.startActivity(intent)
        }

        private fun startDebug(bf: BaseFragment) {
            val builder = AlertDialog.Builder(bf.getParentActivity())
            builder.setTitle(LocaleController.getString("DebugMenu", R.string.DebugMenu))
            val items: Array<CharSequence?> = arrayOf(
                    LocaleController.getString("DebugMenuImportContacts", R.string.DebugMenuImportContacts),
                    LocaleController.getString("DebugMenuReloadContacts", R.string.DebugMenuReloadContacts),
                    LocaleController.getString("DebugMenuResetContacts", R.string.DebugMenuResetContacts),
                    LocaleController.getString("DebugMenuResetDialogs", R.string.DebugMenuResetDialogs),
                    if (BuildVars.LOGS_ENABLED) LocaleController.getString("DebugMenuDisableLogs", R.string.DebugMenuDisableLogs) else LocaleController.getString("DebugMenuEnableLogs", R.string.DebugMenuEnableLogs),
                    if (SharedConfig.inappCamera) LocaleController.getString("DebugMenuDisableCamera", R.string.DebugMenuDisableCamera) else LocaleController.getString("DebugMenuEnableCamera", R.string.DebugMenuEnableCamera),
                    LocaleController.getString("DebugMenuClearMediaCache", R.string.DebugMenuClearMediaCache),
                    LocaleController.getString("DebugMenuCallSettings", R.string.DebugMenuCallSettings),
                    null,
                    if (BuildVars.DEBUG_PRIVATE_VERSION) "Check for app updates" else null,
                    LocaleController.getString("DebugMenuReadAllDialogs", R.string.DebugMenuReadAllDialogs),
                    if (SharedConfig.pauseMusicOnRecord) LocaleController.getString("DebugMenuDisablePauseMusic", R.string.DebugMenuDisablePauseMusic) else LocaleController.getString("DebugMenuEnablePauseMusic", R.string.DebugMenuEnablePauseMusic),
                    if (BuildVars.DEBUG_VERSION && !AndroidUtilities.isTablet() && Build.VERSION.SDK_INT >= 23) if (SharedConfig.smoothKeyboard) LocaleController.getString("DebugMenuDisableSmoothKeyboard", R.string.DebugMenuDisableSmoothKeyboard) else LocaleController.getString("DebugMenuEnableSmoothKeyboard", R.string.DebugMenuEnableSmoothKeyboard) else null,
                    if (Build.VERSION.SDK_INT >= 29) if (SharedConfig.chatBubbles) "Disable chat bubbles" else "Enable chat bubbles" else null
            )
            builder.setItems(items) { dialog: DialogInterface?, which: Int ->
                if (which == 0) {
                    bf.getUserConfig().syncContacts = true
                    bf.getUserConfig().saveConfig(false)
                    bf.getContactsController().forceImportContacts()
                } else if (which == 1) {
                    bf.getContactsController().loadContacts(false, 0)
                } else if (which == 2) {
                    bf.getContactsController().resetImportedContacts()
                } else if (which == 3) {
                    bf.getMessagesController().forceResetDialogs()
                } else if (which == 4) {
                    BuildVars.LOGS_ENABLED = !BuildVars.LOGS_ENABLED
                    val sharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("systemConfig", Context.MODE_PRIVATE)
                    sharedPreferences.edit().putBoolean("logsEnabled", BuildVars.LOGS_ENABLED).commit()
                } else if (which == 5) {
                    SharedConfig.toggleInappCamera()
                } else if (which == 6) {
                    bf.getMessagesStorage().clearSentMedia()
                    SharedConfig.setNoSoundHintShowed(false)
                    val editor = MessagesController.getGlobalMainSettings().edit()
                    editor.remove("archivehint").remove("proximityhint").remove("archivehint_l").remove("gifhint").remove("soundHint").remove("themehint").remove("filterhint").commit()
                    SharedConfig.textSelectionHintShows = 0
                    SharedConfig.lockRecordAudioVideoHint = 0
                    SharedConfig.stickersReorderingHintUsed = false
                } else if (which == 7) {
                    VoIPHelper.showCallDebugSettings(bf.parentActivity)
                } else if (which == 8) {
                    SharedConfig.toggleRoundCamera16to9()
                } else if (which == 9) {
                    (bf.parentActivity as LaunchActivity).checkAppUpdate(true)
                } else if (which == 10) {
                    bf.getMessagesStorage().readAllDialogs(-1)
                } else if (which == 11) {
                    SharedConfig.togglePauseMusicOnRecord()
                } else if (which == 12) {
                    SharedConfig.toggleSmoothKeyboard()
                    if (SharedConfig.smoothKeyboard && bf.getParentActivity() != null) {
                        bf.parentActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
                    }
                } else if (which == 13) {
                    SharedConfig.toggleChatBubbles()
                }
            }
            builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null)
            bf.showDialog(builder.create())
        }
    }
}
