#!/bin/bash

# ============================================
# Leafly — Harness Init Script (Advanced)
# ============================================

set -e

echo "🌱 Leafly Harness — Iniciando verificación..."
echo ""

# Colores
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

success() {
  echo -e "${GREEN}✓${NC} $1"
}

error() {
  echo -e "${RED}✗${NC} $1"
}

warning() {
  echo -e "${YELLOW}!${NC} $1"
}

info() {
  echo -e "${BLUE}→${NC} $1"
}

# --------------------------------------------
# 1. Verificar que estamos en la raíz
# --------------------------------------------
if [ ! -f "AGENTS.md" ]; then
  error "No se encontró AGENTS.md. Ejecuta este script desde la raíz del proyecto (leafly/)."
  exit 1
fi
success "Raíz del proyecto detectada"

# --------------------------------------------
# 2. Verificar / crear progress/current.md
# --------------------------------------------
echo ""
echo "Verificando progreso..."

if [ ! -d "progress" ]; then
  mkdir -p progress
  warning "Carpeta progress/ creada"
fi

if [ ! -f "progress/current.md" ]; then
  cat > progress/current.md << 'EOF'
# Progress — Leafly

## Estado actual
Proyecto recién inicializado. Ninguna feature implementada todavía.

## Feature actual
Ninguna.

## Próximo paso
Empezar con la feature de prioridad 1: `onboarding-first-plant`.

## Notas
- Estructura del harness creada.
- Listo para la primera implementación dual.
EOF
  warning "progress/current.md no existía → se ha creado con contenido inicial"
else
  success "progress/current.md encontrado"
fi

# --------------------------------------------
# 3. Verificar archivos críticos del harness
# --------------------------------------------
REQUIRED_FILES=(
  "AGENTS.md"
  "feature_list.json"
  "docs/architecture.md"
  "docs/design-system.md"
  "ios/AGENTS.md"
  "android/AGENTS.md"
)

echo ""
echo "Verificando archivos del harness..."

MISSING=0
for file in "${REQUIRED_FILES[@]}"; do
  if [ -f "$file" ]; then
    success "$file"
  else
    error "Falta: $file"
    MISSING=1
  fi
done

if [ "$MISSING" -eq 1 ]; then
  echo ""
  error "Faltan archivos críticos. Corrige la estructura antes de continuar."
  exit 1
fi

# --------------------------------------------
# 4. Verificar specs
# --------------------------------------------
echo ""
echo "Verificando specs..."

if [ -d "features" ]; then
  SPEC_COUNT=$(find features -name "*.feature" 2>/dev/null | wc -l | tr -d ' ')
  if [ "$SPEC_COUNT" -gt 0 ]; then
    success "Carpeta features/ encontrada ($SPEC_COUNT specs)"
  else
    warning "Carpeta features/ existe pero no contiene archivos .feature"
  fi
else
  error "Falta la carpeta features/"
  exit 1
fi

# --------------------------------------------
# 5. Verificar estructura de plataformas
# --------------------------------------------
echo ""
echo "Verificando estructura de plataformas..."

if [ -d "ios" ]; then
  success "Carpeta ios/ encontrada"
else
  error "Falta la carpeta ios/"
  exit 1
fi

if [ -d "android" ]; then
  success "Carpeta android/ encontrada"
else
  error "Falta la carpeta android/"
  exit 1
fi

# --------------------------------------------
# 6. Análisis de feature_list.json
# --------------------------------------------
echo ""
echo "Estado de las features..."

if command -v python3 &> /dev/null; then
  python3 - << 'EOF'
import json
import sys

try:
    with open("feature_list.json", "r") as f:
        features = json.load(f)

    pending = [f for f in features if f.get("status") == "pending"]
    in_progress = [f for f in features if f.get("status") == "in_progress"]
    done = [f for f in features if f.get("status") == "done"]

    print(f"  → Pending:     {len(pending)}")
    print(f"  → In Progress: {len(in_progress)}")
    print(f"  → Done:        {len(done)}")
    print()

    if in_progress:
        f = in_progress[0]
        print(f"  Feature en curso: {f['id']} — {f['title']}")
    elif pending:
        # Ordenar por priority
        pending_sorted = sorted(pending, key=lambda x: x.get("priority", 999))
        f = pending_sorted[0]
        print(f"  Siguiente feature recomendada: {f['id']} — {f['title']}")
        print(f"  Spec: {f.get('spec', 'N/A')}")
    else:
        print("  Todas las features están completadas.")

except Exception as e:
    print(f"  ! No se pudo analizar feature_list.json: {e}")
EOF
else
  warning "python3 no está disponible → se omite el análisis de features"
fi

# --------------------------------------------
# 7. Resumen final
# --------------------------------------------
echo ""
echo "============================================"
echo -e "${GREEN}Harness listo.${NC}"
echo "============================================"
echo ""
echo "Próximos pasos recomendados:"
echo "  1. Lee progress/current.md"
echo "  2. Revisa la siguiente feature pendiente (arriba)"
echo "  3. Lee la spec correspondiente en features/"
echo "  4. Empieza a implementar (recomendado: primero iOS)"
echo ""