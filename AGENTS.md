# AGENTS.md — Leafly (Plant Care Assistant)

App nativa dual (iOS + Android) que ayuda a las personas a cuidar sus plantas domésticas mediante recordatorios inteligentes, cuidados claros e historial visual.

## Objetivo
Construir una experiencia simple, calmada y útil. La app debe transmitir tranquilidad, no complejidad.

## Stack oficial
- **iOS**: Swift 6 + SwiftUI + Observation (`@Observable`) + SwiftData
- **Android**: Kotlin 2.1+ + Jetpack Compose + ViewModel + StateFlow + Room
- Persistencia local (sin backend en el MVP)
- Notificaciones locales
- Cámara nativa de cada plataforma

## Design System
Seguir **estrictamente** las reglas definidas en `docs/design-system.md`.
La interfaz debe sentirse suave, moderna, orgánica y calmada (cards redondeadas, mucho espacio en blanco, verdes naturales, sombras suaves).

## Antes de cualquier trabajo
1. Ejecuta `./init.sh` y verifica que termina limpio.
2. Lee `progress/current.md`.
3. Elige **solo UNA** feature con estado `pending` o `in_progress` de `feature_list.json`.
4. Lee la spec completa correspondiente en `features/`.
5. Si la feature es dual, implementa primero en iOS y luego en Android (o al revés, pero nunca las dos a la vez sin terminar una).

## Reglas duras
- Una feature a la vez.
- Nunca marques una feature como `done` hasta que esté implementada y verificada en **ambas** plataformas.
- No inventes requisitos. Si no está en la spec, pregunta.
- Prioriza código limpio, testeable y mantenible.
- Los textos de la UI deben ser claros, humanos y calmados.
- Toda la interfaz debe respetar el design system definido en `docs/design-system.md`.
- Sigue las mejores prácticas de cada plataforma (ver `ios/AGENTS.md` y `android/AGENTS.md`).

## Definition of Done (dual)
Una feature solo se marca `done` cuando:
- Cumple la spec en iOS y en Android
- Tests unitarios básicos pasan
- El flujo principal se puede recorrer sin crashes
- La UI respeta el design system
- No hay deuda técnica evidente introducida
- `feature_list.json` y `progress/current.md` están actualizados

## Routing de plataforma
- Si la tarea dice solo iOS → trabaja en `ios/`
- Si dice solo Android → trabaja en `android/`
- Si dice "ambas", "dual" o no especifica → implementa en las dos (recomendado: primero iOS)

## Archivos clave
- `docs/architecture.md` → decisiones arquitectónicas compartidas
- `docs/design-system.md` → colores, tipografía, componentes y tono (obligatorio)
- `ios/AGENTS.md` y `android/AGENTS.md` → reglas específicas de plataforma