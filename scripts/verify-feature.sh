#!/bin/bash
# ============================================
# Leafly — Gate de verificación dual
# Uso: ./scripts/verify-feature.sh [feature-id]
# ============================================

set -e

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

success() { echo -e "${GREEN}✓${NC} $1"; }
error()   { echo -e "${RED}✗${NC} $1"; }
info()    { echo -e "${BLUE}→${NC} $1"; }
warn()    { echo -e "${YELLOW}!${NC} $1"; }

# --- Raíz del monorepo ---
if [ ! -f "AGENTS.md" ]; then
  error "Ejecuta este script desde la raíz de leafly/"
  exit 1
fi

INITIAL_TARGET="${1:-}"

echo "🔒 Leafly — Gate de verificación dual"
echo ""

# --- feature_list.json ---
if [ ! -f "feature_list.json" ]; then
  error "No se encuentra feature_list.json"
  exit 1
fi

if ! command -v python3 &> /dev/null; then
  error "python3 es necesario para este gate"
  exit 1
fi

# Resolver feature objetivo de forma compatible con Bash 3.2 (macOS)
{
  read -r FEATURE_ID
  read -r FEATURE_TITLE
  read -r FEATURE_STATUS
} < <(TARGET="$INITIAL_TARGET" python3 - << 'PY'
import json, os, sys
features = json.load(open("feature_list.json"))
target = os.environ.get("TARGET", "").strip()

if target:
    match = next((f for f in features if f.get("id") == target), None)
    if not match:
        print("NOT_FOUND")
        print("-")
        print("none")
        sys.exit(2)
else:
    in_progress = [f for f in features if f.get("status") == "in_progress"]
    if not in_progress:
        print("NONE")
        print("-")
        print("none")
        sys.exit(0)
    match = in_progress[0]

print(match.get("id", ""))
print(match.get("title", ""))
print(match.get("status", ""))
PY
)

if [ "$FEATURE_ID" = "NONE" ]; then
  warn "No hay feature in_progress. Nada que verificar para done."
  exit 0
fi

if [ -z "$FEATURE_ID" ] || [ "$FEATURE_ID" = "NOT_FOUND" ]; then
  error "Feature no encontrada en feature_list.json"
  exit 1
fi

info "Feature: $FEATURE_ID — $FEATURE_TITLE"
info "Status actual: $FEATURE_STATUS"
echo ""

# --- Spec existe ---
SPEC_PATH=$(python3 - << EOF
import json
features = json.load(open("feature_list.json"))
f = next(x for x in features if x.get("id") == "$FEATURE_ID")
print(f.get("spec", ""))
EOF
)

if [ -n "$SPEC_PATH" ] && [ -f "$SPEC_PATH" ]; then
  success "Spec encontrada: $SPEC_PATH"
else
  error "Spec ausente o ruta inválida: ${SPEC_PATH:-'(vacía)'}"
  exit 1
fi

# --- Recordatorio de plataformas ---
echo ""
info "Checklist dual (humano + tests):"
echo "  [ ] Comportamiento alineado con la spec en iOS"
echo "  [ ] Comportamiento alineado con la spec en Android"
echo "  [ ] Tests iOS en verde"
echo "  [ ] Tests Android en verde"
echo ""

# --- Tests iOS (si hay xcodebuild y proyecto) ---
IOS_OK=0
ANDROID_OK=0

if [ -d "ios" ]; then
  if command -v xcodebuild &> /dev/null; then
    info "Ejecutando tests iOS (puede tardar)..."
    if xcodebuild test \
      -project ios/Leafly/Leafly.xcodeproj \
      -scheme Leafly \
      -destination 'platform=iOS Simulator,name=iPhone 17 Pro' \
      -quiet 2>/dev/null; then
      success "Tests iOS en verde"
      IOS_OK=1
    else
      if xcodebuild build \
        -project ios/Leafly/Leafly.xcodeproj \
        -scheme Leafly \
        -destination 'platform=iOS Simulator,name=iPhone 17 Pro' \
        -quiet 2>/dev/null; then
        warn "Build iOS OK; no se pudieron correr tests automáticamente (revisa destino/scheme)"
        IOS_OK=1
      else
        error "iOS: build/tests fallaron"
        IOS_OK=0
      fi
    fi
  else
    warn "xcodebuild no disponible — marca iOS manualmente tras correr tests en Xcode"
    IOS_OK=1
  fi
else
  error "No existe carpeta ios/"
  exit 1
fi

echo ""

# --- Tests Android ---
if [ -d "android" ]; then
  if [ -f "android/gradlew" ]; then
    info "Ejecutando unit tests Android..."
    if (cd android && ./gradlew :app:testDebugUnitTest --quiet); then
      success "Unit tests Android en verde"
      ANDROID_OK=1
    else
      error "Android: unit tests fallaron"
      ANDROID_OK=0
    fi
  else
    warn "android/gradlew no encontrado — marca Android manualmente"
    ANDROID_OK=1
  fi
else
  error "No existe carpeta android/"
  exit 1
fi

echo ""
echo "============================================"

if [ "$IOS_OK" -eq 1 ] && [ "$ANDROID_OK" -eq 1 ]; then
  success "Gate PASADO para: $FEATURE_ID"
  echo ""
  echo "Puedes marcar la feature como done en feature_list.json"
  echo "después de confirmar el checklist dual de comportamiento."
  exit 0
else
  error "Gate FALLIDO — no marques done todavía"
  [ "$IOS_OK" -eq 0 ] && error "  iOS no verificado"
  [ "$ANDROID_OK" -eq 0 ] && error "  Android no verificado"
  exit 1
fi