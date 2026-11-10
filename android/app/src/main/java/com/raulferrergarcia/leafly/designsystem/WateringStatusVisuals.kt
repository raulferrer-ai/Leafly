package com.raulferrergarcia.leafly.designsystem

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.raulferrergarcia.leafly.R
import com.raulferrergarcia.leafly.domain.model.WateringStatus

/**
 * Chip de estado no interactivo: fondo suave, icono pequeño y texto.
 * Se usa para mostrar el estado de riego de una planta de un vistazo.
 */
@Composable
fun LeaflyStatusBadge(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(color.copy(alpha = 0.14f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(13.dp),
            )
        } else {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .background(color, CircleShape),
            )
        }

        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = color,
        )
    }
}

/** Color calmado asociado a cada estado de riego. */
val WateringStatus.color: Color
    get() = when (this) {
        WateringStatus.HEALTHY -> LeaflyColors.Healthy
        WateringStatus.NEEDS_WATER_SOON -> LeaflyColors.NeedsWaterSoon
        WateringStatus.NEEDS_WATER_NOW -> LeaflyColors.NeedsWaterNow
    }

/** Icono suave asociado a cada estado de riego. */
val WateringStatus.statusIcon: ImageVector
    get() = when (this) {
        WateringStatus.HEALTHY -> Icons.Rounded.CheckCircle
        WateringStatus.NEEDS_WATER_SOON -> Icons.Rounded.WaterDrop
        WateringStatus.NEEDS_WATER_NOW -> Icons.Rounded.Error
    }

/** Texto cercano que se muestra para cada estado de riego. */
@get:StringRes
val WateringStatus.labelRes: Int
    get() = when (this) {
        WateringStatus.HEALTHY -> R.string.watering_status_healthy
        WateringStatus.NEEDS_WATER_SOON -> R.string.watering_status_soon
        WateringStatus.NEEDS_WATER_NOW -> R.string.watering_status_now
    }
