package com.raulferrergarcia.leafly.features.plants

import app.cash.turbine.test
import com.raulferrergarcia.leafly.domain.model.Plant
import com.raulferrergarcia.leafly.domain.model.PlantPhoto
import com.raulferrergarcia.leafly.domain.repository.PlantNotFoundException
import com.raulferrergarcia.leafly.domain.repository.PlantRepository
import com.raulferrergarcia.leafly.domain.scheduling.WateringReminderScheduling
import com.raulferrergarcia.leafly.domain.usecase.ObservePlantsUseCase
import com.raulferrergarcia.leafly.domain.usecase.SyncWateringRemindersUseCase
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

    private fun createViewModel(
        repository: FakePlantRepository,
        reminders: FakeWateringReminderScheduling = FakeWateringReminderScheduling(),
    ): PlantsViewModel = PlantsViewModel(
        observePlants = ObservePlantsUseCase(repository),
        syncWateringReminders = SyncWateringRemindersUseCase(reminders),
    )

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
    fun `synchronizes watering reminders with the plants`() = runTest(dispatcher) {
        val plants = listOf(Plant(name = "Monstera"), Plant(name = "Ficus"))
        val reminders = FakeWateringReminderScheduling()
        val viewModel = createViewModel(
            repository = FakePlantRepository(initial = plants),
            reminders = reminders,
        )

        viewModel.uiState.test {
            var state = awaitItem()
            while (state.isLoading) {
                state = awaitItem()
            }

            assertEquals(plants, reminders.syncedPlants.last())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a reminder failure does not break the list`() = runTest(dispatcher) {
        val reminders = FakeWateringReminderScheduling().apply { failOnSync = true }
        val viewModel = createViewModel(
            repository = FakePlantRepository(initial = listOf(Plant(name = "Ficus"))),
            reminders = reminders,
        )

        viewModel.uiState.test {
            var state = awaitItem()
            while (state.isLoading) {
                state = awaitItem()
            }

            assertEquals("Ficus", state.plants.single().name)
            assertNull(state.loadError)
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
private class FakeWateringReminderScheduling : WateringReminderScheduling {

    val syncedPlants = mutableListOf<List<Plant>>()
    val cancelledPlantIds = mutableListOf<String>()
    var failOnSync: Boolean = false

    override suspend fun sync(plants: List<Plant>) {
        if (failOnSync) throw IOException("No se pudieron programar los recordatorios")
        syncedPlants.add(plants)
    }

    override suspend fun cancelReminder(plantId: String) {
        cancelledPlantIds.add(plantId)
    }
}

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

    override suspend fun update(plant: Plant) {
        plantsFlow.update { current ->
            val index = current.indexOfFirst { it.id == plant.id }
            if (index < 0) {
                throw PlantNotFoundException(plant.id)
            }
            current.toMutableList().apply { this[index] = plant }
        }
    }

    override fun observePhotos(plantId: String): Flow<List<PlantPhoto>> =
        flow<List<PlantPhoto>> { emptyList<PlantPhoto>() }

    override suspend fun addPhoto(photo: PlantPhoto) {}
}
