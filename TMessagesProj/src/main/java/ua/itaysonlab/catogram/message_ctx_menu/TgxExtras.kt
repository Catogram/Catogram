package ua.itaysonlab.catogram.message_ctx_menu

import android.app.Activity
import android.net.Uri
import androidx.core.view.inputmethod.InputContentInfoCompat
import org.telegram.messenger.*
import ua.itaysonlab.catogram.CatogramConfig
import ua.itaysonlab.redesign.BaseActionedSwipeFragment
import ua.itaysonlab.redesign.sheet.TgxMessageMenuSheetFragment
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

    @JvmStatic
    fun createSheetMenu(options: ArrayList<Int>, items: ArrayList<CharSequence>, icons: ArrayList<Int>, parentActivity: Activity, processSelectedOption: (Int) -> Unit): TgxMessageMenuSheetFragment {
        val list = mutableListOf<BaseActionedSwipeFragment.Action>()

        options.forEachIndexed { index, data ->
            list.add(BaseActionedSwipeFragment.Action(
                    id = "$data",
                    title = "${items[index]}",
                    icon = icons[index]
            ))
        }

        return TgxMessageMenuSheetFragment(list, processSelectedOption)
    }

    @JvmStatic
    fun convertAndSend(accountInstance: AccountInstance, path: Uri, dialog_id: Long, replyingMessageObject: MessageObject?, inputContentInfo: InputContentInfoCompat, notify: Boolean) {
        
    }

    @JvmStatic
    fun createForwardTimeName(obj: MessageObject, orig: CharSequence): String {
        if (!CatogramConfig.msgForwardDate) return orig.toString()
        //return orig.toString()
        return "$orig (${LocaleController.formatDate(obj.messageOwner.fwd_from.date.toLong())})"
    }
}