Feature: Editar una planta
  Como usuario
  Quiero cambiar el nombre, el tipo y la foto principal de una planta
  Para mantener mi jardín al día sin crear entradas nuevas

  Scenario: Abrir edición desde el detalle
    Given una planta existente
    When el usuario elige editar
    Then ve el formulario con los datos actuales (nombre, tipo, foto)

  Scenario: Guardar cambios
    When el usuario modifica el nombre y/o el tipo y/o la foto y guarda
    Then los cambios se persisten
    And la lista y el detalle reflejan los nuevos datos

  Scenario: Nombre obligatorio
    When el usuario deja el nombre vacío y intenta guardar
    Then ve un error claro
    And no se guarda

  Scenario: Cancelar
    When el usuario cancela la edición
    Then no se persisten cambios
    And vuelve al detalle o a la lista sin alterar la planta

  Scenario: Foto opcional
    When el usuario cambia o quita la foto principal
    Then el avatar de lista y detalle se actualiza al guardar