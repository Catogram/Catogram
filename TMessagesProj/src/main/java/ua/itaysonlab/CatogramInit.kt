package ua.itaysonlab

import android.content.Context
import androidx.core.graphics.ColorUtils
import bruhcollective.itaysonlab.airui.core.externalapi.ExternalApiProviders
import bruhcollective.itaysonlab.airui.core.externalapi.providers.AccentColorProvider
import bruhcollective.itaysonlab.airui.core.externalapi.providers.DarkModeProvider
import org.telegram.ui.ActionBar.Theme

object CatogramInit {
    fun init(ctx: Context) {
        ExternalApiProviders.accentColorProvider = object: AccentColorProvider() {
            override fun resolveColor(context: Context): Int {
                return Theme.getColor(Theme.key_windowBackgroundWhiteBlueText)
            }
        }

        ExternalApiProviders.darkModeProvider = object: DarkModeProvider() {
            override fun provide(ctx: Context): Boolean {
                return ColorUtils.calculateLuminance(Theme.getColor(Theme.key_windowBackgroundWhite)) < 0.5f
            }
        }
    }
}