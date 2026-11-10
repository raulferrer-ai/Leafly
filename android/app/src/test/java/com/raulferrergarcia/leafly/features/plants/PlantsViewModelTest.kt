package com.raulferrergarcia.leafly.features.plants

import app.cash.turbine.test
import com.raulferrergarcia.leafly.domain.model.Plant
import com.raulferrergarcia.leafly.domain.repository.PlantRepository
import com.raulferrergarcia.leafly.domain.usecase.ObservePlantsUseCase
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlantsViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(repository: FakePlantRepository): PlantsViewModel =
        PlantsViewModel(ObservePlantsUseCase(repository))

    @Test
    fun `shows the plants from the repository`() = runTest(dispatcher) {
        val repository = FakePlantRepository(
            initial = listOf(Plant(name = "Monstera"), Plant(name = "Ficus")),
        )
        val viewModel = createViewModel(repository)

        viewModel.uiState.test {
            var state = awaitItem()
            while (state.isLoading) {
                state = awaitItem()
            }

            assertEquals(listOf("Monstera", "Ficus"), state.plants.map { it.name })
            assertNull(state.loadError)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `reflects new plants without reloading`() = runTest(dispatcher) {
        val repository = FakePlantRepository()
        val viewModel = createViewModel(repository)

        viewModel.uiState.test {
            var state = awaitItem()
            while (state.isLoading) {
                state = awaitItem()
            }
            assertTrue(state.plants.isEmpty())

            repository.plantsFlow.value = listOf(Plant(name = "Orquídea"))

            val updated = awaitItem()
            assertEquals("Orquídea", updated.plants.single().name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a read failure shows a calm error state`() = runTest(dispatcher) {
        val repository = FakePlantRepository(failOnObserve = true)
        val viewModel = createViewModel(repository)

        viewModel.uiState.test {
            var state = awaitItem()
            while (state.isLoading) {
                state = awaitItem()
            }

            assertFalse(state.isLoading)
            assertEquals(PlantsLoadError.LoadFailed, state.loadError)
            assertTrue(state.plants.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `retry recovers after a failure`() = runTest(dispatcher) {
        val repository = FakePlantRepository(failOnObserve = true)
        val viewModel = createViewModel(repository)

        viewModel.uiState.test {
            var state = awaitItem()
            while (state.isLoading) {
                state = awaitItem()
            }
            assertEquals(PlantsLoadError.LoadFailed, state.loadError)

            repository.failOnObserve = false
            repository.plantsFlow.value = listOf(Plant(name = "Ficus"))
            viewModel.retry()

            state = awaitItem()
            while (state.plants.isEmpty()) {
                state = awaitItem()
            }

            assertEquals("Ficus", state.plants.single().name)
            assertNull(state.loadError)
            cancelAndIgnoreRemainingEvents()
        }
    }
}

/**
 * Repositorio en memoria para los tests del ViewModel.
 */
private class FakePlantRepository(
    initial: List<Plant> = emptyList(),
    var failOnObserve: Boolean = false,
) : PlantRepository {

    val plantsFlow = MutableStateFlow(initial)

    override fun observePlants(): Flow<List<Plant>> =
        if (failOnObserve) {
            flow { throw IOException("No se pudo observar") }
        } else {
            plantsFlow
        }

    override suspend fun add(plant: Plant) {
        plantsFlow.update { current -> current + plant }
    }
}
