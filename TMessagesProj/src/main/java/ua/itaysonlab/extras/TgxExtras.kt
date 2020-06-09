package ua.itaysonlab.extras

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.core.view.inputmethod.InputContentInfoCompat
import com.google.android.exoplayer2.util.Log
import org.telegram.messenger.*
import ua.itaysonlab.redesign.BaseActionedSwipeFragment
import ua.itaysonlab.redesign.sheet.TgxMessageMenuSheetFragment
import ua.itaysonlab.redesign.slides.TgxMessageMenuFragment
import java.io.File
import java.io.FileOutputStream

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
        try {
            val file = File(ApplicationLoader.applicationContext.cacheDir, "CG_TempConvert_${path.hashCode()}.webp")
            file.createNewFile()

            val os = file.outputStream()

            val bitmap = BitmapFactory.decodeStream(ApplicationLoader.applicationContext.contentResolver.openInputStream(path))
            bitmap.compress(Bitmap.CompressFormat.WEBP, 100, os)
            os.close()
            bitmap.recycle()

            //file.deleteOnExit()

            SendMessagesHelper.prepareSendingDocument(accountInstance, file.absolutePath, file.absolutePath, null, null, "image/webp", dialog_id, replyingMessageObject, null, null, notify, 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun createForwardTimeName(obj: MessageObject, orig: CharSequence): String {
        //return orig.toString()
        return "$orig [${LocaleController.formatDate(obj.messageOwner.fwd_from.date.toLong())}]"
    }
}