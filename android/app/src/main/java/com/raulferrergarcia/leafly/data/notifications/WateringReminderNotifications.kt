package com.raulferrergarcia.leafly.data.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.raulferrergarcia.leafly.MainActivity
import com.raulferrergarcia.leafly.R
import com.raulferrergarcia.leafly.domain.model.WateringReminder
import com.raulferrergarcia.leafly.domain.model.WateringStatus

/**
 * Presentación de las notificaciones de riego.
 *
 * Centraliza el canal, el texto calmado y la comprobación de permiso: si el
 * usuario no ha concedido notificaciones (Android 13+), simplemente no se
 * muestra nada y la app sigue funcionando.
 */
object WateringReminderNotifications {

    const val CHANNEL_ID = "watering_reminders"

    /** Crea el canal una sola vez. Requiere API 26+, que es el mínimo de la app. */
    fun ensureChannel(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.watering_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = context.getString(R.string.watering_channel_description)
        }
        manager.createNotificationChannel(channel)
    }

    /** Muestra el recordatorio de una planta concreta. */
    fun show(
        context: Context,
        plantId: String,
        plantName: String,
        status: WateringStatus,
    ) {
        if (status == WateringStatus.HEALTHY) return

        val notifications = NotificationManagerCompat.from(context)
        if (!notifications.areNotificationsEnabled()) return
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        ensureChannel(context)

        val body = context.getString(status.reminderBodyRes(), plantName)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_watering_notification)
            .setContentTitle(context.getString(status.reminderTitleRes(), plantName))
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(openAppIntent(context))
            .build()

        notifications.notify(WateringReminder.stableId(plantId), notification)
    }

    /** Retira la notificación ya mostrada, si la hubiera. */
    fun cancel(context: Context, plantId: String) {
        NotificationManagerCompat.from(context)
            .cancel(WateringReminder.stableId(plantId))
    }

    private fun openAppIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun WateringStatus.reminderTitleRes(): Int = when (this) {
        WateringStatus.HEALTHY,
        WateringStatus.NEEDS_WATER_NOW,
        -> R.string.watering_reminder_now_title

        WateringStatus.NEEDS_WATER_SOON -> R.string.watering_reminder_soon_title
    }

    private fun WateringStatus.reminderBodyRes(): Int = when (this) {
        WateringStatus.HEALTHY,
        WateringStatus.NEEDS_WATER_NOW,
        -> R.string.watering_reminder_now_body

        WateringStatus.NEEDS_WATER_SOON -> R.string.watering_reminder_soon_body
    }
}
