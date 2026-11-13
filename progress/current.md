# Progress — Leafly (harness-hardening)

## Estado actual
Harness endurecido operativo. `plant-search-filter` cerrada en ambas plataformas (gate dual PASADO).

## Features en esta rama
| Feature | Estado |
|---------|--------|
| empty-states-polish | done |
| plant-search-filter | done |

## plant-search-filter — resumen
- Búsqueda por nombre (case-insensitive)
- Filtro por estado de riego (Al día / Riega pronto / Necesita agua)
- Combinación de ambos + empty calmado + limpiar
- iOS: ViewModel + UI + tests
- Android: ViewModel/StateFlow + Compose + unit tests
- `./scripts/verify-feature.sh plant-search-filter` → PASADO

## Próximo paso
Elegir siguiente feature de producto (p. ej. settings-notifications, watering-log) o merge/PR de harness-hardening hacia main.