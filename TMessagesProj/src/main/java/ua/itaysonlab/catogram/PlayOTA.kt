package ua.itaysonlab.catogram

import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import org.telegram.messenger.R
import org.telegram.ui.LaunchActivity
import java.lang.ref.WeakReference

object PlayOTA {
    private const val REQCODE = 1337
    private lateinit var umf: AppUpdateManager
    private var actRef: WeakReference<LaunchActivity>? = null

    private val listener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            onUpdateDownloaded()
        }
    }

    private fun onUpdateDownloaded() {
        umf.unregisterListener(listener)
        showUi()
    }

    @JvmStatic
    fun onResume() {
        if (!::umf.isInitialized) return
        umf.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                showUi()
            }
        }
    }

    private fun showUi() {
        if (actRef == null || actRef!!.get() == null) {
            umf.completeUpdate()
        }

        Snackbar.make(
                actRef!!.get()!!.findViewById(android.R.id.content),
                R.string.CG_GooglePlay_OTADownloaded,
                Snackbar.LENGTH_INDEFINITE
        ).apply {
            setAction(R.string.CG_GooglePlay_OTADownloaded_Restart) {
                umf.completeUpdate()
            }
            //setActionTextColor(resources.getColor(R.color.snackbar_action_text_color))
            show()
        }
    }

    @JvmStatic
    fun init(act: LaunchActivity) {
        if (!::umf.isInitialized) umf = AppUpdateManagerFactory.create(act)
        actRef = WeakReference(act)

        umf.registerListener(listener)
        umf.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                // Request the update.
                umf.startUpdateFlowForResult(
                        // Pass the intent that is returned by 'getAppUpdateInfo()'.
                        appUpdateInfo,
                        // Or 'AppUpdateType.FLEXIBLE' for flexible updates.
                        AppUpdateType.FLEXIBLE,
                        // The current activity making the update request.
                        act,
                        // Include a request code to later monitor this update request.
                        REQCODE
                )
            }
        }
    }
}