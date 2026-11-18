package com.raulferrergarcia.leafly.data.local

import com.raulferrergarcia.leafly.domain.model.Plant
import com.raulferrergarcia.leafly.domain.model.PlantType
import com.raulferrergarcia.leafly.domain.model.WateringLogEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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

    @Test
    fun `entity keeps the last watered date`() {
        val entity = PlantEntity(
            id = "planta-5",
            name = "Regada",
            typeKey = null,
            createdAt = 50,
            lastWateredAt = 1_800_000_000_000,
        )

        assertEquals(1_800_000_000_000, entity.toDomain().lastWateredAt)
    }

    @Test
    fun `domain plant maps the last watered date`() {
        val plant = Plant(
            id = "planta-6",
            name = "Regada",
            createdAt = 60,
            lastWateredAt = 1_900_000_000_000,
        )

        assertEquals(1_900_000_000_000, plant.toEntity().lastWateredAt)
    }

    @Test
    fun `round trip keeps the last watered date`() {
        val plant = Plant(
            name = "Ficus",
            type = PlantType.FICUS,
            createdAt = 70,
            lastWateredAt = 75,
        )

        assertEquals(plant, plant.toEntity().toDomain())
    }

    @Test
    fun `entity maps its photo to the domain`() {
        val photo = byteArrayOf(1, 2, 3)
        val entity = PlantEntity(
            id = "planta-7",
            name = "Con foto",
            typeKey = null,
            createdAt = 80,
            photoData = photo,
        )

        assertTrue(photo.contentEquals(entity.toDomain().photoData))
    }

    @Test
    fun `domain plant maps its photo to the entity`() {
        val photo = byteArrayOf(4, 5)
        val plant = Plant(
            id = "planta-8",
            name = "Con foto",
            createdAt = 90,
            photoData = photo,
        )

        assertTrue(photo.contentEquals(plant.toEntity().photoData))
    }

    @Test
    fun `round trip keeps the photo`() {
        val plant = Plant(
            name = "Con foto",
            createdAt = 100,
            photoData = byteArrayOf(7, 8, 9),
        )

        assertEquals(plant, plant.toEntity().toDomain())
    }

    @Test
    fun `entity without photo maps to a plant without photo`() {
        val entity = PlantEntity(
            id = "planta-9",
            name = "Sin foto",
            typeKey = null,
            createdAt = 110,
        )

        assertNull(entity.toDomain().photoData)
    }

    // ------------------------------------------------------------------
    // Historial de riegos
    // ------------------------------------------------------------------

    @Test
    fun `watering log entry entity maps to domain`() {
        val entity = WateringLogEntryEntity(
            id = "entry-1",
            plantId = "planta-1",
            wateredAt = 1_800_000_000_000,
        )

        val entry = entity.toDomain()

        assertEquals("entry-1", entry.id)
        assertEquals("planta-1", entry.plantId)
        assertEquals(1_800_000_000_000, entry.wateredAt)
    }

    @Test
    fun `watering log domain maps to entity`() {
        val entry = WateringLogEntry(
            id = "entry-2",
            plantId = "planta-2",
            wateredAt = 1_900_000_000_000,
        )

        val entity = entry.toEntity(plantId = "planta-2")

        assertEquals("entry-2", entity.id)
        assertEquals("planta-2", entity.plantId)
        assertEquals(1_900_000_000_000, entity.wateredAt)
    }

    @Test
    fun `watering log round trip keeps every field`() {
        val entry = WateringLogEntry(
            id = "entry-3",
            plantId = "planta-3",
            wateredAt = 2_000_000_000_000,
        )

        assertEquals(entry, entry.toEntity(plantId = entry.plantId).toDomain())
    }
}
