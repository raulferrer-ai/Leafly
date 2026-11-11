package com.raulferrergarcia.leafly.features.plants

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.raulferrergarcia.leafly.R
import com.raulferrergarcia.leafly.data.photos.PlantPhotoProcessor
import com.raulferrergarcia.leafly.designsystem.LeaflyCard
import com.raulferrergarcia.leafly.designsystem.LeaflyColors
import com.raulferrergarcia.leafly.designsystem.LeaflyPrimaryButton
import com.raulferrergarcia.leafly.designsystem.LeaflyQuietButton
import com.raulferrergarcia.leafly.designsystem.LeaflyRadius
import com.raulferrergarcia.leafly.designsystem.LeaflySpacing
import com.raulferrergarcia.leafly.designsystem.LeaflyCopy
import com.raulferrergarcia.leafly.designsystem.rememberPlantPhoto
import com.raulferrergarcia.leafly.domain.model.PlantPhoto
import java.io.File
import java.io.IOException
import kotlinx.coroutines.launch

/**
 * Sección del detalle que muestra y amplía el historial visual de la planta.
 *
 * Reutiliza el procesado de imagen y los pickers de la pantalla de añadir
 * planta. La sección solo se ocupa de elegir y preparar la foto; guardarla
 * es responsabilidad del ViewModel a través de [onAddPhoto].
 */
@Composable
fun PlantPhotoHistorySection(
    photos: List<PlantPhoto>,
    isAddingPhoto: Boolean,
    photoErrorMessage: String?,
    onAddPhoto: (ByteArray) -> Unit,
    onRetry: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val photoProcessor = remember(context) {
        PlantPhotoProcessor(context.applicationContext)
    }

    val hasCamera = remember(context) {
        context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
    }

    var pendingCameraPath by remember { mutableStateOf<String?>(null) }

    var isShowingGallery by remember { mutableStateOf(false) }
    var isShowingCamera by remember { mutableStateOf(false) }
    var isChoosingSource by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    // ------------------------------------------------------------------
    // Helpers de texto (stringResource es @Composable, así que se usa
    // context.resources.getString desde los callbacks no composables).
    // ------------------------------------------------------------------

    fun res(id: Int): String = context.resources.getString(id)
    // ------------------------------------------------------------------
    // Cámara
    // ------------------------------------------------------------------

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
    ) { saved ->
        val path = pendingCameraPath
        pendingCameraPath = null
        if (path == null || !saved) {
            if (path != null) {
                photoProcessor.delete(File(path))
            }
            return@rememberLauncherForActivityResult
        }

        val file = File(path)
        isProcessing = true
        message = null
        scope.launch {
            val processed = photoProcessor.process(file)
            if (processed != null) {
                onAddPhoto(processed)
            } else {
                message = res(R.string.add_plant_photo_capture_failed)
            }
            photoProcessor.delete(file)
            isProcessing = false
        }
    }

    fun launchCamera() {
        val file = try {
            photoProcessor.createCameraPhotoFile()
        } catch (error: IOException) {
            message = res(R.string.add_plant_photo_camera_unavailable)
            return
        }

        val uri = try {
            photoProcessor.uriFor(file)
        } catch (error: IllegalArgumentException) {
            photoProcessor.delete(file)
            message = res(R.string.add_plant_photo_camera_unavailable)
            return
        }

        pendingCameraPath = file.absolutePath

        try {
            cameraLauncher.launch(uri)
        } catch (error: ActivityNotFoundException) {
            photoProcessor.delete(file)
            pendingCameraPath = null
            message = res(R.string.add_plant_photo_camera_unavailable)
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            launchCamera()
        } else {
            message = res(R.string.add_plant_photo_camera_permission)
        }
    }

    fun requestCamera() {
        isChoosingSource = false
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA,
        ) == PackageManager.PERMISSION_GRANTED

        if (granted) {
            launchCamera()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // ------------------------------------------------------------------
    // Galería
    // ------------------------------------------------------------------

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult

        isProcessing = true
        message = null
        scope.launch {
            val processed = photoProcessor.process(uri)
            if (processed != null) {
                onAddPhoto(processed)
            } else {
                message = res(R.string.add_plant_photo_load_failed)
            }
            isProcessing = false
        }
    }

    fun pickGallery() {
        isChoosingSource = false
        galleryLauncher.launch(
            PickVisualMediaRequest(
                ActivityResultContracts.PickVisualMedia.ImageOnly,
            ),
        )
    }

    // ------------------------------------------------------------------
    // Diálogo de elección de fuente
    // ------------------------------------------------------------------

    fun openSourceDialog() {
        message = null
        isChoosingSource = true
    }

    fun closeSourceDialog() {
        isChoosingSource = false
        message = null
    }

    // ------------------------------------------------------------------
    // UI
    // ------------------------------------------------------------------

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(LeaflySpacing.sm),
    ) {
        HistoryHeader()

        if (photoErrorMessage != null) {
            PhotoErrorCard(
                message = photoErrorMessage,
                onRetry = onRetry,
            )
        } else if (photos.isEmpty()) {
            EmptyState(
                onAddFirstPhoto = ::openSourceDialog,
                isProcessing = isProcessing,
            )
        } else {
            HistoryStrip(photos = photos)
            AddAnotherPhotoButton(
                onAddAnotherPhoto = ::openSourceDialog,
                isAddingPhoto = isAddingPhoto,
            )
        }

        if (isProcessing) {
            ProcessingRow()
        }

        if (message != null) {
            Text(
                text = message!!,
                style = MaterialTheme.typography.bodyMedium,
                color = LeaflyColors.TextSecondary,
            )
        }

        // Diálogo de "Añadir una foto" como superposición.
        if (isChoosingSource) {
            SourceChoiceDialog(
                hasCamera = hasCamera,
                onCamera = ::requestCamera,
                onGallery = ::pickGallery,
                onCancel = ::closeSourceDialog,
            )
        }
    }
}

