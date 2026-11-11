Feature: Estados vacíos y de error calmados
  Como usuario
  Quiero mensajes claros y tranquilos cuando no hay datos o algo falla
  Para no sentirme perdido ni alarmado

  Scenario: Lista sin plantas (defensivo)
    Given que no hay plantas
    When el usuario llega a la zona de lista
    Then ve un estado vacío calmado
    And ve una forma clara de añadir la primera planta
    And el tono del texto es humano y en español

  Scenario: Historial de fotos sin imágenes
    Given una planta sin fotos en el historial
    When el usuario mira la sección de historial
    Then ve un estado vacío calmado
    And ve una acción clara para añadir la primera foto

  Scenario: Error de carga recuperable
    Given que falla la carga de la lista o del detalle
    When el usuario ve el error
    Then el mensaje es claro y no técnico
    And existe una acción de reintentar
    And el diseño sigue el design system (sin alertas agresivas)

  Scenario: Coherencia dual
    Then los mensajes equivalentes en iOS y Android transmiten el mismo significado
    And respetan el tono del design system