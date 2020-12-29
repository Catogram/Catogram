package ua.itaysonlab.catogram.translate.impl

abstract class BaseTranslationImpl {
    abstract fun supportsDetection(): Boolean
    abstract fun translateText(txt: String, lang: String, toLang: String, callback: (String) -> Unit)
    abstract fun detectLang(txt: String, callback: (String) -> Unit)
    abstract fun supportedLanguages(): List<String>
}