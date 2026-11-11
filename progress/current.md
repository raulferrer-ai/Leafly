# Progress — Leafly

## Estado actual
MVP (6 features) en done. Harness endurecido: gate + sensores + flywheel.
Feature de prueba del harness: `empty-states-polish`.

## Feature actual
`empty-states-polish` (in_progress)

## Implementación iOS — completada y verificada

### Cambios realizados
1. **`DesignSystem/LeaflyCopy.swift`** (nuevo): Copia centralizada para estados vacíos y de error. Evita strings mágicos dispersos. Incluye:
   - `Empty.Plants`: título, subtítulo y botón de la lista vacía
   - `Empty.PhotoHistory`: título, subtítulo y botón del historial sin fotos
   - `Error.Plants`: título, mensaje y botón de reintentar para carga de lista
   - `Error.PhotoHistory`: mensaje y botón de reintentar para historial de fotos
   - `Error.Watering`: mensaje de error al regar
   - `Error.AddPhoto`: mensaje de error al guardar foto

2. **`Features/Plants/PlantsHomeView.swift`**: Estados vacíos y de error de la lista usan `LeaflyCopy`. `emptyState` usa `LeaflyCopy.Empty.Plants.*`; `errorCard` usa `LeaflyCopy.Error.Plants.*` con botón "Reintentar" que llama a `viewModel.load()`.

3. **`Features/Plants/PlantDetailView.swift`**:
   - `errorCard(message:)`: muestra `LeaflyCopy.Error.Plants.title` + mensaje + botón "Reintentar" que llama a `viewModel.markAsWatered()` (corregido: antes llamaba erróneamente a `loadPhotoHistory()`).
   - `photoErrorCard(message:)`: nuevo, con reintentar que llama a `viewModel.loadPhotoHistory()`.
   - `PlantPhotoHistorySection` recibe `photoErrorMessage` y `onRetry`.

4. **`Features/Plants/PlantPhotoHistorySection.swift`**:
   - Estado vacío usa `LeaflyCopy.Empty.PhotoHistory.*` con botón "Añadir la primera foto".
   - Nuevo `photoErrorCard` con reintentar vía `onRetry` (prioridad sobre estado vacío).
   - `onRetry` es `@MainActor () async -> Void`.

5. **`LeaflyTests/PlantDetailViewModelTests.swift`**: Añadido `testLoadPhotoHistoryRetryAfterFailureLoadsSuccessfully()` que verifica que tras un error de carga, reintentar carga las fotos correctamente.

### Sensores iOS (AGENTS.md)
- `xcodebuild build`: ✓ Verde (iPhone 17 Pro, iOS 26.1)
- `xcodebuild test -only-testing:LeaflyTests`: ✓ TEST SUCCEEDED (87 tests, 0 fallos)
- `xcodebuild test -only-testing:LeaflyUITests`: ✓ TEST SUCCEEDED (todos los tests de UI pasan)

### Sin desviaciones
- No se inventaron flujos nuevos.
- No se cambió lógica de riego, recordatorios ni persistencia.
- Se reutilizan componentes del design system (LeaflyScreen, LeaflyCard, LeaflyPrimaryButtonStyle, LeaflyQuietButtonStyle, colores, tipografía).
- Textos en español, tono calmado y humano.

## Implementación Android — completada y verificada

### Cambios realizados
1. **`designsystem/LeaflyCopy.kt`** (existente): Copia centralizada para estados vacíos y de error (equivalente a iOS). Incluye `Empty.Plants`, `Empty.PhotoHistory`, `Error.Plants`, `Error.PhotoHistory`, `Error.Watering`, `Error.AddPhoto`.

2. **`features/plants/PlantDetailViewModel.kt`**:
   - Mensaje de error de carga de fotos usa `LeaflyCopy.Error.PhotoHistory.message` (antes hardcodeado).
   - Mensaje de error de añadir foto usa `LeaflyCopy.Error.AddPhoto.message` (antes hardcodeado).

3. **`features/plants/PlantPhotoHistorySection.kt`**:
   - `EmptyState` usa `LeaflyCopy.Empty.PhotoHistory.*` (title, subtitle, button).
   - `PhotoErrorCard` usa `LeaflyCopy.Error.PhotoHistory.message` y `LeaflyCopy.Error.PhotoHistory.retry`.
   - Título del error de fotos se mantiene como `stringResource(R.string.plant_photo_history_error_title)` (consistente con iOS).

4. **`features/plants/PlantsScreen.kt`**:
   - `EmptyCard` usa `LeaflyCopy.Empty.Plants.*` (title, subtitle, button).
   - `ErrorCard` usa `LeaflyCopy.Error.Plants.*` (title, message, retry).

5. **`features/plants/PlantDetailScreen.kt`**:
   - `ErrorCard()` usa `LeaflyCopy.Error.Watering.message` (antes hardcodeado).

6. **`test/features/plants/PlantDetailViewModelTest.kt`**:
   - Test de carga de fotos con fallo verifica `LeaflyCopy.Error.PhotoHistory.message`.
   - Añadido `failOnAddPhoto` flag a `FakeDetailPlantRepository`.
   - Añadido test `addPhoto failure shows calm error message` que verifica `LeaflyCopy.Error.AddPhoto.message`.

### Sensores Android (AGENTS.md)
- `./gradlew :app:compileDebugKotlin`: ✓ BUILD SUCCESSFUL
- `./gradlew :app:testDebugUnitTest`: ✓ BUILD SUCCESSFUL (88 tests, 0 failures)
- `./gradlew :app:lintDebug`: ✓ BUILD SUCCESSFUL

### Sin desviaciones
- No se inventaron flujos nuevos.
- No se cambió lógica de riego, recordatorios ni persistencia.
- Se reutilizan componentes del design system (LeaflyCard, LeaflyQuietButton, LeaflyPrimaryButton, colores, tipografía).
- Textos en español, tono calmado y humano.

## Próximo paso
Ejecutar `./scripts/verify-feature.sh` para el gate dual. **NOTA:** El gate falla por problemas de entorno del harness, no de código:
- iOS: el script usa `name=iPhone 16` que no existe (solo `iPhone 17 Pro`). Ya verificado con `iPhone 17 Pro`.
- Android: gradlew usa Java 8 por defecto, necesita Java 21. Ya verificado con Java 21.
No marcar `done` todavía (falta gate dual pasar en entorno correcto).
