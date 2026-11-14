package com.raulferrergarcia.leafly.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.raulferrergarcia.leafly.domain.model.NotificationPreferences

private const val KEY_HOUR = "notification_preferred_hour"
private const val KEY_MINUTE = "notification_preferred_minute"
private const val PREFS_NAME = "leafly_notification_preferences"

/**
 * Persistencia de las preferencias de notificación sobre SharedPreferences.
 *
 * Guarda la hora y minuto preferidos para los recordatorios de riego.
 */
class NotificationPreferencesRepository(
    private val prefs: SharedPreferences,
) {
    /** Crea una instancia leyendo de las preferencias predeterminadas de la app. */
    constructor(context: Context) : this(
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
    )

    /** Lee las preferencias guardadas, o devuelve los valores por defecto. */
    fun get(): NotificationPreferences {
        val hour = prefs.getInt(KEY_HOUR, NotificationPreferences.Default.hour)
        val minute = prefs.getInt(KEY_MINUTE, NotificationPreferences.Default.minute)
        return NotificationPreferences(
            hour = hour.coerceIn(0, 23),
            minute = minute.coerceIn(0, 59),
        )
    }

    /** Guarda las preferencias. */
    fun save(preferences: NotificationPreferences) {
        prefs.edit()
            .putInt(KEY_HOUR, preferences.hour)
            .putInt(KEY_MINUTE, preferences.minute)
            .apply()
    }
}
