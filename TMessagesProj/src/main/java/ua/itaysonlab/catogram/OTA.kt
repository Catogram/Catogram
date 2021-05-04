package ua.itaysonlab.catogram

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES
import android.widget.Toast
import androidx.core.app.ActivityCompat
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
import java.util.*


object OTA: CoroutineScope by MainScope() {

    var needDownload = false
    lateinit var changelog: String
    lateinit var parseddString: String
    private fun checkBS(context: Context, callback: (Boolean) -> Unit) {
        launch {
            try {
                val request: Request =
                        Request.Builder().url("https://ctwoon.eu/catogram.json").build()
                withContext(Dispatchers.IO) {
                    val response = OkHttpClient().newCall(request).execute()
                    parseddString = response.body!!.string()
                    val parsedString = JSONObject(parseddString)
                    if (parsedString.getString("version") != CatogramExtras.CG_VERSION) {
                        changelog = parsedString.getString("changelog")
                        needDownload = true
                    } else needDownload = false
                }
            }
            catch (e: Exception) {
                val builder = AlertDialog.Builder(context)
                builder.setTitle("Something went wrong")
                        .setMessage(e.toString())
                        .setPositiveButton("OK") { dialog, _ ->
                            dialog.cancel()
                        }
                builder.show()
                return@launch
            }
            callback.invoke(needDownload)
        }
    }

    @JvmStatic
    fun download(context: Context, b: Boolean) {
        checkBS(context) { needDownload ->
            if (needDownload) {
                val builder = org.telegram.ui.ActionBar.AlertDialog.Builder(context)
                builder.setTitle(LocaleController.getString("CG_Found", R.string.CG_Found))
                        .setMessage(changelog)
                        .setPositiveButton(LocaleController.getString("CG_Download", R.string.CG_Download)) { _, _ ->
                            install(context)
                        }
                builder.show()
            }
            else if (b) Toast.makeText(context, LocaleController.getString("CG_Not_Found", R.string.CG_Not_Found), Toast.LENGTH_SHORT).show()
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
            launch {
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
                            val builder = org.telegram.ui.ActionBar.AlertDialog.Builder(context)
                            builder.setTitle(LocaleController.getString("ErrorOccurred", R.string.ErrorOccurred))
                                    .setMessage("Response code: " + response.code.toString())
                                    .setPositiveButton("OK") { dialog, _ ->
                                        dialog.cancel()
                                    }
                            builder.show()
                            return@withContext
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
                        }
                        else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O && file.exists()) {
                            installApp(file, context)
                        } else {
                            val builder = org.telegram.ui.ActionBar.AlertDialog.Builder(context)
                            builder.setTitle(LocaleController.getString("ErrorOccurred", R.string.ErrorOccurred))
                                    .setPositiveButton("OK") { dialog, _ ->
                                        dialog.cancel()
                                    }
                            builder.show()
                        }
                    }
                }
                catch (e: Exception) {
                    val builder = org.telegram.ui.ActionBar.AlertDialog.Builder(context)
                    builder.setTitle(LocaleController.getString("ErrorOccurred", R.string.ErrorOccurred))
                            .setMessage(e.toString())
                            .setPositiveButton("OK") { dialog, _ ->
                                dialog.cancel()
                            }
                    builder.show()
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