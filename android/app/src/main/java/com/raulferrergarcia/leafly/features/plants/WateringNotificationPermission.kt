package com.raulferrergarcia.leafly.features.plants

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.raulferrergarcia.leafly.domain.model.Plant
import com.raulferrergarcia.leafly.domain.model.WateringStatus
import com.raulferrergarcia.leafly.domain.model.wateringStatus

/**
 * Pide permiso de notificaciones (Android 13+) de forma adecuada:
 * solo cuando hay una planta que necesita atención y una sola vez por sesión.
 *
 * Si el usuario lo deniega, no pasa nada: la app sigue funcionando y los
 * recordatorios simplemente no se muestran.
 */
@Composable
internal fun RequestWateringNotificationPermission(plants: List<Plant>) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val context = LocalContext.current
    val hasPlantThatNeedsWater = remember(plants) {
        plants.any { plant -> plant.wateringStatus() != WateringStatus.HEALTHY }
    }
    var didAsk by rememberSaveable { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { /* Si se deniega, seguimos con calma. */ },
    )

    LaunchedEffect(hasPlantThatNeedsWater) {
        if (!hasPlantThatNeedsWater || didAsk) return@LaunchedEffect

        val isGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED

        if (!isGranted) {
            didAsk = true
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
