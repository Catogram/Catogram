package ua.itaysonlab.catogram.translate

import androidx.appcompat.app.AppCompatActivity
import org.telegram.messenger.MessageObject
import ua.itaysonlab.catogram.translate.impl.BaseTranslationImpl
import ua.itaysonlab.catogram.translate.impl.DeeplTranslateImpl
import ua.itaysonlab.catogram.translate.impl.GoogleTranslateImpl
import ua.itaysonlab.catogram.translate.impl.YandexTranslateImpl

object TranslateAPI {
    enum class TranslationImpl(val uiName: String, val clazz: BaseTranslationImpl) {
        GOOGLE("Google Translate", GoogleTranslateImpl()),
        YANDEX("Yandex Translate", YandexTranslateImpl()),
        DEEPL("DeepL", DeeplTranslateImpl())
    }

    @JvmStatic
    fun callTranslationDialog(msg: MessageObject, act: AppCompatActivity) {
        TranslationSheetFragment(msg, TranslationImpl.YANDEX).show(act.supportFragmentManager, null)
    }

    fun translate(text: String, lang: String, callback: (String) -> Unit) {

    }
}