package com.raulferrergarcia.leafly.domain.model

/**
 * Recordatorio local de riego.
 *
 * Es un valor de dominio: no conoce `AlarmManager` ni `NotificationManager`.
 * La capa de datos lo traduce a una alarma y a una notificación del sistema.
 */
data class WateringReminder(
    val plantId: String,
    val plantName: String,
    val status: WateringStatus,
    /** Instante en el que debe entregarse el recordatorio. */
    val fireAtMillis: Long,
) {
    /**
     * Identificador estable por planta. Se usa tanto para la alarma como para
     * la notificación, de forma que cancelar una no afecta a las demás.
     */
    val notificationId: Int
        get() = stableId(plantId)

    companion object {
        /** Mismo id estable para la alarma y la notificación de una planta. */
        fun stableId(plantId: String): Int = plantId.hashCode() and 0x7FFF_FFFF
    }
}
