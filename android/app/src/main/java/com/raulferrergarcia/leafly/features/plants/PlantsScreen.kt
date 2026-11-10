package com.raulferrergarcia.leafly.features.plants

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.raulferrergarcia.leafly.R
import com.raulferrergarcia.leafly.designsystem.LeaflyCard
import com.raulferrergarcia.leafly.designsystem.LeaflyColors
import com.raulferrergarcia.leafly.designsystem.LeaflyScreen
import com.raulferrergarcia.leafly.designsystem.LeaflySpacing
import com.raulferrergarcia.leafly.designsystem.LeaflyTheme
import com.raulferrergarcia.leafly.designsystem.icon
import com.raulferrergarcia.leafly.domain.model.Plant
import com.raulferrergarcia.leafly.domain.model.PlantType
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Pantalla a la que se llega tras guardar la primera planta.
 *
 * Por ahora muestra las plantas guardadas; la lista completa con estados de
 * riego llegará en la siguiente feature.
 */
@Composable
fun PlantsScreen(
    plants: List<Plant>,
    modifier: Modifier = Modifier,
) {
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
                    .padding(top = LeaflySpacing.xxl, bottom = LeaflySpacing.xl),
                verticalArrangement = Arrangement.spacedBy(LeaflySpacing.lg),
            ) {
                Header(plantsCount = plants.size)

                Column(verticalArrangement = Arrangement.spacedBy(LeaflySpacing.sm)) {
                    plants.forEach { plant ->
                        PlantCard(plant = plant)
                    }
                }
            }
        }
    }
}

@Composable
private fun Header(plantsCount: Int) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(LeaflySpacing.xs),
    ) {
        Text(
            text = stringResource(R.string.plants_eyebrow),
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.75f),
        )
        Text(
            text = stringResource(R.string.plants_title),
            style = MaterialTheme.typography.displayLarge,
            color = Color.White,
        )
        Text(
            text = if (plantsCount == 1) {
                stringResource(R.string.plants_subtitle_one)
            } else {
                stringResource(R.string.plants_subtitle_many, plantsCount)
            },
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.88f),
        )
    }
}

@Composable
private fun PlantCard(plant: Plant) {
    val type = plant.type

    LeaflyCard(padding = LeaflySpacing.md) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .background(LeaflyColors.PrimarySoft, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = type?.icon ?: Icons.Rounded.Eco,
                    contentDescription = null,
                    tint = LeaflyColors.Secondary,
                    modifier = Modifier.size(24.dp),
                )
            }

            Spacer(modifier = Modifier.width(LeaflySpacing.md))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = plant.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = LeaflyColors.TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                if (type != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = type.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = LeaflyColors.TextSecondary,
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(
                        R.string.plant_added_on,
                        formattedCreationDate(plant.createdAt),
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = LeaflyColors.TextTertiary,
                )
            }
        }
    }
}

private val SpanishDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d 'de' MMMM", Locale.forLanguageTag("es-ES"))

private fun formattedCreationDate(createdAt: Long): String =
    Instant.ofEpochMilli(createdAt)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .format(SpanishDateFormatter)

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun PlantsScreenPreview() {
    LeaflyTheme {
        PlantsScreen(
            plants = listOf(
                Plant(name = "Monstera", type = PlantType.MONSTERA),
                Plant(name = "La de la abuela"),
            ),
        )
    }
}
