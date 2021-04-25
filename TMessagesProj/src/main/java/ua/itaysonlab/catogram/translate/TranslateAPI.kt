package ua.itaysonlab.catogram.translate

import androidx.appcompat.app.AppCompatActivity
import org.telegram.messenger.MessageObject
import org.telegram.tgnet.TLRPC

object TranslateAPI {
    @JvmStatic
    fun callTranslationDialog(msg: MessageObject, act: AppCompatActivity) {
        TranslationSheetFragment(msg).show(act.supportFragmentManager, null)
    }

    fun extractMessageText(msg: MessageObject): String {
        return when {
            msg.isPhoto -> msg.messageOwner.media.photo.caption
            msg.isPoll -> {
                val poll = (msg.messageOwner.media as TLRPC.TL_messageMediaPoll).poll
                "$poll\n\n${poll.answers.joinToString("\n") { "- ${it.text}" }}"
            }
            else -> if (msg.caption != null) msg.caption.toString() else msg.messageText.toString()
        }
    }
}