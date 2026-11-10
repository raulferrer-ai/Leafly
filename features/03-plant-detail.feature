Feature: Detalle de planta + marcar como regada
  Como usuario
  Quiero ver el detalle de una planta y poder marcarla como regada
  Para actualizar su estado de riego de forma rápida y clara

  Background:
    Given que el usuario tiene al menos una planta
    And está en la pantalla de detalle de esa planta

  Scenario: Ver información de la planta
    Then debe ver el nombre de la planta
    And debe ver el tipo de planta si existe
    And debe ver el estado de riego actual (Al día / Riega pronto / Necesita agua)
    And debe ver cuándo se regó por última vez (o cuándo se añadió)

  Scenario: Marcar como regada
    When el usuario pulsa la acción de "Ya la regué" (o equivalente)
    Then la fecha de último riego se actualiza a ahora
    And el estado de riego pasa a "Al día"
    And el cambio se persiste

  Scenario: Volver a la lista
    When el usuario vuelve atrás
    Then regresa a la lista de plantas
    And la lista refleja el estado actualizado si se marcó como regada

  Scenario: Información calmada y clara
    Then la pantalla debe sentirse simple y no sobrecargada
    And los textos deben ser humanos y cercanos