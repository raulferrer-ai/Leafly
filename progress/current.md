# Progress — Leafly

## Estado actual
Features completadas: onboarding, plant-list, plant-detail, watering-reminders, add-plant-photo.

## Feature actual
### plant-photo-history → in_progress (iOS)
Última feature del MVP (prioridad 6). Implementada solo en iOS; no se toca Android.

## Completado
### onboarding-first-plant → done
### plant-list → done
### plant-detail → done
### watering-reminders → done
### add-plant-photo → done
- iOS: cámara/galería, photoData, avatar en lista y detalle
- Android: TakePicture + PickVisualMedia, Room v3, mismo comportamiento
- Verificación dual superada

## plant-photo-history (iOS) — resumen de la implementación
### Dominio / Persistencia
- `PlantPhoto` (dominio): `id`, `plantID`, `addedAt`, `data` (JPEG ya procesado).
- `PlantPhotoEntity` (SwiftData): `@Model`, `@Attribute(.externalStorage) photoData`, `@Relationship(deleteRule: .cascade)` hacia `PlantEntity`.
- `PlantEntity.photos`: relación `@Relationship(deleteRule: .cascade, inverse: \PlantPhotoEntity.plant)`.
- `LeaflyModelContainer`: registra `PlantPhotoEntity` junto a `PlantEntity`.
- `PlantRepository` expone `fetchPhotos(for:)` (ordenadas de más reciente a más antigua) y `addPhoto(_:)` (establece `photoData` de la planta a la nueva foto).
- `SwiftDataPlantRepository` implementa ambos métodos.

### ViewModel (`PlantDetailViewModel`)
- Estado: `photos`, `isLoadingPhotos`, `isAddingPhoto`, `photoErrorMessage`.
- `loadPhotoHistory()` carga el historial ordenado.
- `addPhoto(_ data:)` guarda, inserta primero y actualiza `plant.photoData`.
- `markAsWatered()` usa `var wateredPlant = plant` para no perder `photoData`.

### UI (`PlantPhotoHistorySection`)
- Historial horizontal con miniaturas y leyenda de fecha (`PlantPhotoDateLabel`).
- Estado vacío calmado + botón "Añadir la primera foto".
- Botón "Añadir otra foto" que abre diálogo: "Hacer una foto", "Elegir de la galería", "Cancelar".
- Reusa `PlantPhotoProcessor`, `CameraPicker`, `CameraAccess` (permiso denegado sin fallar).
- Integrada en `PlantDetailView` con `.task` y `onPlantUpdated`.

### Tests
- `PlantDetailViewModelTests`: 7 tests de historial (carga, orden, fallo, añadir, agua conserva foto).
- `SwiftDataPlantRepositoryTests`: 5 tests de persistencia (vacío, añadir, orden más reciente primero, planta desconocida, actualizar conserva historial).
- `PlantPhotoDateLabelTests`: 4 tests (Hoy, Ayer, día/mes, año otro año).
- `PlantPhotoHistoryUITests`: empty-state con cancel (dismiss por fuera), historial ordenado con fechas.
- `UITestSeed` (DEBUG): siembra fotos con `-seedPhotoHistoryForUITests`.

## Verificación
- Build: ✓ (iPhone 17 Pro, iOS 26.1)
- Unit tests (LeaflyTests): 87 passed, 0 failed.
- UI tests (LeaflyUITests): todos pasan, incluidos `testEmptyHistoryOffersAFirstPhotoAndCancelKeepsItEmpty` y `testHistoryShowsPhotosFromNewestToOldestWithDates`.

## Próximo paso
Revisar diff final de iOS. Feature se mantiene `in_progress` (no se marca `done`).
