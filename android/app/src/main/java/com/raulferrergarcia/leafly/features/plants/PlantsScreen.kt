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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.raulferrergarcia.leafly.designsystem.LeaflyChip
import com.raulferrergarcia.leafly.designsystem.LeaflyCircularIconButton
import com.raulferrergarcia.leafly.designsystem.LeaflyColors
import com.raulferrergarcia.leafly.designsystem.LeaflyPrimaryButton
import com.raulferrergarcia.leafly.designsystem.LeaflyRadius
import com.raulferrergarcia.leafly.designsystem.LeaflyScreen
import com.raulferrergarcia.leafly.designsystem.LeaflySpacing
import com.raulferrergarcia.leafly.designsystem.LeaflyCopy
import com.raulferrergarcia.leafly.designsystem.LeaflyStatusBadge
import com.raulferrergarcia.leafly.designsystem.LeaflyTheme
import com.raulferrergarcia.leafly.designsystem.labelRes
import com.raulferrergarcia.leafly.designsystem.statusIcon
import com.raulferrergarcia.leafly.domain.model.Plant
import com.raulferrergarcia.leafly.domain.model.PlantType
import com.raulferrergarcia.leafly.domain.model.WateringStatus
import com.raulferrergarcia.leafly.domain.model.wateringStatus
import com.raulferrergarcia.leafly.domain.repository.PlantRepository
import com.raulferrergarcia.leafly.domain.usecase.AddPlantUseCase
import com.raulferrergarcia.leafly.domain.usecase.CancelWateringReminderUseCase
import com.raulferrergarcia.leafly.domain.usecase.MarkPlantWateredUseCase
import com.raulferrergarcia.leafly.domain.usecase.ObservePlantsUseCase
import com.raulferrergarcia.leafly.domain.usecase.SyncWateringRemindersUseCase
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
    syncWateringReminders: SyncWateringRemindersUseCase,
    cancelWateringReminder: CancelWateringReminderUseCase,
    plantRepository: PlantRepository,
    modifier: Modifier = Modifier,
) {
    val plantsViewModel: PlantsViewModel = viewModel(
        factory = PlantsViewModel.factory(
            plantRepository = plantRepository,
            syncWateringReminders = syncWateringReminders,
        ),
    )
    val uiState by plantsViewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()

    RequestWateringNotificationPermission(plants = uiState.plants)

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
                onSearchTextChange = plantsViewModel::setSearchText,
                onStatusFilterChange = plantsViewModel::setStatusFilter,
                onClearFilters = plantsViewModel::clearFilters,
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
                        repository = plantRepository,
                        markPlantWatered = markPlantWatered,
                        cancelWateringReminder = cancelWateringReminder,
                    ),
                )

                PlantDetailScreen(
                    viewModel = detailViewModel,
                    onBack = { navController.popBackStack() },
                    onAddPhoto = { data ->
                        detailViewModel.addPhoto(data)
                    },
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

// ============================================================================
// Componentes privados (definidos antes de su uso)
// ============================================================================

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
                    text = LeaflyCopy.Error.Plants.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = LeaflyColors.TextPrimary,
                )
            }

            Text(
                text = LeaflyCopy.Error.Plants.message,
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
                    text = LeaflyCopy.Error.Plants.retry,
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
                text = LeaflyCopy.Empty.Plants.title,
                style = MaterialTheme.typography.titleMedium,
                color = LeaflyColors.TextPrimary,
            )

            Text(
                text = LeaflyCopy.Empty.Plants.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = LeaflyColors.TextSecondary,
                textAlign = TextAlign.Center,
            )

            LeaflyPrimaryButton(
                text = LeaflyCopy.Empty.Plants.button,
                onClick = onAddPlant,
            )
        }
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
                    color = when (status) {
                        WateringStatus.HEALTHY -> LeaflyColors.Healthy
                        WateringStatus.NEEDS_WATER_SOON -> LeaflyColors.NeedsWaterSoon
                        WateringStatus.NEEDS_WATER_NOW -> LeaflyColors.NeedsWaterNow
                    },
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

// ============================================================================
// Lista principal
// ============================================================================

