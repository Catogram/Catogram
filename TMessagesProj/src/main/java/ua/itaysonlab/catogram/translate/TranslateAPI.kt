package ua.itaysonlab.catogram.translate

import androidx.appcompat.app.AppCompatActivity
import org.telegram.messenger.MessageObject

object TranslateAPI {

    @JvmStatic
    fun callTranslationDialog(msg: MessageObject, act: AppCompatActivity) {
        TranslationSheetFragment(msg).show(act.supportFragmentManager, null)
    }
}