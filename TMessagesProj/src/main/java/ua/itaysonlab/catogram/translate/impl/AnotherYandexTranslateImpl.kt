package ua.itaysonlab.catogram.translate.impl

import android.widget.Toast
import com.google.android.play.core.internal.bf
import org.json.JSONObject
import org.telegram.ui.ActionBar.BaseFragment
import org.telegram.ui.Components.EditTextCaption
import ua.itaysonlab.catogram.CatogramConfig
import ua.itaysonlab.extras.doAsync
import ua.itaysonlab.extras.uiThread
import java.net.URL
import java.net.URLEncoder

abstract class AnotherYandexTranslateImpl: BaseTranslationImpl() {


    companion object {

        private val listOfSupportedLangs = listOf("en", "de", "fr", "it", "ru", "tr", "es", "zh", "ja", "ko", "am", "ar", "az", "bg", "cs", "da", "el", "et", "fa", "fi", "he", "hi", "hr", "hu", "hy", "id", "is", "ka", "km", "lo", "lt", "lv", "ms", "my", "ne", "nl", "no", "pl", "pt", "ro", "sk", "sl", "sr", "sv", "th", "tl", "uk", "vi")
        fun supportsDetection() = true

        // https://translate.yandex.net/api/v1/tr.json/translate?srv=android&text=hello&lang=zh
        // res: {"code":200,"lang":"en-ru","text":["привет"]}
        private const val API_TRANSLATE_BASEURL = "https://translate.yandex.net/api/v1/tr.json/translate?srv=android"

        // https://translate.yandex.net/api/v1/tr.json/detect?srv=android
        // res: {"code":200,"lang":"ru"}
        private const val API_DETECT_BASEURL = "https://translate.yandex.net/api/v1/tr.json/detect?srv=android"


        fun translateText(txt: String, lang: String, toLang: String, callback: (String) -> Unit) {
            doAsync {
                val rst = JSONObject(URL("$API_TRANSLATE_BASEURL&text=${URLEncoder.encode(txt, "UTF-8")}&lang=$lang-$toLang").readText()).optJSONArray("text")!!.getString(0)
                uiThread {
                    callback.invoke(rst)
                }
            }
        }

         fun detectLang(txt: String, callback: (String) -> Unit) {
            doAsync {
                val rst = JSONObject(URL("$API_DETECT_BASEURL&text=${URLEncoder.encode(txt, "UTF-8")}").readText()).optString("lang")
                uiThread {
                    callback.invoke(rst)
                }
            }
        }

         fun supportedLanguages(): List<String> {
            return listOfSupportedLangs
        }
            @JvmStatic
            fun translateEditText(txt: String, editText: EditTextCaption) {
                return detectLang(txt) { detectedLang ->
                    translateText(txt, detectedLang, CatogramConfig.trLang) { text ->
                        editText.setText(text)
                    }
                }
            }
        }
    }