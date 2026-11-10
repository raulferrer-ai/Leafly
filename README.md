# Leafly

App nativa dual (iOS + Android) para cuidar plantas domésticas.

Misma experiencia en ambas plataformas: onboarding, lista con estado de riego, detalle, recordatorios locales, foto al añadir planta e historial visual.

---

## Qué es

Leafly ayuda a no olvidar el riego. Añades tus plantas, ves de un vistazo cuáles necesitan agua, marcas cuando las riegas y recibes recordatorios locales. Opcionalmente puedes adjuntar fotos y seguir cómo crecen.

No es un wrapper multiplataforma: son **dos apps nativas** (SwiftUI + Jetpack Compose) coordinadas por el mismo harness.

---

## Stack

| | iOS | Android |
|---|-----|---------|
| UI | SwiftUI | Jetpack Compose |
| Lenguaje | Swift 6 | Kotlin |
| Estado | `@Observable` | ViewModel + StateFlow |
| Persistencia | SwiftData | Room |
| Notificaciones | UserNotifications | AlarmManager + NotificationManager |

---

## Harness engineering

El proyecto se desarrolla con un harness compartido:

- **`AGENTS.md`** — reglas del agente, Definition of Done dual, routing de plataforma
- **`features/*.feature`** — specs en Gherkin (fuente de verdad del comportamiento)
- **`feature_list.json`** — tablero de features y estados
- **`progress/current.md`** — memoria de sesión
- **`docs/architecture.md`** + **`docs/design-system.md`** — decisiones compartidas
- **`ios/AGENTS.md`** / **`android/AGENTS.md`** — reglas específicas de cada plataforma

Regla dura: **una feature no se marca `done` hasta que está implementada y verificada en iOS y en Android.**

---

## Features del MVP

1. Onboarding y primera planta  
2. Lista con estado de riego  
3. Detalle + “Ya la regué”  
4. Recordatorios locales  
5. Foto al añadir planta  
6. Historial visual de fotos  

Todas en **done** en ambas plataformas.

---

## Estructura

```text
leafly/
├── AGENTS.md
├── feature_list.json
├── progress/
├── docs/
├── features/          # Specs Gherkin compartidas
├── ios/               # Xcode / SwiftUI
└── android/           # Gradle / Compose
```

## Cómo arrancar

```bash
./init.sh
```
Verifica la estructura del harness y muestra la siguiente feature pendiente (si las hay).

- iOS: abrir ios/Leafly.xcodeproj en Xcode
- Android: abrir la carpeta android/ en Android Studio

## Design system

Interfaz calmada y orgánica: verdes naturales, cards muy redondeadas, mucho espacio en blanco, textos humanos. Definido en docs/design-system.md y aplicado de forma equivalente en ambas plataformas.

## Por qué este repo

Ejemplo práctico de:
- Desarrollo dual nativo (no multiplataforma)
- Coordinación con harness engineering (specs compartidas + DoD dual)
- Arquitectura moderna en cada plataforma sin compartir código de UI
Construido de forma incremental, feature a feature, con tests en iOS y Android.