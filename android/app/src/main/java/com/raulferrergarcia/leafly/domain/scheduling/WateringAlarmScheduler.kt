package com.raulferrergarcia.leafly.domain.scheduling

import com.raulferrergarcia.leafly.domain.model.WateringReminder

/**
 * Programa y cancela las alarmas locales de riego.
 *
 * Es la frontera con el sistema operativo. El servicio de recordatorios decide
 * *qué* programar; esta interfaz solo sabe *cómo* hacerlo, y permite usar un
 * doble de prueba en los tests.
 */
interface WateringAlarmScheduler {
    /** Programa (o reemplaza) la alarma de un recordatorio. */
    fun schedule(reminder: WateringReminder)

    /** Cancela la alarma y la notificación pendiente de una planta. */
    fun cancel(plantId: String)
}
