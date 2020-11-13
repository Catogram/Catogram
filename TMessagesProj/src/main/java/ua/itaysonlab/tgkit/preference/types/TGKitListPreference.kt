package ua.itaysonlab.tgkit.preference.types

import android.app.Activity
import android.content.DialogInterface
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.util.Pair
import bruhcollective.itaysonlab.airui.miui.popup.DropdownPopup
import bruhcollective.itaysonlab.airui.miui.popup.adapter.SelectiveMenuAdapter
import bruhcollective.itaysonlab.airui.miui.popup.adapter.content.DoubleLineContentAdapter
import org.telegram.ui.ActionBar.BaseFragment
import org.telegram.ui.Components.AlertsCreator
import ua.itaysonlab.catogram.CatogramConfig
import ua.itaysonlab.tgkit.preference.TGKitPreference

class TGKitListPreference : TGKitPreference() {
    var divider = false
    var contract: TGTLContract? = null

    override fun getType(): TGPType {
        return TGPType.LIST
    }

    fun callActionHueta(bf: BaseFragment, pr: Activity, view: View, x: Float, y: Float, ti: TempInterface) {
        if (CatogramConfig.useAirUiPopup) {
            val actions = ArrayList<SelectiveMenuAdapter.Item>()
            if (contract!!.hasIcons()) {
                for ((first, second, third) in contract!!.getOptionsIcons()) {
                    actions.add(SelectiveMenuAdapter.Item(first.toString(), second, null, ContextCompat.getDrawable(pr, third!!), contract!!.getValue() == second, false))
                }
            } else {
                for (act in contract!!.getOptions()) {
                    actions.add(SelectiveMenuAdapter.Item(act.first.toString(), act.second, null, null, contract!!.getValue() == act.second, false))
                }
            }

            val drp = DropdownPopup(pr, view) { position: Int, id ->
                contract!!.setValue(id.id.toInt())
                ti.update()
            }

            drp.setAdapter(SelectiveMenuAdapter(pr, actions, DoubleLineContentAdapter(pr, actions)))
            drp.show(View.TEXT_DIRECTION_INHERIT, View.TEXT_ALIGNMENT_TEXT_START, x, y)
        } else {
            var selected: Int = 0
            val titleArray = mutableListOf<String>()
            val idArray = mutableListOf<Int>()

            if (contract!!.hasIcons()) {
                contract!!.getOptionsIcons().forEachIndexed { index, triple ->
                    titleArray.add(triple.second)
                    idArray.add(triple.first)

                    if (contract!!.getValue() == triple.second) selected = index
                }
            } else {
                contract!!.getOptions().forEachIndexed { index, pair ->
                    titleArray.add(pair.second)
                    idArray.add(pair.first)

                    if (contract!!.getValue() == pair.second) selected = index
                }
            }

            val d = AlertsCreator.createSingleChoiceDialog(pr, titleArray.toTypedArray(), title, selected) { di, sel ->
                contract!!.setValue(idArray[sel])
                ti.update()
            }

            bf.visibleDialog = d

            d.show()
        }
    }

    interface TGTLContract {
        fun setValue(id: Int)
        fun getValue(): String
        fun getOptions(): List<Pair<Int, String>>
        fun getOptionsIcons(): List<Triple<Int, String, Int?>>
        fun hasIcons(): Boolean
    }

    interface TempInterface {
        fun update()
    }
}