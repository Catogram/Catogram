package ua.itaysonlab.catogram.translate.impl

import android.os.StrictMode
import okhttp3.Callback
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.telegram.messenger.LocaleController
import org.telegram.messenger.R
import org.telegram.ui.Components.EditTextCaption
import org.telegram.ui.Components.EditTextEmoji
import ua.itaysonlab.catogram.CatogramConfig
import ua.itaysonlab.extras.doAsync
import java.net.URLEncoder
import ua.itaysonlab.extras.uiThread

class GoogleTranslateImpl  {
    companion object {
        private const val api_translate_url = "https://translate.googleapis.com/translate_a/single?client=gtx&dt=t&dj=1&sl=auto"
        @JvmStatic
        fun translateText(txt: String?, e: Boolean, callback: (String) -> Unit) {
            doAsync {
                val tl = if (e) CatogramConfig.trLang else LocaleController.getString("LanguageCode", R.string.LanguageCode)

                val request: Request = Request.Builder()
                        .url("$api_translate_url&tl=$tl&q=${URLEncoder.encode(txt, "UTF-8")}")
                        .build()

                val response = OkHttpClient().newCall(request).execute()
                var sb = StringBuilder()
                var arr = JSONObject(response.body()!!.string()).getJSONArray("sentences")
                for (i in 0 until arr.length()) {
                    sb.append(arr.getJSONObject(i).getString("trans"))
                }

                uiThread {
                    callback.invoke(sb.toString())
                }
            }
        }
        @JvmStatic
        fun translateEditText(txt: String, editText: EditTextCaption) {
            return translateText(txt, true) { text ->
                    editText.setText(text)
                }
        }
        @JvmStatic
        fun translateComment(txt: String, editText: EditTextEmoji) {
            return translateText(txt, true) { text ->
                editText.setText(text)
            }
        }
    }
}