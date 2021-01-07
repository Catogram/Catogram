package ua.itaysonlab.catogram.double_bottom

import com.google.android.exoplayer2.util.Log
import org.telegram.messenger.SharedConfig
import org.telegram.messenger.UserConfig

/**
 * Option is AVAILABLE:
 * - in first 2 minutes from the logging of the any account (every login creates the process again)
 * - only if account size >= 2
 */
object DoubleBottomBridge {
    fun startTimer() {
        // In 2 minutes time you can now visit CG settings and see a "Double Bottom" option here (25 in debug)
        DoubleBottomStorageBridge.dbTimerExpireDate = System.currentTimeMillis() + DoubleBottomStorageBridge.DB_TIMER_END
    }

    fun isDbConfigAvailable(): Boolean {
        return (System.currentTimeMillis() <= DoubleBottomStorageBridge.dbTimerExpireDate) && UserConfig.getActivatedAccountsCount() > 1 && SharedConfig.passcodeHash.isNotEmpty()
    }

    fun isDbSetupCompleted(): Boolean {
        return DoubleBottomStorageBridge.storageInstance.map.isNotEmpty()
    }

    // return -1 if no account is found
    @JvmStatic fun checkPasscodeForAnyOfAccounts(code: String): Int {
        if (!isDbSetupCompleted()) return -1

        val masterHash = DoubleBottomPasscodeActivity.getHash(code)
        Log.d("DoubleBottom", "masterHash: $masterHash")
        return DoubleBottomStorageBridge.storageInstance.map.filter {
            Log.d("DoubleBottom", "hash for ${it.key}: ${it.value.hash}")
            it.value.hash == masterHash
        }.get(0)?.id ?: -1
    }

    @JvmStatic fun findLocalAccIdByTgId(id: Int): Int {
        for (i in 0 until UserConfig.MAX_ACCOUNT_COUNT) {
            val uc = UserConfig.getInstance(i)
            if (uc.isClientActivated && uc.currentUser.id == id) return i
        }

        return 0
    }
}