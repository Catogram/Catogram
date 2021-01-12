package ua.itaysonlab.catogram.translate

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.v5_translation_markup.view.*
import org.telegram.messenger.LocaleController
import org.telegram.messenger.MessageObject
import org.telegram.messenger.R
import org.telegram.ui.ActionBar.Theme

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
        val iso = LocaleController.getInstance().currentLocaleInfo.baseLangCode
        return if (impl.clazz.supportedLanguages().contains(iso)) iso else impl.clazz.supportedLanguages()[0]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog!!.window!!.navigationBarColor = Theme.getColor(Theme.key_windowBackgroundWhite)
        vview.backgroundTintList = ColorStateList.valueOf(Theme.getColor(Theme.key_windowBackgroundWhite))


        vview.mk_ct.visibility = View.INVISIBLE
        vview.mk_ld.visibility = View.VISIBLE

        vview.tvTitle.setTextColor(Theme.getColor(Theme.key_actionBarDefaultTitle))
        vview.tvDivider.backgroundTintList = ColorStateList.valueOf(Theme.getColor(Theme.key_divider))

        vview.orig_txt.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText))
        vview.trsl_txt.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText))
        vview.orig_txt.text = LocaleController.getString("CG_Translate_Orig", R.string.CG_Translate_Orig)
        vview.trsl_txt.text = LocaleController.getString("CG_Translate_Translated", R.string.CG_Translate_Translated)

        vview.orig_card.backgroundTintList = ColorStateList.valueOf(Theme.getColor(Theme.key_windowBackgroundGray))
        vview.trsl_card.backgroundTintList = ColorStateList.valueOf(Theme.getColor(Theme.key_windowBackgroundGray))

        vview.orig.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText))
        vview.trsl.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText))

        impl.clazz.detectLang(txt) {
            impl.clazz.translateText(txt, it, getAutoIsoLang()) { text ->
                vview.orig.text = txt
                vview.trsl.text = text

                vview.mk_ct.visibility = View.VISIBLE
                vview.mk_ld.visibility = View.INVISIBLE
            }
        }
    }
}