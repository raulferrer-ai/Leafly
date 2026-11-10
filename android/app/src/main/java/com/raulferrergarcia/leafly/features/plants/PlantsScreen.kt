package com.raulferrergarcia.leafly.features.plants

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.raulferrergarcia.leafly.R
import com.raulferrergarcia.leafly.designsystem.LeaflyCard
import com.raulferrergarcia.leafly.designsystem.LeaflyCircularIconButton
import com.raulferrergarcia.leafly.designsystem.LeaflyColors
import com.raulferrergarcia.leafly.designsystem.LeaflyPrimaryButton
import com.raulferrergarcia.leafly.designsystem.LeaflyScreen
import com.raulferrergarcia.leafly.designsystem.LeaflySpacing
import com.raulferrergarcia.leafly.designsystem.LeaflyStatusBadge
import com.raulferrergarcia.leafly.designsystem.LeaflyTheme
import com.raulferrergarcia.leafly.designsystem.color
import com.raulferrergarcia.leafly.designsystem.labelRes
import com.raulferrergarcia.leafly.designsystem.statusIcon
import com.raulferrergarcia.leafly.domain.model.Plant
import com.raulferrergarcia.leafly.domain.model.PlantType
import com.raulferrergarcia.leafly.domain.model.wateringStatus
import com.raulferrergarcia.leafly.domain.usecase.AddPlantUseCase
import com.raulferrergarcia.leafly.domain.usecase.MarkPlantWateredUseCase
import com.raulferrergarcia.leafly.domain.usecase.ObservePlantsUseCase
import com.raulferrergarcia.leafly.features.onboarding.AddPlantMode
import com.raulferrergarcia.leafly.features.onboarding.AddPlantScreen
import com.raulferrergarcia.leafly.features.onboarding.AddPlantViewModel

/**
 * Jardín: lista de todas las plantas con su estado de riego.
 *
 * La navegación vive aquí: lista → añadir planta y lista → detalle.
 */
