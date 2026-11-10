package com.raulferrergarcia.leafly.data.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.raulferrergarcia.leafly.domain.model.WateringReminder
import com.raulferrergarcia.leafly.domain.model.WateringStatus

/**
 * Recibe la alarma de riego y muestra la notificación local.
 *
 * Es intencionadamente fino: no decide nada, solo presenta el aviso que el
 * servicio ya preparó.
 */
class WateringReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION) return

        val plantId = intent.getStringExtra(EXTRA_PLANT_ID) ?: return
        val plantName = intent.getStringExtra(EXTRA_PLANT_NAME) ?: return
        val status = intent.getStringExtra(EXTRA_STATUS)
            ?.let { name -> WateringStatus.entries.firstOrNull { it.name == name } }
            ?: WateringStatus.NEEDS_WATER_SOON

        WateringReminderNotifications.show(
            context = context,
            plantId = plantId,
            plantName = plantName,
            status = status,
        )
    }

    companion object {
        const val ACTION = "com.raulferrergarcia.leafly.action.WATERING_REMINDER"

        private const val EXTRA_PLANT_ID = "extra_plant_id"
        private const val EXTRA_PLANT_NAME = "extra_plant_name"
        private const val EXTRA_STATUS = "extra_status"

        /** Intención completa, con los datos que necesita la notificación. */
        fun intent(context: Context, reminder: WateringReminder): Intent =
            Intent(context, WateringReminderReceiver::class.java).apply {
                action = ACTION
                putExtra(EXTRA_PLANT_ID, reminder.plantId)
                putExtra(EXTRA_PLANT_NAME, reminder.plantName)
                putExtra(EXTRA_STATUS, reminder.status.name)
            }

        /** Intención mínima para cancelar la alarma de una planta. */
        fun intentForPlant(context: Context, plantId: String): Intent =
            Intent(context, WateringReminderReceiver::class.java).apply {
                action = ACTION
                putExtra(EXTRA_PLANT_ID, plantId)
            }
    }
}
