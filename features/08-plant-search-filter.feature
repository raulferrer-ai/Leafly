Feature: Buscar y filtrar mis plantas
  Como usuario con varias plantas
  Quiero buscar por nombre y filtrar por estado de riego
  Para encontrar rápido la que necesito

  Background:
    Given que el usuario tiene plantas en su lista

  Scenario: Buscar por nombre
    When el usuario escribe parte del nombre de una planta
    Then la lista muestra solo las plantas cuyo nombre coincide
    And la búsqueda no distingue mayúsculas/minúsculas

  Scenario: Filtrar por estado de riego
    When el usuario elige un filtro de estado (Al día, Riega pronto o Necesita agua)
    Then la lista muestra solo las plantas con ese estado

  Scenario: Combinar búsqueda y filtro
    When el usuario tiene texto de búsqueda y un filtro de estado activos
    Then la lista aplica ambos criterios a la vez

  Scenario: Sin resultados
    When ningún resultado cumple búsqueda y/o filtro
    Then ve un estado vacío calmado
    And puede limpiar búsqueda o filtros con facilidad

  Scenario: Limpiar
    When el usuario limpia la búsqueda y quita el filtro
    Then vuelve a ver la lista completa