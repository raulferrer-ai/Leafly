package com.raulferrergarcia.leafly.designsystem

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.Forest
import androidx.compose.material.icons.rounded.Grass
import androidx.compose.material.icons.rounded.LocalFlorist
import androidx.compose.material.icons.rounded.Park
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.ui.graphics.vector.ImageVector
import com.raulferrergarcia.leafly.domain.model.PlantType

/**
 * Iconos suaves que acompañan a cada tipo de planta.
 *
 * El dominio no conoce Compose: esta traducción vive en la capa visual.
 */
val PlantType.icon: ImageVector
    get() = when (this) {
        PlantType.MONSTERA -> Icons.Rounded.Eco
        PlantType.FICUS -> Icons.Rounded.Park
        PlantType.POTHOS -> Icons.Rounded.Grass
        PlantType.CACTUS -> Icons.Rounded.WbSunny
        PlantType.SUCCULENT -> Icons.Rounded.Spa
        PlantType.FERN -> Icons.Rounded.Forest
        PlantType.ORCHID -> Icons.Rounded.LocalFlorist
    }

/** Icono para cuando todavía no se ha elegido un tipo. */
val NoPlantTypeIcon: ImageVector
    get() = Icons.AutoMirrored.Rounded.HelpOutline
