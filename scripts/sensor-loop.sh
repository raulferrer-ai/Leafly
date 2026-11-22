#!/usr/bin/env bash
# Ejecuta el sensor de una plataforma en modo iteración (loop)

set -u

PLATFORM="${1:-}"
case "$PLATFORM" in
  ios|android) ;;
  *)
    echo "Uso: $0 <ios|android>" >&2
    exit 2
    ;;
esac

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT" || exit 2

echo "🔁 Loop sensor [$PLATFORM] — $(date '+%H:%M:%S')"
if "./scripts/verify-feature.sh" --only "$PLATFORM"; then
  echo "✓ Iteración en verde para $PLATFORM. Siguiente paso: revisar cambios o pasar a la otra plataforma/gate."
  exit 0
else
  rc=$?
  echo "✗ Iteración en rojo para $PLATFORM (exit $rc). Corregir y reintentar antes de declarar listo." >&2
  exit "$rc"
fi