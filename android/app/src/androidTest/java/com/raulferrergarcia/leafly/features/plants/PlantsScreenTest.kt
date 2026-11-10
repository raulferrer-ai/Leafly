package com.raulferrergarcia.leafly.features.plants

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.raulferrergarcia.leafly.designsystem.LeaflyTheme
import com.raulferrergarcia.leafly.domain.model.Plant
import com.raulferrergarcia.leafly.domain.repository.PlantRepository
import com.raulferrergarcia.leafly.domain.usecase.AddPlantUseCase
import com.raulferrergarcia.leafly.domain.usecase.ObservePlantsUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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
        composeRule.onNodeWithText("Muy pronto").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Volver a mis plantas").assertIsDisplayed()
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

    private fun setPlantsContent(repository: FakePlantRepository) {
        composeRule.setContent {
            LeaflyTheme {
                PlantsScreen(
                    observePlants = ObservePlantsUseCase(repository),
                    addPlant = AddPlantUseCase(repository),
                )
            }
        }
    }
}

private class FakePlantRepository(
    initial: List<Plant>,
) : PlantRepository {

    private val plantsFlow = MutableStateFlow(initial)

    override fun observePlants(): Flow<List<Plant>> = plantsFlow

    override suspend fun add(plant: Plant) {
        plantsFlow.value = plantsFlow.value + plant
    }
}
