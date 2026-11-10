# Architecture — Leafly

## Principios
- Arquitectura limpia y testeable
- Separación clara de capas
- Persistencia local primero (MVP sin backend)
- Una sola fuente de verdad por plataforma
- Código moderno y mantenible

## iOS (Swift 6 + SwiftUI)
- **UI**: SwiftUI + Observation framework (`@Observable`)
- **Estado**: ViewModels observables
- **Persistencia**: SwiftData
- **Concurrencia**: async/await nativo
- **Patrón**: MVVM ligero + Domain layer simple
- Estructura recomendada:
  - `App/`
  - `Features/` (una carpeta por feature)
  - `Domain/` (modelos y casos de uso)
  - `Data/` (SwiftData models + repositories)
  - `DesignSystem/`

## Android (Kotlin + Compose)
- **UI**: Jetpack Compose
- **Estado**: ViewModel + StateFlow / UiState
- **Persistencia**: Room
- **Concurrencia**: Coroutines + Flow
- **Patrón**: MVVM / Unidirectional Data Flow
- Estructura recomendada:
  - `app/`
  - `feature/` (una por feature)
  - `domain/`
  - `data/` (Room + repositories)
  - `designsystem/`

## Decisiones compartidas
- Los modelos de dominio deben ser lo más parecidos posible entre plataformas.
- Las reglas de negocio (cuándo regar, etc.) viven en el domain layer.
- No se comparte código entre iOS y Android en el MVP (native edges).
- Cada plataforma es responsable de su propia implementación de la misma spec.