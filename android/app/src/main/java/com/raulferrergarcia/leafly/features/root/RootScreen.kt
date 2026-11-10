package com.raulferrergarcia.leafly.features.root

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.raulferrergarcia.leafly.designsystem.LeaflyScreen
import com.raulferrergarcia.leafly.domain.repository.PlantRepository
import com.raulferrergarcia.leafly.domain.usecase.AddPlantUseCase
import com.raulferrergarcia.leafly.domain.usecase.CancelWateringReminderUseCase
import com.raulferrergarcia.leafly.domain.usecase.MarkPlantWateredUseCase
import com.raulferrergarcia.leafly.domain.usecase.ObservePlantsUseCase
import com.raulferrergarcia.leafly.domain.usecase.SyncWateringRemindersUseCase
import com.raulferrergarcia.leafly.features.onboarding.OnboardingFlowScreen
import com.raulferrergarcia.leafly.features.plants.PlantsScreen

/**
 * Punto de entrada de la UI. Escucha las plantas guardadas y alterna entre el
 * onboarding y el jardín con una transición suave.
 */
@Composable
fun LeaflyRoot(
    observePlants: ObservePlantsUseCase,
    addPlant: AddPlantUseCase,
    markPlantWatered: MarkPlantWateredUseCase,
    syncWateringReminders: SyncWateringRemindersUseCase,
    cancelWateringReminder: CancelWateringReminderUseCase,
    plantRepository: PlantRepository,
    modifier: Modifier = Modifier,
) {
    val rootViewModel: RootViewModel = viewModel(
        factory = RootViewModel.factory(observePlants),
    )
    val uiState by rootViewModel.uiState.collectAsStateWithLifecycle()

    Crossfade(
        targetState = uiState,
        modifier = modifier,
        animationSpec = tween(durationMillis = 450),
        label = "rootContent",
    ) { state ->
        when (state) {
            RootUiState.Loading -> LeaflyScreen {}
            RootUiState.Onboarding -> OnboardingFlowScreen(addPlant = addPlant)
            RootUiState.Plants -> PlantsScreen(
                observePlants = observePlants,
                addPlant = addPlant,
                markPlantWatered = markPlantWatered,
                syncWateringReminders = syncWateringReminders,
                cancelWateringReminder = cancelWateringReminder,
                plantRepository = plantRepository,
            )
        }
    }
}
