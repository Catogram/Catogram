package ua.itaysonlab.catogram.preferences

import android.util.Base64
import org.telegram.messenger.LocaleController
import org.telegram.messenger.R
import org.telegram.messenger.UserConfig
import org.telegram.tgnet.TLRPC
import org.telegram.ui.ActionBar.BaseFragment
import ua.itaysonlab.catogram.double_bottom.DoubleBottomPasscodeActivity
import ua.itaysonlab.catogram.double_bottom.DoubleBottomStorageBridge
import ua.itaysonlab.catogram.preferences.ktx.*
import ua.itaysonlab.tgkit.preference.types.TGKitTextIconRow

class DoubleBottomPreferencesEntry : BasePreferencesEntry {
    override fun getPreferences(bf: BaseFragment) = tgKitScreen(LocaleController.getString("AS_Header_DoubleBottom", R.string.AS_Header_DoubleBottom)) {
        category(LocaleController.getString("CG_DB_Accounts", R.string.CG_DB_Accounts)) {
            getProfiles().forEach { profile ->
                textIcon {
                    title = "${profile.first_name} ${profile.last_name} (${profile.username})"
                    icon = R.drawable.user_circle_outline_28
                    listener = TGKitTextIconRow.TGTIListener {
                        setupBottomFor(bf, profile)
                    }
                }
            }

            hint(LocaleController.getString("CG_DB_AccountsHint", R.string.CG_DB_AccountsHint))
        }
    }

    private fun setupBottomFor(bf: BaseFragment, profile: TLRPC.User) {
        bf.presentFragment(DoubleBottomPasscodeActivity(1) { hash, salt, type ->
            DoubleBottomStorageBridge.storageInstance = DoubleBottomStorageBridge.storageInstance.also {
                it.map[profile.id.toString()] = DoubleBottomStorageBridge.DBAccountData(
                        type = type,
                        hash = hash,
                        salt = Base64.encodeToString(salt, Base64.DEFAULT)
                )
            }
        })
    }

    private fun getProfiles(): List<TLRPC.User> {
        val list = mutableListOf<TLRPC.User>()

        for (i in 0..UserConfig.MAX_ACCOUNT_COUNT) {
            val uc = UserConfig.getInstance(i)
            if (uc.isClientActivated) list.add(uc.currentUser)
        }

        return list
    }
}