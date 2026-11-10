package com.raulferrergarcia.leafly.data.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import com.raulferrergarcia.leafly.domain.model.WateringReminder
import com.raulferrergarcia.leafly.domain.scheduling.WateringAlarmScheduler

/**
 * Implementación real de `WateringAlarmScheduler` sobre `AlarmManager`.
 *
 * Se usa una alarma inexacta (`setAndAllowWhileIdle`) porque es la opción más
 * simple y fiable para un recordatorio de riego: no exige el permiso especial
 * de alarmas exactas de Android 12+ y sigue funcionando en modo Doze.
 */
class AlarmManagerWateringAlarmScheduler(
    context: Context,
) : WateringAlarmScheduler {

    private val appContext: Context = context.applicationContext
    private val alarmManager: AlarmManager =
        requireNotNull(appContext.getSystemService(AlarmManager::class.java)) {
            "AlarmManager no está disponible"
        }

    override fun schedule(reminder: WateringReminder) {
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            reminder.fireAtMillis,
            pendingIntentFor(reminder),
        )
    }

    override fun cancel(plantId: String) {
        alarmManager.cancel(pendingIntentFor(plantId))
        WateringReminderNotifications.cancel(appContext, plantId)
    }

    private fun pendingIntentFor(reminder: WateringReminder): PendingIntent =
        pendingIntentFor(
            requestCode = reminder.notificationId,
            intent = WateringReminderReceiver.intent(appContext, reminder),
        )

    private fun pendingIntentFor(plantId: String): PendingIntent =
        pendingIntentFor(
            requestCode = WateringReminder.stableId(plantId),
            intent = WateringReminderReceiver.intentForPlant(appContext, plantId),
        )

    private fun pendingIntentFor(requestCode: Int, intent: android.content.Intent): PendingIntent =
        PendingIntent.getBroadcast(
            appContext,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
}
