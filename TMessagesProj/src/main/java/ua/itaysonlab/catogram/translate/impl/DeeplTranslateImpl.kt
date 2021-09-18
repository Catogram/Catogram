package ua.itaysonlab.catogram.translate.impl

import kotlinx.coroutines.*
import org.deepl.DeepLTranslater
import org.telegram.messenger.LocaleController
import org.telegram.messenger.R
import org.telegram.ui.Components.EditTextCaption
import org.telegram.ui.Components.EditTextEmoji
import ua.itaysonlab.catogram.CatogramConfig
import java.util.*

object DeeplTranslateImpl : CoroutineScope by MainScope() {

    @JvmStatic
    fun translateText(txt: String?, e: Boolean, callback: (String) -> Unit) {
        launch {
            try {
                val tl = when {
                    e -> CatogramConfig.trLang
                    else -> when (LocaleController.getString(
                        "LanguageCode",
                        R.string.LanguageCode
                    )) {
                        "zh_hans", "zh_hant" -> "zh"
                        "pt_BR" -> "pt"
                        else -> LocaleController.getString("LanguageCode", R.string.LanguageCode)
                    }
                }

                withContext(Dispatchers.IO) {
                    val a = DeepLTranslater().translate(
                        txt, "auto",
                        tl?.toUpperCase(Locale.getDefault()), null
                    )
                    withContext(Dispatchers.Main) {
                        callback.invoke(a)
                    }
                }

            } catch (e: Exception) {
                callback.invoke(LocaleController.getString("CG_NoInternet", R.string.CG_NoInternet))
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