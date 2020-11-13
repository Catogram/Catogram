package ua.itaysonlab.catogram

import android.view.View
import org.telegram.messenger.R
import org.telegram.tgnet.TLRPC
import org.telegram.ui.Cells.ChatMessageCell
import org.telegram.ui.ChatActivity

// I've created this so CG features can be injected in a source file with 1 line only (maybe)
// Because manual editing of drklo's sources harms your mental health.
object CGFeatureHooks {
    @JvmStatic
    fun hookHideWhenBlocked(cell: ChatMessageCell, act: ChatActivity) {
        if (!CGControversive.isControversiveFeaturesEnabled()) return
        if (CatogramConfig.hideUserIfBlocked && cell.messageObject.isFromUser) {
            val isBlocked = act.messagesController.blockePeers.indexOfKey((cell.messageObject.messageOwner.from_id as TLRPC.TL_peerUser).user_id) >= 0
            cell.visibility = if (isBlocked) View.GONE else View.VISIBLE
        } else {
            cell.visibility = View.VISIBLE
        }
    }

    @JvmStatic
    fun getProperNotificationIcon(): Int {
        return if (CatogramConfig.oldNotificationIcon) R.drawable.notification else R.drawable.cg_notification
    }
}