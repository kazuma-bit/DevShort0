package com.example

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.Toast
import androidx.core.content.ContextCompat

class DevToggleWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_TOGGLE) {
            handleToggle(context)
        } else if (intent.action == ACTION_OPEN) {
            DevSettingsManager.openDeveloperSettings(context)
        }
    }

    private fun handleToggle(context: Context) {
        val hasPermission = DevSettingsManager.canWriteSecureSettings(context)
        if (hasPermission) {
            val isCurrentlyOn = DevSettingsManager.isDevSettingsEnabled(context)
            val newState = !isCurrentlyOn
            val success = DevSettingsManager.setDevSettingsEnabled(context, newState)
            if (success) {
                val message = if (newState) "Developer Options: ON" else "Developer Options: OFF"
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Failed to toggle setting", Toast.LENGTH_SHORT).show()
                DevSettingsManager.openDeveloperSettings(context)
            }
        } else {
            // Cannot directly toggle without WRITE_SECURE_SETTINGS, open developer settings
            DevSettingsManager.openDeveloperSettings(context)
            Toast.makeText(
                context,
                "Grant ADB permission in app for direct 1-tap toggling",
                Toast.LENGTH_LONG
            ).show()
        }
        updateAllWidgets(context)
    }

    companion object {
        const val ACTION_TOGGLE = "com.example.devtoggle.ACTION_TOGGLE"
        const val ACTION_OPEN = "com.example.devtoggle.ACTION_OPEN"

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, DevToggleWidgetProvider::class.java)
            val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            for (id in allWidgetIds) {
                updateAppWidget(context, appWidgetManager, id)
            }
        }

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val isEnabled = DevSettingsManager.isDevSettingsEnabled(context)
            val hasPermission = DevSettingsManager.canWriteSecureSettings(context)

            val views = RemoteViews(context.packageName, R.layout.widget_dev_toggle)

            if (isEnabled) {
                views.setTextViewText(R.id.widget_status_badge, context.getString(R.string.status_on))
                views.setInt(
                    R.id.widget_status_badge,
                    "setBackgroundResource",
                    R.drawable.widget_status_pill_on
                )
                views.setTextColor(
                    R.id.widget_status_badge,
                    ContextCompat.getColor(context, R.color.dev_green)
                )
                views.setInt(
                    R.id.widget_power_icon,
                    "setBackgroundResource",
                    R.drawable.widget_btn_bg_on
                )
            } else {
                views.setTextViewText(R.id.widget_status_badge, context.getString(R.string.status_off))
                views.setInt(
                    R.id.widget_status_badge,
                    "setBackgroundResource",
                    R.drawable.widget_status_pill_off
                )
                views.setTextColor(
                    R.id.widget_status_badge,
                    ContextCompat.getColor(context, R.color.dev_gray)
                )
                views.setInt(
                    R.id.widget_power_icon,
                    "setBackgroundResource",
                    R.drawable.widget_btn_bg_off
                )
            }

            if (hasPermission) {
                views.setTextViewText(
                    R.id.widget_subtitle,
                    if (isEnabled) "Tap to turn OFF" else "Tap to turn ON"
                )
            } else {
                views.setTextViewText(
                    R.id.widget_subtitle,
                    context.getString(R.string.tap_to_open)
                )
            }

            // Clicking toggles developer options (or opens if permission not granted)
            val toggleIntent = Intent(context, DevToggleWidgetProvider::class.java).apply {
                action = ACTION_TOGGLE
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                appWidgetId,
                toggleIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)
            views.setOnClickPendingIntent(R.id.widget_power_icon, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
