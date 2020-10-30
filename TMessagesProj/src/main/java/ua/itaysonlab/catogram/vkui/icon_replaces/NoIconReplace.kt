package ua.itaysonlab.catogram.vkui.icon_replaces

import android.util.SparseIntArray
import androidx.core.os.bundleOf
import org.telegram.messenger.R
import ua.itaysonlab.catogram.vkui.newSparseInt

class NoIconReplace: BaseIconReplace() {
    override val replaces: SparseIntArray = newSparseInt()
}