@Composable
fun PlantsScreen(
    observePlants: ObservePlantsUseCase,
    addPlant: AddPlantUseCase,
    markPlantWatered: MarkPlantWateredUseCase,
    modifier: Modifier = Modifier,
) {
    val plantsViewModel: PlantsViewModel = viewModel(
        factory = PlantsViewModel.factory(observePlants),
    )
    val uiState by plantsViewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = PlantsRoute.LIST,
        modifier = modifier.fillMaxSize(),
    ) {
        composable(PlantsRoute.LIST) {
            PlantsListScreen(
                uiState = uiState,
                onAddPlant = { navController.navigate(PlantsRoute.ADD_PLANT) },
                onPlantClick = { plant -> navController.navigate(PlantsRoute.detail(plant.id)) },
                onRetry = plantsViewModel::retry,
            )
        }

        composable(PlantsRoute.ADD_PLANT) {
            val addPlantViewModel: AddPlantViewModel = viewModel(
                factory = AddPlantViewModel.factory(addPlant),
            )

            AddPlantScreen(
                viewModel = addPlantViewModel,
                mode = AddPlantMode.ANOTHER_PLANT,
                onCancel = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }

        composable(
            route = PlantsRoute.DETAIL,
            arguments = listOf(
                navArgument(PlantsRoute.PLANT_ID_ARG) { type = NavType.StringType },
            ),
        ) { entry ->
            val plantId = entry.arguments?.getString(PlantsRoute.PLANT_ID_ARG)
            val plant = uiState.plants.firstOrNull { it.id == plantId }

            if (plant != null) {
                val detailViewModel: PlantDetailViewModel = viewModel(
                    key = plant.id,
                    factory = PlantDetailViewModel.factory(
                        plant = plant,
                        markPlantWatered = markPlantWatered,
                    ),
                )

                PlantDetailScreen(
                    viewModel = detailViewModel,
                    onBack = { navController.popBackStack() },
                )
            } else {
                MissingPlantContent(
                    isLoading = uiState.isLoading,
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Lista
// ---------------------------------------------------------------------------

@Composable
private fun PlantsListScreen(
    uiState: PlantsUiState,
    onAddPlant: () -> Unit,
    onPlantClick: (Plant) -> Unit,
    onRetry: () -> Unit,
) {
    LeaflyScreen {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentAlignment = Alignment.TopCenter,
        ) {
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = LeaflySpacing.contentMaxWidth)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(
                    start = LeaflySpacing.screenHorizontal,
                    end = LeaflySpacing.screenHorizontal,
                    top = LeaflySpacing.xxl,
                    bottom = LeaflySpacing.xl,
                ),
                verticalArrangement = Arrangement.spacedBy(LeaflySpacing.sm),
            ) {
                item {
                    Header(
                        plantsCount = uiState.plants.size,
                        onAddPlant = onAddPlant,
                    )
                    Spacer(modifier = Modifier.height(LeaflySpacing.sm))
                }

                when {
                    uiState.isLoading && uiState.plants.isEmpty() -> item {
                        LoadingCard()
                    }

                    uiState.loadError != null -> item {
                        ErrorCard(onRetry = onRetry)
                    }

                    uiState.plants.isEmpty() -> item {
                        EmptyCard(onAddPlant = onAddPlant)
                    }

                    else -> items(items = uiState.plants, key = { it.id }) { plant ->
                        PlantCard(
                            plant = plant,
                            onClick = { onPlantClick(plant) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Header(
    plantsCount: Int,
    onAddPlant: () -> Unit,
) {
    val subtitle = when (plantsCount) {
        0 -> stringResource(R.string.plants_subtitle_zero)
        1 -> stringResource(R.string.plants_subtitle_one)
        else -> stringResource(R.string.plants_subtitle_many, plantsCount)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier.weight(1f),
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
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.88f),
            )
        }

        Spacer(modifier = Modifier.width(LeaflySpacing.md))

        LeaflyCircularIconButton(
            icon = Icons.Rounded.Add,
            contentDescription = stringResource(R.string.plants_add_plant),
            onClick = onAddPlant,
        )
    }
}

@Composable
private fun PlantCard(
    plant: Plant,
    onClick: () -> Unit,
) {
    val status = plant.wateringStatus()

    LeaflyCard(
        padding = LeaflySpacing.md,
        onClick = onClick,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            PlantAvatar(plant = plant)

            Spacer(modifier = Modifier.width(LeaflySpacing.md))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = plant.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = LeaflyColors.TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                plant.type?.let { type ->
                    Text(
                        text = type.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = LeaflyColors.TextSecondary,
                    )
                }

                Text(
                    text = plantWateringSummary(plant),
                    style = MaterialTheme.typography.bodyMedium,
                    color = LeaflyColors.TextTertiary,
                )
            }

            Spacer(modifier = Modifier.width(LeaflySpacing.sm))

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(LeaflySpacing.xs),
            ) {
                LeaflyStatusBadge(
                    text = stringResource(status.labelRes),
                    color = status.color,
                    icon = status.statusIcon,
                )

                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = null,
                    tint = LeaflyColors.TextTertiary,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
    }
}

@Composable
private fun LoadingCard() {
    val loadingDescription = stringResource(R.string.plants_loading)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = LeaflySpacing.xl)
            .semantics { contentDescription = loadingDescription },
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = Color.White)
    }
}

@Composable
private fun ErrorCard(onRetry: () -> Unit) {
    LeaflyCard {
        Column(verticalArrangement = Arrangement.spacedBy(LeaflySpacing.sm)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Warning,
                    contentDescription = null,
                    tint = LeaflyColors.NeedsWaterNow,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(LeaflySpacing.xs))
                Text(
                    text = stringResource(R.string.plants_error_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = LeaflyColors.TextPrimary,
                )
            }

            Text(
                text = stringResource(R.string.plants_error_message),
                style = MaterialTheme.typography.bodyMedium,
                color = LeaflyColors.TextSecondary,
            )

            TextButton(
                onClick = onRetry,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = LeaflyColors.Secondary,
                ),
            ) {
                Text(
                    text = stringResource(R.string.plants_retry),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@Composable
private fun EmptyCard(onAddPlant: () -> Unit) {
    LeaflyCard {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(LeaflySpacing.md),
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(LeaflyColors.PrimarySoft, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Eco,
                    contentDescription = null,
                    tint = LeaflyColors.Secondary,
                    modifier = Modifier.size(24.dp),
                )
            }

            Text(
                text = stringResource(R.string.plants_empty_title),
                style = MaterialTheme.typography.titleMedium,
                color = LeaflyColors.TextPrimary,
            )

            Text(
                text = stringResource(R.string.plants_empty_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = LeaflyColors.TextSecondary,
                textAlign = TextAlign.Center,
            )

            LeaflyPrimaryButton(
                text = stringResource(R.string.plants_empty_action),
                onClick = onAddPlant,
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Detalle ausente (solo durante una restauración muy breve)
// ---------------------------------------------------------------------------

@Composable
private fun MissingPlantContent(
    isLoading: Boolean,
    onBack: () -> Unit,
) {
    LaunchedEffect(isLoading) {
        if (!isLoading) onBack()
    }

    LeaflyScreen {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(color = Color.White)
        }
    }
}

// ---------------------------------------------------------------------------
// Rutas
// ---------------------------------------------------------------------------

private object PlantsRoute {
    const val LIST = "plants"
    const val ADD_PLANT = "add-plant"
    const val PLANT_ID_ARG = "plantId"
    const val DETAIL = "plant/{$PLANT_ID_ARG}"

    fun detail(plantId: String): String = "plant/${Uri.encode(plantId)}"
}

// ---------------------------------------------------------------------------
// Preview
// ---------------------------------------------------------------------------

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun PlantsScreenPreview() {
    val now = System.currentTimeMillis()
    val dayMillis = 24L * 60 * 60 * 1000

    LeaflyTheme {
        PlantsListScreen(
            uiState = PlantsUiState(
                plants = listOf(
                    Plant(name = "Monstera", type = PlantType.MONSTERA),
                    Plant(
                        name = "Ficus",
                        type = PlantType.FICUS,
                        lastWateredAt = now - 6 * dayMillis,
                    ),
                    Plant(
                        name = "La de la abuela",
                        lastWateredAt = now - 10 * dayMillis,
                    ),
                ),
                isLoading = false,
            ),
            onAddPlant = {},
            onPlantClick = {},
            onRetry = {},
        )
    }
}
