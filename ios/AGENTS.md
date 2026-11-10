# AGENTS.md — iOS (Leafly)

## Stack obligatorio
- Swift 6
- SwiftUI
- Observation framework (`@Observable`)
- SwiftData
- async/await
- XCTest

## Arquitectura
- MVVM ligero + Domain layer
- ViewModels marcados con `@Observable`
- Repositorios para acceso a datos
- Modelos de dominio separados de los modelos de SwiftData cuando tenga sentido

## Design System (obligatorio)
Seguir **estrictamente** `docs/design-system.md`:
- Cards muy redondeadas con sombra suave
- Mucho espacio en blanco
- Paleta de verdes naturales
- Tipografía amable y legible
- Componentes con formas orgánicas y calmadas
- Textos cercanos y humanos

## Reglas de código
- Preferir valor semántico y legibilidad sobre cleverness
- Usar `async/await` de forma idiomática
- Evitar force unwraps
- Nombres claros y en inglés (código) / español (UI)
- Las vistas deben ser lo más declarativas posible

## Tests
- Escribir tests unitarios para la lógica de dominio y ViewModels
- Comando típico: `xcodebuild test ...` (ajustar según el esquema del proyecto)

## Estilo
- Seguir las Human Interface Guidelines
- Animaciones sutiles y naturales
- Evitar sobrecargar la pantalla
- La UI debe transmitir calma y cuidado

## Definition of Done (iOS)
- Compila sin warnings relevantes
- Cumple la spec
- Tests básicos pasan
- La UI respeta el design system
- Se puede navegar el flujo principal sin crashes