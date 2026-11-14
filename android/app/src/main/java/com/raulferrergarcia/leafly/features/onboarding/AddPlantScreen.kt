package com.raulferrergarcia.leafly.features.onboarding

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.HourglassEmpty
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.raulferrergarcia.leafly.R
import com.raulferrergarcia.leafly.data.photos.PlantPhotoProcessor
import com.raulferrergarcia.leafly.designsystem.LeaflyBottomBar
import com.raulferrergarcia.leafly.designsystem.LeaflyCard
import com.raulferrergarcia.leafly.designsystem.LeaflyChip
import com.raulferrergarcia.leafly.designsystem.LeaflyCircularIconButton
import com.raulferrergarcia.leafly.designsystem.LeaflyColors
import com.raulferrergarcia.leafly.designsystem.LeaflyPrimaryButton
import com.raulferrergarcia.leafly.designsystem.LeaflyQuietButton
import com.raulferrergarcia.leafly.designsystem.LeaflyRadius
import com.raulferrergarcia.leafly.designsystem.LeaflyScreen
import com.raulferrergarcia.leafly.designsystem.LeaflySpacing
import com.raulferrergarcia.leafly.designsystem.NoPlantTypeIcon
import com.raulferrergarcia.leafly.designsystem.icon
import com.raulferrergarcia.leafly.designsystem.rememberPlantPhoto
import com.raulferrergarcia.leafly.domain.model.Plant
import com.raulferrergarcia.leafly.domain.model.PlantType
import java.io.File
import java.io.IOException
import kotlinx.coroutines.launch

/**
 * Pantalla para añadir o editar una planta.
 */
