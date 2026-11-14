package com.raulferrergarcia.leafly.features.onboarding

import app.cash.turbine.test
import com.raulferrergarcia.leafly.domain.model.Plant
import com.raulferrergarcia.leafly.domain.model.PlantPhoto
import com.raulferrergarcia.leafly.domain.model.PlantType
import com.raulferrergarcia.leafly.domain.repository.PlantNotFoundException
import com.raulferrergarcia.leafly.domain.repository.PlantRepository
import com.raulferrergarcia.leafly.domain.usecase.AddPlantUseCase
import com.raulferrergarcia.leafly.domain.usecase.EditPlantUseCase
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
    fun `a plant can be saved without a photo`() = runTest(dispatcher) {
        val repository = FakePlantRepository()
        val viewModel = createViewModel(repository)

        viewModel.onNameChange("Monstera")
        viewModel.save()

        assertTrue(viewModel.uiState.value.isSaved)
        assertNull(repository.plants.single().photoData)
    }

    @Test
    fun `saving keeps the chosen photo`() = runTest(dispatcher) {
        val repository = FakePlantRepository()
        val viewModel = createViewModel(repository)
        val photo = byteArrayOf(1, 2, 3)

        viewModel.onNameChange("Monstera")
        viewModel.applyPhoto(photo)
        viewModel.save()

        val saved = repository.plants.single()
        assertTrue(photo.contentEquals(saved.photoData))
    }

    @Test
    fun `removing the photo saves the plant without image`() = runTest(dispatcher) {
        val repository = FakePlantRepository()
        val viewModel = createViewModel(repository)

        viewModel.onNameChange("Ficus")
        viewModel.applyPhoto(byteArrayOf(9, 9))
        viewModel.removePhoto()
        viewModel.save()

        assertTrue(viewModel.uiState.value.isSaved)
        assertNull(repository.plants.single().photoData)
    }

    @Test
    fun `applying a photo clears the processing state`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        val photo = byteArrayOf(4, 5)

        viewModel.startPhotoProcessing()
        assertTrue(viewModel.uiState.value.isProcessingPhoto)

        viewModel.applyPhoto(photo)

        val state = viewModel.uiState.value
        assertFalse(state.isProcessingPhoto)
        assertNull(state.photoMessage)
        assertTrue(photo.contentEquals(state.photoData))
    }

    @Test
    fun `a photo failure shows a calm message and stops processing`() = runTest(dispatcher) {
        val viewModel = createViewModel()

        viewModel.startPhotoProcessing()
        viewModel.failPhotoProcessing(AddPlantPhotoMessage.PhotoLoadFailed)

        val state = viewModel.uiState.value
        assertFalse(state.isProcessingPhoto)
        assertEquals(AddPlantPhotoMessage.PhotoLoadFailed, state.photoMessage)
    }

    @Test
    fun `a denied camera permission only shows a message and still allows saving`() =
        runTest(dispatcher) {
            val repository = FakePlantRepository()
            val viewModel = createViewModel(repository)

            viewModel.onNameChange("Ficus")
            viewModel.showPhotoMessage(AddPlantPhotoMessage.CameraPermissionDenied)

            assertEquals(
                AddPlantPhotoMessage.CameraPermissionDenied,
                viewModel.uiState.value.photoMessage,
            )
            assertFalse(viewModel.uiState.value.isProcessingPhoto)

            viewModel.save()

            assertTrue(viewModel.uiState.value.isSaved)
            assertNull(repository.plants.single().photoData)
        }

    @Test
    fun `saving while the photo is being processed does nothing`() = runTest(dispatcher) {
        val repository = FakePlantRepository()
        val viewModel = createViewModel(repository)

        viewModel.onNameChange("Monstera")
        viewModel.startPhotoProcessing()
        viewModel.save()

        val state = viewModel.uiState.value
        assertFalse(state.isSaving)
        assertFalse(state.isSaved)
        assertTrue(repository.plants.isEmpty())
    }

    @Test
    fun `reset clears the chosen photo`() = runTest(dispatcher) {
        val viewModel = createViewModel()

        viewModel.applyPhoto(byteArrayOf(7))
        viewModel.reset()

        assertNull(viewModel.uiState.value.photoData)
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

    // MARK: - Edición

    @Test
    fun `load precaches plant data for editing`() = runTest(dispatcher) {
        val repository = FakePlantRepository()
        val viewModel = createViewModel(repository)
        val plant = Plant(
            id = "plant-123",
            name = "Monstera",
            type = PlantType.MONSTERA,
            photoData = byteArrayOf(1, 2, 3),
            lastWateredAt = System.currentTimeMillis() - 86_400_000,
        )

        viewModel.load(plant)

        val state = viewModel.uiState.value
        assertEquals("Monstera", state.name)
        assertEquals(PlantType.MONSTERA, state.selectedType)
        assertTrue(byteArrayOf(1, 2, 3).contentEquals(state.photoData))
        assertEquals(plant, state.editingPlant)
    }

    @Test
    fun `edit save updates existing plant preserving id and dates`() = runTest(dispatcher) {
        val repository = FakePlantRepository()
        val viewModel = AddPlantViewModel(
            AddPlantUseCase(repository),
            FakeEditPlantUseCase(repository),
        )
        val plant = Plant(
            id = "plant-123",
            name = "Monstera",
            type = PlantType.MONSTERA,
            createdAt = System.currentTimeMillis() - 172_800_000,
            lastWateredAt = System.currentTimeMillis() - 86_400_000,
        )
        repository.plantsFlow.update { listOf(plant) }

        viewModel.load(plant)
        viewModel.onNameChange("Monstera Editada")
        viewModel.toggleType(PlantType.FICUS)

        viewModel.save()

        val state = viewModel.uiState.value
        assertTrue(state.isSaved)
        assertFalse(state.isSaving)
        assertNull(state.nameError)
        assertNull(state.saveError)

        val updated = repository.plants.single { it.id == "plant-123" }
        assertEquals("Monstera Editada", updated.name)
        assertEquals(PlantType.FICUS, updated.type)
        // Preserva id y fechas originales
        assertEquals("plant-123", updated.id)
        assertEquals(plant.createdAt, updated.createdAt)
        assertEquals(plant.lastWateredAt, updated.lastWateredAt)
    }

    @Test
    fun `edit save without name shows error and does not persist`() = runTest(dispatcher) {
        val repository = FakePlantRepository()
        val viewModel = AddPlantViewModel(
            AddPlantUseCase(repository),
            FakeEditPlantUseCase(repository),
        )
        val plant = Plant(id = "plant-123", name = "Monstera")
        repository.plantsFlow.update { listOf(plant) }

        viewModel.load(plant)
        viewModel.onNameChange("")

        viewModel.save()

        assertEquals(AddPlantError.BlankName, viewModel.uiState.value.nameError)
        assertFalse(viewModel.uiState.value.isSaving)
        // La planta no debe haberse modificado
        assertEquals("Monstera", repository.plants.single().name)
    }

    @Test
    fun `edit save repository failure shows calm message`() = runTest(dispatcher) {
        val repository = FakePlantRepository().apply { failOnUpdate = true }
        val viewModel = AddPlantViewModel(
            AddPlantUseCase(repository),
            FakeEditPlantUseCase(repository),
        )
        val plant = Plant(id = "plant-123", name = "Monstera")
        repository.plantsFlow.update { listOf(plant) }

        viewModel.load(plant)
        viewModel.onNameChange("Monstera Editada")
        viewModel.save()

        val state = viewModel.uiState.value
        assertFalse(state.isSaved)
        assertFalse(state.isSaving)
        assertEquals(AddPlantError.SaveFailed, state.saveError)
    }

    @Test
    fun `edit removing photo saves plant without image`() = runTest(dispatcher) {
        val repository = FakePlantRepository()
        val viewModel = AddPlantViewModel(
            AddPlantUseCase(repository),
            FakeEditPlantUseCase(repository),
        )
        val plant = Plant(
            id = "plant-123",
            name = "Monstera",
            photoData = byteArrayOf(9, 9),
        )
        repository.plantsFlow.update { listOf(plant) }

        viewModel.load(plant)
        viewModel.removePhoto()
        viewModel.save()

        assertTrue(viewModel.uiState.value.isSaved)
        assertNull(viewModel.uiState.value.photoData)
        val updated = repository.plants.single { it.id == "plant-123" }
        assertNull(updated.photoData)
    }
}

/**
 * Repositorio en memoria para los tests del ViewModel.
 */
private class FakePlantRepository : PlantRepository {

    val plantsFlow = MutableStateFlow<List<Plant>>(emptyList())

    val plants: List<Plant>
        get() = plantsFlow.value

    var failOnAdd: Boolean = false
    var failOnUpdate: Boolean = false

    override fun observePlants(): Flow<List<Plant>> = plantsFlow

    override suspend fun add(plant: Plant) {
        if (failOnAdd) throw IOException("No se pudo guardar")
        plantsFlow.update { current -> current + plant }
    }

    override suspend fun update(plant: Plant) {
        if (failOnUpdate) throw IOException("No se pudo actualizar")
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

/**
 * Doble de prueba para EditPlantUseCase.
 */
private class FakeEditPlantUseCase(
    private val repository: PlantRepository,
) : EditPlantUseCase(repository)
