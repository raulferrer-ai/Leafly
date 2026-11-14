package com.raulferrergarcia.leafly.features.settings

import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.raulferrergarcia.leafly.data.repository.NotificationPreferencesRepository
import com.raulferrergarcia.leafly.domain.model.Plant
import com.raulferrergarcia.leafly.domain.usecase.SyncWateringRemindersUseCase
import com.raulferrergarcia.leafly.designsystem.LeaflyCard
import com.raulferrergarcia.leafly.designsystem.LeaflyColors
import com.raulferrergarcia.leafly.designsystem.LeaflyCopy
import com.raulferrergarcia.leafly.designsystem.LeaflyPrimaryButton
import com.raulferrergarcia.leafly.designsystem.LeaflyQuietButton
import com.raulferrergarcia.leafly.designsystem.LeaflyScreen
import com.raulferrergarcia.leafly.designsystem.LeaflySpacing

/**
 * Pantalla de ajustes de notificaciones de riego.
 *
 * Muestra el estado del permiso de notificaciones y permite elegir la hora
 * preferida de los recordatorios de riego.
 */
@Composable
fun SettingsScreen(
    plants: List<Plant>,
    onSavePreferredTime: (Int, Int) -> Unit,
    onOpenSystemSettings: () -> Unit,
    syncWateringReminders: SyncWateringRemindersUseCase,
    preferencesRepository: NotificationPreferencesRepository,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    val settingsViewModel: SettingsViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                SettingsViewModel(
                    preferencesRepository = preferencesRepository,
                    syncWateringReminders = syncWateringReminders,
                )
            }
        },
    )

    val uiState by settingsViewModel.uiState.collectAsStateWithLifecycle()

    // Cargar estado inicial una vez
    var loaded by remember { mutableStateOf(false) }
    if (!loaded) {
        val notificationManager = androidx.core.app.NotificationManagerCompat.from(context)
        settingsViewModel.load(notificationManager.areNotificationsEnabled())
        loaded = true
    }

    var timePickerOpen by remember { mutableStateOf(false) }
    var selectedHour by remember { mutableStateOf(uiState.preferredHour) }
    var selectedMinute by remember { mutableStateOf(uiState.preferredMinute) }

    val openSettingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { onOpenSystemSettings() }

    LeaflyScreen(
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = LeaflySpacing.screenHorizontal),
            verticalArrangement = Arrangement.spacedBy(LeaflySpacing.lg),
        ) {
            // Cabecera
            Text(
                text = LeaflyCopy.Notifications.title,
                style = MaterialTheme.typography.displayLarge,
                color = LeaflyColors.TextPrimary,
            )

            Text(
                text = LeaflyCopy.Notifications.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = LeaflyColors.TextSecondary,
            )

            Spacer(modifier = Modifier.height(LeaflySpacing.sm))

            // Tarjeta de permiso
            LeaflyCard {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(LeaflySpacing.md),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val icon = if (uiState.isPermissionGranted) {
                            Icons.Default.Notifications
                        } else {
                            Icons.Default.NotificationsOff
                        }
                        val tint = if (uiState.isPermissionGranted) {
                            LeaflyColors.Healthy
                        } else {
                            LeaflyColors.TextTertiary
                        }

                        androidx.compose.material3.Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = tint,
                            modifier = Modifier.size(20.dp),
                        )

                        Spacer(modifier = Modifier.width(LeaflySpacing.sm))

                        Text(
                            text = LeaflyCopy.Notifications.Permission.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = LeaflyColors.TextPrimary,
                        )
                    }

                    Text(
                        text = if (uiState.isPermissionGranted) {
                            LeaflyCopy.Notifications.Permission.granted
                        } else {
                            LeaflyCopy.Notifications.Permission.denied
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = LeaflyColors.TextSecondary,
                    )

                    if (!uiState.isPermissionGranted) {
                        LeaflyQuietButton(
                            text = LeaflyCopy.Notifications.Permission.openSettings,
                            onClick = {
                                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                }
                                if (intent.resolveActivity(context.packageManager) != null) {
                                    openSettingsLauncher.launch(intent)
                                }
                            },
                        )
                    }
                }
            }

            // Tarjeta de hora preferida
            LeaflyCard {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(LeaflySpacing.md),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Rounded.AccessTime,
                            contentDescription = null,
                            tint = LeaflyColors.Secondary,
                            modifier = Modifier.size(20.dp),
                        )

                        Spacer(modifier = Modifier.width(LeaflySpacing.sm))

                        Text(
                            text = LeaflyCopy.Notifications.Time.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = LeaflyColors.TextPrimary,
                        )
                    }

                    Text(
                        text = LeaflyCopy.Notifications.Time.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = LeaflyColors.TextSecondary,
                    )

                    Spacer(modifier = Modifier.height(LeaflySpacing.sm))

                    Row(
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = "%02d:%02d".format(selectedHour, selectedMinute),
                            style = MaterialTheme.typography.displayMedium,
                            color = LeaflyColors.TextPrimary,
                        )
                    }

                    Spacer(modifier = Modifier.height(LeaflySpacing.sm))

                    LeaflyPrimaryButton(
                        text = "Cambiar hora",
                        onClick = { timePickerOpen = true },
                    )
                }
            }

            // Diálogo del selector de hora
            if (timePickerOpen) {
                TimePickerDialog(
                    hour = selectedHour,
                    minute = selectedMinute,
                    onDismiss = { timePickerOpen = false },
                    onTimeSelected = { hour, minute ->
                        timePickerOpen = false
                        selectedHour = hour
                        selectedMinute = minute
                        settingsViewModel.setPreferredTime(hour, minute, plants)
                        onSavePreferredTime(hour, minute)
                    },
                )
            }
        }
    }
}

