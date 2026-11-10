package com.raulferrergarcia.leafly.designsystem

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

/**
 * Adaptación de Material 3 al design system de Leafly.
 *
 * No usamos el color scheme genérico de Material ni el color dinámico: la app
 * tiene una identidad propia, suave y natural.
 */
private val LeaflyColorScheme = lightColorScheme(
    primary = LeaflyColors.Primary,
    onPrimary = LeaflyColors.OnPrimary,
    primaryContainer = LeaflyColors.PrimarySoft,
    onPrimaryContainer = LeaflyColors.Secondary,
    secondary = LeaflyColors.Secondary,
    onSecondary = LeaflyColors.OnPrimary,
    secondaryContainer = LeaflyColors.PrimarySoft,
    onSecondaryContainer = LeaflyColors.Secondary,
    tertiary = LeaflyColors.Healthy,
    onTertiary = LeaflyColors.OnPrimary,
    background = LeaflyColors.BackgroundBottom,
    onBackground = LeaflyColors.TextPrimary,
    surface = LeaflyColors.Surface,
    onSurface = LeaflyColors.TextPrimary,
    surfaceVariant = LeaflyColors.FieldSurface,
    onSurfaceVariant = LeaflyColors.TextSecondary,
    outline = LeaflyColors.TextTertiary,
    outlineVariant = LeaflyColors.Separator,
    error = LeaflyColors.NeedsWaterNow,
    onError = LeaflyColors.OnPrimary,
)

private val LeaflyShapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(LeaflyRadius.chip),
    medium = RoundedCornerShape(LeaflyRadius.field),
    large = RoundedCornerShape(LeaflyRadius.control),
    extraLarge = RoundedCornerShape(LeaflyRadius.card),
)

@Composable
fun LeaflyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LeaflyColorScheme,
        typography = LeaflyTypography,
        shapes = LeaflyShapes,
        content = content,
    )
}
