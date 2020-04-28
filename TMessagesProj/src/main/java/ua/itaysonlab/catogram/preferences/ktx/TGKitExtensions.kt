package ua.itaysonlab.catogram.preferences.ktx

import ua.itaysonlab.tgkit.preference.TGKitCategory
import ua.itaysonlab.tgkit.preference.TGKitPreference
import ua.itaysonlab.tgkit.preference.TGKitSettings
import ua.itaysonlab.tgkit.preference.types.TGKitSliderPreference
import ua.itaysonlab.tgkit.preference.types.TGKitSwitchPreference
import ua.itaysonlab.tgkit.preference.types.TGKitTextDetailRow
import ua.itaysonlab.tgkit.preference.types.TGKitTextIconRow

fun tgKitScreen(name: String, block: TGKitScreen.() -> Unit) = TGKitSettings(name, mutableListOf<TGKitCategory>().apply(block))

fun TGKitScreen.category(name: String, block: TGKitPreferences.() -> Unit) = add(
        TGKitCategory(name, mutableListOf<TGKitPreference>().apply(block))
)

fun TGKitPreferences.switch(block: TGKitSwitchPreference.() -> Unit) = add(TGKitSwitchPreference().apply(block))
fun TGKitPreferences.slider(block: TGKitSliderPreference.() -> Unit) = add(TGKitSliderPreference().apply(block))
fun TGKitPreferences.textIcon(block: TGKitTextIconRow.() -> Unit) = add(TGKitTextIconRow().apply(block))
fun TGKitPreferences.textDetail(block: TGKitTextDetailRow.() -> Unit) = add(TGKitTextDetailRow().apply(block))

fun TGKitSwitchPreference.contract(getValue: () -> Boolean, setValue: (Boolean) -> Unit) {
    contract = object: TGKitSwitchPreference.TGSPContract {
        override fun getPreferenceValue() = getValue()
        override fun toggleValue() = setValue(!getValue())
    }
}

typealias TGKitScreen = MutableList<TGKitCategory>
typealias TGKitPreferences = MutableList<TGKitPreference>