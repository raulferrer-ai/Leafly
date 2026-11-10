package com.raulferrergarcia.leafly.features.plants

import com.raulferrergarcia.leafly.domain.model.PlantPhoto
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val PhotoDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM", Locale.forLanguageTag("es-ES"))

private val PhotoDateFormatterWithYear: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM 'de' yyyy", Locale.forLanguageTag("es-ES"))

/**
 * Texto calmado para la fecha de una foto del historial.
 *
 * Las fotos recientes se leen como "Hoy" o "Ayer"; las demás muestran el día
 * y el mes, y añaden el año solo cuando no es el actual.
 */
fun PlantPhotoDateLabel(photo: PlantPhoto, nowMillis: Long = System.currentTimeMillis()): String {
    val now = Instant.ofEpochMilli(nowMillis).atZone(ZoneId.systemDefault()).toLocalDate()
    val photoDate = Instant.ofEpochMilli(photo.addedAt).atZone(ZoneId.systemDefault()).toLocalDate()

    if (photoDate == now) {
        return "Hoy"
    }

    val yesterday = now.minusDays(1)
    if (photoDate == yesterday) {
        return "Ayer"
    }

    return if (photoDate.getYear() == now.getYear()) {
        photoDate.format(PhotoDateFormatter)
    } else {
        photoDate.format(PhotoDateFormatterWithYear)
    }
}
