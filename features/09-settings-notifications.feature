Feature: Ajustes de notificaciones de riego
  Como usuario
  Quiero ver y controlar los recordatorios de riego
  Para que me avisen a una hora razonable sin sorpresas

  Scenario: Entrar en ajustes
    When el usuario abre Ajustes
    Then ve una sección de notificaciones de riego
    And el tono de los textos es calmado y en español

  Scenario: Estado del permiso
    Given el permiso de notificaciones está concedido o denegado
    When el usuario mira la sección de notificaciones
    Then ve un estado claro del permiso
    And si está denegado, ve una forma calmada de ir a ajustes del sistema

  Scenario: Hora del recordatorio
    When el usuario cambia la hora preferida de los recordatorios
    Then la hora queda guardada
    And los próximos recordatorios usan esa hora (con la misma lógica de margen que ya existe)

  Scenario: Sin plantas con sed
    Given no hay plantas en estado "Riega pronto" o "Necesita agua"
    When el usuario está en la app
    Then no se piden permisos de más ni se muestran avisos agresivos