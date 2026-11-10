# Progress — Leafly

## Estado actual
Feature `onboarding-first-plant` completada (iOS + Android).
`plant-list` implementada y verificada en iOS. Falta Android.

## Feature actual
`plant-list` (in_progress)

## Completado
- onboarding-first-plant → done (dual)
- plant-list → iOS implementado y verificado (pendiente Android)

## En curso
- plant-list → falta implementación en Android

## iOS: plant-list
- Lista de plantas con nombre, tipo opcional y estado de riego.
- Estado de riego calculado desde `lastWateredAt` (o `createdAt` si aún no se ha regado):
  - 0–4 días → “Al día”
  - 5–6 días → “Riega pronto”
  - 7+ días → “Necesita agua”
- Navegación a detalle placeholder (`PlantDetailPlaceholderView`).
- Añadir planta desde la lista mediante sheet, reutilizando `AddPlantView`.
- Estados de carga, error con reintento y lista vacía.
- Tests unitarios (dominio, ViewModel, persistencia) y UI tests de lista, navegación y alta.

## Próximo paso
Implementar `plant-list` en Android (Kotlin + Compose + Room) siguiendo la misma spec y el design system.
