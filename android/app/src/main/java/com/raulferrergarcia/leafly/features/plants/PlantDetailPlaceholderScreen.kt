package com.raulferrergarcia.leafly.features.plants

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.raulferrergarcia.leafly.R
import com.raulferrergarcia.leafly.designsystem.LeaflyCard
import com.raulferrergarcia.leafly.designsystem.LeaflyCircularIconButton
import com.raulferrergarcia.leafly.designsystem.LeaflyColors
import com.raulferrergarcia.leafly.designsystem.LeaflyScreen
import com.raulferrergarcia.leafly.designsystem.LeaflySpacing
import com.raulferrergarcia.leafly.designsystem.LeaflyStatusBadge
import com.raulferrergarcia.leafly.designsystem.LeaflyTheme
import com.raulferrergarcia.leafly.designsystem.color
import com.raulferrergarcia.leafly.designsystem.labelRes
import com.raulferrergarcia.leafly.designsystem.statusIcon
import com.raulferrergarcia.leafly.domain.model.Plant
import com.raulferrergarcia.leafly.domain.model.PlantType
import com.raulferrergarcia.leafly.domain.model.WateringStatus
import com.raulferrergarcia.leafly.domain.model.wateringStatus

/**
 * Pantalla de detalle provisional.
 * La feature de detalle completa llegará después con historial y riegos.
 */
@Composable
fun PlantDetailPlaceholderScreen(
    plant: Plant,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val status = plant.wateringStatus()

    LeaflyScreen(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = LeaflySpacing.contentMaxWidth)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = LeaflySpacing.screenHorizontal)
                    .padding(top = LeaflySpacing.sm, bottom = LeaflySpacing.xl),
                verticalArrangement = Arrangement.spacedBy(LeaflySpacing.lg),
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    LeaflyCircularIconButton(
                        icon = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = stringResource(R.string.plant_detail_back),
                        onClick = onBack,
                    )
                }

                Header(plant = plant)

                StatusCard(plant = plant, status = status)

                ComingSoonCard()
            }
        }
    }
}

@Composable
private fun Header(plant: Plant) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(LeaflySpacing.xs),
    ) {
        Text(
            text = stringResource(R.string.plant_detail_eyebrow),
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.75f),
        )
        Text(
            text = plant.name,
            style = MaterialTheme.typography.displayLarge,
            color = Color.White,
        )

        plant.type?.let { type ->
            Text(
                text = type.displayName,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.88f),
            )
        }
    }
}

@Composable
private fun StatusCard(
    plant: Plant,
    status: WateringStatus,
) {
    LeaflyCard(padding = LeaflySpacing.md) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            PlantAvatar(plant = plant)

            Spacer(modifier = Modifier.width(LeaflySpacing.md))

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LeaflyStatusBadge(
                    text = stringResource(status.labelRes),
                    color = status.color,
                    icon = status.statusIcon,
                )
                Text(
                    text = plantWateringSummary(plant),
                    style = MaterialTheme.typography.bodyMedium,
                    color = LeaflyColors.TextSecondary,
                )
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun ComingSoonCard() {
    LeaflyCard {
        Column(verticalArrangement = Arrangement.spacedBy(LeaflySpacing.sm)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = LeaflyColors.TextPrimary,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(LeaflySpacing.xs))
                Text(
                    text = stringResource(R.string.plant_detail_coming_soon_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = LeaflyColors.TextPrimary,
                )
            }

            Text(
                text = stringResource(R.string.plant_detail_coming_soon_body),
                style = MaterialTheme.typography.bodyLarge,
                color = LeaflyColors.TextSecondary,
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun PlantDetailPlaceholderPreview() {
    LeaflyTheme {
        PlantDetailPlaceholderScreen(
            plant = Plant(name = "Monstera", type = PlantType.MONSTERA),
            onBack = {},
        )
    }
}
