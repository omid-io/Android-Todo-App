package com.example.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
    viewModel: TodoViewModel,
    onDismiss: () -> Unit,
    onManageCategories: () -> Unit
) {
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
                .padding(bottom = 28.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = stringResource(R.string.settings),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = if (isDarkTheme) Color.White else Color(0xFF0F172A),
                modifier = Modifier.padding(bottom = 2.dp)
            )

            // Card 1: Appearance & Preferences
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassCard(shape = RoundedCornerShape(18.dp), isDarkTheme = isDarkTheme)
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    SettingsRow(
                        icon = if (isDarkTheme) Icons.Rounded.DarkMode else Icons.Rounded.LightMode,
                        title = stringResource(R.string.change_theme),
                        isDarkTheme = isDarkTheme,
                        action = {
                            Switch(
                                checked = isDarkTheme,
                                onCheckedChange = { viewModel.toggleTheme() },
                                modifier = Modifier.scale(0.85f)
                            )
                        }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        thickness = 0.5.dp,
                        color = if (isDarkTheme) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.05f)
                    )

                    SettingsRow(
                        icon = Icons.Rounded.Language,
                        title = stringResource(R.string.app_language),
                        subtitle = if (appLanguage == "fa") "فارسی" else "English",
                        isDarkTheme = isDarkTheme,
                        onClick = {
                            val nextLang = if (appLanguage == "fa") "en" else "fa"
                            viewModel.setLanguage(nextLang)
                        }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        thickness = 0.5.dp,
                        color = if (isDarkTheme) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.05f)
                    )

                    SettingsRow(
                        icon = Icons.Rounded.Category,
                        title = stringResource(R.string.manage_categories),
                        isDarkTheme = isDarkTheme,
                        onClick = onManageCategories
                    )
                }
            }

            // Card 2: Backup & Restore
            val context = LocalContext.current
            val filePickerLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.GetContent()
            ) { uri: Uri? ->
                uri?.let {
                    val content = context.contentResolver.openInputStream(it)?.bufferedReader()?.use { r -> r.readText() }
                    if (content != null) {
                        viewModel.importBackup(content)
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassCard(shape = RoundedCornerShape(18.dp), isDarkTheme = isDarkTheme)
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    SettingsRow(
                        icon = Icons.Rounded.CloudUpload,
                        title = stringResource(R.string.export_backup),
                        isDarkTheme = isDarkTheme,
                        onClick = { viewModel.exportBackup() }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        thickness = 0.5.dp,
                        color = if (isDarkTheme) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.05f)
                    )

                    SettingsRow(
                        icon = Icons.Rounded.CloudDownload,
                        title = stringResource(R.string.import_backup),
                        isDarkTheme = isDarkTheme,
                        onClick = { filePickerLauncher.launch("*/*") }
                    )
                }
            }

            // Card 3: Updates & Version Info
            val updateResult by viewModel.updateCheckResult.collectAsState()
            val isCheckingUpdate by viewModel.isCheckingUpdate.collectAsState()

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassCard(shape = RoundedCornerShape(18.dp), isDarkTheme = isDarkTheme)
            ) {
                SettingsRow(
                    icon = Icons.Rounded.SystemUpdate,
                    title = stringResource(R.string.check_for_updates),
                    subtitle = stringResource(R.string.current_version_prefix) + " v${com.example.BuildConfig.VERSION_NAME}",
                    isDarkTheme = isDarkTheme,
                    action = if (isCheckingUpdate) {
                        {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else null,
                    onClick = {
                        viewModel.checkForUpdates(isManual = true)
                    }
                )
            }

            if (updateResult is UpdateCheckResult.NewVersionAvailable) {
                val newVersion = (updateResult as UpdateCheckResult.NewVersionAvailable)
                AlertDialog(
                    onDismissRequest = { viewModel.clearUpdateResult() },
                    title = {
                        Text(
                            text = stringResource(R.string.update_available_title),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = stringResource(R.string.update_available_desc, newVersion.latestVersion),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            if (newVersion.releaseNotes.isNotBlank()) {
                                Text(
                                    text = newVersion.releaseNotes.take(300),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val intent = android.content.Intent(
                                    android.content.Intent.ACTION_VIEW,
                                    Uri.parse(newVersion.downloadUrl)
                                )
                                context.startActivity(intent)
                                viewModel.clearUpdateResult()
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(stringResource(R.string.download_update), fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { viewModel.clearUpdateResult() }) {
                            Text(stringResource(R.string.later))
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun SettingsRow(
    icon: ImageVector,
    title: String,
    isDarkTheme: Boolean,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    action: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = if (isDarkTheme) 0.15f else 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title, 
                style = MaterialTheme.typography.bodyMedium, 
                fontWeight = FontWeight.Bold,
                color = if (isDarkTheme) Color.White else Color(0xFF0F172A)
            )
            if (subtitle != null) {
                Text(
                    text = subtitle, 
                    style = MaterialTheme.typography.bodySmall, 
                    color = if (isDarkTheme) Color.White.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.5f)
                )
            }
        }
        if (action != null) {
            action()
        } else if (onClick != null) {
            Icon(
                imageVector = Icons.Default.ChevronRight, 
                contentDescription = null, 
                tint = if (isDarkTheme) Color.White.copy(alpha = 0.25f) else Color.Black.copy(alpha = 0.25f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
