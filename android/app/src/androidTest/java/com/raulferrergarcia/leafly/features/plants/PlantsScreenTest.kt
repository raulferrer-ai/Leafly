package com.raulferrergarcia.leafly.features.plants

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.raulferrergarcia.leafly.designsystem.LeaflyTheme
import com.raulferrergarcia.leafly.domain.model.Plant
import com.raulferrergarcia.leafly.domain.repository.PlantNotFoundException
import com.raulferrergarcia.leafly.domain.repository.PlantRepository
import com.raulferrergarcia.leafly.domain.scheduling.WateringReminderScheduling
import com.raulferrergarcia.leafly.domain.usecase.AddPlantUseCase
import com.raulferrergarcia.leafly.domain.usecase.CancelWateringReminderUseCase
import com.raulferrergarcia.leafly.domain.usecase.MarkPlantWateredUseCase
import com.raulferrergarcia.leafly.domain.usecase.ObservePlantsUseCase
import com.raulferrergarcia.leafly.domain.usecase.SyncWateringRemindersUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.junit.Rule
import org.junit.Test

class PlantsScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val dayMillis = 24L * 60 * 60 * 1000

    @Test
    fun showsTheThreeWateringStatuses() {
        val now = System.currentTimeMillis()
        val repository = FakePlantRepository(
            listOf(
                Plant(name = "Monstera", lastWateredAt = now),
                Plant(name = "Ficus", lastWateredAt = now - 6 * dayMillis),
                Plant(name = "La de la abuela", lastWateredAt = now - 10 * dayMillis),
            ),
        )

        setPlantsContent(repository)

        composeRule.onNodeWithText("Monstera").assertIsDisplayed()
        composeRule.onNodeWithText("Ficus").assertIsDisplayed()
        composeRule.onNodeWithText("La de la abuela").assertIsDisplayed()

        composeRule.onNodeWithText("Al día").assertIsDisplayed()
        composeRule.onNodeWithText("Riega pronto").assertIsDisplayed()
        composeRule.onNodeWithText("Necesita agua").assertIsDisplayed()
    }

    @Test
    fun tappingAPlantOpensItsDetail() {
        val repository = FakePlantRepository(
            listOf(Plant(name = "Monstera")),
        )

        setPlantsContent(repository)

        composeRule.onNodeWithText("Monstera").performClick()

        composeRule.onNodeWithText("TU PLANTA").assertIsDisplayed()
        composeRule.onNodeWithText("Añadida hoy").assertIsDisplayed()
        composeRule.onNodeWithText("Ya la regué").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Volver a mis plantas").assertIsDisplayed()
    }

    @Test
    fun markingAsWateredUpdatesDetailAndList() {
        val repository = FakePlantRepository(
            listOf(Plant(name = "Monstera")),
        )

        setPlantsContent(repository)

        composeRule.onNodeWithText("Monstera").performClick()
        composeRule.onNodeWithText("Ya la regué").performClick()

        composeRule.onNodeWithText("Regada hoy").assertIsDisplayed()
        composeRule.onNodeWithText("Al día").assertIsDisplayed()

        composeRule.onNodeWithContentDescription("Volver a mis plantas").performClick()

        composeRule.onNodeWithText("Mis plantas").assertIsDisplayed()
        composeRule.onNodeWithText("Regada hoy").assertIsDisplayed()
        composeRule.onNodeWithText("Al día").assertIsDisplayed()
    }

    @Test
    fun addButtonOpensTheAddPlantScreen() {
        val repository = FakePlantRepository(
            listOf(Plant(name = "Monstera")),
        )

        setPlantsContent(repository)

        composeRule.onNodeWithContentDescription("Añadir una planta").performClick()

        composeRule.onNodeWithText("NUEVA PLANTA").assertIsDisplayed()
        composeRule.onNodeWithText("Añade una planta").assertIsDisplayed()
    }

    @Test
    fun addPlantScreenOffersAnOptionalPhoto() {
        val repository = FakePlantRepository(
            listOf(Plant(name = "Monstera")),
        )

        setPlantsContent(repository)

        composeRule.onNodeWithContentDescription("Añadir una planta").performClick()

        composeRule.onNodeWithText("La foto de tu planta").assertIsDisplayed()
        composeRule.onNodeWithText("Todavía sin foto").assertIsDisplayed()
        composeRule.onNodeWithText("Elegir de la galería")
            .performScrollTo()
            .assertIsDisplayed()
    }

    private fun setPlantsContent(repository: FakePlantRepository) {
        val reminders = FakeWateringReminderScheduling()
        composeRule.setContent {
            LeaflyTheme {
                PlantsScreen(
                    observePlants = ObservePlantsUseCase(repository),
                    addPlant = AddPlantUseCase(repository),
                    markPlantWatered = MarkPlantWateredUseCase(repository),
                    syncWateringReminders = SyncWateringRemindersUseCase(reminders),
                    cancelWateringReminder = CancelWateringReminderUseCase(reminders),
                )
            }
        }
    }
}

private class FakeWateringReminderScheduling : WateringReminderScheduling {

    override suspend fun sync(plants: List<Plant>) = Unit

    override suspend fun cancelReminder(plantId: String) = Unit
}

private class FakePlantRepository(
    initial: List<Plant>,
) : PlantRepository {

    private val plantsFlow = MutableStateFlow(initial)

    override fun observePlants(): Flow<List<Plant>> = plantsFlow

    override suspend fun add(plant: Plant) {
        plantsFlow.update { current -> current + plant }
    }

    override suspend fun update(plant: Plant) {
        plantsFlow.update { current ->
            val index = current.indexOfFirst { it.id == plant.id }
            if (index < 0) {
                throw PlantNotFoundException(plant.id)
            }
            current.toMutableList().apply { this[index] = plant }
        }
    }
}
