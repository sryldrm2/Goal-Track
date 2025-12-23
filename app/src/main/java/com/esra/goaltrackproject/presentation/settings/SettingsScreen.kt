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
            // YATAY MOD
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    SettingsSectionTitle("Profil Ayarları")
                    SettingsItem(
                        title = "Profil Bilgileri",
                        subtitle = viewModel.userName,
                        icon = Icons.Default.Person,
                        onClick = { showEditDialog = true }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)
                    
                    SettingsSectionTitle("Uygulama Ayarları")
                    SettingsSwitchItem(
                        title = "Bildirimler",
                        subtitle = "Günlük hedeflerini hatırlat",
                        icon = Icons.Default.Notifications,
                        checked = viewModel.notificationsEnabled,
                        onCheckedChange = { viewModel.updateNotificationsEnabled(it) }
                    )
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
                    Spacer(modifier = Modifier.height(32.dp))
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    SettingsSectionTitle("Hakkında")
                    SettingsItem(
                        title = "Uygulama Versiyonu",
                        subtitle = "1.0.0",
                        icon = Icons.Default.Info,
                        onClick = {}
                    )
                    SettingsItem(
                        title = "Gizlilik Politikası",
                        icon = Icons.Default.PrivacyTip,
                        onClick = {}
                    )
                    SettingsItem(
                        title = "Geri Bildirim",
                        icon = Icons.Default.Email,
                        onClick = {}
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }
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

@Composable
fun SettingsItem(
    title: String,
    subtitle: String? = null,
    icon: ImageVector,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        },
        supportingContent = subtitle?.let {
            { Text(it, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)) }
        },
        leadingContent = {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        },
        trailingContent = {
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
        },
        modifier = Modifier.clickable { onClick() }
    )
}

@Composable
fun SettingsSwitchItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        },
        supportingContent = {
            Text(subtitle, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        },
        leadingContent = {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    )
}

@Composable
fun EditProfileDialog(
    currentName: String,
    currentAvatar: Int,
    onDismiss: () -> Unit,
    onSave: (String, Int) -> Unit
) {
    var tempName by remember { mutableStateOf(currentName) }
    var tempAvatar by remember { mutableIntStateOf(currentAvatar) }

    val avatarList = listOf(
        R.drawable.avatar1,
        R.drawable.avatar2,
        R.drawable.avatar3,
        R.drawable.avatar4,
        R.drawable.avatar5,
        R.drawable.avatar6,
        R.drawable.avatar7,
        R.drawable.avatar8
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Profili Düzenle", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // İsim Alanı
                OutlinedTextField(
                    value = tempName,
                    onValueChange = { tempName = it },
                    label = { Text("Adınız") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Avatar Seçimi (Yatay Kaydırılabilir)
                Text("Avatar Seç", style = MaterialTheme.typography.labelLarge)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(avatarList) { avatar ->
                        val isSelected = tempAvatar == avatar
                        Image(
                            painter = painterResource(id = avatar),
                            contentDescription = null,
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray,
                                    shape = CircleShape
                                )
                                .clickable { tempAvatar = avatar }
                                .padding(4.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(tempName, tempAvatar) }) {
                Text("Kaydet")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("İptal") }
        }
    )
}

@Composable
fun ThemeSelectionDialog(
    currentMode: Int,
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit
) {
    val options = listOf("Sistem Varsayılanı", "Açık Mod", "Karanlık Mod")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Görünüm Seçin") },
        text = {
            Column {
                options.forEachIndexed { index, label ->
                    Row(
                        Modifier.fillMaxWidth().clickable { onSelect(index) }.padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = currentMode == index, onClick = { onSelect(index) })
                        Text(label, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Kapat") } }
    )
}