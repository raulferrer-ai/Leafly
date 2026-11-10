package com.raulferrergarcia.leafly.features.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.raulferrergarcia.leafly.domain.usecase.AddPlantUseCase

private enum class OnboardingStep {
    Welcome,
    AddPlant,
}

/**
 * Flujo de bienvenida + primera planta.
 *
 * Al guardar, el repositorio emite la nueva planta y el routing raíz cambia
 * automáticamente al jardín. No hace falta navegar a mano.
 */
@Composable
fun OnboardingFlowScreen(
    addPlant: AddPlantUseCase,
    modifier: Modifier = Modifier,
) {
    var step by rememberSaveable { mutableStateOf(OnboardingStep.Welcome) }

    val addPlantViewModel: AddPlantViewModel = viewModel(
        factory = AddPlantViewModel.factory(addPlant),
    )

    AnimatedContent(
        targetState = step,
        modifier = modifier,
        transitionSpec = {
            val movingForward = targetState == OnboardingStep.AddPlant
            val enter = fadeIn(animationSpec = tween(durationMillis = 350)) +
                slideInHorizontally(animationSpec = tween(durationMillis = 350)) { width ->
                    if (movingForward) width / 4 else -width / 4
                }
            val exit = fadeOut(animationSpec = tween(durationMillis = 250)) +
                slideOutHorizontally(animationSpec = tween(durationMillis = 350)) { width ->
                    if (movingForward) -width / 8 else width / 8
                }
            enter togetherWith exit
        },
        label = "onboardingStep",
    ) { currentStep ->
        when (currentStep) {
            OnboardingStep.Welcome -> WelcomeScreen(
                onAddPlant = {
                    addPlantViewModel.reset()
                    step = OnboardingStep.AddPlant
                },
            )

            OnboardingStep.AddPlant -> AddPlantScreen(
                viewModel = addPlantViewModel,
                onCancel = {
                    addPlantViewModel.reset()
                    step = OnboardingStep.Welcome
                },
            )
        }
    }
}
