package ua.itaysonlab.redesign.sheet

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.DrawableRes
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.v5_slide_item.view.*
import org.telegram.messenger.R
import org.telegram.ui.ActionBar.Theme
import ua.itaysonlab.redesign.BaseActionedSwipeFragment

abstract class BaseActionedSheetFragment: BottomSheetDialogFragment() {
    var needToTint = true

    abstract fun getActions(): List<BaseActionedSwipeFragment.Action>

    abstract fun processActionClick(id: String)

    // null - do not show header
    open fun getHeader(parent: ViewGroup, ctx: Context): View? {
        return null
    }

    private fun getActionsView(ctx: Context): View {
        return LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL

            getActions().forEach { action ->
                addView(LayoutInflater.from(ctx).inflate(R.layout.v5_slide_item, this, false).apply {
                    if (action.icon == -1) {
                        this.action_iv.visibility = View.GONE
                    } else {
                        this.action_iv.setImageResource(action.icon)
                        if (needToTint) this.action_iv.imageTintList = ColorStateList.valueOf(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText))
                    }

                    this.action_tv.text = action.title
                    this.action_tv.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText))
                    this.setOnClickListener {
                        processActionClick(action.id)
                        dismiss()
                    }
                })
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return LinearLayout(inflater.context).apply {
            setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite))

            orientation = LinearLayout.VERTICAL

            getHeader(this, inflater.context)?.let {
                it.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite))
                addView(it)
            }

            addView(getActionsView(inflater.context))
        }
    }

    data class Action(
            val id: String,
            @DrawableRes val icon: Int,
            val title: String
    )
}