package com.example.ui

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.DevSettingsManager
import com.example.DevToggleWidgetProvider
import com.example.ui.components.AdbPermissionCard
import com.example.ui.components.ExtraDevToolsCard
import com.example.ui.components.HeroToggleCard
import com.example.ui.components.QuickSettingsTileCard
import com.example.ui.components.WidgetPreviewCard
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    isDevEnabled: Boolean,
    isUsbDebuggingEnabled: Boolean,
    hasWriteSecurePermission: Boolean,
    onRefreshState: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    fun showMessage(msg: String) {
        coroutineScope.launch {
            snackbarHostState.showSnackbar(msg)
        }
    }

    fun handleToggleDevSettings() {
        if (hasWriteSecurePermission) {
            val newState = !isDevEnabled
            val success = DevSettingsManager.setDevSettingsEnabled(context, newState)
            if (success) {
                onRefreshState()
                DevToggleWidgetProvider.updateAllWidgets(context)
                showMessage(if (newState) "Developer Options turned ON" else "Developer Options turned OFF")
            } else {
                showMessage("Could not toggle setting directly; opening settings...")
                DevSettingsManager.openDeveloperSettings(context)
            }
        } else {
            showMessage("Opening Developer Options (Grant ADB for background 1-tap toggling)")
            DevSettingsManager.openDeveloperSettings(context)
        }
    }

    fun handleToggleUsbDebugging() {
        if (hasWriteSecurePermission) {
            val newState = !isUsbDebuggingEnabled
            val success = DevSettingsManager.setUsbDebuggingEnabled(context, newState)
            if (success) {
                onRefreshState()
                showMessage(if (newState) "USB Debugging enabled" else "USB Debugging disabled")
            } else {
                DevSettingsManager.openDeveloperSettings(context)
            }
        } else {
            DevSettingsManager.openDeveloperSettings(context)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "DevToggle",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            onRefreshState()
                            showMessage("Status refreshed")
                        },
                        modifier = Modifier.testTag("refresh_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh status"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 640.dp)
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Hero Master Toggle Card
                    item {
                        HeroToggleCard(
                            isDevEnabled = isDevEnabled,
                            hasWriteSecurePermission = hasWriteSecurePermission,
                            onToggle = { handleToggleDevSettings() },
                            onOpenSettings = {
                                DevSettingsManager.openDeveloperSettings(context)
                            }
                        )
                    }

                    // Home Screen Widget Preview & Pin Card
                    item {
                        WidgetPreviewCard(
                            isDevEnabled = isDevEnabled,
                            hasWriteSecurePermission = hasWriteSecurePermission,
                            onPinWidget = {
                                val pinned = DevSettingsManager.requestPinWidget(context)
                                if (!pinned) {
                                    showMessage("Touch & hold your home screen and choose 'Widgets' > 'DevToggle'")
                                }
                            },
                            onSimulateWidgetClick = {
                                handleToggleDevSettings()
                            }
                        )
                    }

                    // ADB Direct Toggle Permission Status & Setup Guide
                    item {
                        AdbPermissionCard(
                            hasWriteSecurePermission = hasWriteSecurePermission,
                            onCopyAdbCommand = {
                                clipboardManager.setText(AnnotatedString(DevSettingsManager.ADB_GRANT_COMMAND))
                                showMessage("ADB command copied to clipboard!")
                            },
                            onVerifyPermission = {
                                onRefreshState()
                                val verified = DevSettingsManager.canWriteSecureSettings(context)
                                if (verified) {
                                    showMessage("Success! WRITE_SECURE_SETTINGS is granted ✓")
                                } else {
                                    showMessage("Permission not detected yet. Make sure to run the ADB command.")
                                }
                            }
                        )
                    }

                    // Quick Settings Tile helper
                    item {
                        QuickSettingsTileCard(
                            isDevEnabled = isDevEnabled,
                            onTileAddedFeedback = { msg -> showMessage(msg) }
                        )
                    }

                    // Extra developer options / USB Debugging
                    item {
                        ExtraDevToolsCard(
                            isUsbDebuggingEnabled = isUsbDebuggingEnabled,
                            hasWriteSecurePermission = hasWriteSecurePermission,
                            onToggleUsbDebugging = { handleToggleUsbDebugging() },
                            onOpenSystemSettings = {
                                try {
                                    val intent = Intent(Settings.ACTION_SETTINGS).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    showMessage("Could not open settings")
                                }
                            }
                        )
                    }

                    // Footer
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "DevToggle • Fast developer settings switch & widget",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
