Feature: Historial visual de fotos de la planta
  Como usuario
  Quiero ver y ampliar el historial de fotos de una planta
  Para seguir cómo ha ido creciendo con el tiempo

  Background:
    Given que el usuario está en el detalle de una planta

  Scenario: Ver historial cuando hay fotos
    Given la planta tiene una o más fotos guardadas
    When el usuario mira la sección de historial
    Then ve las fotos ordenadas de más reciente a más antigua
    And puede distinguir cuándo se añadió cada una (fecha)

  Scenario: Añadir una nueva foto al historial
    When el usuario elige añadir una foto (cámara o galería)
    And confirma
    Then la nueva foto se guarda asociada a esa planta
    And aparece como la más reciente en el historial
    And la foto principal/avatar de la planta puede actualizarse a la más reciente

  Scenario: Planta sin fotos en el historial
    Given la planta no tiene fotos todavía
    When el usuario mira el historial
    Then ve un estado vacío calmado
    And tiene una forma clara de añadir la primera foto

  Scenario: Cancelar no añade foto
    When el usuario abre cámara o galería
    And cancela
    Then el historial no cambia

  Scenario: Permiso de cámara denegado
    When el usuario intenta usar la cámara
    And deniega el permiso
    Then la app no falla
    And puede usar la galería o salir sin añadir foto