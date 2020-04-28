package ua.itaysonlab.catogram.preferences

import ua.itaysonlab.tgkit.preference.TGKitSettings

interface BasePreferencesEntry {
    fun getPreferences(): TGKitSettings
}