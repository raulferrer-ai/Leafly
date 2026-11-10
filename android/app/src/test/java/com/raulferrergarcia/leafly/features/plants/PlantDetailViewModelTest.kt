package com.raulferrergarcia.leafly.features.plants

import app.cash.turbine.test
import com.raulferrergarcia.leafly.domain.model.Plant
import com.raulferrergarcia.leafly.domain.model.PlantType
import com.raulferrergarcia.leafly.domain.repository.PlantNotFoundException
import com.raulferrergarcia.leafly.domain.repository.PlantRepository
import com.raulferrergarcia.leafly.domain.scheduling.WateringReminderScheduling
import com.raulferrergarcia.leafly.domain.usecase.CancelWateringReminderUseCase
import com.raulferrergarcia.leafly.domain.usecase.MarkPlantWateredUseCase
import java.io.IOException
import kotlinx.coroutines.CompletableDeferred
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlantDetailViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val now = 1_800_000_000_000L

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        plant: Plant,
        repository: FakeDetailPlantRepository = FakeDetailPlantRepository(listOf(plant)),
        clock: () -> Long = { now },
        reminders: FakeDetailWateringReminders = FakeDetailWateringReminders(),
    ): PlantDetailViewModel = PlantDetailViewModel(
        plant = plant,
        markPlantWatered = MarkPlantWateredUseCase(repository, clock),
        cancelWateringReminder = CancelWateringReminderUseCase(reminders),
    )

    @Test
    fun `shows the plant without saving or errors`() {
        val plant = Plant(name = "Ficus", type = PlantType.FICUS)
        val viewModel = createViewModel(plant)

        val state = viewModel.uiState.value

        assertEquals(plant, state.plant)
        assertFalse(state.isSaving)
        assertNull(state.saveError)
    }

    @Test
    fun `marking as watered updates the plant date`() = runTest(dispatcher) {
        val plant = Plant(
            name = "Monstera",
            type = PlantType.MONSTERA,
            lastWateredAt = now - 10L * DAY_MILLIS,
        )
        val viewModel = createViewModel(plant)

        viewModel.markAsWatered()

        val state = viewModel.uiState.value
        assertEquals(now, state.plant.lastWateredAt)
        assertFalse(state.isSaving)
        assertNull(state.saveError)
    }

    @Test
    fun `marking as watered persists the change`() = runTest(dispatcher) {
        val plant = Plant(name = "Potos", type = PlantType.POTHOS)
        val repository = FakeDetailPlantRepository(listOf(plant))
        val viewModel = createViewModel(plant, repository)

        viewModel.markAsWatered()

        val saved = repository.plantsFlow.value.single()
        assertEquals(plant.id, saved.id)
        assertEquals(now, saved.lastWateredAt)
    }

    @Test
    fun `marking as watered cancels the plant reminder`() = runTest(dispatcher) {
        val plant = Plant(name = "Monstera", lastWateredAt = now - 8 * DAY_MILLIS)
        val reminders = FakeDetailWateringReminders()
        val viewModel = createViewModel(plant, reminders = reminders)

        viewModel.markAsWatered()

        assertEquals(listOf(plant.id), reminders.cancelledPlantIds)
    }

    @Test
    fun `a save failure keeps the reminder`() = runTest(dispatcher) {
        val plant = Plant(name = "Monstera", lastWateredAt = now - 8 * DAY_MILLIS)
        val repository = FakeDetailPlantRepository(listOf(plant)).apply { failOnUpdate = true }
        val reminders = FakeDetailWateringReminders()
        val viewModel = createViewModel(plant, repository, reminders = reminders)

        viewModel.markAsWatered()

        assertTrue(reminders.cancelledPlantIds.isEmpty())
    }

    @Test
    fun `a save failure keeps the plant and shows a calm error`() = runTest(dispatcher) {
        val plant = Plant(name = "Orquídea", lastWateredAt = now - 8L * DAY_MILLIS)
        val repository = FakeDetailPlantRepository(listOf(plant)).apply { failOnUpdate = true }
        val viewModel = createViewModel(plant, repository)

        viewModel.markAsWatered()

        val state = viewModel.uiState.value
        assertEquals(plant, state.plant)
        assertEquals(PlantDetailSaveError.SaveFailed, state.saveError)
        assertFalse(state.isSaving)
    }

    @Test
    fun `a second tap while saving is ignored`() = runTest(dispatcher) {
        val plant = Plant(name = "Monstera")
        val repository = FakeDetailPlantRepository(listOf(plant)).apply { gateUpdate = CompletableDeferred() }
        val viewModel = createViewModel(plant, repository)

        viewModel.markAsWatered()
        viewModel.markAsWatered()

        assertEquals(1, repository.updateCalls)

        repository.gateUpdate?.complete(Unit)
    }

    @Test
    fun `an unknown plant surfaces the save error`() = runTest(dispatcher) {
        val plant = Plant(name = "Fantasma")
        val viewModel = createViewModel(plant, FakeDetailPlantRepository())

        viewModel.markAsWatered()

        assertEquals(PlantDetailSaveError.SaveFailed, viewModel.uiState.value.saveError)
    }

    @Test
    fun `state flow emits saving and saved states`() = runTest(dispatcher) {
        val plant = Plant(name = "Ficus")
        val repository = FakeDetailPlantRepository(listOf(plant)).apply { gateUpdate = CompletableDeferred() }
        val viewModel = createViewModel(plant, repository)

        viewModel.uiState.test {
            assertEquals(plant, awaitItem().plant)

            viewModel.markAsWatered()
            assertTrue(awaitItem().isSaving)

            repository.gateUpdate?.complete(Unit)

            val saved = awaitItem()
            assertFalse(saved.isSaving)
            assertEquals(now, saved.plant.lastWateredAt)

            cancelAndIgnoreRemainingEvents()
        }
    }

    private companion object {
        const val DAY_MILLIS = 24L * 60 * 60 * 1000
    }
}

private class FakeDetailWateringReminders : WateringReminderScheduling {

    val syncedPlants = mutableListOf<List<Plant>>()
    val cancelledPlantIds = mutableListOf<String>()

    override suspend fun sync(plants: List<Plant>) {
        syncedPlants.add(plants)
    }

    override suspend fun cancelReminder(plantId: String) {
        cancelledPlantIds.add(plantId)
    }
}

private class FakeDetailPlantRepository(
    initial: List<Plant> = emptyList(),
) : PlantRepository {

    val plantsFlow = MutableStateFlow(initial)

    var failOnUpdate: Boolean = false
    var gateUpdate: CompletableDeferred<Unit>? = null
    var updateCalls: Int = 0
        private set

    override fun observePlants(): Flow<List<Plant>> = plantsFlow

    override suspend fun add(plant: Plant) {
        plantsFlow.update { current -> current + plant }
    }

    override suspend fun update(plant: Plant) {
        updateCalls++
        gateUpdate?.await()

        if (failOnUpdate) throw IOException("No se pudo actualizar")

        plantsFlow.update { current ->
            val index = current.indexOfFirst { it.id == plant.id }
            if (index < 0) {
                throw PlantNotFoundException(plant.id)
            }
            current.toMutableList().apply { this[index] = plant }
        }
    }
}
