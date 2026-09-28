package com.example

import android.Manifest
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.util.Log

object DevSettingsManager {
    private const val TAG = "DevSettingsManager"

    // Package name for ADB command
    const val ADB_GRANT_COMMAND = "adb shell pm grant com.example android.permission.WRITE_SECURE_SETTINGS"

    /**
     * Checks if Developer Options is currently enabled on the system.
     */
    fun isDevSettingsEnabled(context: Context): Boolean {
        return try {
            Settings.Global.getInt(
                context.contentResolver,
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
                0
            ) == 1
        } catch (e: Exception) {
            Log.e(TAG, "Error reading development_settings_enabled", e)
            false
        }
    }

    /**
     * Checks if the app has been granted android.permission.WRITE_SECURE_SETTINGS.
     */
    fun canWriteSecureSettings(context: Context): Boolean {
        return context.checkCallingOrSelfPermission(
            Manifest.permission.WRITE_SECURE_SETTINGS
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Directly writes to development_settings_enabled if WRITE_SECURE_SETTINGS is granted.
     * Returns true if successful, false otherwise.
     */
    fun setDevSettingsEnabled(context: Context, enabled: Boolean): Boolean {
        return try {
            val result = Settings.Global.putInt(
                context.contentResolver,
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
                if (enabled) 1 else 0
            )
            vibrate(context)
            result
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write development_settings_enabled", e)
            false
        }
    }

    /**
     * Checks if USB Debugging (adb_enabled) is active.
     */
    fun isUsbDebuggingEnabled(context: Context): Boolean {
        return try {
            Settings.Global.getInt(
                context.contentResolver,
                Settings.Global.ADB_ENABLED,
                0
            ) == 1
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Sets USB Debugging if WRITE_SECURE_SETTINGS is granted.
     */
    fun setUsbDebuggingEnabled(context: Context, enabled: Boolean): Boolean {
        return try {
            val result = Settings.Global.putInt(
                context.contentResolver,
                Settings.Global.ADB_ENABLED,
                if (enabled) 1 else 0
            )
            vibrate(context)
            result
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Opens the system Developer Options settings screen.
     */
    fun openDeveloperSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.w(TAG, "Could not open developer settings directly, opening general settings", e)
            try {
                val fallbackIntent = Intent(Settings.ACTION_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(fallbackIntent)
            } catch (e2: Exception) {
                Log.e(TAG, "Could not open settings", e2)
            }
        }
    }

    /**
     * Subtle haptic feedback.
     */
    fun vibrate(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(45, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                vibrator?.vibrate(45)
            }
        } catch (_: Exception) {
        }
    }

    /**
     * Registers a ContentObserver for changes to developer options or USB debugging.
     */
    fun registerSettingsObserver(
        context: Context,
        onChanged: () -> Unit
    ): ContentObserver {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                super.onChange(selfChange, uri)
                onChanged()
            }
        }

        try {
            val devUri = Settings.Global.getUriFor(Settings.Global.DEVELOPMENT_SETTINGS_ENABLED)
            context.contentResolver.registerContentObserver(devUri, false, observer)

            val adbUri = Settings.Global.getUriFor(Settings.Global.ADB_ENABLED)
            context.contentResolver.registerContentObserver(adbUri, false, observer)
        } catch (e: Exception) {
            Log.w(TAG, "Could not register settings observer", e)
        }

        return observer
    }

    fun unregisterSettingsObserver(context: Context, observer: ContentObserver) {
        try {
            context.contentResolver.unregisterContentObserver(observer)
        } catch (e: Exception) {
            Log.w(TAG, "Could not unregister settings observer", e)
        }
    }

    /**
     * Request to pin the widget to home screen (Android 8.0+).
     */
    fun requestPinWidget(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val appWidgetManager = context.getSystemService(AppWidgetManager::class.java)
            val provider = ComponentName(context, DevToggleWidgetProvider::class.java)
            if (appWidgetManager != null && appWidgetManager.isRequestPinAppWidgetSupported) {
                return appWidgetManager.requestPinAppWidget(provider, null, null)
            }
        }
        return false
    }
}
