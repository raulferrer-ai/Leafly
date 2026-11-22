# Leafly — Matriz de paridad iOS / Android

Documento de referencia para auditar y presentar la paridad funcional, de reglas de negocio y de arquitectura entre las dos implementaciones nativas de **Leafly** (**iOS · Swift 6 / SwiftUI / SwiftData** y **Android · Kotlin / Jetpack Compose / Room**).

El proyecto no comparte código de interfaz ni usa un *runtime* multiplataforma: comparte especificaciones Gherkin (`features/*.feature`), criterios del *design system* (`docs/design-system.md`), reglas de trabajo (`AGENTS.md`, `ios/AGENTS.md`, `android/AGENTS.md`) y puertas de verificación (`scripts/verify-feature.sh`).

## Reglas compartidas de dominio y producto

* **Idioma y tono de la interfaz**: textos en español, tono calmado, cercano y humano, sin alertas agresivas ni lenguaje técnico frío.
* **Estados de riego**: cálculo basado en la antigüedad respecto a `lastWateredAt` (`0–4` días → *Al día*, `5–6` días → *Riega pronto*, `7+` días → *Necesita agua*), reflejado de forma coherente en lista, detalle y programación de recordatorios.
* **Recordatorios locales**: solo se programan para plantas en *Riega pronto* o *Necesita agua*; no se notifica a plantas *Al día*; al marcar *Ya la regué*, se actualiza `lastWateredAt`, se añade entrada al historial de riegos y se cancela o reprograma el recordatorio aplicando la hora preferida configurada y su regla de margen.
* **Permisos del sistema**: si el usuario deniega permisos de cámara o de notificaciones (incluyendo Android 13+), la aplicación no falla y ofrece alternativas calmadas (galería, guardar/salir sin foto o acceso a ajustes del sistema).
* **Borrado y consistencia de datos**: eliminar una planta requiere confirmación calmada, cancela recordatorios asociados y elimina o deja inaccesibles fotos e historial de riegos vinculados a esa planta.

## Matriz de paridad por feature (12/12 `done`)

| # | Feature ID | Spec | iOS (SwiftUI · SwiftData) | Android (Compose · Room) | Verificación y notas de paridad |
| :- | :--- | :--- | :--- | :--- | :--- |
| 01 | `onboarding-first-plant` | `features/01-onboarding-first-plant.feature` | Pantalla de bienvenida calmada, alta inicial en SwiftData y navegación a lista | Pantalla de bienvenida calmada, alta inicial en Room y navegación a lista | Nombre obligatorio, validación sin guardar, cancelar sin persistir y transición a lista |
| 02 | `plant-list` | `features/02-plant-list.feature` | Lista de plantas con nombre, estado de riego, acción de alta y navegación a detalle | Lista de plantas con nombre, estado de riego, acción de alta y navegación a detalle | Mismos estados visuales de riego y estética de cards redondeadas en español |
| 03 | `plant-detail` | `features/03-plant-detail.feature` | Detalle con nombre, tipo opcional, estado de riego, último riego y acción *Ya la regué* | Detalle con nombre, tipo opcional, estado de riego, último riego y acción *Ya la regué* | Actualiza `lastWateredAt`, pasa a *Al día*, persiste y refresca la lista al volver |
| 04 | `watering-reminders` | `features/04-watering-reminders.feature` | Recordatorios locales mediante `UserNotifications` y servicio dedicado | Recordatorios locales mediante canal, `AlarmManager`/scheduler y servicio dedicado | Solo plantas con sed; cancelación/reprogramación al regar; sin error con permiso denegado |
| 05 | `add-plant-photo` | `features/05-add-plant-photo.feature` | Foto opcional desde cámara o galería persistida en local y visible en lista/detalle | Foto opcional desde cámara o galería persistida en local y visible en lista/detalle | Foto opcional, cancelar no crea planta ni guarda foto, cámara denegada no bloquea el flujo |
| 06 | `plant-photo-history` | `features/06-plant-photo-history.feature` | Historial de fotos ordenado de más reciente a más antigua con fecha y actualización de avatar | Historial de fotos ordenado de más reciente a más antigua con fecha y actualización de avatar | Estado vacío calmado con CTA para añadir primera foto, alta desde cámara/galería y cancelación limpia |
| 07 | `empty-states-polish` | `features/07-empty-states-polish.feature` | Estados vacíos (lista e historial) y error recuperable con copy centralizado (`LeaflyCopy`) y DS | Estados vacíos (lista e historial) y error recuperable alineados al DS y copy centralizado | Mensajes no técnicos en español, acción de reintentar y tono calmado en ambas apps |
| 08 | `plant-search-filter` | `features/08-plant-search-filter.feature` | Búsqueda por nombre insensible a mayúsculas y filtros por estado de riego | Búsqueda por nombre insensible a mayúsculas y filtros por estado de riego | Combinación de búsqueda + filtro, estado sin resultados con acción para limpiar y restauración completa |
| 09 | `settings-notifications` | `features/09-settings-notifications.feature` | Ajustes con estado de permiso iOS, hora preferida persistida y acceso a ajustes del sistema | Ajustes con estado de permiso Android 13+, hora preferida persistida y acceso a ajustes | Misma hora configurada y regla de margen; sin pedir permisos de forma agresiva sin plantas con sed |
| 10 | `edit-plant` | `features/10-edit-plant.feature` | Edición desde detalle precargando nombre, tipo y foto principal | Edición desde detalle precargando nombre, tipo y foto principal | Nombre obligatorio, cambio o retirada de foto, persistencia en repositorio y cancelar sin cambios |
| 11 | `delete-plant` | `features/11-delete-plant.feature` | Confirmación calmada, borrado en SwiftData, cascada de fotos y cancelación de recordatorio | Confirmación calmada, borrado en Room, cascada (`plant_photos`), cancelación de recordatorio | Verificar limpieza/inaccesibilidad del historial de riegos (Feature 12) y retorno al flujo sin plantas |
| 12 | `watering-log` | `features/12-watering-log.feature` | Historial de riegos persistido en SwiftData, orden más reciente primero y fechas humanas | Historial de riegos persistido en Room, orden más reciente primero y fechas humanas | *Ya la regué* añade entrada; sin riegos muestra estado vacío calmado sin inventar fechas |

## Brechas conocidas y puntos de auditoría para entrevistas

* **Paridad de cobertura de tests unitarios**: existe diferencia en el número y tipo de ficheros de test entre plataformas (por ejemplo, tests de ajustes, preferencias de notificación o repositorio en iOS frente a las suites actuales en Android), documentada como entrada abierta en `progress/flywheel-log.md`.
* **Cascada completa al eliminar una planta (`11` + `12`)**: conviene confirmar en ambas plataformas que borrar una planta elimina o deja inaccesible no solo sus fotos y recordatorios, sino también su historial de riegos.
* **Validación humana del checklist dual**: el gate automatizado verifica ejecución y resultados de tests en verde y coherencia del tablero/specs, mientras que la equivalencia fina de UX y copy se revisa en el punto de suspensión humano de `docs/workflow.md`.