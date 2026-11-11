# Flywheel — de fallo a regla

Cuando el agente (o nosotros) se desvía de la spec, del design system o del DoD, no basta con corregir el código. Se registra el fallo y se convierte en guía o sensor.

## Plantilla de entrada

Copia esto en `progress/flywheel-log.md` (o al final de `progress/current.md` en sesiones cortas):

### YYYY-MM-DD — <título corto>

- **Síntoma:** qué hizo mal el agente (o qué se coló a revisión).
- **Causa probable:** spec ambigua / regla ausente / sensor inexistente / otra.
- **Corrección en código:** qué se cambió en la app (breve).
- **Cambio en el harness:**
  - [ ] Nueva regla en `AGENTS.md` o `ios|android/AGENTS.md`
  - [ ] Ajuste de spec en `features/*.feature`
  - [ ] Nuevo test o aserción (sensor)
  - [ ] Nada (justificar por qué no aplica)
- **Texto de la regla o del test:** (pegar el snippet añadido)

## Regla de oro

Si el mismo tipo de fallo ocurre **dos veces**, el harness *debe* ganar una regla o un test. Una vez puede ser ruido; dos veces es un agujero del arnés.

## Qué no es flywheel

- Reescribir la feature entera sin tocar reglas ni tests.
- Añadir párrafos largos a `AGENTS.md` sin haber visto el fallo en la práctica.
- Reglas que el agente no puede comprobar (preferir sensores cuando sea posible).