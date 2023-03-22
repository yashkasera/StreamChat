package com.yashkasera.streamchat.util

import android.Manifest.permission.*
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.karumi.dexter.Dexter
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.yashkasera.streamchat.R

object PermissionUtils {

    fun requestAudioAndStoragePermissions(
        activity: Activity?,
        multiplePermissionsListener: MultiplePermissionsListener
    ) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            Dexter.withContext(activity)
                .withPermissions(
                    RECORD_AUDIO,
                    READ_EXTERNAL_STORAGE,
                    WRITE_EXTERNAL_STORAGE
                )
                .withListener(multiplePermissionsListener).check()
        } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            Dexter.withContext(activity)
                .withPermissions(
                    RECORD_AUDIO,
                    READ_EXTERNAL_STORAGE
                )
                .withListener(multiplePermissionsListener).check()
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Dexter.withContext(activity)
                .withPermissions(
                    RECORD_AUDIO,
                    READ_MEDIA_VIDEO,
                    READ_MEDIA_AUDIO,
                )
                .withListener(multiplePermissionsListener).check()
        }
    }

    @SuppressLint("InlinedApi")
    fun isAudioAndStoragePermissionEnabled(context: Context): Boolean {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            return ContextCompat.checkSelfPermission(context, RECORD_AUDIO) +
                    ContextCompat.checkSelfPermission(context, READ_EXTERNAL_STORAGE) +
                    ContextCompat.checkSelfPermission(
                        context,
                        WRITE_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
        } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(context, RECORD_AUDIO) +
                    ContextCompat.checkSelfPermission(
                        context,
                        READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
        } else {
            return arrayOf(RECORD_AUDIO, READ_MEDIA_VIDEO, READ_MEDIA_AUDIO).all {
                ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
            }
        }
    }

    fun permissionPermanentlyDeniedDialog(
        activity: Activity,
        message: Int = R.string.permission_permanently_denied,
        onPermissionDenied: (() -> Unit)? = null
    ) {
        MaterialAlertDialogBuilder(activity)
            .setMessage(message)
            .setPositiveButton(R.string.settings) { _, _ ->
                openSettings(activity)
            }
            .setNegativeButton(R.string.not_now) { _, _ ->
                onPermissionDenied?.invoke()
            }
            .show()
    }

    private fun openSettings(activity: Activity) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", activity.packageName, null)
        intent.data = uri
        activity.startActivity(intent)
    }
}