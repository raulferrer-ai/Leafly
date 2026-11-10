package com.raulferrergarcia.leafly.features.plants

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.raulferrergarcia.leafly.R
import com.raulferrergarcia.leafly.domain.model.Plant
import com.raulferrergarcia.leafly.domain.model.daysSinceWatering
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val PlantDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM", Locale.forLanguageTag("es-ES"))

/**
 * Texto calmado con la última vez que se regó la planta.
 * Si aún no se ha regado, se muestra cuándo se añadió.
 */
@Composable
fun plantWateringSummary(plant: Plant): String {
    val days = plant.daysSinceWatering()

    if (plant.lastWateredAt == null) {
        return when (days) {
            0 -> stringResource(R.string.plants_added_today)
            1 -> stringResource(R.string.plants_added_yesterday)
            else -> stringResource(
                R.string.plant_added_on,
                formattedPlantDate(plant.createdAt),
            )
        }
    }

    return when (days) {
        0 -> stringResource(R.string.plants_watered_today)
        1 -> stringResource(R.string.plants_watered_yesterday)
        else -> stringResource(R.string.plants_watered_days_ago, days)
    }
}

private fun formattedPlantDate(epochMillis: Long): String =
    Instant.ofEpochMilli(epochMillis)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .format(PlantDateFormatter)
