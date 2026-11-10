Feature: Añadir planta con foto (cámara o galería)
  Como usuario
  Quiero poder añadir una foto al crear una planta
  Para reconocerla más fácilmente en la lista y el detalle

  Background:
    Given que el usuario está en el flujo de añadir una planta

  Scenario: Añadir planta con foto desde la cámara
    When el usuario elige hacer una foto
    And concede el permiso de cámara si el sistema lo pide
    And toma una foto
    And completa el nombre (obligatorio)
    And guarda
    Then la planta se crea con esa foto
    And la foto se muestra en la lista y/o en el detalle

  Scenario: Añadir planta con foto desde la galería
    When el usuario elige una foto de la galería
    And selecciona una imagen
    And completa el nombre
    And guarda
    Then la planta se crea con esa foto

  Scenario: Añadir planta sin foto sigue siendo válido
    When el usuario no añade ninguna foto
    And completa el nombre
    And guarda
    Then la planta se crea correctamente sin imagen

  Scenario: Cancelar no guarda la planta ni la foto
    When el usuario selecciona una foto
    And cancela el flujo
    Then no se crea ninguna planta

  Scenario: Permiso de cámara denegado
    When el usuario intenta usar la cámara
    And deniega el permiso
    Then la app no falla
    And puede seguir usando la galería o guardar sin foto