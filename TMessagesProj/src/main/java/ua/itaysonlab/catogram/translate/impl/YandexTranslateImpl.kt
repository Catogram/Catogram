package ua.itaysonlab.catogram.translate.impl

class YandexTranslateImpl: BaseTranslationImpl() {
    override fun supportsDetection() = true

    companion object {
        // https://translate.yandex.net/api/v1/tr.json/translate?srv=android&text=hello&lang=zh
        // res: {"code":200,"lang":"en-ru","text":["привет"]}
        private const val API_TRANSLATE_BASEURL = "https://translate.yandex.net/api/v1/tr.json/translate?srv=android&"
        // https://translate.yandex.net/api/v1/tr.json/detect?srv=android
        // res: {"code":200,"lang":"ru"}
        private const val API_DETECT_BASEURL = "https://translate.yandex.net/api/v1/tr.json/detect?srv=android&"
    }

    override fun translateText(txt: String, lang: String, callback: (String) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun detectLang(txt: String, callback: (String) -> Unit) {
        TODO("Not yet implemented")
    }
}