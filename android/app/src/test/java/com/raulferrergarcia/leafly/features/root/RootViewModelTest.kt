package com.raulferrergarcia.leafly.features.root

import app.cash.turbine.test
import com.raulferrergarcia.leafly.domain.model.Plant
import com.raulferrergarcia.leafly.domain.model.PlantType
import com.raulferrergarcia.leafly.domain.repository.PlantRepository
import com.raulferrergarcia.leafly.domain.usecase.ObservePlantsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RootViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `without plants the app shows onboarding`() = runTest(dispatcher) {
        val viewModel = RootViewModel(ObservePlantsUseCase(FakePlantRepository()))

        viewModel.uiState.test {
            var state = awaitItem()
            while (state == RootUiState.Loading) {
                state = awaitItem()
            }
            assertEquals(RootUiState.Onboarding, state)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `saving the first plant leads to the garden`() = runTest(dispatcher) {
        val repository = FakePlantRepository()
        val viewModel = RootViewModel(ObservePlantsUseCase(repository))

        viewModel.uiState.test {
            var state = awaitItem()
            while (state == RootUiState.Loading) {
                state = awaitItem()
            }
            assertEquals(RootUiState.Onboarding, state)

            repository.add(Plant(name = "Monstera", type = PlantType.MONSTERA))

            val gardenState = awaitItem()
            assertTrue(gardenState is RootUiState.Plants)
            val plants = (gardenState as RootUiState.Plants).plants
            assertEquals(listOf("Monstera"), plants.map { it.name })

            cancelAndIgnoreRemainingEvents()
        }
    }
}

private class FakePlantRepository : PlantRepository {

    private val plantsFlow = MutableStateFlow<List<Plant>>(emptyList())

    override fun observePlants(): Flow<List<Plant>> = plantsFlow

    override suspend fun add(plant: Plant) {
        plantsFlow.update { current -> current + plant }
    }
}
