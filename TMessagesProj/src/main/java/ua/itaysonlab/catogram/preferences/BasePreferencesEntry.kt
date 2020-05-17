package ua.itaysonlab.catogram.preferences

import org.telegram.ui.ActionBar.BaseFragment
import ua.itaysonlab.tgkit.preference.TGKitSettings

interface BasePreferencesEntry {
    fun getPreferences(bf: BaseFragment): TGKitSettings
}