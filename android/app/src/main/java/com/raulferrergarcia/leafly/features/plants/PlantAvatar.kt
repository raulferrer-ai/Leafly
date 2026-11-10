package com.raulferrergarcia.leafly.features.plants

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.raulferrergarcia.leafly.designsystem.LeaflyColors
import com.raulferrergarcia.leafly.designsystem.icon
import com.raulferrergarcia.leafly.domain.model.Plant

/** Avatar circular suave con el icono del tipo de planta. */
@Composable
fun PlantAvatar(
    plant: Plant,
    modifier: Modifier = Modifier,
    size: Dp = 58.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .background(LeaflyColors.PrimarySoft, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = plant.type?.icon ?: Icons.Rounded.Eco,
            contentDescription = null,
            tint = LeaflyColors.Secondary,
            modifier = Modifier.size(size * 0.4f),
        )
    }
}
