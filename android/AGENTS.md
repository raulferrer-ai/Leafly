# AGENTS.md — Android (Leafly)

## Stack obligatorio
- Kotlin 2.1+
- Jetpack Compose
- ViewModel + StateFlow (o UiState inmutable)
- Room
- Coroutines + Flow
- JUnit + Turbine (o similar) para tests de Flow

## Arquitectura
- Unidirectional Data Flow / MVVM moderno
- UiState inmutable por pantalla
- Repositorios
- Domain layer con casos de uso cuando aporte claridad

## Design System (obligatorio)
Seguir **estrictamente** `docs/design-system.md`:
- Cards muy redondeadas con sombra suave
- Mucho espacio en blanco
- Paleta de verdes naturales
- Tipografía amable y legible
- Componentes con formas orgánicas y calmadas
- Textos cercanos y humanos
- Adaptar Material 3 al design system de Leafly (no usar el look genérico de Material)

## Reglas de código
- Preferir inmutabilidad
- Evitar lógica de negocio dentro de las Composables
- Usar `remember`, `derivedStateOf` y side-effects de forma correcta
- Nombres claros y en inglés (código) / español (UI)

## Tests
- Tests unitarios de ViewModels y de la lógica de dominio
- Verificar emisiones de StateFlow

## Estilo
- Animaciones sutiles
- Buen uso de spacing y tipografía
- La UI debe transmitir calma y cuidado

## Definition of Done (Android)
- Compila limpiamente
- Cumple la spec
- Tests básicos pasan
- La UI respeta el design system
- El flujo principal funciona sin crashes