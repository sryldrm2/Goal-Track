package com.esra.goaltrackproject.presentation.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalConfiguration
import com.esra.goaltrackproject.R
import com.esra.goaltrackproject.presentation.components.common.SettingsItem
import com.esra.goaltrackproject.presentation.components.common.SettingsSwitchItem
import com.esra.goaltrackproject.presentation.components.settings.EditProfileDialog
import com.esra.goaltrackproject.presentation.components.settings.ThemeSelectionDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    var showEditDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }

    if (showEditDialog) {
        EditProfileDialog(
            currentName = viewModel.userName,
            currentAvatar = viewModel.avatarResId,
            onDismiss = { showEditDialog = false },
            onSave = { newName, newAvatar ->
                viewModel.updateUserName(newName)
                viewModel.updateAvatar(newAvatar)
                showEditDialog = false
            }
        )
    }

    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentMode = viewModel.themeMode,
            onDismiss = { showThemeDialog = false },
            onSelect = { mode ->
                viewModel.updateThemeMode(mode)
                showThemeDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ayarlar", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorScheme.background,
                    titleContentColor = colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        val configuration = LocalConfiguration.current
        val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

        if (isLandscape) {
            // YATAY MOD - TEK KOLON
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(colorScheme.background)
            ) {
                item { SettingsSectionTitle("Profil Ayarları") }
                item {
                    SettingsItem(
                        title = "Profil Bilgileri",
                        subtitle = viewModel.userName,
                        icon = Icons.Default.Person,
                        onClick = { showEditDialog = true }
                    )
                }

                item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp) }

                // Uygulama Ayarları
                item { SettingsSectionTitle("Uygulama Ayarları") }
                item {
                    SettingsSwitchItem(
                        title = "Bildirimler",
                        subtitle = "Günlük hedeflerini hatırlat",
                        icon = Icons.Default.Notifications,
                        checked = viewModel.notificationsEnabled,
                        onCheckedChange = { viewModel.updateNotificationsEnabled(it) }
                    )
                }

                item {
                    SettingsItem(
                        title = "Görünüm",
                        subtitle = when(viewModel.themeMode) {
                            1 -> "Açık Mod"
                            2 -> "Karanlık Mod"
                            else -> "Sistem Varsayılanı"
                        },
                        icon = Icons.Default.Palette,
                        onClick = { showThemeDialog = true }
                    )
                }

                item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp) }

                // Hakkında ve Destek
                item { SettingsSectionTitle("Hakkında") }
                item {
                    SettingsItem(
                        title = "Uygulama Versiyonu",
                        subtitle = "1.0.0",
                        icon = Icons.Default.Info,
                        onClick = {}
                    )
                }
                item {
                    SettingsItem(
                        title = "Gizlilik Politikası",
                        icon = Icons.Default.PrivacyTip,
                        onClick = {}
                    )
                }
                item {
                    SettingsItem(
                        title = "Geri Bildirim",
                        icon = Icons.Default.Email,
                        onClick = {}
                    )
                }

                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        } else {
            // DİKEY MOD
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(colorScheme.background)
            ) {
                item { SettingsSectionTitle("Profil Ayarları") }
                item {
                    SettingsItem(
                        title = "Profil Bilgileri",
                        subtitle = viewModel.userName,
                        icon = Icons.Default.Person,
                        onClick = { showEditDialog = true }
                    )
                }

                item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp) }

                // 2. Uygulama Ayarları
                item { SettingsSectionTitle("Uygulama Ayarları") }
                item {
                    SettingsSwitchItem(
                        title = "Bildirimler",
                        subtitle = "Günlük hedeflerini hatırlat",
                        icon = Icons.Default.Notifications,
                        checked = viewModel.notificationsEnabled,
                        onCheckedChange = { viewModel.updateNotificationsEnabled(it) }
                    )
                }

                item {
                    SettingsItem(
                        title = "Görünüm",
                        subtitle = when(viewModel.themeMode) {
                            1 -> "Açık Mod"
                            2 -> "Karanlık Mod"
                            else -> "Sistem Varsayılanı"
                        },
                        icon = Icons.Default.Palette,
                        onClick = { showThemeDialog = true }
                    )
                }

                item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp) }

                // 3. Hakkında ve Destek
                item { SettingsSectionTitle("Hakkında") }
                item {
                    SettingsItem(
                        title = "Uygulama Versiyonu",
                        subtitle = "1.0.0",
                        icon = Icons.Default.Info,
                        onClick = {}
                    )
                }
                item {
                    SettingsItem(
                        title = "Gizlilik Politikası",
                        icon = Icons.Default.PrivacyTip,
                        onClick = {}
                    )
                }
                item {
                    SettingsItem(
                        title = "Geri Bildirim",
                        icon = Icons.Default.Email,
                        onClick = {}
                    )
                }

                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold
    )
}

