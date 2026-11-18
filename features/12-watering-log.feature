Feature: Historial de riegos de una planta
  Como usuario
  Quiero ver cuándo he regado una planta
  Para no depender solo del último riego

  Scenario: Ver historial en el detalle
    Given una planta que se ha regado al menos una vez
    When el usuario abre el detalle
    Then ve un historial de riegos ordenado del más reciente al más antiguo
    And cada entrada muestra una fecha comprensible (p. ej. Hoy, Ayer, o fecha)

  Scenario: Registrar riego añade entrada
    When el usuario marca "Ya la regué"
    Then se actualiza lastWateredAt como hasta ahora
    And aparece una nueva entrada al inicio del historial

  Scenario: Planta nunca regada
    Given una planta sin riegos registrados
    When el usuario abre el detalle
    Then ve un estado vacío calmado del historial
    And no se inventan fechas

  Scenario: Persistencia
    When el usuario regresa más tarde al detalle
    Then el historial sigue disponible (persistido en local)