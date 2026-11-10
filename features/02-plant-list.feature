Feature: Lista de mis plantas con estado de riego
  Como usuario que ya tiene plantas
  Quiero ver todas mis plantas en una lista clara
  Para saber de un vistazo cuáles necesitan atención

  Background:
    Given que el usuario ya ha añadido al menos una planta

  Scenario: Ver la lista cuando hay plantas
    When el usuario abre la app
    Then debe ver una lista con todas sus plantas
    And cada planta muestra su nombre
    And cada planta muestra un indicador de estado de riego

  Scenario: Estado de riego visible
    Given una planta que fue regada hace poco
    When el usuario mira la lista
    Then esa planta muestra un estado de "bien" o equivalente visual calmado

    Given una planta que necesita riego pronto
    When el usuario mira la lista
    Then esa planta muestra un estado de advertencia suave

    Given una planta que necesita riego urgente
    When el usuario mira la lista
    Then esa planta muestra un estado de atención más evidente

  Scenario: Lista vacía no debería ocurrir aquí
    # Esta feature asume que ya hay plantas.
    # El caso de cero plantas se cubre en onboarding.

  Scenario: Navegación a detalle (preparación)
    When el usuario toca una planta de la lista
    Then se navega a la pantalla de detalle de esa planta
    # El detalle completo se implementará en la siguiente feature

  Scenario: Acción para añadir otra planta
    When el usuario está en la lista
    Then debe existir una forma clara de añadir una nueva planta