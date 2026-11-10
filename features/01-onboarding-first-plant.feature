Feature: Onboarding y añadir la primera planta
  Como usuario nuevo
  Quiero poder añadir mi primera planta de forma simple
  Para empezar a usar la app sin fricción

  Background:
    Given la app se abre por primera vez
    And no existen plantas guardadas

  Scenario: Flujo de onboarding completo
    When el usuario ve la pantalla de bienvenida
    Then debe ver un mensaje claro y calmado que explique el propósito de la app
    And un botón principal para "Añadir mi primera planta"

  Scenario: Añadir planta de forma manual
    When el usuario elige añadir una planta
    And introduce un nombre para la planta
    And opcionalmente selecciona un tipo de planta de una lista básica
    And confirma
    Then la planta se guarda localmente
    And el usuario es llevado a la lista de plantas (aunque solo tenga una)

  Scenario: Cancelar el onboarding
    When el usuario está en el flujo de añadir planta
    And decide cancelar
    Then vuelve a la pantalla de bienvenida
    And no se guarda ninguna planta

  Scenario: Validaciones
    When el usuario intenta guardar una planta sin nombre
    Then se muestra un mensaje de error claro y no se permite guardar