package ua.itaysonlab.redesign.sheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.telegram.messenger.MessageObject
import org.telegram.ui.ActionBar.Theme
import ua.itaysonlab.catogram.translate.TranslateAPI

class TranslationSheetFragment(val obj: MessageObject, val impl: TranslateAPI.TranslationImpl): BottomSheetDialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return LinearLayout(inflater.context).apply {
            setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite))
            orientation = LinearLayout.VERTICAL


        }
    }
}