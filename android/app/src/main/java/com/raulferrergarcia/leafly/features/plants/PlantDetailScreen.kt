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
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.raulferrergarcia.leafly.R
import com.raulferrergarcia.leafly.designsystem.LeaflyCard
import com.raulferrergarcia.leafly.designsystem.LeaflyCircularIconButton
import com.raulferrergarcia.leafly.designsystem.LeaflyColors
import com.raulferrergarcia.leafly.designsystem.LeaflyPrimaryButton
import com.raulferrergarcia.leafly.designsystem.LeaflyScreen
import com.raulferrergarcia.leafly.designsystem.LeaflySpacing
import com.raulferrergarcia.leafly.designsystem.LeaflyCopy
import com.raulferrergarcia.leafly.designsystem.LeaflyStatusBadge
import com.raulferrergarcia.leafly.designsystem.LeaflyTheme
import com.raulferrergarcia.leafly.designsystem.color
import com.raulferrergarcia.leafly.designsystem.labelRes
import com.raulferrergarcia.leafly.designsystem.statusIcon
import com.raulferrergarcia.leafly.domain.model.Plant
import com.raulferrergarcia.leafly.domain.model.PlantType
import com.raulferrergarcia.leafly.domain.model.WateringStatus
import com.raulferrergarcia.leafly.domain.model.wateringStatus
import com.raulferrergarcia.leafly.domain.usecase.AddPlantUseCase
import com.raulferrergarcia.leafly.domain.usecase.EditPlantUseCase
import com.raulferrergarcia.leafly.features.onboarding.AddPlantScreen
import com.raulferrergarcia.leafly.features.onboarding.AddPlantViewModel
import com.raulferrergarcia.leafly.features.onboarding.AddPlantMode

/**
 * Detalle de una planta: su información, su estado de riego y la acción
 * principal para marcarla como regada.
 */
@Composable
fun PlantDetailScreen(
    viewModel: PlantDetailViewModel,
    onBack: () -> Unit,
    onAddPhoto: (ByteArray) -> Unit = {},
    onEditPlant: (Plant) -> Unit = {},
    addPlant: AddPlantUseCase,
    editPlant: EditPlantUseCase,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val plant = uiState.plant

    val addPlantViewModel: AddPlantViewModel = viewModel(
        factory = AddPlantViewModel.factory(addPlant, editPlant),
    )

    var isShowingEdit by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadPhotoHistory()
    }

    PlantDetailContent(
        uiState = uiState,
        onBack = onBack,
        onWater = viewModel::markAsWatered,
        onAddPhoto = onAddPhoto,
        onRetry = { viewModel.loadPhotoHistory() },
        onEditPlant = {
            isShowingEdit = true
        },
        modifier = modifier,
    )

    if (isShowingEdit) {
        AddPlantScreen(
            viewModel = addPlantViewModel,
            onCancel = { isShowingEdit = false },
            mode = AddPlantMode.EDIT_PLANT,
            editingPlant = plant,
            onSaved = {
                isShowingEdit = false
                viewModel.reloadPlant()
                val updatedPlant = viewModel.uiState.value.plant
                onEditPlant(updatedPlant)
            },
        )
    }
}

@Composable
internal fun PlantDetailContent(
    uiState: PlantDetailUiState,
    onBack: () -> Unit,
    onWater: () -> Unit,
    onAddPhoto: (ByteArray) -> Unit = {},
    onRetry: () -> Unit = {},
    onEditPlant: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val plant = uiState.plant
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

                Header(plant = plant, onEditPlant = onEditPlant)

                StatusCard(plant = plant, status = status)

                LeaflyPrimaryButton(
                    text = stringResource(R.string.plant_detail_water_action),
                    onClick = onWater,
                    enabled = !uiState.isSaving,
                    loading = uiState.isSaving,
                    icon = Icons.Rounded.WaterDrop,
                )

                if (uiState.saveError != null) {
                    ErrorCard()
                }

                PlantPhotoHistorySection(
                    photos = uiState.photos,
                    isAddingPhoto = uiState.isAddingPhoto,
                    photoErrorMessage = uiState.photoErrorMessage,
                    onAddPhoto = onAddPhoto,
                    onRetry = onRetry,
                )
            }
        }
    }
}

@Composable
private fun Header(
    plant: Plant,
    onEditPlant: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(LeaflySpacing.xs),
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.plant_detail_eyebrow),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.75f),
            )

            Spacer(modifier = Modifier.weight(1f))

            LeaflyCircularIconButton(
                icon = Icons.Rounded.Edit,
                contentDescription = stringResource(R.string.plant_detail_edit),
                onClick = onEditPlant,
            )
        }

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
private fun ErrorCard() {
    LeaflyCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Rounded.Warning,
                contentDescription = null,
                tint = LeaflyColors.NeedsWaterNow,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(LeaflySpacing.xs))
            Text(
                text = LeaflyCopy.Error.Watering.message,
                style = MaterialTheme.typography.bodyMedium,
                color = LeaflyColors.TextSecondary,
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun PlantDetailPreview() {
    LeaflyTheme {
        PlantDetailContent(
            uiState = PlantDetailUiState(
                plant = Plant(
                    name = "Monstera",
                    type = PlantType.MONSTERA,
                    lastWateredAt = System.currentTimeMillis() - 6L * 24 * 60 * 60 * 1000,
                ),
            ),
            onBack = {},
            onWater = {},
            onAddPhoto = {},
            onEditPlant = {},
        )
    }
}
