# Leafly

App nativa dual (**iOS · SwiftUI** y **Android · Jetpack Compose**) para el cuidado de plantas domésticas.

No es un experimento multiplataforma. Son **dos implementaciones nativas** coordinadas por un **harness** compartido: especificaciones, reglas, criterios de hecho y validación.

> Hipótesis: con agentes de IA podemos **compartir el conocimiento del producto**, no necesariamente el código de la UI.

---

## Por qué existe este repo

Durante años, iOS + Android implicaba elegir: dos equipos nativos o una base de código compartida (Flutter, React Native, KMP…).

Leafly explora una tercera vía de trabajo:

- **Mismo producto** (specs Gherkin, design system, Definition of Done dual)
- **Código nativo separado** por plataforma
- **Un agente por sesión**, centrado en una plataforma, guiado por el harness

El objetivo del proyecto es **aprender y documentar harness engineering** aplicado al desarrollo dual nativo — no publicar en las stores.

Artículos relacionados:

- [Artículo 1 — Harness Engineering for Dual-Native Mobile Development: Beyond AI Coding Assistants](https://raulferrergarcia.com/posts/harness-engineering-for-dual-native-development/harness-engineering-dual-native-mobile/)
- [Artículo 2 — Endurecer el harness (gates, sensores, flywheel)](URL_ARTICULO_2) *(opcional)*

---

## Stack

| Plataforma | Tecnología |
|------------|------------|
| **iOS** | Swift 6 · SwiftUI · @Observable · SwiftData |
| **Android** | Kotlin · Jetpack Compose · ViewModel · StateFlow · Room |
| **Harness** | `AGENTS.md`, specs Gherkin, `feature_list.json`, `progress/`, gate `verify-feature.sh`, CI |

---

## Qué hay en el producto (MVP)

- Onboarding y alta de la primera planta
- Lista con estado de riego (Al día / Riega pronto / Necesita agua)
- Detalle + «Ya la regué»
- Recordatorios locales (hora configurable en ajustes)
- Foto principal + historial de fotos
- Búsqueda y filtro por estado
- Editar y eliminar planta
- Estados vacíos y de error calmados

Todo implementado **en iOS y en Android**, con el mismo contrato de comportamiento.

---

## Estructura del harness

```text
leafly/
├── AGENTS.md                 # Constitución del proyecto (DoD dual, sensores, reglas)
├── feature_list.json         # Tablero de features y estados
├── features/                 # Specs Gherkin (fuente de verdad del comportamiento)
├── progress/                 # Memoria de sesión + flywheel log
├── docs/
│   ├── architecture.md
│   ├── design-system.md
│   └── flywheel.md
├── scripts/
│   └── verify-feature.sh     # Gate: no "done" sin verificación dual
├── init.sh                   # Chequeo de estructura y estado
├── ios/                      # App nativa iOS (+ ios/AGENTS.md)
└── android/                  # App nativa Android (+ android/AGENTS.md)
```

### Flujo de una feature

1. Spec en `features/*.feature`
2. Entrada en `feature_list.json` → `in_progress`
3. Implementación **solo en una plataforma** (sesión iOS o Android)
4. Sensores de esa plataforma (tests / build)
5. La otra plataforma
6. Checklist dual de comportamiento
7. `./scripts/verify-feature.sh <feature-id>`
8. Solo entonces → `done`

---

## Cómo arrancar

### Requisitos

- **iOS:** Xcode reciente, simulador iPhone
- **Android:** Android Studio, JDK 17+, emulador o dispositivo
- **Harness:** `bash`, `python3` (para el gate)

### Comprobar el harness

```bash
./init.sh
```

### iOS

Abrir `ios/Leafly/Leafly.xcodeproj` en Xcode, scheme **Leafly**, Run.

Tests:

```bash
xcodebuild test \
  -project ios/Leafly/Leafly.xcodeproj \
  -scheme Leafly \
  -destination 'platform=iOS Simulator,name=iPhone 16'
```

(Ajusta el nombre del simulador al que tengas.)

### Android

```bash
cd android
./gradlew :app:installDebug
# o
./gradlew :app:testDebugUnitTest
```

### Gate dual (antes de marcar una feature como done)

```bash
./scripts/verify-feature.sh
# o
./scripts/verify-feature.sh <feature-id>
```

---

## CI

GitHub Actions en cada push/PR a `main`:

- Chequeo del harness (`./init.sh`)
- Unit tests Android (JDK 17)
- Build iOS (macOS runner)

---

## Tags y ramas

| Ref | Significado |
|-----|------------|
| `main` | Línea estable del MVP + harness |
| `v1.0-harness-mvp` | Punto fijo recomendado para enlazar en el artículo |
| `harness-hardening` | Histórico del trabajo de gates/sensores/flywheel (si se conserva) |

```bash
git tag -a v1.0-harness-mvp -m "Dual native MVP + harness engineering"
git push origin v1.0-harness-mvp
```

---

## Principios que guiaron el trabajo

- **Una feature en progreso** a la vez
- **Una plataforma por sesión** del agente
- **Done = dual** (iOS y Android), no «casi listo en una»
- **Specs antes que código**; el design system no se improvisa
- **Sensores y gate** como feedback, no como adorno
- Textos de producto en **español**, tono calmado

---

## Qué no es este proyecto

- No es una app multiplataforma (no Flutter / RN / KMP de UI)
- No está orientado a publicación en App Store / Play (sin release signing ni store assets de producción)
- No es un framework de agentes: es un **caso de estudio** de harness aplicado

---

## Autor

Proyecto de aprendizaje y escritura sobre **harness engineering** y desarrollo dual nativo.

Si el enfoque te resulta útil, el detalle narrativo está en los artículos del blog enlazados arriba.
