package ua.itaysonlab.redesign.sheet

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
import ua.itaysonlab.catogram.translate.TranslateAPI

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog!!.window!!.navigationBarColor = Theme.getColor(Theme.key_windowBackgroundWhite)

        vview.mk_ct.visibility = View.INVISIBLE
        vview.mk_ld.visibility = View.VISIBLE

        vview.orig_txt.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText))
        vview.trsl_txt.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText))
        vview.orig.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText))
        vview.trsl.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText))

        vview.orig_txt.text = LocaleController.getString("CG_Translate_Orig", R.string.CG_Translate_Orig)
        vview.trsl_txt.text = LocaleController.getString("CG_Translate_Translated", R.string.CG_Translate_Translated)

        impl.clazz.detectLang(txt) {
            impl.clazz.translateText(txt, it, LocaleController.getLocaleStringIso639()) { text ->
                vview.orig.text = txt
                vview.trsl.text = text

                vview.mk_ct.visibility = View.VISIBLE
                vview.mk_ld.visibility = View.INVISIBLE
            }
        }
    }
}