package com.raulferrergarcia.leafly.designsystem

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Paleta semántica de Leafly.
 *
 * Sigue `docs/design-system.md`: verdes naturales, superficies cálidas y
 * acentos tierra muy suaves. Ningún color debe sentirse agresivo.
 */
object LeaflyColors {

    // Fondos
    val BackgroundTop = Color(0xFF4E7A5E)
    val BackgroundMiddle = Color(0xFF93B49B)
    val BackgroundBottom = Color(0xFFEEF4E9)

    val Surface = Color(0xFFF9F8F6)
    val ElevatedSurface = Color(0xFFFFFFFF)
    val FieldSurface = Color(0xFFF1EFE9)

    // Verdes
    val Primary = Color(0xFF4F8B63)
    val PrimarySoft = Color(0xFFE0EDDF)
    val Secondary = Color(0xFF315B44)

    // Estados de planta
    val Healthy = Color(0xFF5E9C6F)
    val NeedsWaterSoon = Color(0xFFD7A45C)
    val NeedsWaterNow = Color(0xFFC97A62)

    // Textos
    val TextPrimary = Color(0xFF2F2B26)
    val TextSecondary = Color(0xFF6D675E)
    val TextTertiary = Color(0xFFA8A198)

    // Otros
    val OnPrimary = Color.White
    val Separator = Color(0xFFE8E4DC)

    /**
     * Degradado de fondo: verde profundo arriba, luz de mañana abajo.
     */
    val BackgroundBrush: Brush
        get() = Brush.linearGradient(
            colors = listOf(BackgroundTop, BackgroundMiddle, BackgroundBottom),
        )
}
