package ua.itaysonlab.catogram.preferences

import org.telegram.messenger.LocaleController
import org.telegram.messenger.R
import org.telegram.ui.ActionBar.BaseFragment
import org.telegram.ui.LaunchActivity
import ua.itaysonlab.catogram.preferences.ktx.category
import ua.itaysonlab.catogram.preferences.ktx.textDetail
import ua.itaysonlab.catogram.preferences.ktx.textIcon
import ua.itaysonlab.catogram.preferences.ktx.tgKitScreen
import ua.itaysonlab.tgkit.preference.types.TGKitTextIconRow

class GPDonateEntry : BasePreferencesEntry {
    override fun getPreferences(bf: BaseFragment) = tgKitScreen(LocaleController.getString("CG_GooglePlay_Donate", R.string.CG_GooglePlay_Donate)) {
        category(LocaleController.getString("CG_GooglePlay_Info", R.string.CG_GooglePlay_Info)) {
            textDetail {
                title = LocaleController.getString("CG_GooglePlay_Info", R.string.CG_GooglePlay_Info)
                detail = LocaleController.getString("CG_GooglePlay_InfoDesc", R.string.CG_GooglePlay_InfoDesc)
            }
        }

        category(LocaleController.getString("CG_GooglePlay_ChooseAmount", R.string.CG_GooglePlay_ChooseAmount)) {
            textIcon {
                title = LocaleController.getString("CG_GooglePlay_Amount_One", R.string.CG_GooglePlay_Amount_One)
                divider = true

                listener = TGKitTextIconRow.TGTIListener {
                    (bf.parentActivity as LaunchActivity).bp.consumePurchase("cg_donation_one")
                    (bf.parentActivity as LaunchActivity).bp.purchase(bf.parentActivity, "cg_donation_one")
                }
            }

            textIcon {
                title = LocaleController.getString("CG_GooglePlay_Amount_Two", R.string.CG_GooglePlay_Amount_Two)
                divider = true

                listener = TGKitTextIconRow.TGTIListener {
                    (bf.parentActivity as LaunchActivity).bp.consumePurchase("cg_donation_two")
                    (bf.parentActivity as LaunchActivity).bp.purchase(bf.parentActivity, "cg_donation_two")
                }
            }

            textIcon {
                title = LocaleController.getString("CG_GooglePlay_Amount_Five", R.string.CG_GooglePlay_Amount_Five)
                divider = true

                listener = TGKitTextIconRow.TGTIListener {
                    (bf.parentActivity as LaunchActivity).bp.consumePurchase("cg_donation_five")
                    (bf.parentActivity as LaunchActivity).bp.purchase(bf.parentActivity, "cg_donation_five")
                }
            }
        }
    }
}