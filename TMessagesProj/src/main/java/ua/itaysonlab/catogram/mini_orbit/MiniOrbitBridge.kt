package ua.itaysonlab.catogram.mini_orbit

import com.google.android.exoplayer2.util.Log
import kotlinx.coroutines.suspendCancellableCoroutine
import org.telegram.tgnet.ConnectionsManager
import org.telegram.tgnet.TLRPC

object MiniOrbitBridge {
    suspend fun requestChatList(manager: ConnectionsManager) {
        return suspendCancellableCoroutine {
            manager.sendRequest(TLRPC.TL_messages_getChats()) { res, err ->
                Log.d("MiniOrbitBridge", res.toString())

            }
        }
    }
}