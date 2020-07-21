package ua.itaysonlab.catogram.translate.impl

import org.json.JSONObject
import org.telegram.messenger.LocaleController
import ua.itaysonlab.extras.doAsync
import ua.itaysonlab.extras.uiThread
import java.net.URL

class YandexTranslateImpl: BaseTranslationImpl() {
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
            val rst = JSONObject(URL("$API_TRANSLATE_BASEURL&text=$txt&lang=$lang-$toLang").readText()).optJSONArray("text")!!.getString(0)
            uiThread {
                callback.invoke(rst)
            }
        }
    }

    override fun detectLang(txt: String, callback: (String) -> Unit) {
        doAsync {
            val rst = JSONObject(URL("$API_DETECT_BASEURL&text=$txt").readText()).optString("lang")
            uiThread {
                callback.invoke(rst)
            }
        }
    }
}