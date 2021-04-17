package ua.itaysonlab.catogram.translate.impl

import android.os.StrictMode
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.telegram.messenger.LocaleController
import org.telegram.messenger.R
import ua.itaysonlab.catogram.CatogramConfig
import java.net.URLEncoder


class GoogleTranslateImpl  {
    companion object {
        @JvmStatic
        fun translateText(txt: String?, e: Boolean):String {
            val policy = StrictMode.ThreadPolicy.Builder()
                    .permitAll().build()
            StrictMode.setThreadPolicy(policy)

            val url = HttpUrl.Builder()
                    .scheme("https")
                    .host("translate.googleapis.com")
                    .addPathSegment("translate_a")
                    .addPathSegment("single")
                    .addQueryParameter("client", "gtx")
                    .addQueryParameter("sl", "auto")
                    .addQueryParameter("tl", if (e)
                                                        CatogramConfig.trLang
                                                    else
                                                    LocaleController.getString("LanguageCode", R.string.LanguageCode))
                    .addQueryParameter("dt", "t")
                    .addQueryParameter("dj", "1")
                    .addQueryParameter("q", URLEncoder.encode(txt, "UTF-8"))
                    .build()

            val request: Request = Request.Builder()
                    .addHeader("accept", "application/json")
                    .url(url)
                    .build()

            val response = OkHttpClient().newCall(request).execute()
            return JSONObject(response.body()!!.string()).getJSONArray("sentences").getJSONObject(0).getString("trans")
        }
        fun aTranslateText(txt: String, e: Boolean): String {

        }
    }
}