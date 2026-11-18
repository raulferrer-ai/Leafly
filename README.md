# Leafly

App nativa dual (**iOS · SwiftUI** y **Android · Jetpack Compose**) para el cuidado de plantas domésticas.

No es un experimento multiplataforma. Son **dos implementaciones nativas** coordinadas por un **harness** compartido: especificaciones, reglas, criterios de hecho y validación.

> Hipótesis: con agentes de IA podemos **compartir el conocimiento del producto**, no necesariamente el código de la UI.

---

## Por qué existe este repo

Durante años, iOS + Android implicaba elegir: dos equipos nativos o una base de código compartida (Flutter, React Native, KMP…).

Leafly explora otra vía:

- **Mismo producto** (specs Gherkin, design system, Definition of Done dual)
- **Código nativo separado** por plataforma
- **Un agente por sesión**, centrado en una plataforma, guiado por el harness

El objetivo es **aprender y documentar harness engineering** aplicado al desarrollo dual nativo — no publicar en las stores.

**Artículos**

- [1 — Harness engineering for dual-native mobile](https://raulferrergarcia.com/posts/harness-engineering-for-dual-native-development/harness-engineering-dual-native-mobile/)
- [2 — From declared done to demonstrated done](URL_ARTICULO_2)
- [3 — From gate to loop](URL_ARTICULO_3)

---

## Stack

| Plataforma | Tecnología |
|------------|------------|
| **iOS** | Swift 6 · SwiftUI · @Observable · SwiftData |
| **Android** | Kotlin · Jetpack Compose · ViewModel · StateFlow · Room |
| **Harness** | `AGENTS.md`, specs Gherkin, `feature_list.json`, `progress/`, gate, CI |

---

## Versiones

| Tag | Qué representa |
|-----|----------------|
| [v1.0-harness-mvp](https://github.com/raulferrer-ai/Leafly/tree/v1.0-harness-mvp) | MVP dual + harness (orientación + gate) |
| [v2.0-loop-mvp](https://github.com/raulferrer-ai/Leafly/tree/v2.0-loop-mvp) | Cierre en modo Loop: sensor en verde antes de dar plataforma por lista; gate antes de `done` |

En **2.0**, una feature no se marca `done` si el agente solo declara que está lista: hace falta el ciclo build/test y el gate dual.

---

## Qué hay en el producto

- Onboarding y alta de la primera planta
- Lista con estado de riego + búsqueda y filtros
- Detalle, editar, eliminar
- Ya la regué + historial de riegos (2.0)
- Recordatorios locales (hora configurable)
- Foto principal + historial de fotos
- Ajustes de notificaciones
- Estados vacíos y de error calmados

Todo en **iOS y Android**, mismo contrato de comportamiento.

---

## Estructura del harness

```text
leafly/
├── AGENTS.md                 # Reglas, DoD dual, sensores, Loop (2.x)
├── feature_list.json         # Tablero de features
├── features/                 # Specs Gherkin
├── progress/                 # Sesión + flywheel log
├── docs/
│   ├── architecture.md
│   ├── design-system.md
│   └── flywheel.md
├── scripts/
│   └── verify-feature.sh     # Gate dual antes de done
├── init.sh
├── ios/
└── android/
```

### Flujo de una feature (1.x a 2.x)

1. Spec en `features/*.feature`
2. `in_progress` en `feature_list.json`
3. Implementación en **una** plataforma
4. **Sensor** de esa plataforma en verde (si falla, corregir; no listo en rojo)
5. La otra plataforma + su sensor
6. Checklist dual de comportamiento
7. `./scripts/verify-feature.sh <feature-id>`
8. Solo entonces `done`

### Loop (2.x)

Documentado en `AGENTS.md`: el agente no reporta plataforma terminada con tests/build en rojo; `done` exige gate dual.

---

## Cómo arrancar

### Requisitos

- **iOS:** Xcode, simulador iPhone
- **Android:** Android Studio, **JDK 17+**, emulador o dispositivo
- **Harness:** `bash`, `python3`

### Harness

```bash
./init.sh
```

### iOS

Abrir `ios/Leafly/Leafly.xcodeproj`, scheme **Leafly**, Run.

```bash
xcodebuild test \
  -project ios/Leafly/Leafly.xcodeproj \
  -scheme Leafly \
  -destination 'platform=iOS Simulator,name=iPhone 16'
```

(Ajusta el nombre del simulador.)

### Android

```bash
export JAVA_HOME=…   # JDK 17+, no Java 8
cd android
./gradlew :app:testDebugUnitTest
```

### Gate dual

```bash
./scripts/verify-feature.sh
./scripts/verify-feature.sh <feature-id>
```

---

## CI

En cada push/PR a `main`:

- `./init.sh`
- Unit tests Android (JDK 17)
- Build iOS (macOS runner)

---

## Principios

- Una feature en progreso a la vez
- Una plataforma por sesión del agente
- **Done = dual** (iOS + Android) + gate
- Specs antes que código; design system obligatorio
- Sensores y gate como feedback, no como adorno
- Textos de producto en español, tono calmado
- En 2.x: **loop** — no avanzar el estado del producto en rojo

---

## Qué no es este proyecto

- No es multiplataforma de UI (no Flutter / RN / KMP de interfaz)
- No está orientado a App Store / Play
- No es un framework de agentes: es un **caso de estudio** de harness a loop

---

## Autor

Proyecto de aprendizaje y escritura sobre **harness engineering** y desarrollo dual nativo.

Detalle narrativo en los artículos del blog enlazados arriba.
