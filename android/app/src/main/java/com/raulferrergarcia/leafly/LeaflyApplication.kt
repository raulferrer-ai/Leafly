package com.raulferrergarcia.leafly

import android.app.Application
import com.raulferrergarcia.leafly.data.notifications.WateringReminderNotifications

class LeaflyApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        WateringReminderNotifications.ensureChannel(this)
        container = AppContainer(this)
    }
}
