package ua.itaysonlab.catogram.translate

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.v5_translation_markup.view.*
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.LocaleController
import org.telegram.messenger.MessageObject
import org.telegram.messenger.R
import org.telegram.ui.ActionBar.Theme
import org.telegram.messenger.ApplicationLoader

class TranslationSheetFragment(val obj: MessageObject, val impl: TranslateAPI.TranslationImpl): BottomSheetDialogFragment() {
    private lateinit var vview: View
    private val txt by lazy {
        return@lazy if (obj.caption != null) obj.caption.toString() else obj.messageText.toString()
    }

    override fun getTheme(): Int {
        return R.style.TransSheet
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return LinearLayout(inflater.context).apply {
            setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite))
            orientation = LinearLayout.VERTICAL

            vview = inflater.inflate(R.layout.v5_translation_markup, container, false)
            addView(vview)
        }
    }

    private fun getAutoIsoLang(): String {
        val iso = LocaleController.getString("LanguageCode", R.string.LanguageCode)
        return if (impl.clazz.supportedLanguages().contains(iso)) iso else if (LocaleController.getString("LanguageCode", R.string.LanguageCode).contains("pt_BR")) impl.clazz.supportedLanguages()[38] else impl.clazz.supportedLanguages()[0]
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val whiteBg = Theme.getColor(Theme.key_windowBackgroundWhite)
        val blackText = Theme.getColor(Theme.key_windowBackgroundWhiteBlackText)
        val blackColor = ColorStateList.valueOf(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText))
        val grayColor = Theme.getColor(Theme.key_windowBackgroundWhiteGrayText)
        val grayBg = ColorStateList.valueOf(Theme.getColor(Theme.key_windowBackgroundGray))

        dialog!!.window!!.navigationBarColor = whiteBg
        vview.backgroundTintList = ColorStateList.valueOf(whiteBg)

        vview.close.setOnClickListener {
            dismiss()
        }

        vview.copyText.setOnClickListener {
            AndroidUtilities.addToClipboard(vview.trsl.text.toString())
            Toast.makeText(ApplicationLoader.applicationContext, LocaleController.getString("TextCopied", R.string.TextCopied), Toast.LENGTH_SHORT).show()
            dismiss()
        }

        vview.close.imageTintList = blackColor
        vview.copyText.imageTintList = blackColor

        vview.copyText.visibility = View.GONE
        vview.mk_ct.visibility = View.INVISIBLE
        vview.mk_ld.visibility = View.VISIBLE

        vview.tvTitle.setTextColor(blackText)
        vview.tvDivider.backgroundTintList = ColorStateList.valueOf(grayColor)

        vview.orig_txt.setTextColor(grayColor)
        vview.trsl_txt.setTextColor(grayColor)
        vview.orig_txt_lang.setTextColor(grayColor)
        vview.trsl_txt_lang.setTextColor(grayColor)

        vview.orig_txt.text = LocaleController.getString("CG_Translate_Orig", R.string.CG_Translate_Orig)
        vview.trsl_txt.text = LocaleController.getString("CG_Translate_Translated", R.string.CG_Translate_Translated)
        vview.tvTitle.text = LocaleController.getString("CG_Translator", R.string.CG_Translator)

        vview.orig_card.backgroundTintList = grayBg
        vview.trsl_card.backgroundTintList = grayBg

        vview.orig.setTextColor(blackText)
        vview.trsl.setTextColor(blackText)

        impl.clazz.detectLang(txt) { detectedLang ->
            impl.clazz.translateText(txt, detectedLang, getAutoIsoLang()) { text ->
                vview.orig.text = txt
                vview.trsl.text = text
                
                vview.orig_txt_lang.text = " • $detectedLang"
                vview.trsl_txt_lang.text = " • ${getAutoIsoLang()}"

                vview.mk_ct.visibility = View.VISIBLE
                vview.mk_ld.visibility = View.INVISIBLE
                vview.copyText.visibility = View.VISIBLE
            }
        }
    }
}