Feature: Recordatorios locales de riego
  Como usuario
  Quiero recibir recordatorios cuando una planta necesita agua
  Para no olvidarme de regarla

  Background:
    Given que el usuario tiene al menos una planta
    And ha concedido permiso de notificaciones (si el sistema lo requiere)

  Scenario: Programar recordatorio según estado de riego
    Given una planta cuyo estado es "Riega pronto" o "Necesita agua"
    When el sistema evalúa los recordatorios
    Then se programa una notificación local para esa planta
    And el texto del recordatorio es claro y calmado (nombre de la planta + que necesita agua)

  Scenario: No molestar si la planta está al día
    Given una planta cuyo estado es "Al día"
    When el sistema evalúa los recordatorios
    Then no se envía notificación para esa planta

  Scenario: Al marcar como regada se actualiza el recordatorio
    Given una planta que tenía un recordatorio pendiente
    When el usuario marca la planta como regada
    Then el recordatorio anterior se cancela o se reprograma
    And el nuevo estado "Al día" evita notificaciones inmediatas

  Scenario: Varias plantas
    Given el usuario tiene varias plantas con distintos estados
    When el sistema evalúa los recordatorios
    Then solo se notifican las que necesitan atención
    And cada notificación identifica claramente la planta

  Scenario: Permiso denegado
    Given el usuario no ha concedido permiso de notificaciones
    When la app intenta programar recordatorios
    Then no falla de forma visible
    And la app sigue funcionando con normalidad