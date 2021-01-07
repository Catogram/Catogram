package ua.itaysonlab.catogram.double_bottom

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
        return (System.currentTimeMillis() <= DoubleBottomStorageBridge.dbTimerExpireDate) && UserConfig.getActivatedAccountsCount() > 1
    }

    fun isDbSetupCompleted(): Boolean {
        return DoubleBottomStorageBridge.storageInstance.map.isNotEmpty()
    }
}