@Composable
private fun PlantsListScreen(
    uiState: PlantsUiState,
    onAddPlant: () -> Unit,
    onPlantClick: (Plant) -> Unit,
    onRetry: () -> Unit,
    onSearchTextChange: (String) -> Unit,
    onStatusFilterChange: (WateringStatus) -> Unit,
    onClearFilters: () -> Unit,
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
                        plantsCount = uiState.filteredPlants.size,
                        hasFilters = uiState.hasActiveFilters,
                        onAddPlant = onAddPlant,
                    )
                    Spacer(modifier = Modifier.height(LeaflySpacing.sm))
                }

                item {
                    SearchAndFilters(
                        searchText = uiState.searchText,
                        selectedStatusFilter = uiState.selectedStatusFilter,
                        hasActiveFilters = uiState.hasActiveFilters,
                        onSearchTextChange = onSearchTextChange,
                        onStatusFilterChange = onStatusFilterChange,
                        onClearFilters = onClearFilters,
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

                    uiState.filteredPlants.isEmpty() && uiState.hasActiveFilters -> item {
                        EmptySearchResultsCard(onClearFilters = onClearFilters)
                    }

                    uiState.filteredPlants.isEmpty() -> item {
                        EmptyCard(onAddPlant = onAddPlant)
                    }

                    else -> items(items = uiState.filteredPlants, key = { it.id }) { plant ->
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

// ---------------------------------------------------------------------------
// Búsqueda y filtros
// ---------------------------------------------------------------------------

@Composable
private fun SearchAndFilters(
    searchText: String,
    selectedStatusFilter: WateringStatus?,
    hasActiveFilters: Boolean,
    onSearchTextChange: (String) -> Unit,
    onStatusFilterChange: (WateringStatus) -> Unit,
    onClearFilters: () -> Unit,
) {
    val textState = remember(searchText) { mutableStateOf(searchText) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(LeaflySpacing.md),
    ) {
        // Campo de búsqueda
        BasicTextField(
            value = textState.value,
            onValueChange = { newText ->
                textState.value = newText
                onSearchTextChange(newText)
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 54.dp)
                .padding(horizontal = LeaflySpacing.md)
                .background(LeaflyColors.FieldSurface, RoundedCornerShape(LeaflyRadius.field)),
        )

        Spacer(modifier = Modifier.height(LeaflySpacing.sm))

        // Chips de filtro por estado
        Text(
            text = stringResource(R.string.plant_filter_by_status),
            style = MaterialTheme.typography.labelSmall,
            color = LeaflyColors.TextTertiary,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(LeaflySpacing.sm),
        ) {
            WateringStatus.values().forEach { status ->
                val isSelected = selectedStatusFilter == status
                LeaflyChip(
                    title = stringResource(status.labelRes),
                    icon = status.statusIcon,
                    selected = isSelected,
                    onClick = { onStatusFilterChange(status) },
                )
            }
        }

        if (hasActiveFilters) {
            Spacer(modifier = Modifier.height(LeaflySpacing.sm))
            TextButton(
                onClick = onClearFilters,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = LeaflyColors.Secondary,
                ),
            ) {
                Text(
                    text = LeaflyCopy.Empty.SearchResults.button,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@Composable
private fun EmptySearchResultsCard(onClearFilters: () -> Unit) {
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
                    imageVector = Icons.Default.Clear,
                    contentDescription = null,
                    tint = LeaflyColors.Secondary,
                    modifier = Modifier.size(24.dp),
                )
            }

            Text(
                text = LeaflyCopy.Empty.SearchResults.title,
                style = MaterialTheme.typography.titleMedium,
                color = LeaflyColors.TextPrimary,
            )

            Text(
                text = LeaflyCopy.Empty.SearchResults.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = LeaflyColors.TextSecondary,
                textAlign = TextAlign.Center,
            )

            LeaflyPrimaryButton(
                text = LeaflyCopy.Empty.SearchResults.button,
                onClick = onClearFilters,
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Cabecera
// ---------------------------------------------------------------------------

@Composable
private fun Header(
    plantsCount: Int,
    hasFilters: Boolean = false,
    onAddPlant: () -> Unit,
) {
    val subtitle = when {
        hasFilters -> stringResource(R.string.plants_search_results_count, plantsCount)
        plantsCount == 0 -> stringResource(R.string.plants_subtitle_zero)
        plantsCount == 1 -> stringResource(R.string.plants_subtitle_one)
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
            onSearchTextChange = {},
            onStatusFilterChange = {},
            onClearFilters = {},
        )
    }
}