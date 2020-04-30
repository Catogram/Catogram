package ua.itaysonlab.extras

import android.app.Activity
import android.util.Log
import ua.itaysonlab.redesign.BaseActionedSwipeFragment
import ua.itaysonlab.redesign.slides.TgxMessageMenuFragment

object TgxExtras {
    @JvmStatic
    fun createSlideMenu(options: ArrayList<Int>, items: ArrayList<CharSequence>, icons: ArrayList<Int>, parentActivity: Activity, processSelectedOption: (Int) -> Unit): TgxMessageMenuFragment {
        val list = mutableListOf<BaseActionedSwipeFragment.Action>()

        options.forEachIndexed { index, data ->
            list.add(BaseActionedSwipeFragment.Action(
                    id = "$data",
                    title = "${items[index]}",
                    icon = icons[index]
            ))
        }

        return TgxMessageMenuFragment(list, processSelectedOption)
    }
}