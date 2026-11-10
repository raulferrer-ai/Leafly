# Progress — Leafly

## Estado actual
- `onboarding-first-plant`: implementada y verificada en **iOS**.
- Android: pendiente.
- La feature **no se marca como `done`** hasta completar y verificar también Android.

## Feature actual
`onboarding-first-plant` — iOS completado, Android pendiente.

## Qué está hecho en iOS
- Pantalla de bienvenida calmada con el mensaje de propósito de la app y el botón principal «Añadir mi primera planta».
- Pantalla para añadir la primera planta:
  - Nombre obligatorio.
  - Tipo de planta opcional elegido de una lista básica (Monstera, Ficus, Potos, Cactus, Suculenta, Helecho, Orquídea).
  - Acción de cancelar que vuelve a la bienvenida sin guardar nada.
  - Validación clara y sin guardado cuando falta el nombre.
- Persistencia local con SwiftData (`PlantEntity` + `SwiftDataPlantRepository`).
- Modelo de dominio `Plant` / `PlantType` independiente de SwiftData.
- `RootView` decide el arranque:
  - Sin plantas → onboarding.
  - Con plantas → lista de plantas.
- Design system ampliado: colores, tipografía, espaciados, fondo degradado, cards, chips y estilos de botón.
- Tests unitarios (ViewModel y repositorio) y UI (alta, cancelación y validación) en verde.
- Proyecto configurado en Swift 6 (lenguaje) con SwiftUI + Observation + SwiftData.

## Próximo paso
1. Implementar `onboarding-first-plant` en Android (Kotlin + Compose + Room).
2. Recorrer el flujo completo en ambas plataformas.
3. Solo entonces marcar la feature como `done` en `feature_list.json`.

## Notas
- Comando de verificación iOS:
  `xcodebuild -project ios/Leafly/Leafly.xcodeproj -scheme Leafly -destination 'platform=iOS Simulator,name=iPhone 17 Pro' test`
- Los UI tests usan el argumento de lanzamiento `-useInMemoryStoreForUITests` para partir siempre de un jardín vacío.
- El flujo se ha validado visualmente en vertical; la app mantiene el soporte de landscape del proyecto.
- Siguiente feature pendiente tras esta: `plant-list`.
