package ua.itaysonlab.catogram.message_ctx_menu

import android.app.Activity
import android.view.View
import androidx.core.content.ContextCompat
import bruhcollective.itaysonlab.airui.miui.popup.DropdownPopup
import bruhcollective.itaysonlab.airui.miui.popup.adapter.SelectiveMenuAdapter

object AirExtras {
    @JvmStatic
    fun showAirMenu(options: ArrayList<Int>, items: ArrayList<CharSequence>, icons: ArrayList<Int>, parentActivity: Activity, anchor: View, processSelectedOption: (Int) -> Unit) {
        val list = mutableListOf<SelectiveMenuAdapter.Item>()

        options.forEachIndexed { index, data ->
            list.add(SelectiveMenuAdapter.Item(
                    id = "$data",
                    title = "${items[index]}",
                    icon = ContextCompat.getDrawable(parentActivity, icons[index])
            ))
        }

        DropdownPopup(parentActivity, anchor) { idx, el ->
            processSelectedOption(el.id.toInt())
        }.also {
            it.setAdapter(SelectiveMenuAdapter(
                    parentActivity,
                    list
            ))
        }.show(View.TEXT_DIRECTION_INHERIT, View.TEXT_ALIGNMENT_TEXT_START) // TODO look
    }
}