@Composable
fun AddPlantScreen(
    viewModel: AddPlantViewModel,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    mode: AddPlantMode = AddPlantMode.FIRST_PLANT,
    editingPlant: Plant? = null,
    onSaved: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val nameFocusRequester = remember { FocusRequester() }
    var isNameFocused by remember { mutableStateOf(false) }

    val photoProcessor = remember(context) {
        PlantPhotoProcessor(context.applicationContext)
    }
    val scope = rememberCoroutineScope()
    var pendingCameraPath by rememberSaveable { mutableStateOf<String?>(null) }

    val hasCamera = remember(context) {
        context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
    ) { saved ->
        val path = pendingCameraPath
        pendingCameraPath = null
        if (path == null) return@rememberLauncherForActivityResult

        val file = File(path)
        if (!saved) {
            photoProcessor.delete(file)
            return@rememberLauncherForActivityResult
        }

        viewModel.startPhotoProcessing()
        scope.launch {
            try {
                val processed = photoProcessor.process(file)
                if (processed != null) {
                    viewModel.applyPhoto(processed)
                } else {
                    viewModel.failPhotoProcessing(AddPlantPhotoMessage.PhotoCaptureFailed)
                }
            } finally {
                photoProcessor.delete(file)
            }
        }
    }

    fun launchCamera() {
        val file = try {
            photoProcessor.createCameraPhotoFile()
        } catch (error: IOException) {
            viewModel.failPhotoProcessing(AddPlantPhotoMessage.CameraUnavailable)
            return
        }

        val uri = try {
            photoProcessor.uriFor(file)
        } catch (error: IllegalArgumentException) {
            photoProcessor.delete(file)
            viewModel.failPhotoProcessing(AddPlantPhotoMessage.CameraUnavailable)
            return
        }

        pendingCameraPath = file.absolutePath

        try {
            cameraLauncher.launch(uri)
        } catch (error: ActivityNotFoundException) {
            photoProcessor.delete(file)
            pendingCameraPath = null
            viewModel.failPhotoProcessing(AddPlantPhotoMessage.CameraUnavailable)
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            launchCamera()
        } else {
            viewModel.showPhotoMessage(AddPlantPhotoMessage.CameraPermissionDenied)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult

        viewModel.startPhotoProcessing()
        scope.launch {
            val processed = photoProcessor.process(uri)
            if (processed != null) {
                viewModel.applyPhoto(processed)
            } else {
                viewModel.failPhotoProcessing(AddPlantPhotoMessage.PhotoLoadFailed)
            }
        }
    }

    fun requestCamera() {
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

    fun cancel() {
        pendingCameraPath?.let { path -> photoProcessor.delete(File(path)) }
        pendingCameraPath = null
        viewModel.reset()
        onCancel()
    }

    var hasAppeared by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { hasAppeared = true }

    // Si hay una planta para editar, precargamos sus datos al aparecer.
    LaunchedEffect(editingPlant) {
        if (editingPlant != null) {
            viewModel.load(editingPlant)
        }
    }

    val contentAlpha by animateFloatAsState(
        targetValue = if (hasAppeared) 1f else 0f,
        animationSpec = tween(durationMillis = 450),
        label = "addPlantAlpha",
    )
    val contentOffset by animateDpAsState(
        targetValue = if (hasAppeared) 0.dp else 14.dp,
        animationSpec = tween(durationMillis = 450),
        label = "addPlantOffset",
    )

    // Si la validación falla, llevamos el foco al nombre para corregirlo.
    LaunchedEffect(uiState.nameError) {
        if (uiState.nameError != null) {
            nameFocusRequester.requestFocus()
        }
    }

    // Al guardar, cerramos el teclado y avisamos a quien nos abrió.
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            focusManager.clearFocus()
            onSaved()
        }
    }

    val isEditing = editingPlant != null

    LeaflyScreen(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .widthIn(max = LeaflySpacing.contentMaxWidth)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = LeaflySpacing.screenHorizontal)
                    .padding(top = LeaflySpacing.sm, bottom = LeaflySpacing.lg)
                    .graphicsLayer {
                        alpha = contentAlpha
                        translationY = contentOffset.toPx()
                    },
            ) {
                TopBar(
                    onCancel = ::cancel,
                    mode = mode,
                    isEditing = isEditing,
                )

                Spacer(modifier = Modifier.height(LeaflySpacing.lg))

                Header(
                    mode = mode,
                    isEditing = isEditing,
                )

                Spacer(modifier = Modifier.height(LeaflySpacing.lg))

                NameSection(
                    name = uiState.name,
                    nameError = uiState.nameError,
                    isFocused = isNameFocused,
                    focusRequester = nameFocusRequester,
                    onNameChange = viewModel::onNameChange,
                    onFocusChanged = { isNameFocused = it },
                    onDone = { focusManager.clearFocus() },
                )

                Spacer(modifier = Modifier.height(LeaflySpacing.lg))

                PhotoSection(
                    photoData = uiState.photoData,
                    isProcessing = uiState.isProcessingPhoto,
                    message = uiState.photoMessage,
                    hasCamera = hasCamera,
                    onPickGallery = {
                        galleryLauncher.launch(
                            PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly,
                            ),
                        )
                    },
                    onTakePhoto = ::requestCamera,
                    onRemovePhoto = viewModel::removePhoto,
                )

                Spacer(modifier = Modifier.height(LeaflySpacing.lg))

                TypeSection(
                    selectedType = uiState.selectedType,
                    onSelectNone = viewModel::clearType,
                    onToggleType = viewModel::toggleType,
                )
            }

            SaveSection(
                isSaving = uiState.isSaving,
                isProcessingPhoto = uiState.isProcessingPhoto,
                saveError = uiState.saveError,
                onSave = viewModel::save,
                isEditing = isEditing,
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Cabecera
// ---------------------------------------------------------------------------

@Composable
private fun TopBar(
    onCancel: () -> Unit,
    mode: AddPlantMode,
    isEditing: Boolean = false,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LeaflyCircularIconButton(
            icon = Icons.Rounded.Close,
            contentDescription = stringResource(
                if (isEditing) R.string.edit_plant_cancel else mode.cancelContentDescriptionRes,
            ),
            onClick = onCancel,
        )

        Text(
            text = stringResource(mode.eyebrowRes),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.75f),
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.width(44.dp))
    }
}

@Composable
private fun Header(
    mode: AddPlantMode,
    isEditing: Boolean = false,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(LeaflySpacing.xs),
    ) {
        Text(
            text = stringResource(
                if (isEditing) R.string.edit_plant_title else mode.titleRes,
            ),
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
        )
        Text(
            text = stringResource(
                if (isEditing) R.string.edit_plant_subtitle else mode.subtitleRes,
            ),
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.88f),
        )
    }
}

// ---------------------------------------------------------------------------
// Nombre
// ---------------------------------------------------------------------------