// -----------------------------------------------------------------------
// Sub-componentes
// -----------------------------------------------------------------------

@Composable
private fun HistoryHeader() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = stringResource(R.string.plant_photo_history_title),
            style = MaterialTheme.typography.headlineSmall,
            color = Color(0xFF2F2B26),
        )
        Text(
            text = stringResource(R.string.plant_photo_history_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF6D675E),
        )
    }
}

@Composable
private fun HistoryStrip(photos: List<PlantPhoto>) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(LeaflySpacing.sm),
    ) {
        items(items = photos, key = { it.id }) { photo ->
            PlantPhotoHistoryItem(photo = photo)
        }
    }
}

@Composable
private fun AddAnotherPhotoButton(
    onAddAnotherPhoto: () -> Unit,
    isAddingPhoto: Boolean,
) {
    LeaflyQuietButton(
        text = stringResource(R.string.plant_photo_history_add_another),
        icon = Icons.Rounded.AddAPhoto,
        onClick = onAddAnotherPhoto,
        enabled = !isAddingPhoto,
    )
}

@Composable
private fun EmptyState(
    onAddFirstPhoto: () -> Unit,
    isProcessing: Boolean,
) {
    LeaflyCard {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(LeaflySpacing.md),
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(Color(0xFFE0EDDF)),
                contentAlignment = Alignment.Center,
            ) {
                androidx.compose.material3.Icon(
                    imageVector = Icons.Rounded.AddAPhoto,
                    contentDescription = null,
                    tint = Color(0xFF315B44),
                    modifier = Modifier.size(28.dp),
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = LeaflyCopy.Empty.PhotoHistory.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF2F2B26),
                )
                Text(
                    text = LeaflyCopy.Empty.PhotoHistory.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF6D675E),
                    textAlign = TextAlign.Center,
                )
            }

            LeaflyPrimaryButton(
                text = LeaflyCopy.Empty.PhotoHistory.button,
                onClick = onAddFirstPhoto,
                enabled = !isProcessing,
                icon = Icons.Rounded.CameraAlt,
            )
        }
    }
}

