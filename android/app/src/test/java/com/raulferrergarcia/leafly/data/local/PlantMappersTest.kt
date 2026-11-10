package com.raulferrergarcia.leafly.data.local

import com.raulferrergarcia.leafly.domain.model.Plant
import com.raulferrergarcia.leafly.domain.model.PlantType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PlantMappersTest {

    @Test
    fun `entity maps to domain keeping its type`() {
        val entity = PlantEntity(
            id = "planta-1",
            name = "Monstera",
            typeKey = "monstera",
            createdAt = 1_700_000_000_000,
        )

        val plant = entity.toDomain()

        assertEquals("planta-1", plant.id)
        assertEquals("Monstera", plant.name)
        assertEquals(PlantType.MONSTERA, plant.type)
        assertEquals(1_700_000_000_000, plant.createdAt)
    }

    @Test
    fun `entity without type maps to a plant without type`() {
        val entity = PlantEntity(
            id = "planta-2",
            name = "La de la abuela",
            typeKey = null,
            createdAt = 10,
        )

        assertNull(entity.toDomain().type)
    }

    @Test
    fun `unknown stored type is ignored instead of crashing`() {
        val entity = PlantEntity(
            id = "planta-3",
            name = "Misteriosa",
            typeKey = "tipo-que-ya-no-existe",
            createdAt = 20,
        )

        assertNull(entity.toDomain().type)
    }

    @Test
    fun `domain plant maps to entity storing the type key`() {
        val plant = Plant(
            id = "planta-4",
            name = "Helecho",
            type = PlantType.FERN,
            createdAt = 30,
        )

        val entity = plant.toEntity()

        assertEquals("planta-4", entity.id)
        assertEquals("Helecho", entity.name)
        assertEquals("fern", entity.typeKey)
        assertEquals(30, entity.createdAt)
    }

    @Test
    fun `round trip keeps every field`() {
        val plant = Plant(name = "Orquídea", type = PlantType.ORCHID, createdAt = 42)

        assertEquals(plant, plant.toEntity().toDomain())
    }
}
