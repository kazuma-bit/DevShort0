package com.example

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.widget.Toast

class DevToggleTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()
        val context = applicationContext
        val hasPermission = DevSettingsManager.canWriteSecureSettings(context)

        if (hasPermission) {
            val currentState = DevSettingsManager.isDevSettingsEnabled(context)
            val newState = !currentState
            val success = DevSettingsManager.setDevSettingsEnabled(context, newState)
            if (success) {
                Toast.makeText(
                    context,
                    if (newState) "Developer Options: ON" else "Developer Options: OFF",
                    Toast.LENGTH_SHORT
                ).show()
            }
            updateTileState()
            DevToggleWidgetProvider.updateAllWidgets(context)
        } else {
            // Open developer settings
            val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    val pendingIntent = PendingIntent.getActivity(
                        this,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    startActivityAndCollapse(pendingIntent)
                } else {
                    @Suppress("DEPRECATION")
                    startActivityAndCollapse(intent)
                }
            } catch (e: Exception) {
                DevSettingsManager.openDeveloperSettings(this)
            }
            Toast.makeText(
                context,
                "Grant ADB permission in DevToggle for 1-tap shade toggling",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun updateTileState() {
        val tile = qsTile ?: return
        val isEnabled = DevSettingsManager.isDevSettingsEnabled(this)
        tile.state = if (isEnabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = getString(R.string.tile_label)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            tile.subtitle = if (isEnabled) getString(R.string.status_on) else getString(R.string.status_off)
        }
        tile.updateTile()
    }
}
