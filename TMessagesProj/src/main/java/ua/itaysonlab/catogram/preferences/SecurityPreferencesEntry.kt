package ua.itaysonlab.catogram.preferences

import org.telegram.messenger.LocaleController
import org.telegram.messenger.R
import org.telegram.ui.ActionBar.BaseFragment
import ua.itaysonlab.catogram.CatogramConfig
import ua.itaysonlab.catogram.preferences.ktx.*

class SecurityPreferencesEntry : BasePreferencesEntry {
    override fun getPreferences(bf: BaseFragment) = tgKitScreen(LocaleController.getString("AS_Category_Security", R.string.AS_Category_Security)) {
        category(LocaleController.getString("AS_Header_Auth", R.string.AS_Header_Auth)) {
            switch {
                title = LocaleController.getString("AS_Biometric", R.string.AS_Biometric)
                summary = LocaleController.getString("AS_BiometricDesc", R.string.AS_BiometricDesc)

                contract({
                    return@contract CatogramConfig.useBiometricPrompt
                }) {
                    CatogramConfig.useBiometricPrompt = it
                }
            }
        }

        category(LocaleController.getString("AS_Header_Privacy", R.string.AS_Header_Privacy)) {
            /*switch {
                title = LocaleController.getString("AS_NoProxyPromo", R.string.AS_NoProxyPromo)
                divider = true

                contract({
                    return@contract CatogramConfig.hideProxySponsor
                }) {
                    //CatogramConfig.hideProxySponsor = it
                }
            }*/

            switch {
                title = LocaleController.getString("AS_NoTyping", R.string.AS_NoTyping)

                contract({
                    return@contract CatogramConfig.noTyping
                }) {
                    CatogramConfig.noTyping = it
                }
            }

            switch {
                title = LocaleController.getString("AS_ForwardNoAuthorship", R.string.AS_ForwardNoAuthorship)
                summary = LocaleController.getString("AS_ForwardNoAuthorship_Desc", R.string.AS_ForwardNoAuthorship_Desc)

                contract({
                    return@contract CatogramConfig.forwardNoAuthorship
                }) {
                    CatogramConfig.forwardNoAuthorship = it
                }
            }
        }
    }
}