@Composable
private fun NameSection(
    name: String,
    nameError: AddPlantError?,
    isFocused: Boolean,
    focusRequester: FocusRequester,
    onNameChange: (String) -> Unit,
    onFocusChanged: (Boolean) -> Unit,
    onDone: () -> Unit,
) {
    LeaflyCard {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(LeaflySpacing.sm),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.add_plant_name_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = LeaflyColors.TextPrimary,
                )
                Text(
                    text = stringResource(R.string.add_plant_name_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = LeaflyColors.TextSecondary,
                )
            }

            val fieldShape = RoundedCornerShape(LeaflyRadius.field)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 54.dp)
                    .clip(fieldShape)
                    .background(LeaflyColors.FieldSurface)
                    .border(
                        width = 1.5.dp,
                        color = if (isFocused) {
                            LeaflyColors.Primary.copy(alpha = 0.45f)
                        } else {
                            Color.Transparent
                        },
                        shape = fieldShape,
                    )
                    .padding(horizontal = LeaflySpacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Eco,
                    contentDescription = null,
                    tint = if (nameError != null) {
                        LeaflyColors.NeedsWaterNow
                    } else {
                        LeaflyColors.TextTertiary
                    },
                    modifier = Modifier.size(18.dp),
                )

                Spacer(modifier = Modifier.width(LeaflySpacing.sm))

                BasicTextField(
                    value = name,
                    onValueChange = onNameChange,
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester)
                        .onFocusChanged { onFocusChanged(it.isFocused) },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = LeaflyColors.TextPrimary,
                    ),
                    cursorBrush = SolidColor(LeaflyColors.Primary),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(onDone = { onDone() }),
                    decorationBox = { innerTextField ->
                        Box {
                            if (name.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.add_plant_name_placeholder),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = LeaflyColors.TextTertiary,
                                )
                            }
                            innerTextField()
                        }
                    },
                )
            }

            AnimatedVisibility(
                visible = nameError != null,
                enter = fadeIn(animationSpec = tween(durationMillis = 200)) +
                    expandVertically(animationSpec = tween(durationMillis = 200)),
                exit = fadeOut(animationSpec = tween(durationMillis = 150)) +
                    shrinkVertically(animationSpec = tween(durationMillis = 150)),
            ) {
                if (nameError != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Warning,
                            contentDescription = null,
                            tint = LeaflyColors.NeedsWaterNow,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(LeaflySpacing.xs))
                        Text(
                            text = nameError.message(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = LeaflyColors.NeedsWaterNow,
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Foto
// ---------------------------------------------------------------------------

@Composable
private fun PhotoSection(
    photoData: ByteArray?,
    isProcessing: Boolean,
    message: AddPlantPhotoMessage?,
    hasCamera: Boolean,
    onPickGallery: () -> Unit,
    onTakePhoto: () -> Unit,
    onRemovePhoto: () -> Unit,
) {
    val photo = rememberPlantPhoto(photoData, maxSize = 720)

    LeaflyCard {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(LeaflySpacing.sm),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.add_plant_photo_title),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.headlineSmall,
                    color = LeaflyColors.TextPrimary,
                )
                Text(
                    text = stringResource(R.string.add_plant_optional),
                    style = MaterialTheme.typography.labelSmall,
                    color = LeaflyColors.TextTertiary,
                )
            }

            Text(
                text = stringResource(R.string.add_plant_photo_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = LeaflyColors.TextSecondary,
            )

            if (photo != null) {
                Image(
                    bitmap = photo,
                    contentDescription = stringResource(
                        R.string.add_plant_photo_preview_description,
                    ),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(LeaflyRadius.control)),
                )
            } else {
                PhotoPlaceholder(isProcessing = isProcessing)
            }

            LeaflyQuietButton(
                text = stringResource(
                    if (photoData == null) {
                        R.string.add_plant_photo_pick
                    } else {
                        R.string.add_plant_photo_change
                    },
                ),
                icon = Icons.Rounded.PhotoLibrary,
                onClick = onPickGallery,
                enabled = !isProcessing,
            )

            if (hasCamera) {
                LeaflyQuietButton(
                    text = stringResource(R.string.add_plant_photo_camera),
                    icon = Icons.Rounded.PhotoCamera,
                    onClick = onTakePhoto,
                    enabled = !isProcessing,
                )
            }

            if (photoData != null) {
                LeaflyQuietButton(
                    text = stringResource(R.string.add_plant_photo_remove),
                    icon = Icons.Rounded.DeleteOutline,
                    onClick = onRemovePhoto,
                    enabled = !isProcessing,
                    destructive = true,
                )
            }

            AnimatedVisibility(
                visible = message != null,
                enter = fadeIn(animationSpec = tween(durationMillis = 200)),
                exit = fadeOut(animationSpec = tween(durationMillis = 150)),
            ) {
                if (message != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Info,
                            contentDescription = null,
                            tint = LeaflyColors.TextSecondary,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(LeaflySpacing.xs))
                        Text(
                            text = message.message(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = LeaflyColors.TextSecondary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PhotoPlaceholder(isProcessing: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clip(RoundedCornerShape(LeaflyRadius.control))
            .background(LeaflyColors.FieldSurface),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(LeaflySpacing.xs),
        ) {
            Icon(
                imageVector = if (isProcessing) {
                    Icons.Rounded.HourglassEmpty
                } else {
                    Icons.Rounded.AddAPhoto
                },
                contentDescription = null,
                tint = LeaflyColors.Secondary.copy(alpha = 0.7f),
                modifier = Modifier.size(26.dp),
            )
            Text(
                text = stringResource(
                    if (isProcessing) {
                        R.string.add_plant_photo_processing
                    } else {
                        R.string.add_plant_photo_empty
                    },
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = LeaflyColors.TextSecondary,
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Tipo
// ---------------------------------------------------------------------------

@Composable
private fun TypeSection(
    selectedType: PlantType?,
    onSelectNone: () -> Unit,
    onToggleType: (PlantType) -> Unit,
) {
    val typeOptions: List<PlantType?> = remember { listOf<PlantType?>(null) + PlantType.entries }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(LeaflySpacing.sm),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.add_plant_type_title),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
            )
            Text(
                text = stringResource(R.string.add_plant_optional),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.72f),
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(LeaflySpacing.sm)) {
            typeOptions.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(LeaflySpacing.sm),
                ) {
                    rowItems.forEach { type ->
                        LeaflyChip(
                            title = type?.displayName
                                ?: stringResource(R.string.add_plant_type_none),
                            icon = type?.icon ?: NoPlantTypeIcon,
                            selected = selectedType == type,
                            onClick = {
                                if (type == null) onSelectNone() else onToggleType(type)
                            },
                            modifier = Modifier.weight(1f),
                        )
                    }

                    if (rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Guardar
// ---------------------------------------------------------------------------

@Composable
private fun SaveSection(
    isSaving: Boolean,
    isProcessingPhoto: Boolean,
    saveError: AddPlantError?,
    onSave: () -> Unit,
    isEditing: Boolean = false,
) {
    LeaflyBottomBar {
        AnimatedVisibility(
            visible = saveError != null,
            enter = fadeIn(animationSpec = tween(durationMillis = 200)),
            exit = fadeOut(animationSpec = tween(durationMillis = 150)),
        ) {
            if (saveError != null) {
                Text(
                    text = saveError.message(),
                    modifier = Modifier.padding(bottom = LeaflySpacing.sm),
                    style = MaterialTheme.typography.bodyMedium,
                    color = LeaflyColors.NeedsWaterNow,
                    textAlign = TextAlign.Center,
                )
            }
        }

        LeaflyPrimaryButton(
            text = stringResource(
                if (isSaving) {
                    R.string.add_plant_saving
                } else if (isEditing) {
                    R.string.edit_plant_save
                } else {
                    R.string.add_plant_save
                },
            ),
            onClick = onSave,
            enabled = !isProcessingPhoto,
            loading = isSaving,
        )
    }
}

// ---------------------------------------------------------------------------
// Errores → texto de UI
// ---------------------------------------------------------------------------

@Composable
private fun AddPlantError.message(): String = when (this) {
    AddPlantError.BlankName -> stringResource(R.string.error_name_required)
    AddPlantError.SaveFailed -> stringResource(R.string.error_save_failed)
}

@Composable
private fun AddPlantPhotoMessage.message(): String = when (this) {
    AddPlantPhotoMessage.CameraPermissionDenied ->
        stringResource(R.string.add_plant_photo_camera_permission)

    AddPlantPhotoMessage.CameraUnavailable ->
        stringResource(R.string.add_plant_photo_camera_unavailable)

    AddPlantPhotoMessage.PhotoLoadFailed ->
        stringResource(R.string.add_plant_photo_load_failed)

    AddPlantPhotoMessage.PhotoCaptureFailed ->
        stringResource(R.string.add_plant_photo_capture_failed)
}
