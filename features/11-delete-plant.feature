Feature: Eliminar una planta
  Como usuario
  Quiero poder borrar una planta de mi jardín
  Para quitar las que ya no cuido sin dejar datos huérfanos

  Scenario: Confirmar eliminación
    Given una planta en el detalle
    When el usuario elige eliminar
    Then ve una confirmación calmada (no alarmista)
    And puede cancelar sin borrar

  Scenario: Eliminar de verdad
    When el usuario confirma la eliminación
    Then la planta desaparece de la persistencia
    And ya no aparece en la lista
    And se cancelan sus recordatorios de riego si los había
    And el historial de fotos de esa planta no queda accesible

  Scenario: Tras borrar la última planta
    Given era la única planta
    When el usuario la elimina
    Then vuelve al flujo de onboarding / estado vacío coherente con la app