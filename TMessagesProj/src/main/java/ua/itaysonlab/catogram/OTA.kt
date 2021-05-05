package ua.itaysonlab.catogram

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.FileProvider
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.buffer
import okio.sink
import org.json.JSONObject
import org.telegram.messenger.BuildConfig
import org.telegram.messenger.LocaleController
import org.telegram.messenger.R
import ua.itaysonlab.extras.CatogramExtras
import java.io.File
import java.net.HttpURLConnection


object OTA : CoroutineScope by MainScope() {

    var needDownload = false

    lateinit var changelog: String
    lateinit var parseddString: String

    lateinit var version: String

    lateinit var handler: CoroutineExceptionHandler

    lateinit var broadcastReceiver: BroadcastReceiver

    private fun checkBS(callback: (Boolean) -> Unit) {
        launch(handler) {
            try {
                val request: Request =
                        Request.Builder().url("https://ctwoon.eu/catogram.json").build()
                withContext(Dispatchers.IO) {
                    val response = OkHttpClient().newCall(request).execute()
                    parseddString = response.body!!.string()
                    val parsedString = JSONObject(parseddString)
                    if (parsedString.getString("version") != CatogramExtras.CG_VERSION) {
                        version = parsedString.getString("version")
                        changelog = parsedString.getString("changelog")
                        needDownload = true
                    } else needDownload = false
                }
                callback.invoke(needDownload)
            } catch (e: java.lang.Exception) {
                throw e
            }
        }
    }

    @JvmStatic
    fun download(context: Context, b: Boolean) {
        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {
                when (intent.extras!!.getString("action_name")) {
                    "action_download" -> {
                        install(context!!)
                    }

                    "action_changelog" -> {
                        download(context!!, true)
                    }
                }
            }
        }
        try {
            context.unregisterReceiver(broadcastReceiver)
        } catch (e: Exception) {
        }
        handler = CoroutineExceptionHandler { _, exception ->
            val builder = org.telegram.ui.ActionBar.AlertDialog.Builder(context)
            builder.setTitle(LocaleController.getString("ErrorOccurred", R.string.ErrorOccurred))
                    .setMessage(exception.message)
                    .setPositiveButton("OK") { dialog, _ ->
                        dialog.cancel()
                    }
            builder.show()
        }
        checkBS { needDownload ->
            if (needDownload && b) {
                val builder = org.telegram.ui.ActionBar.AlertDialog.Builder(context)
                builder.setTitle(LocaleController.getString("CG_Found", R.string.CG_Found) + " • " + version)
                        .setMessage(changelog)
                        .setPositiveButton(LocaleController.getString("CG_Download", R.string.CG_Download)) { _, _ ->
                            install(context)
                        }
                builder.show()
            } else if (needDownload && !b) {
                context.registerReceiver(broadcastReceiver, IntentFilter("OTA_NOTIF"))
                if (Build.VERSION.SDK_INT >= 26) {
                    val channel = NotificationChannel("channel01", "name",
                            NotificationManager.IMPORTANCE_HIGH) // for heads up notifications

                    channel.description = "description"

                    val notificationManager: NotificationManager? = context.getSystemService(NotificationManager::class.java)

                    notificationManager!!.createNotificationChannel(channel)
                }

                val intentDownload = Intent(context, NotificationActionService::class.java)
                        .setAction("action_download")
                val pendingIntentDownload = PendingIntent.getBroadcast(
                        context, 0,
                        intentDownload, PendingIntent.FLAG_UPDATE_CURRENT
                )

                val intentChangelog = Intent(context, NotificationActionService::class.java)
                        .setAction("action_changelog")

                val pendingIntentChangelog = PendingIntent.getBroadcast(
                        context, 0,
                        intentChangelog, PendingIntent.FLAG_UPDATE_CURRENT
                )

                val notification: Notification = NotificationCompat.Builder(context, "channel01")
                        .setSmallIcon(R.drawable.cg_notification)
                        .setContentTitle(LocaleController.getString("CG_Found", R.string.CG_Found))
                        .setContentText(version)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .addAction(R.drawable.download_outline_28, LocaleController.getString("CG_Download", R.string.CG_Download), pendingIntentDownload)
                        .addAction(R.drawable.download_outline_28, LocaleController.getString("CG_Changelog", R.string.CG_Changelog), pendingIntentChangelog)
                        .build()

                val notificationManager = NotificationManagerCompat.from(context)
                notificationManager.notify(1337, notification)
            } else if (b) Toast.makeText(context, LocaleController.getString("CG_Not_Found", R.string.CG_Not_Found), Toast.LENGTH_SHORT).show()
        }
    }

    private fun requestPermissionsCompat(
            permissionsArray: Array<String>,
            requestCode: Int,
            context: Context,
    ) {
        ActivityCompat.requestPermissions(context as Activity, permissionsArray, requestCode)
    }

    private fun checkSelfPermissionCompat(permission: String, context: Context) =
            ActivityCompat.checkSelfPermission(context, permission)

    fun install(context: Context) {
        try {
            context.unregisterReceiver(broadcastReceiver)
        } catch (e: Exception) {
        }
        if (checkSelfPermissionCompat(WRITE_EXTERNAL_STORAGE, context) !=
                PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionsCompat(
                    arrayOf(WRITE_EXTERNAL_STORAGE),
                    0,
                    context
            )
        }
        if (checkSelfPermissionCompat(WRITE_EXTERNAL_STORAGE, context) ==
                PackageManager.PERMISSION_GRANTED
        ) {
            launch(handler) {
                try {
                    val progressDialog = org.telegram.ui.ActionBar.AlertDialog(context, 3)
                    progressDialog.show()

                    val request: Request = Request.Builder()
                            .url("https://github.com/catogram/catogram/releases/latest/download/app.apk")
                            .build()
                    withContext(Dispatchers.IO) {
                        val response = OkHttpClient().newCall(request).execute()
                        val body = response.body

                        if (response.code != HttpURLConnection.HTTP_OK) {
                            progressDialog.cancel()
                            throw RuntimeException("Response code: " + response.code.toString())
                        }

                        val file: File = File.createTempFile(
                                "ota",
                                ".apk",
                                context.externalCacheDir
                        )
                        val sink = file.sink().buffer()

                        body?.source().use { input ->
                            sink.use { output ->
                                if (input != null) {
                                    output.writeAll(input)
                                }
                            }
                        }

                        progressDialog.dismiss()

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !context.packageManager.canRequestPackageInstalls()) {
                            context.startActivity(
                                    Intent(
                                            ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                                            Uri.parse("package:ua.itaysonlab.messenger")
                                    )
                            )
                        }

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && context.packageManager.canRequestPackageInstalls() && file.exists()) {
                            installApp(file, context)
                        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O && file.exists()) {
                            installApp(file, context)
                        }
                    }
                } catch (e: Exception) {
                    throw e
                }
            }
        }
    }

    private fun installApp(file: File, context: Context) {
        val install = Intent(Intent.ACTION_VIEW)
        install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        install.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        install.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
        install.data = FileProvider.getUriForFile(
                context,
                BuildConfig.APPLICATION_ID + ".provider",
                file
        )
        context.startActivity(install)
    }
}