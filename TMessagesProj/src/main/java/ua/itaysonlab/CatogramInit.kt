package ua.itaysonlab

import android.content.Context
import bruhcollective.itaysonlab.airui.core.externalapi.ExternalApiProviders
import bruhcollective.itaysonlab.airui.core.externalapi.providers.AccentColorProvider
import bruhcollective.itaysonlab.airui.core.externalapi.providers.DarkModeProvider
import bruhcollective.itaysonlab.airui.core.externalapi.providers.PaletteProvider
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
                return Theme.isCurrentThemeNight()
            }
        }

        ExternalApiProviders.paletteProvider = object: PaletteProvider() {
            override fun getColor(ctx: Context, color: Color): Int {
                return Theme.getColor(when (color) {
                    Color.TEXT -> Theme.key_windowBackgroundWhiteBlackText
                    Color.TEXT_SECONDARY -> Theme.key_windowBackgroundWhiteGrayText
                    Color.BACKGROUND -> Theme.key_windowBackgroundWhite
                })
            }
        }
    }
}