package ua.itaysonlab.catogram.translate.impl

class DeeplTranslateImpl: BaseTranslationImpl() {
    override fun supportsDetection() = false

    override fun translateText(txt: String, lang: String, toLang: String, callback: (String) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun detectLang(txt: String, callback: (String) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun supportedLanguages(): List<String> {
        TODO("Not yet implemented")
    }
}