@Composable
private fun ProcessingRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircularProgressIndicator(
            color = Color(0xFF4F8B63),
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(LeaflySpacing.xs))
        Text(
            text = stringResource(R.string.plant_photo_history_processing),
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF6D675E),
        )
    }
}

@Composable
private fun PhotoErrorCard(
    message: String,
    onRetry: () -> Unit,
) {
    LeaflyCard {
        Column(verticalArrangement = Arrangement.spacedBy(LeaflySpacing.sm)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Warning,
                    contentDescription = null,
                    tint = LeaflyColors.NeedsWaterNow,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(LeaflySpacing.xs))
                Text(
                    text = stringResource(R.string.plant_photo_history_error_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = LeaflyColors.TextPrimary,
                )
            }

            Text(
                text = LeaflyCopy.Error.PhotoHistory.message,
                style = MaterialTheme.typography.bodyMedium,
                color = LeaflyColors.TextSecondary,
            )

            LeaflyQuietButton(
                text = LeaflyCopy.Error.PhotoHistory.retry,
                icon = Icons.Rounded.Refresh,
                onClick = onRetry,
            )
        }
    }
}

@Composable
private fun PlantPhotoHistoryItem(photo: PlantPhoto) {
    val photoImage = rememberPlantPhoto(photo.data, maxSize = 256)

    Column(
        modifier = Modifier.size(116.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (photoImage != null) {
            Image(
                bitmap = photoImage,
                contentDescription = stringResource(
                    R.string.plant_photo_history_item_description,
                    PlantPhotoDateLabel(photo),
                ),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(116.dp)
                    .clip(RoundedCornerShape(LeaflyRadius.control)),
            )
        } else {
            Box(
                modifier = Modifier
                    .size(116.dp)
                    .clip(RoundedCornerShape(LeaflyRadius.control))
                    .background(Color(0xFFF1EFE9)),
                contentAlignment = Alignment.Center,
            ) {
                androidx.compose.material3.Icon(
                    imageVector = Icons.Rounded.PhotoLibrary,
                    contentDescription = null,
                    tint = Color(0xFFA8A198),
                    modifier = Modifier.size(24.dp),
                )
            }
        }

        Text(
            text = PlantPhotoDateLabel(photo),
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF6D675E),
            maxLines = 1,
        )
    }
}

@Composable
private fun SourceChoiceDialog(
    hasCamera: Boolean,
    onCamera: () -> Unit,
    onGallery: () -> Unit,
    onCancel: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x000000).copy(alpha = 0.4f))
            .clickable(onClick = onCancel),
        contentAlignment = Alignment.Center,
    ) {
        LeaflyCard(
            modifier = Modifier
                .widthIn(max = 320.dp)
                .padding(horizontal = LeaflySpacing.screenHorizontal),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(LeaflySpacing.xs),
            ) {
                Text(
                    text = stringResource(R.string.plant_photo_history_dialog_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF2F2B26),
                )
                Text(
                    text = stringResource(R.string.plant_photo_history_dialog_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF6D675E),
                )

                if (hasCamera) {
                    LeaflyQuietButton(
                        text = stringResource(R.string.plant_photo_history_camera),
                        icon = Icons.Rounded.CameraAlt,
                        onClick = onCamera,
                    )
                }

                LeaflyQuietButton(
                    text = stringResource(R.string.plant_photo_history_gallery),
                    icon = Icons.Rounded.PhotoLibrary,
                    onClick = onGallery,
                )

                LeaflyQuietButton(
                    text = stringResource(android.R.string.cancel),
                    icon = Icons.Rounded.Close,
                    onClick = onCancel,
                )
            }
        }
    }
}
