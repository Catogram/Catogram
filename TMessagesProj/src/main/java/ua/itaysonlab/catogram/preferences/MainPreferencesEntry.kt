package ua.itaysonlab.catogram.preferences

import android.content.ComponentName
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
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
                icon = R.drawable.ic_palette_outline_28
                divider = true
                listener = TGKitTextIconRow.TGTIListener {
                    it.presentFragment(CatogramPreferencesNavigator.createAppearance())
                }
            }

            textIcon {
                title = LocaleController.getString("AS_Header_Chats", R.string.AS_Header_Chats)
                icon = R.drawable.vkui_chats_outline_28
                divider = true
                listener = TGKitTextIconRow.TGTIListener {
                    it.presentFragment(CatogramPreferencesNavigator.createChats())
                }
            }

            textIcon {
                title = LocaleController.getString("AS_Category_Security", R.string.AS_Category_Security)
                icon = R.drawable.vkui_lock_outline_28
                divider = true
                listener = TGKitTextIconRow.TGTIListener {
                    it.presentFragment(CatogramPreferencesNavigator.createSecurity())
                }
            }

            textIcon {
                title = LocaleController.getString("DebugMenu", R.string.DebugMenu)
                icon = R.drawable.ic_bug_outline_28
                divider = true
                listener = TGKitTextIconRow.TGTIListener {
                    startDebug(it)
                }
            }

            textIcon {
                title = LocaleController.getString("CG_GooglePlay_Donate", R.string.CG_GooglePlay_Donate)
                icon = R.drawable.ic_money_transfer_outline_28
                listener = TGKitTextIconRow.TGTIListener {
                    it.presentFragment(CatogramPreferencesNavigator.createDonate())
                }
            }
        }

        category(LocaleController.getString("AS_Header_About", R.string.AS_Header_About)) {
            textDetail {
                title = "Catogram " + CatogramExtras.CG_VERSION + " [" + BuildVars.BUILD_VERSION_STRING + "]"
                detail = LocaleController.getString("CG_AboutDesc", R.string.CG_AboutDesc)
                divider = true
            }

            textIcon {
                title = LocaleController.getString("CG_ToChannel", R.string.CG_ToChannel)
                value = "@catogram"
                divider = true
                listener = TGKitTextIconRow.TGTIListener {
                    goToChannel(it)
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

        private fun startDebug(bf: BaseFragment) {
            val builder = AlertDialog.Builder(bf.parentActivity)
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
                    if (BuildVars.DEBUG_VERSION && !AndroidUtilities.isTablet()) if (SharedConfig.smoothKeyboard) LocaleController.getString("DebugMenuDisableSmoothKeyboard", R.string.DebugMenuDisableSmoothKeyboard) else LocaleController.getString("DebugMenuEnableSmoothKeyboard", R.string.DebugMenuEnableSmoothKeyboard) else null
            )
            builder.setItems(items) { dialog: DialogInterface?, which: Int ->
                if (which == 0) {
                    UserConfig.getInstance(bf.currentAccount).syncContacts = true
                    UserConfig.getInstance(bf.currentAccount).saveConfig(false)
                    ContactsController.getInstance(bf.currentAccount).forceImportContacts()
                } else if (which == 1) {
                    ContactsController.getInstance(bf.currentAccount).loadContacts(false, 0)
                } else if (which == 2) {
                    ContactsController.getInstance(bf.currentAccount).resetImportedContacts()
                } else if (which == 3) {
                    MessagesController.getInstance(bf.currentAccount).forceResetDialogs()
                } else if (which == 4) {
                    BuildVars.LOGS_ENABLED = !BuildVars.LOGS_ENABLED
                    val sharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("systemConfig", Context.MODE_PRIVATE)
                    sharedPreferences.edit().putBoolean("logsEnabled", BuildVars.LOGS_ENABLED).commit()
                } else if (which == 5) {
                    SharedConfig.toggleInappCamera()
                } else if (which == 6) {
                    MessagesStorage.getInstance(bf.currentAccount).clearSentMedia()
                    SharedConfig.setNoSoundHintShowed(false)
                    val editor = MessagesController.getGlobalMainSettings().edit()
                    editor.remove("archivehint").remove("archivehint_l").remove("gifhint").remove("soundHint").remove("themehint").commit()
                    SharedConfig.textSelectionHintShows = 0
                } else if (which == 7) {
                    VoIPHelper.showCallDebugSettings(bf.parentActivity)
                } else if (which == 8) {
                    SharedConfig.toggleRoundCamera16to9()
                } else if (which == 9) {
                    (bf.parentActivity as LaunchActivity).checkAppUpdate(true)
                } else if (which == 10) {
                    MessagesStorage.getInstance(bf.currentAccount).readAllDialogs(-1)
                } else if (which == 11) {
                    SharedConfig.togglePauseMusicOnRecord()
                } else if (which == 12) {
                    SharedConfig.toggleSmoothKeyboard()
                    if (SharedConfig.smoothKeyboard && bf.parentActivity != null) {
                        bf.parentActivity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
                    }
                }
            }
            builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null)
            bf.showDialog(builder.create())
        }
    }
}