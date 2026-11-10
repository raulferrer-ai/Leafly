package com.raulferrergarcia.leafly.domain.usecase

import com.raulferrergarcia.leafly.domain.model.Plant
import com.raulferrergarcia.leafly.domain.model.PlantType
import com.raulferrergarcia.leafly.domain.repository.PlantNotFoundException
import com.raulferrergarcia.leafly.domain.repository.PlantRepository
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MarkPlantWateredUseCaseTest {

    private val now = 1_800_000_000_000L

    @Test
    fun `sets last watered at to now`() = runTest {
        val plant = Plant(
            name = "Monstera",
            type = PlantType.MONSTERA,
            lastWateredAt = now - 10L * DAY_MILLIS,
        )
        val repository = FakePlantRepository(listOf(plant))
        val useCase = MarkPlantWateredUseCase(repository) { now }

        val watered = useCase(plant)

        assertEquals(now, watered.lastWateredAt)
    }

    @Test
    fun `keeps the rest of the plant unchanged`() = runTest {
        val createdAt = now - 30L * DAY_MILLIS
        val plant = Plant(
            name = "Potos",
            type = PlantType.POTHOS,
            createdAt = createdAt,
        )
        val repository = FakePlantRepository(listOf(plant))
        val useCase = MarkPlantWateredUseCase(repository) { now }

        val watered = useCase(plant)

        assertEquals(plant.id, watered.id)
        assertEquals("Potos", watered.name)
        assertEquals(PlantType.POTHOS, watered.type)
        assertEquals(createdAt, watered.createdAt)
    }

    @Test
    fun `persists the watered plant in the repository`() = runTest {
        val plant = Plant(name = "Ficus")
        val repository = FakePlantRepository(listOf(plant))
        val useCase = MarkPlantWateredUseCase(repository) { now }

        useCase(plant)

        val saved = repository.plantsFlow.value.single()
        assertEquals(now, saved.lastWateredAt)
    }

    @Test
    fun `a repository failure is propagated`() = runTest {
        val plant = Plant(name = "Orquídea")
        val repository = FakePlantRepository(listOf(plant)).apply { failOnUpdate = true }
        val useCase = MarkPlantWateredUseCase(repository) { now }

        var failure: Throwable? = null
        try {
            useCase(plant)
        } catch (error: Throwable) {
            failure = error
        }

        assertTrue(failure is IOException)
        assertEquals(plant, repository.plantsFlow.value.single())
    }

    @Test
    fun `an unknown plant throws plant not found`() = runTest {
        val repository = FakePlantRepository()
        val useCase = MarkPlantWateredUseCase(repository) { now }

        var failure: Throwable? = null
        try {
            useCase(Plant(name = "Fantasma"))
        } catch (error: Throwable) {
            failure = error
        }

        assertTrue(failure is PlantNotFoundException)
    }

    private companion object {
        const val DAY_MILLIS = 24L * 60 * 60 * 1000
    }
}

private class FakePlantRepository(
    initial: List<Plant> = emptyList(),
) : PlantRepository {

    val plantsFlow = MutableStateFlow(initial)

    var failOnUpdate: Boolean = false

    override fun observePlants(): Flow<List<Plant>> = plantsFlow

    override suspend fun add(plant: Plant) {
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
}
