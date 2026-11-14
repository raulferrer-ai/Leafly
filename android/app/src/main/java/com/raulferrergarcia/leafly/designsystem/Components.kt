package com.raulferrergarcia.leafly.designsystem

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ---------------------------------------------------------------------------
// Fondo y contenedor de pantalla
// ---------------------------------------------------------------------------

/**
 * Fondo común de la app: degradado verde suave con formas orgánicas difusas.
 */
@Composable
fun LeaflyBackground(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LeaflyColors.BackgroundBrush),
    ) {
        Box(
            modifier = Modifier
                .size(360.dp)
                .offset(x = (-140).dp, y = (-260).dp)
                .background(
                    brush = Brush.radialGradient(
                        colorStops = arrayOf(
                            0f to Color.White.copy(alpha = 0.20f),
                            1f to Color.Transparent,
                        ),
                    ),
                    shape = CircleShape,
                ),
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(320.dp)
                .offset(x = 120.dp, y = 140.dp)
                .background(
                    brush = Brush.radialGradient(
                        colorStops = arrayOf(
                            0f to LeaflyColors.PrimarySoft.copy(alpha = 0.35f),
                            1f to Color.Transparent,
                        ),
                    ),
                    shape = CircleShape,
                ),
        )
    }
}

/**
 * Contenedor de pantalla: fondo de Leafly + contenido.
 */
@Composable
fun LeaflyScreen(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(modifier = modifier.fillMaxSize()) {
        LeaflyBackground()
        Box(modifier = Modifier.fillMaxSize(), content = content)
    }
}

// ---------------------------------------------------------------------------
// Card
// ---------------------------------------------------------------------------

/**
 * Card flotante de Leafly: superficie blanco roto, radio grande y sombra suave.
 */
@Composable
fun LeaflyCard(
    modifier: Modifier = Modifier,
    padding: Dp = LeaflySpacing.lg,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(LeaflyRadius.card)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 18.dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.18f),
                spotColor = Color.Black.copy(alpha = 0.18f),
            )
            .clip(shape)
            .background(LeaflyColors.Surface)
            .then(
                if (onClick != null) {
                    Modifier.clickable(role = Role.Button, onClick = onClick)
                } else {
                    Modifier
                },
            )
            .padding(padding),
        content = content,
    )
}

// ---------------------------------------------------------------------------
// Botones
// ---------------------------------------------------------------------------

/**
 * Botón principal: pill verde, texto blanco y sombra suave.
 *
 * [icon] es opcional: se coloca a la izquierda del texto. Mientras [loading]
 * está activo se muestra un indicador en su lugar.
 */
@Composable
fun LeaflyPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    icon: ImageVector? = null,
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp),
        enabled = enabled && !loading,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = LeaflyColors.Primary,
            contentColor = LeaflyColors.OnPrimary,
            disabledContainerColor = LeaflyColors.Primary.copy(alpha = 0.55f),
            disabledContentColor = LeaflyColors.OnPrimary.copy(alpha = 0.85f),
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 10.dp,
            pressedElevation = 4.dp,
            disabledElevation = 0.dp,
        ),
        contentPadding = PaddingValues(horizontal = LeaflySpacing.lg),
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = LeaflyColors.OnPrimary,
                strokeWidth = 2.dp,
            )
            Spacer(modifier = Modifier.width(LeaflySpacing.xs))
        } else if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = LeaflyColors.OnPrimary,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(LeaflySpacing.xs))
        }

        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

/**
 * Botón secundario y tranquilo: superficie suave, icono y texto.
 *
 * Se usa para acciones opcionales que no deben competir con el botón
 * principal (por ejemplo, elegir una foto o quitarla).
 */
@Composable
fun LeaflyQuietButton(
    text: String,
    icon: ImageVector? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    destructive: Boolean = false,
) {
    val contentColor = when {
        !enabled -> LeaflyColors.TextTertiary
        destructive -> LeaflyColors.NeedsWaterNow
        else -> LeaflyColors.Secondary
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(LeaflyRadius.control))
            .background(LeaflyColors.FieldSurface)
            .clickable(
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(horizontal = LeaflySpacing.md),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(LeaflySpacing.xs))
        }
        Text(
            text = text,
            color = contentColor,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * Botón redondo para acciones discretas sobre el degradado (por ejemplo, cerrar).
 */
@Composable
fun LeaflyCircularIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(44.dp)
            .shadow(
                elevation = 12.dp,
                shape = CircleShape,
                ambientColor = Color.Black.copy(alpha = 0.16f),
                spotColor = Color.Black.copy(alpha = 0.16f),
            )
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.92f)),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = LeaflyColors.Secondary,
            modifier = Modifier.size(18.dp),
        )
    }
}

// ---------------------------------------------------------------------------
// Chip
// ---------------------------------------------------------------------------

/**
 * Chip de selección: muy redondeado, con icono y texto.
 */
// Marca
// ---------------------------------------------------------------------------

/**
 * Marca suave de Leafly: hoja sobre un círculo luminoso.
 */
@Composable
fun LeaflyMark(
    modifier: Modifier = Modifier,
    size: Dp = 96.dp,
) {
    Box(
        modifier = modifier.size(size * 1.35f),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colorStops = arrayOf(
                            0f to Color.White.copy(alpha = 0.26f),
                            1f to Color.Transparent,
                        ),
                    ),
                    shape = CircleShape,
                ),
        )

        Box(
            modifier = Modifier
                .size(size)
                .shadow(
                    elevation = 20.dp,
                    shape = CircleShape,
                    ambientColor = Color.Black.copy(alpha = 0.18f),
                    spotColor = Color.Black.copy(alpha = 0.18f),
                )
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color.White, LeaflyColors.PrimarySoft),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.Eco,
                contentDescription = null,
                tint = LeaflyColors.Secondary,
                modifier = Modifier.size(size * 0.42f),
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Barra inferior con degradado
// ---------------------------------------------------------------------------

/**
 * Zona inferior de acción: el contenido se apoya sobre un degradado que se
 * funde con el fondo, para que el botón principal siempre respire.
 */
@Composable
fun LeaflyBottomBar(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colorStops = arrayOf(
                        0f to LeaflyColors.BackgroundBottom.copy(alpha = 0f),
                        0.30f to LeaflyColors.BackgroundBottom.copy(alpha = 0.90f),
                        0.55f to LeaflyColors.BackgroundBottom,
                        1f to LeaflyColors.BackgroundBottom,
                    ),
                ),
            )
            .navigationBarsPadding()
            .padding(horizontal = LeaflySpacing.screenHorizontal)
            .padding(top = LeaflySpacing.lg, bottom = LeaflySpacing.xs),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content,
    )
}

/**
 * Chip de selección para filtros: muy redondeado, con icono y texto.
 *
 * Se usa para filtros de estado (ej. estados de riego) en secciones de lista.
 */
@Composable
fun LeaflyChip(
    title: String,
    icon: ImageVector?,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clickable(
                enabled = true,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(horizontal = LeaflySpacing.sm, vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) LeaflyColors.OnPrimary else LeaflyColors.Secondary,
                modifier = Modifier.size(16.dp),
            )
        }
        Spacer(modifier = Modifier.width(LeaflySpacing.xs))
        Text(
            text = title,
            color = if (selected) LeaflyColors.OnPrimary else LeaflyColors.Secondary,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
        )
    }
}
