package ua.itaysonlab.catogram.preferences

import org.telegram.messenger.LocaleController
import org.telegram.messenger.R
import org.telegram.ui.ActionBar.BaseFragment
import org.telegram.ui.LaunchActivity
import ua.itaysonlab.catogram.preferences.ktx.*
import ua.itaysonlab.tgkit.preference.types.TGKitTextIconRow

class DoubleBottomPreferencesEntry : BasePreferencesEntry {
    override fun getPreferences(bf: BaseFragment) = tgKitScreen(LocaleController.getString("AS_Header_DoubleBottom", R.string.AS_Header_DoubleBottom)) {
        category(LocaleController.getString("CG_DB_Accounts", R.string.CG_DB_Accounts)) {
            textDetail {
                title = LocaleController.getString("CG_GooglePlay_Info", R.string.CG_GooglePlay_Info)
                detail = LocaleController.getString("CG_GooglePlay_InfoDesc", R.string.CG_GooglePlay_InfoDesc)
            }
            
            hint(LocaleController.getString("CG_DB_AccountsHint", R.string.CG_DB_AccountsHint))
        }
    }
}