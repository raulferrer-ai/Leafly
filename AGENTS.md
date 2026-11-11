# AGENTS.md — Leafly

Constitución del proyecto. Léela al inicio de cada sesión.

## Qué es Leafly

App nativa dual (iOS + Android) para el cuidado de plantas domésticas: riego, recordatorios, fotos e historial.

No es multiplataforma: son dos codebases nativas coordinadas por este harness.

## Stack oficial

| Plataforma | Stack |
| ---------- | ----- |
| iOS | Swift 6 · SwiftUI · @Observable · SwiftData |
| Android | Kotlin · Jetpack Compose · ViewModel · StateFlow · Room |

Reglas de plataforma en `ios/AGENTS.md` y `android/AGENTS.md`.

## Fuente de verdad

| Artefacto | Rol |
| --------- | --- |
| `features/*.feature` | Comportamiento (Gherkin) |
| `feature_list.json` | Tablero y estados |
| `progress/current.md` | Memoria de sesión |
| `docs/architecture.md` | Arquitectura |
| `docs/design-system.md` | Diseño visual obligatorio |
| `AGENTS.md` (este archivo) | Reglas del agente y DoD |

No inventes requisitos que no estén en la spec. No contradigas el design system.

## Flujo de trabajo

1. Leer `progress/current.md` y `feature_list.json`.
2. Trabajar **una sola** feature (`in_progress`).
3. Implementar primero en una plataforma; luego en la otra.
4. Verificar comportamiento dual según la spec.
5. Pasar el gate antes de marcar `done`.
6. Actualizar `progress/current.md`.

## Definition of Done (dual)

Una feature solo puede pasar a `"status": "done"` cuando se cumplen **todos** estos puntos:

1. La spec en `features/` describe el comportamiento y se ha respetado.
2. Implementación completa en **iOS** y en **Android**.
3. Sensores de la plataforma en verde (ver sección Sensores); en el cierre dual, gate `./scripts/verify-feature.sh` en verde.
4. Checklist de comportamiento dual revisado (mismo flujo en las dos apps).
5. **Gate automático pasado:**

```bash
./scripts/verify-feature.sh
```

o:

```bash
./scripts/verify-feature.sh <feature-id>
```

Si el gate falla, **no** se cambia el status a `done`.  
No se marca `done` con solo una plataforma terminada.

## Sensores (feedback obligatorio)

Antes de dar una implementación por lista en una plataforma, el agente debe pasar los sensores de esa plataforma. Si un sensor falla, se corrige antes de seguir o de pedir marcar `done`.

### iOS

Desde la raíz del monorepo (ajusta scheme/destino si hace falta):

```bash
xcodebuild test \
  -project ios/Leafly/Leafly.xcodeproj \
  -scheme Leafly \
  -destination 'platform=iOS Simulator,name=iPhone 16'
```

Mínimo aceptable si los tests no se pueden lanzar en ese entorno: `xcodebuild build` con el mismo project/scheme en verde, y dejar constancia en `progress/current.md`.

### Android

```bash
cd android && ./gradlew :app:testDebugUnitTest
```

Opcional pero recomendable antes de `done`:

```bash
cd android && ./gradlew :app:lintDebug
```

### Gate dual

Cuando ambas plataformas están implementadas:

```bash
./scripts/verify-feature.sh
```

Los sensores son **feedback**: el agente los ejecuta (o el humano los ejecuta y pega el resultado). No se declara “listo en iOS/Android” con sensores en rojo.

## Reglas duras

- Una feature en `in_progress` a la vez.
- No implementar Android y iOS a la vez en la misma sesión salvo indicación explícita.
- Textos de producto en español, tono calmado y humano.
- Reutilizar el design system (colores, tipografía, cards, espaciado).
- Preferir arquitectura en capas: Features / Domain / Data / DesignSystem.
- Los tests no son opcionales en features con lógica de dominio o persistencia.

## Scripts del harness

| Script | Uso |
| ------ | --- |
| `./init.sh` | Verificar estructura y ver siguiente feature |
| `./scripts/verify-feature.sh` | Gate dual antes de marcar `done` |

## Al terminar una sesión

- Actualizar `progress/current.md` con estado real.
- No dejar `feature_list.json` inconsistente (p. ej. `done` sin gate).
- Si el agente se desvió de la spec: anotar el fallo y proponer regla o test (flywheel).

## Flywheel

Si en esta sesión el agente se desvía de la spec, del design system o del DoD:

1. Corregir el código.
2. Registrar el fallo con la plantilla de `docs/flywheel.md` en `progress/flywheel-log.md`.
3. Añadir regla y/o test cuando el fallo sea claro y repetible.

No cerrar la sesión solo con el fix de código si el fallo revela un agujero del harness.