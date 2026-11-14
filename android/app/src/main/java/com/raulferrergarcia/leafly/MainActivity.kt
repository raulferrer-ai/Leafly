package com.raulferrergarcia.leafly

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.raulferrergarcia.leafly.designsystem.LeaflyTheme
import com.raulferrergarcia.leafly.features.root.LeaflyRoot

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
        )

        val container = (application as LeaflyApplication).container

        setContent {
            LeaflyTheme {
                LeaflyRoot(
                    observePlants = container.observePlants,
                    addPlant = container.addPlant,
                    markPlantWatered = container.markPlantWatered,
                    syncWateringReminders = container.syncWateringReminders,
                    cancelWateringReminder = container.cancelWateringReminder,
                    plantRepository = container.repository,
                    notificationPreferencesRepository = container.notificationPreferencesRepository,
                )
            }
        }
    }
}
