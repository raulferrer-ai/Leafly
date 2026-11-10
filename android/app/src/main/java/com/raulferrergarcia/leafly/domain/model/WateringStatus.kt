package com.raulferrergarcia.leafly.domain.model

import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/**
 * Estado de riego de una planta, calculado a partir de cuándo se regó por última vez.
 *
 * Regla simple y compartida con la UI:
 * - 0–4 días → al día
 * - 5–6 días → conviene regar pronto
 * - 7 días o más → riego urgente
 */
enum class WateringStatus {
    HEALTHY,
    NEEDS_WATER_SOON,
    NEEDS_WATER_NOW,
    ;

    companion object {
        /** Días desde el último riego a partir de los cuales conviene regar pronto. */
        const val SOON_THRESHOLD_DAYS = 5

        /** Días desde el último riego a partir de los cuales el riego es urgente. */
        const val URGENT_THRESHOLD_DAYS = 7

        /** Traduce días transcurridos a estado. Los valores negativos cuentan como 0. */
        fun fromDays(days: Int): WateringStatus = when (days.coerceAtLeast(0)) {
            in 0 until SOON_THRESHOLD_DAYS -> HEALTHY
            in SOON_THRESHOLD_DAYS until URGENT_THRESHOLD_DAYS -> NEEDS_WATER_SOON
            else -> NEEDS_WATER_NOW
        }
    }
}

/**
 * Fecha de referencia para el riego: el último riego o, si no hay, el día en
 * que se añadió la planta.
 */
val Plant.wateringReferenceMillis: Long
    get() = lastWateredAt ?: createdAt

/**
 * Días completos transcurridos desde la fecha de referencia del riego.
 * Se comparan días naturales en la zona horaria indicada, no horas exactas.
 */
fun Plant.daysSinceWatering(
    nowMillis: Long = System.currentTimeMillis(),
    zoneId: ZoneId = ZoneId.systemDefault(),
): Int {
    val start = Instant.ofEpochMilli(wateringReferenceMillis).atZone(zoneId).toLocalDate()
    val end = Instant.ofEpochMilli(nowMillis).atZone(zoneId).toLocalDate()
    return ChronoUnit.DAYS.between(start, end).toInt().coerceAtLeast(0)
}

/**
 * Estado de riego calculado para un instante concreto.
 * Testeable e independiente de la UI.
 */
fun Plant.wateringStatus(
    nowMillis: Long = System.currentTimeMillis(),
    zoneId: ZoneId = ZoneId.systemDefault(),
): WateringStatus = WateringStatus.fromDays(
    daysSinceWatering(nowMillis = nowMillis, zoneId = zoneId),
)
