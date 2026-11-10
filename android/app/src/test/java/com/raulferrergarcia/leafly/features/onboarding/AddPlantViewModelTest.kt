package com.raulferrergarcia.leafly.features.onboarding

import app.cash.turbine.test
import com.raulferrergarcia.leafly.domain.model.Plant
import com.raulferrergarcia.leafly.domain.model.PlantType
import com.raulferrergarcia.leafly.domain.repository.PlantRepository
import com.raulferrergarcia.leafly.domain.usecase.AddPlantUseCase
import java.io.IOException
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
class AddPlantViewModelTest {

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
        repository: FakePlantRepository = FakePlantRepository(),
    ): AddPlantViewModel = AddPlantViewModel(AddPlantUseCase(repository))

    @Test
    fun `saving without a name shows a clear error and does not persist`() = runTest(dispatcher) {
        val repository = FakePlantRepository()
        val viewModel = createViewModel(repository)

        viewModel.save()

        assertEquals(AddPlantError.BlankName, viewModel.uiState.value.nameError)
        assertFalse(viewModel.uiState.value.isSaving)
        assertTrue(repository.plants.isEmpty())
    }

    @Test
    fun `a name with only spaces is not accepted`() = runTest(dispatcher) {
        val repository = FakePlantRepository()
        val viewModel = createViewModel(repository)

        viewModel.onNameChange("   \n  ")
        viewModel.save()

        assertEquals(AddPlantError.BlankName, viewModel.uiState.value.nameError)
        assertTrue(repository.plants.isEmpty())
    }

    @Test
    fun `saving trims the name and keeps the selected type`() = runTest(dispatcher) {
        val repository = FakePlantRepository()
        val viewModel = createViewModel(repository)

        viewModel.onNameChange("  Mi monstera  ")
        viewModel.toggleType(PlantType.MONSTERA)
        viewModel.save()

        val state = viewModel.uiState.value
        assertNull(state.nameError)
        assertTrue(state.isSaved)
        assertFalse(state.isSaving)

        val saved = repository.plants.single()
        assertEquals("Mi monstera", saved.name)
        assertEquals(PlantType.MONSTERA, saved.type)
    }

    @Test
    fun `a plant without a type can be saved`() = runTest(dispatcher) {
        val repository = FakePlantRepository()
        val viewModel = createViewModel(repository)

        viewModel.onNameChange("La de la abuela")
        viewModel.save()

        assertTrue(viewModel.uiState.value.isSaved)
        assertNull(repository.plants.single().type)
    }

    @Test
    fun `tapping the same type twice deselects it`() = runTest(dispatcher) {
        val viewModel = createViewModel()

        viewModel.toggleType(PlantType.FICUS)
        assertEquals(PlantType.FICUS, viewModel.uiState.value.selectedType)

        viewModel.toggleType(PlantType.FICUS)
        assertNull(viewModel.uiState.value.selectedType)
    }

    @Test
    fun `choosing no type clears the selection`() = runTest(dispatcher) {
        val viewModel = createViewModel()

        viewModel.toggleType(PlantType.ORCHID)
        viewModel.clearType()

        assertNull(viewModel.uiState.value.selectedType)
    }

    @Test
    fun `typing clears the name error`() = runTest(dispatcher) {
        val viewModel = createViewModel()

        viewModel.save()
        assertEquals(AddPlantError.BlankName, viewModel.uiState.value.nameError)

        viewModel.onNameChange("Orquídea")

        assertNull(viewModel.uiState.value.nameError)
    }

    @Test
    fun `a repository failure shows a calm message`() = runTest(dispatcher) {
        val repository = FakePlantRepository().apply { failOnAdd = true }
        val viewModel = createViewModel(repository)

        viewModel.onNameChange("Ficus")
        viewModel.save()

        val state = viewModel.uiState.value
        assertFalse(state.isSaved)
        assertFalse(state.isSaving)
        assertEquals(AddPlantError.SaveFailed, state.saveError)
        assertTrue(repository.plants.isEmpty())
    }

    @Test
    fun `reset leaves the screen as new`() = runTest(dispatcher) {
        val viewModel = createViewModel()

        viewModel.onNameChange("Monstera")
        viewModel.toggleType(PlantType.MONSTERA)
        viewModel.reset()

        assertEquals(AddPlantUiState(), viewModel.uiState.value)
    }

    @Test
    fun `state flow emits every change`() = runTest(dispatcher) {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(AddPlantUiState(), awaitItem())

            viewModel.onNameChange("Potos")
            assertEquals("Potos", awaitItem().name)

            viewModel.toggleType(PlantType.POTHOS)
            assertEquals(PlantType.POTHOS, awaitItem().selectedType)

            cancelAndIgnoreRemainingEvents()
        }
    }
}

/**
 * Repositorio en memoria para los tests del ViewModel.
 */
private class FakePlantRepository : PlantRepository {

    private val plantsFlow = MutableStateFlow<List<Plant>>(emptyList())

    val plants: List<Plant>
        get() = plantsFlow.value

    var failOnAdd: Boolean = false

    override fun observePlants(): Flow<List<Plant>> = plantsFlow

    override suspend fun add(plant: Plant) {
        if (failOnAdd) throw IOException("No se pudo guardar")
        plantsFlow.update { current -> current + plant }
    }
}
