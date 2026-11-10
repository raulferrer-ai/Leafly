# Progress — Leafly

## Estado actual
Features completadas: onboarding, plant-list, plant-detail, watering-reminders.
`add-plant-photo` implementada y verificada en iOS. Falta Android.

## Feature actual
`add-plant-photo` (in_progress)

## Completado
- onboarding-first-plant → done
- plant-list → done
- plant-detail → done
- watering-reminders → done

## En curso
- add-plant-photo
  - iOS → implementado y verificado
  - Android → pendiente

## Implementación iOS
- Dominio / persistencia:
  - `Plant.photoData: Data?` (foto ya procesada, opcional).
  - `PlantEntity.photoData` con `@Attribute(.externalStorage)`: la imagen se
    guarda fuera de la base de datos para mantenerla ligera.
  - `SwiftDataPlantRepository.update` también actualiza la foto.
- Procesado de imagen:
  - `PlantPhotoProcessor`: reduce el lado más largo a 1400 px y comprime en JPEG
    (calidad 0.8). Mantiene la proporción y deja las fotos pequeñas intactas.
- Alta de planta:
  - `AddPlantView` añade una card "La foto de tu planta" (opcional) con:
    - `PhotosPicker` para elegir o cambiar la foto de la galería.
    - Botón de cámara solo si `UIImagePickerController` la considera disponible.
    - Vista previa de la foto elegida y botón "Quitar foto".
    - Mensaje calmado si se deniega el permiso de cámara; galería y guardado
      sin foto siguen disponibles.
  - `CameraPicker`: `UIViewControllerRepresentable` sobre
    `UIImagePickerController`. Cancelar la cámara no descarta una foto ya elegida.
  - `AddPlantViewModel` mantiene la foto solo en memoria (`setPhotoData`,
    `removePhoto`) y la incluye al guardar. Cancelar el flujo no persiste nada.
  - Permiso de cámara con `AVCaptureDevice.authorizationStatus` /
    `requestAccess`; `NSCameraUsageDescription` añadido al Info.plist generado.
  - La galería usa `PhotosPicker`, que no requiere permiso de fototeca.
- Visualización:
  - `PlantAvatar`: avatar circular compartido que muestra la foto si existe y,
    si no, el icono del tipo de planta.
  - Se usa en `PlantCard` (lista) y en `PlantDetailView` (detalle).
  - `PlantPhotoDecoder` cachea las imágenes decodificadas (límite de 24).

## Tests
- `AddPlantViewModelTests`: guardar con foto, no persistir hasta guardar,
  quitar foto y guardar sin foto.
- `PlantPhotoProcessorTests`: reducción con proporción, fotos pequeñas intactas
  y datos inválidos.
- `SwiftDataPlantRepositoryTests`: persistencia de la foto al añadir y al
  actualizar (incluido quitarla).
- `OnboardingUITests`: la sección de foto es opcional y se puede guardar sin foto.

## Verificación
- `xcodebuild build` → OK, sin warnings relevantes.
- Suite completa (unit + UI) → **84 tests, 0 fallos**.
- Info.plist generado contiene `NSCameraUsageDescription`.
- Verificación manual/E2E de galería en simulador:
  - Se selecciona una foto con `PhotosPicker` y aparece la vista previa.
  - Al guardar, la foto se muestra en el avatar de la lista y del detalle.
  - El test E2E temporal se retiró para no depender de que el simulador tenga
    fotos ni de coordenadas fijas.

## Próximo paso
Implementar `add-plant-photo` en Android y, cuando ambas plataformas estén
verificadas, valorar el cierre de la feature (`done`).
