package com.example

import android.database.ContentObserver
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.ui.MainScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private var isDevEnabled by mutableStateOf(false)
    private var isUsbDebuggingEnabled by mutableStateOf(false)
    private var hasWriteSecurePermission by mutableStateOf(false)

    private var settingsObserver: ContentObserver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        refreshState()

        // Register content observer for system settings changes
        settingsObserver = DevSettingsManager.registerSettingsObserver(this) {
            refreshState()
        }

        setContent {
            MyApplicationTheme {
                MainScreen(
                    isDevEnabled = isDevEnabled,
                    isUsbDebuggingEnabled = isUsbDebuggingEnabled,
                    hasWriteSecurePermission = hasWriteSecurePermission,
                    onRefreshState = { refreshState() }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshState()
    }

    override fun onDestroy() {
        super.onDestroy()
        settingsObserver?.let {
            DevSettingsManager.unregisterSettingsObserver(this, it)
        }
    }

    private fun refreshState() {
        isDevEnabled = DevSettingsManager.isDevSettingsEnabled(this)
        isUsbDebuggingEnabled = DevSettingsManager.isUsbDebuggingEnabled(this)
        hasWriteSecurePermission = DevSettingsManager.canWriteSecureSettings(this)
        DevToggleWidgetProvider.updateAllWidgets(this)
    }
}
