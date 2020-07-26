package ua.itaysonlab.catogram.translate.impl

import org.json.JSONObject
import org.telegram.messenger.LocaleController
import ua.itaysonlab.extras.doAsync
import ua.itaysonlab.extras.uiThread
import java.net.URL
import java.net.URLEncoder

class YandexTranslateImpl: BaseTranslationImpl() {
    private val listOfSupportedLangs = listOf("en", "de", "fr", "it", "ru", "tr", "es", "zh", "ja", "ko", "am", "ar", "az", "bg", "cs", "da", "el", "et", "fa", "fi", "he", "hi", "hr", "hu", "hy", "id", "is", "ka", "km", "lo", "lt", "lv", "ms", "my", "ne", "nl", "no", "pl", "pt", "ro", "sk", "sl", "sr", "sv", "th", "tl", "uk", "vi")

    override fun supportsDetection() = true

    companion object {
        // https://translate.yandex.net/api/v1/tr.json/translate?srv=android&text=hello&lang=zh
        // res: {"code":200,"lang":"en-ru","text":["привет"]}
        private const val API_TRANSLATE_BASEURL = "https://translate.yandex.net/api/v1/tr.json/translate?srv=android"
        // https://translate.yandex.net/api/v1/tr.json/detect?srv=android
        // res: {"code":200,"lang":"ru"}
        private const val API_DETECT_BASEURL = "https://translate.yandex.net/api/v1/tr.json/detect?srv=android"
    }

    override fun translateText(txt: String, lang: String, toLang: String, callback: (String) -> Unit) {
        doAsync {
            val rst = JSONObject(URL("$API_TRANSLATE_BASEURL&text=${URLEncoder.encode(txt, "UTF-8")}&lang=$lang-$toLang").readText()).optJSONArray("text")!!.getString(0)
            uiThread {
                callback.invoke(rst)
            }
        }
    }

    override fun detectLang(txt: String, callback: (String) -> Unit) {
        doAsync {
            val rst = JSONObject(URL("$API_DETECT_BASEURL&text=${URLEncoder.encode(txt, "UTF-8")}").readText()).optString("lang")
            uiThread {
                callback.invoke(rst)
            }
        }
    }
}