/** Diálogo simple del selector de hora. */
@Composable
private fun TimePickerDialog(
    hour: Int,
    minute: Int,
    onDismiss: () -> Unit,
    onTimeSelected: (Int, Int) -> Unit,
) {
    var pickerHour by remember { mutableStateOf(hour) }
    var pickerMinute by remember { mutableStateOf(minute) }

    LeaflyCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = LeaflySpacing.screenHorizontal),
    ) {
        Column(
            modifier = Modifier
                .padding(LeaflySpacing.lg),
            verticalArrangement = Arrangement.spacedBy(LeaflySpacing.md),
        ) {
            Text(
                text = "Seleccionar hora",
                style = MaterialTheme.typography.titleMedium,
                color = LeaflyColors.TextPrimary,
            )

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Hora
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Hora",
                        style = MaterialTheme.typography.labelSmall,
                        color = LeaflyColors.TextTertiary,
                    )
                    Row(
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        androidx.compose.material3.IconButton(
                            onClick = { pickerHour = (pickerHour + 23) % 24 },
                        ) {
                            Text(
                                text = "−",
                                style = MaterialTheme.typography.titleLarge,
                                color = LeaflyColors.Secondary,
                            )
                        }
                        Text(
                            text = "%02d".format(pickerHour),
                            style = MaterialTheme.typography.titleLarge,
                            color = LeaflyColors.TextPrimary,
                        )
                        androidx.compose.material3.IconButton(
                            onClick = { pickerHour = (pickerHour + 1) % 24 },
                        ) {
                            Text(
                                text = "+",
                                style = MaterialTheme.typography.titleLarge,
                                color = LeaflyColors.Secondary,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(LeaflySpacing.sm))
                    // Minuto
                    Text(
                        text = "Minuto",
                        style = MaterialTheme.typography.labelSmall,
                        color = LeaflyColors.TextTertiary,
                    )
                    Row(
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        androidx.compose.material3.IconButton(
                            onClick = { pickerMinute = (pickerMinute + 59) % 60 },
                        ) {
                            Text(
                                text = "−",
                                style = MaterialTheme.typography.titleLarge,
                                color = LeaflyColors.Secondary,
                            )
                        }
                        Text(
                            text = "%02d".format(pickerMinute),
                            style = MaterialTheme.typography.titleLarge,
                            color = LeaflyColors.TextPrimary,
                        )
                        androidx.compose.material3.IconButton(
                            onClick = { pickerMinute = (pickerMinute + 1) % 60 },
                        ) {
                            Text(
                                text = "+",
                                style = MaterialTheme.typography.titleLarge,
                                color = LeaflyColors.Secondary,
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                androidx.compose.material3.TextButton(
                    onClick = onDismiss,
                ) {
                    Text(
                        text = "Cancelar",
                        style = MaterialTheme.typography.labelMedium,
                        color = LeaflyColors.Secondary,
                    )
                }

                Spacer(modifier = Modifier.width(LeaflySpacing.md))

                LeaflyPrimaryButton(
                    text = "Guardar",
                    onClick = {
                        onTimeSelected(pickerHour, pickerMinute)
                    },
                    modifier = Modifier.width(120.dp),
                )
            }
        }
    }
}
