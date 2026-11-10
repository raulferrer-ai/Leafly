# Progress — Leafly

## Estado actual
Features completadas: onboarding-first-plant, plant-list.
`plant-detail` implementada y verificada en iOS. Falta Android.

## Feature actual
`plant-detail` (in_progress)

## Completado
- onboarding-first-plant → done (dual)
- plant-list → done (dual)
- plant-detail → iOS implementado y verificado

## En curso
- plant-detail → falta implementación en Android

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
- Dominio/repositorio reutilizados: `WateringStatus`, `Plant.wateringStatus`, `wateringSummary`.
- Tests:
  - `PlantDetailViewModelTests` (8) + `PlantsViewModelTests.replace` (2)
  - `SwiftDataPlantRepositoryTests` de `update` (2)
  - UI test `testMarkingAsWateredUpdatesDetailAndList`
  - Suite completa iOS: unitarios y UI tests en verde.

## Próximo paso
Implementar `plant-detail` en Android (Kotlin + Compose + Room) siguiendo la misma spec.
Después, marcar la feature como `done` cuando esté verificada en ambas plataformas.
