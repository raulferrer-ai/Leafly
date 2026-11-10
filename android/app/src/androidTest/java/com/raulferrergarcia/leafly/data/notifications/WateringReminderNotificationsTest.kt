package com.raulferrergarcia.leafly.data.notifications

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.raulferrergarcia.leafly.domain.model.WateringReminder
import com.raulferrergarcia.leafly.domain.model.WateringStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Comprueba que la notificación real se muestra con el texto calmado y el
 * nombre de la planta, y que se retira al cancelar el recordatorio.
 */
@RunWith(AndroidJUnit4::class)
class WateringReminderNotificationsTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Before
    fun setUp() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            InstrumentationRegistry.getInstrumentation().uiAutomation.grantRuntimePermission(
                context.packageName,
                Manifest.permission.POST_NOTIFICATIONS,
            )
        }
        WateringReminderNotifications.ensureChannel(context)
    }

    @Test
    fun showsACalmNotificationWithThePlantName() {
        val plantId = "planta-urgente"
        WateringReminderNotifications.show(
            context = context,
            plantId = plantId,
            plantName = "Monstera",
            status = WateringStatus.NEEDS_WATER_NOW,
        )

        val notification = awaitNotification(WateringReminder.stableId(plantId))
        assertNotNull(notification)
        assertEquals(
            "Monstera necesita agua",
            notification?.extras?.getString(Notification.EXTRA_TITLE),
        )
        assertEquals(
            "Un riego ahora ayudará a Monstera a volver a estar al día.",
            notification?.extras?.getString(Notification.EXTRA_TEXT),
        )

        WateringReminderNotifications.cancel(context, plantId)
        assertNull(awaitNotification(WateringReminder.stableId(plantId), present = false))
    }

    @Test
    fun showsASoonReminderWithThePlantName() {
        val plantId = "planta-pronto"
        WateringReminderNotifications.show(
            context = context,
            plantId = plantId,
            plantName = "Ficus",
            status = WateringStatus.NEEDS_WATER_SOON,
        )

        val notification = awaitNotification(WateringReminder.stableId(plantId))
        assertNotNull(notification)
        assertEquals(
            "Un poco de agua para Ficus",
            notification?.extras?.getString(Notification.EXTRA_TITLE),
        )
        assertEquals(
            "Riega pronto a Ficus para que siga creciendo con calma.",
            notification?.extras?.getString(Notification.EXTRA_TEXT),
        )

        WateringReminderNotifications.cancel(context, plantId)
    }

    @Test
    fun doesNotNotifyAPlantThatIsUpToDate() {
        val plantId = "planta-al-dia"
        WateringReminderNotifications.show(
            context = context,
            plantId = plantId,
            plantName = "Ficus",
            status = WateringStatus.HEALTHY,
        )

        assertNull(awaitNotification(WateringReminder.stableId(plantId), present = false))
    }

    private fun awaitNotification(
        id: Int,
        present: Boolean = true,
        timeoutMillis: Long = 2_000,
    ): Notification? {
        val deadline = System.currentTimeMillis() + timeoutMillis
        while (System.currentTimeMillis() < deadline) {
            val notification = activeNotification(id)
            if ((notification != null) == present) return notification
            Thread.sleep(50)
        }
        return activeNotification(id)
    }

    private fun activeNotification(id: Int): Notification? = context
        .getSystemService(NotificationManager::class.java)
        .activeNotifications
        .firstOrNull { it.id == id }
        ?.notification
}
