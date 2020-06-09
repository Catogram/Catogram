package ua.itaysonlab.catogram.translate

import androidx.appcompat.app.AppCompatActivity
import org.telegram.messenger.MessageObject
import ua.itaysonlab.catogram.translate.impl.BaseTranslationImpl
import ua.itaysonlab.catogram.translate.impl.GoogleTranslateImpl
import ua.itaysonlab.catogram.translate.impl.YandexTranslateImpl
import ua.itaysonlab.redesign.sheet.TranslationSheetFragment

object TranslateAPI {
    enum class TranslationImpl(val uiNane: String, val clazz: BaseTranslationImpl) {
        GOOGLE("Google Translate", GoogleTranslateImpl()),
        YANDEX("Yandex Translate", YandexTranslateImpl())
    }

    @JvmStatic
    fun callTranslationDialog(msg: MessageObject, act: AppCompatActivity) {
        TranslationSheetFragment(msg, TranslationImpl.YANDEX).show(act.supportFragmentManager, null)
    }

    fun translate(text: String, lang: String, callback: (String) -> Unit) {

    }
}