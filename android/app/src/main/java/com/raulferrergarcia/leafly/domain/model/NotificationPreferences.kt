package com.raulferrergarcia.leafly.domain.model

/**
 * Preferencias de notificación del usuario.
 *
 * Hora y minuto preferidos para los recordatorios de riego. Se persisten
 * localmente y se usan para calcular cuándo se entregan los avisos.
 */
data class NotificationPreferences(
    /** Hora local preferida (0–23). Por defecto 10. */
    val hour: Int = 10,
    /** Minuto local preferido (0–59). Por defecto 0. */
    val minute: Int = 0,
) {
    init {
        require(hour in 0..23) { "La hora debe estar entre 0 y 23" }
        require(minute in 0..59) { "El minuto debe estar entre 0 y 59" }
    }

    companion object {
        /** Preferencias por defecto: 10:00 de la mañana. */
        val Default = NotificationPreferences(hour = 10, minute = 0)
    }
}
