# Progress — Leafly

## Estado actual
Features completadas: onboarding-first-plant, plant-list.
`plant-detail` implementada y verificada en iOS y Android. Pendiente de revisión
conjunta para marcarla como `done`.

## Feature actual
`plant-detail` (in_progress)

## Completado
- onboarding-first-plant → done (dual)
- plant-list → done (dual)
- plant-detail → iOS implementado y verificado
- plant-detail → Android implementado y verificado

## En curso
- plant-detail → revisar ambas plataformas y decidir el paso a `done`

## iOS: plant-detail
- Detalle real (`PlantDetailView`) que sustituye al placeholder.
- Muestra nombre, tipo (si existe), badge de estado de riego y resumen
  (“Regada hoy/ayer/hace N días” o “Añadida hoy/ayer/el …”).
- Acción principal “Ya la regué”:
  - Actualiza `lastWateredAt` a ahora.
  - El estado pasa a “Al día” y el resumen a “Regada hoy”.
  - Se persiste con `PlantRepository.update(_:)` sobre SwiftData.
- Al volver, la lista refleja el cambio al instante con `PlantsViewModel.replace(_:)`.
- Error calmado si el riego no se puede guardar; botón deshabilitado mientras guarda.
- Dominio/repositorio reutilizados: `WateringStatus`, `Plant.wateringStatus`,
  `wateringSummary`.
- Tests:
  - `PlantDetailViewModelTests` (8) + `PlantsViewModelTests.replace` (2)
  - `SwiftDataPlantRepositoryTests` de `update` (2)
  - UI test `testMarkingAsWateredUpdatesDetailAndList`
  - Suite completa iOS: 40 unitarios y 13 UI tests en verde.

## Android: plant-detail
- Detalle real (`PlantDetailScreen`) que sustituye al placeholder
  (`PlantDetailPlaceholderScreen` eliminado).
- Muestra nombre, tipo (si existe), badge de estado de riego y el mismo resumen
  calmado que la lista (`plantWateringSummary`).
- Acción principal “Ya la regué” con icono de gota:
  - `MarkPlantWateredUseCase` actualiza `lastWateredAt` con reloj inyectable.
  - Se persiste con `PlantRepository.update(_:)` sobre Room (`@Update`).
  - Botón deshabilitado y spinner mientras guarda; un doble toque no repite.
  - Error calmado si el riego no se puede guardar.
- Room es la fuente de verdad: la lista se actualiza sola al volver, sin callback.
- `PlantDetailViewModel` (`StateFlow` + `PlantDetailUiState` inmutable) creado
  por planta con `viewModel(key = plant.id, factory = ...)`.
- `LeaflyPrimaryButton` admite un icono opcional para mantener el lenguaje visual.
- Tests:
  - `MarkPlantWateredUseCaseTest` (5)
  - `PlantDetailViewModelTest` (7)
  - `PlantsScreenTest` actualizado + `markingAsWateredUpdatesDetailAndList`
  - Suite Android: 46 unitarios y 5 tests de UI en verde.
- Verificación manual en emulador: “Añadida hoy” → “Ya la regué” → “Regada hoy”,
  lista actualizada al volver y cambio persistente tras reiniciar la app.

## Próximo paso
Revisar el detalle en ambas plataformas y, si todo está correcto, marcar
`plant-detail` como `done` y actualizar `feature_list.json`.
