# Progress — Leafly

## Estado actual
Features completadas: onboarding, plant-list, plant-detail.
`watering-reminders` implementada y verificada en iOS; pendiente Android.

## Feature actual
`watering-reminders` (in_progress)

## Completado
- onboarding-first-plant → done
- plant-list → done
- plant-detail → done

## En curso
- watering-reminders
  - iOS → implementado y verificado
  - Android → pendiente

## Implementación iOS
- Dominio puro:
  - `WateringReminder`: valor del recordatorio (planta, estado, fecha), identificador
    estable por planta (`watering-reminder-<uuid>`) y textos calmados con el nombre.
  - `WateringReminderPlanner`: decide qué recordatorios existen. Solo genera para
    `needsWaterSoon` y `needsWaterNow`; ignora `healthy`. Fecha de entrega: próximo
    hueco de las 10:00 locales con al menos 1 hora de margen (si no, mañana).
  - `WateringReminderScheduling`: contrato que consumen los ViewModels.
- Datos / sistema:
  - `LocalNotificationCenter`: abstracción mockeable del centro de notificaciones.
  - `UserNotificationCenterAdapter`: adaptador real sobre `UNUserNotificationCenter`
    (`requestAuthorization`, `pendingNotificationRequests`, `add`, `removePending`).
  - `WateringReminderService`: sincroniza recordatorios, elimina los obsoletos, pide
    permiso una sola vez por sesión y no falla si el permiso está denegado.
  - `NoopWateringReminders`: implementación vacía para previews.
- Integración:
  - `LeaflyApp` construye `WateringReminderService` y lo inyecta en `RootView`.
  - `PlantsViewModel.load()` y `replace(_:)` sincronizan recordatorios con las plantas.
  - `PlantDetailViewModel.markAsWatered()` cancela el recordatorio de esa planta solo
    si el riego se guardó correctamente.
  - `PlantDetailView` recibe `onPlantUpdated` async para que la lista actualice sus
    recordatorios tras regar.
- Tests (XCTest, con doble de `LocalNotificationCenter`):
  - `WateringReminderPlannerTests`: filtrado por estado, identificadores estables,
    textos con el nombre y regla de fecha/hora.
  - `WateringReminderServiceTests`: programación, limpieza de obsoletos, permiso
    denegado, permiso pedido una sola vez y cancelación individual.
  - `PlantsViewModelTests` y `PlantDetailViewModelTests` actualizados para cubrir la
    sincronización y la cancelación.

## Verificación
- `xcodebuild build` (iOS Simulator) → OK.
- `LeaflyTests` → OK.
- `LeaflyUITests` → OK.
- Comprobación manual en simulador: planta sembrada con último riego hace 10 días
  muestra “Necesita agua” y dispara el diálogo de permiso de notificaciones; la app
  no falla si no se concede.

## Próximo paso
Implementar los recordatorios locales equivalentes en Android y, cuando ambas
plataformas estén verificadas, marcar `watering-reminders` como `done`.
