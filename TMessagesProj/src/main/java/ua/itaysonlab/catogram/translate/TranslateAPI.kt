package ua.itaysonlab.catogram.translate

import ua.itaysonlab.catogram.translate.impl.BaseTranslationImpl
import ua.itaysonlab.catogram.translate.impl.GoogleTranslateImpl
import ua.itaysonlab.catogram.translate.impl.YandexTranslateImpl

object TranslateAPI {
    enum class TranslationImpl(name: String, clazz: BaseTranslationImpl) {
        GOOGLE("Google Translate", GoogleTranslateImpl()),
        YANDEX("Yandex Translate", YandexTranslateImpl())
    }

    fun translate(text: String, lang: String, callback: (String) -> Unit) {

    }
}