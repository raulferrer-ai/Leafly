package com.raulferrergarcia.leafly.features.plants

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.raulferrergarcia.leafly.R
import com.raulferrergarcia.leafly.designsystem.LeaflyCard
import com.raulferrergarcia.leafly.designsystem.LeaflyColors
import com.raulferrergarcia.leafly.designsystem.LeaflyCopy
import com.raulferrergarcia.leafly.designsystem.LeaflyPrimaryButton
import com.raulferrergarcia.leafly.designsystem.LeaflyQuietButton
import com.raulferrergarcia.leafly.designsystem.LeaflyRadius
import com.raulferrergarcia.leafly.designsystem.LeaflySpacing
import com.raulferrergarcia.leafly.domain.model.WateringLogEntry
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val WateringDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM", Locale.forLanguageTag("es-ES"))

private val WateringDateFormatterWithYear: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM 'de' yyyy", Locale.forLanguageTag("es-ES"))

/**
 * Texto calmado para la fecha de una entrada del historial de riegos.
 *
 * Los riegos recientes se leen como "Hoy" o "Ayer"; los demás muestran el día
 * y el mes, y añaden el año solo cuando no es el actual.
 */
private fun WateringLogDateLabel(
    entry: WateringLogEntry,
    nowMillis: Long = System.currentTimeMillis(),
): String {
    val now = Instant.ofEpochMilli(nowMillis).atZone(ZoneId.systemDefault()).toLocalDate()
    val wateringDate = Instant.ofEpochMilli(entry.wateredAt).atZone(ZoneId.systemDefault()).toLocalDate()

    if (wateringDate == now) {
        return "Hoy"
    }

    val yesterday = now.minusDays(1)
    if (wateringDate == yesterday) {
        return "Ayer"
    }

    return if (wateringDate.getYear() == now.getYear()) {
        wateringDate.format(WateringDateFormatter)
    } else {
        wateringDate.format(WateringDateFormatterWithYear)
    }
}

/**
 * Sección del detalle que muestra el historial de riegos de la planta.
 *
 * Las entradas se ordenan del más reciente al más antiguo y cada una muestra
 * una fecha comprensible (Hoy, Ayer o fecha). Cuando no hay riegos, se
 * muestra un estado vacío calmado sin inventar fechas.
 */
@Composable
fun WateringLogSection(
    entries: List<WateringLogEntry>,
    isLoading: Boolean,
    errorMessage: String?,
    onRetry: () -> Unit = {},
    onMarkAsWatered: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(LeaflySpacing.sm),
    ) {
        Header()

        if (errorMessage != null) {
            ErrorCard(
                message = errorMessage,
                onRetry = onRetry,
            )
        } else if (entries.isEmpty()) {
            EmptyState(
                onMarkAsWatered = onMarkAsWatered,
                isLoading = isLoading,
            )
        } else {
            HistoryList(entries = entries)
        }

        if (isLoading) {
            ProcessingRow()
        }
    }
}

// -----------------------------------------------------------------------
// Sub-componentes
// -----------------------------------------------------------------------

@Composable
private fun Header() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = stringResource(R.string.plant_watering_log_title),
            style = MaterialTheme.typography.headlineSmall,
            color = Color(0xFF2F2B26),
        )
        Text(
            text = stringResource(R.string.plant_watering_log_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF6D675E),
        )
    }
}

@Composable
private fun HistoryList(entries: List<WateringLogEntry>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(LeaflySpacing.xs),
    ) {
        entries.forEachIndexed { index, entry ->
            WateringLogEntryRow(
                entry = entry,
                key = "watering-log-entry-$index-${entry.id}",
            )
        }
    }
}

@Composable
private fun WateringLogEntryRow(
    entry: WateringLogEntry,
    key: String,
) {
    val dateLabel = WateringLogDateLabel(entry)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = LeaflySpacing.sm, vertical = LeaflySpacing.xs)
            .background(
                Color(0xFFF1EFE9),
                RoundedCornerShape(LeaflyRadius.control),
            ),
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(LeaflyColors.Primary),
        )

        Spacer(modifier = Modifier.width(LeaflySpacing.md))

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = dateLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF2F2B26),
            )
            Text(
                text = stringResource(R.string.plant_detail_water_action),
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF6D675E),
            )
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun EmptyState(
    onMarkAsWatered: () -> Unit,
    isLoading: Boolean,
) {
    LeaflyCard {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(LeaflySpacing.md),
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(Color(0xFFE0EDDF)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.WaterDrop,
                    contentDescription = null,
                    tint = Color(0xFF315B44),
                    modifier = Modifier.size(28.dp),
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = LeaflyCopy.Empty.WateringLog.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF2F2B26),
                )
                Text(
                    text = LeaflyCopy.Empty.WateringLog.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF6D675E),
                    textAlign = TextAlign.Center,
                )
            }

            LeaflyPrimaryButton(
                text = LeaflyCopy.Empty.WateringLog.button,
                onClick = onMarkAsWatered,
                enabled = !isLoading,
                icon = Icons.Rounded.WaterDrop,
            )
        }
    }
}

@Composable
private fun ProcessingRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircularProgressIndicator(
            color = Color(0xFF4F8B63),
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(LeaflySpacing.xs))
        Text(
            text = stringResource(R.string.plant_photo_history_processing),
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF6D675E),
        )
    }
}

@Composable
private fun ErrorCard(
    message: String,
    onRetry: () -> Unit,
) {
    LeaflyCard {
        Column(verticalArrangement = Arrangement.spacedBy(LeaflySpacing.sm)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Warning,
                    contentDescription = null,
                    tint = LeaflyColors.NeedsWaterNow,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(LeaflySpacing.xs))
                Text(
                    text = stringResource(R.string.plant_watering_log_error_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = LeaflyColors.TextPrimary,
                )
            }

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = LeaflyColors.TextSecondary,
            )

            LeaflyQuietButton(
                text = LeaflyCopy.Error.PhotoHistory.retry,
                icon = Icons.Rounded.Refresh,
                onClick = onRetry,
            )
        }
    }
}
