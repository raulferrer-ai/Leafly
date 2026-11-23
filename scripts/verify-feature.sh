#!/usr/bin/env bash
# ============================================
# Leafly — Gate de verificación dual (v2.1, "fail-closed")
#
# Principio: el gate solo da PASADO si hay PRUEBAS de que ambas
# plataformas están en verde. Todo lo que no se puede comprobar
# (herramienta ausente, 0 tests, build sin tests, board incoherente)
# cuenta como FALLO, nunca como "ok con aviso".
#
# La configuración iOS (simulador, proyecto, scheme) vive SOLO en el
# Makefile; este script la lee de ahí (o de variables de entorno).
# ============================================

set -u

usage() {
  cat <<'USAGE'
Uso:
  ./scripts/verify-feature.sh [feature-id]     gate dual completo (iOS + Android)
  ./scripts/verify-feature.sh --only ios       solo sensor iOS (no es el gate)
  ./scripts/verify-feature.sh --only android   solo sensor Android (no es el gate)
  ./scripts/verify-feature.sh --check-board    solo valida feature_list.json y specs
  make gate [FEATURE=id] | make sensor-ios | make sensor-android

Configuración iOS (DEST, IOS_PROJECT, IOS_SCHEME): definida en el Makefile.
Se puede sobrescribir con variables de entorno.

Códigos de salida: 0 = correcto · 1 = fallo · 2 = uso/entorno incorrecto
USAGE
}

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

success() { echo -e "${GREEN}✓${NC} $1"; }
error()   { echo -e "${RED}✗${NC} $1" >&2; }
info()    { echo -e "${BLUE}→${NC} $1"; }
warn()    { echo -e "${YELLOW}!${NC} $1"; }

# --- Argumentos ---
TARGET=""
ONLY=""
CHECK_BOARD=0
while [ $# -gt 0 ]; do
  case "$1" in
    -h|--help) usage; exit 0 ;;
    --check-board) CHECK_BOARD=1 ;;
    --only)
      shift
      case "${1:-}" in
        ios|android) ONLY="$1" ;;
        *) error "--only requiere 'ios' o 'android'"; exit 2 ;;
      esac
      ;;
    -*) error "Opción desconocida: $1 (usa --help)"; exit 2 ;;
    *)
      if [ -n "$TARGET" ]; then error "Solo se admite un feature-id"; exit 2; fi
      TARGET="$1"
      ;;
  esac
  shift
done

if [ -n "$ONLY" ] && { [ -n "$TARGET" ] || [ "$CHECK_BOARD" -eq 1 ]; }; then
  error "--only no se combina con feature-id ni con --check-board"
  exit 2
fi

# --- Ir a la raíz del monorepo (funciona desde cualquier carpeta) ---
cd "$(dirname "${BASH_SOURCE[0]}")/.." || exit 2
if [ ! -f "AGENTS.md" ]; then
  error "No encuentro AGENTS.md: este script debe vivir en <raíz>/scripts/"
  exit 2
fi

if ! command -v python3 >/dev/null 2>&1; then
  error "python3 es necesario para este gate"
  exit 2
fi

# --- Configuración: entorno primero, Makefile después (única fuente de verdad) ---
cfg() {
  local name="$1" val=""
  case "$name" in
    DEST)        val="${DEST:-}" ;;
    IOS_PROJECT) val="${IOS_PROJECT:-}" ;;
    IOS_SCHEME)  val="${IOS_SCHEME:-}" ;;
  esac
  if [ -z "$val" ] && [ -f Makefile ] && command -v make >/dev/null 2>&1; then
    val="$(make -s "print-$name" 2>/dev/null)"
  fi
  printf '%s' "$val"
}

ANDROID_RESULTS_GLOB="android/app/build/test-results/testDebugUnitTest/TEST-*.xml"

# --- Logs temporales: se borran si todo va bien, se conservan si algo falla ---
LOG_DIR="$(mktemp -d "${TMPDIR:-/tmp}/leafly-gate.XXXXXX")"
KEEP_LOGS=0
cleanup() {
  if [ "$KEEP_LOGS" -eq 1 ]; then
    echo "Logs completos conservados en: $LOG_DIR"
  else
    rm -rf "$LOG_DIR"
  fi
}
trap cleanup EXIT

# ============================================
# Programas Python embebidos (en variables para que el script siga
# siendo compatible con Bash 3.2 de macOS)
# ============================================

# 1) Valida TODO el board y resuelve la feature objetivo.
#    stdout: "NONE" o líneas ID=/TITLE=/STATUS=/SPEC=; errores por stderr.
IFS= read -r -d '' PY_VALIDATE_BOARD <<'PY'
import json, os, re, sys

BOARD = "feature_list.json"
REQUIRED = ("id", "title", "status", "platforms", "priority", "spec")
VALID_PLATFORMS = {"ios", "android"}
target = os.environ.get("TARGET", "").strip()
errors = []

try:
    with open(BOARD, encoding="utf-8") as fh:
        features = json.load(fh)
except (OSError, ValueError) as exc:
    sys.stderr.write("feature_list.json ilegible o JSON inválido: %s\n" % exc)
    sys.exit(1)

if not isinstance(features, list) or not features:
    sys.stderr.write("feature_list.json debe ser una lista no vacía\n")
    sys.exit(1)

seen = set()
in_progress = []

for index, feat in enumerate(features):
    if not isinstance(feat, dict):
        errors.append("entrada #%d: no es un objeto" % index)
        continue
    fid = feat.get("id")
    label = fid if isinstance(fid, str) and fid else "entrada #%d" % index

    for key in REQUIRED:
        if feat.get(key) in (None, "", []):
            errors.append("%s: falta el campo '%s'" % (label, key))

    if isinstance(fid, str) and fid:
        if fid in seen:
            errors.append("%s: id duplicado" % label)
        seen.add(fid)

    platforms = feat.get("platforms")
    if isinstance(platforms, list):
        bad = [p for p in platforms if p not in VALID_PLATFORMS]
        if bad:
            errors.append("%s: plataformas desconocidas %s" % (label, bad))

    if feat.get("status") == "in_progress":
        in_progress.append(label)

    spec = feat.get("spec")
    if isinstance(spec, str) and spec:
        if not os.path.isfile(spec):
            errors.append("%s: la spec no existe (%s)" % (label, spec))
        else:
            with open(spec, encoding="utf-8") as fh:
                text = fh.read()
            if not re.search(r"^\s*Scenario( Outline)?:", text, re.MULTILINE):
                errors.append("%s: la spec no contiene ningún Scenario (%s)" % (label, spec))

if len(in_progress) > 1:
    errors.append("hay %d features in_progress (%s); solo se permite una"
                  % (len(in_progress), ", ".join(in_progress)))

if errors:
    for message in errors:
        sys.stderr.write(message + "\n")
    sys.exit(1)

if target:
    chosen = next((f for f in features if f["id"] == target), None)
    if chosen is None:
        sys.stderr.write("la feature '%s' no existe en feature_list.json\n" % target)
        sys.exit(1)
else:
    active = [f for f in features if f["status"] == "in_progress"]
    if not active:
        print("NONE")
        sys.exit(0)
    chosen = active[0]

if chosen["status"] not in ("in_progress", "done"):
    sys.stderr.write("'%s' tiene status '%s': el gate solo verifica in_progress o done\n"
                     % (chosen["id"], chosen["status"]))
    sys.exit(1)

if set(chosen["platforms"]) != VALID_PLATFORMS:
    sys.stderr.write("'%s' no declara ios y android: el gate es dual\n" % chosen["id"])
    sys.exit(1)

print("ID=%s" % chosen["id"])
print("TITLE=%s" % str(chosen["title"]).replace("\n", " "))
print("STATUS=%s" % chosen["status"])
print("SPEC=%s" % chosen["spec"])
PY

# 2) Cuenta tests de Android a partir de los XML de Gradle.
#    stdout: "<tests> <fallos+errores> <omitidos>"
IFS= read -r -d '' PY_ANDROID_RESULTS <<'PY'
import glob, sys
import xml.etree.ElementTree as ET

files = sorted(glob.glob(sys.argv[1]))
tests = failed = skipped = 0
for path in files:
    root = ET.parse(path).getroot()
    suites = [root] if root.tag == "testsuite" else list(root.iter("testsuite"))
    for suite in suites:
        tests += int(suite.get("tests", 0))
        failed += int(suite.get("failures", 0)) + int(suite.get("errors", 0))
        skipped += int(suite.get("skipped", 0))
print("%d %d %d" % (tests, failed, skipped))
PY

# 3) Lee el resumen JSON de xcresulttool.
#    stdout: "<tests> <fallidos>"; exit 3 si no se puede leer el total.
IFS= read -r -d '' PY_IOS_RESULTS <<'PY'
import json, sys

data = json.load(sys.stdin)
total = data.get("totalTestCount")
if total is None:
    sys.exit(3)
print("%d %d" % (total, data.get("failedTests", 0)))
PY

# ============================================
# Sensores por plataforma
# ============================================

run_ios() {
  local dest project scheme
  dest="$(cfg DEST)"
  project="$(cfg IOS_PROJECT)"
  scheme="$(cfg IOS_SCHEME)"

  if [ -z "$dest" ] || [ -z "$project" ] || [ -z "$scheme" ]; then
    error "iOS: falta la configuración (DEST / IOS_PROJECT / IOS_SCHEME). Ejecuta con 'make' o define las variables; la única fuente es el Makefile."
    return 1
  fi
  if ! command -v xcodebuild >/dev/null 2>&1; then
    error "iOS: xcodebuild no está disponible. El gate dual exige tests iOS (usa un Mac o el CI)."
    return 1
  fi
  if [ ! -d "$project" ]; then
    error "iOS: no existe el proyecto $project"
    return 1
  fi

  local log="$LOG_DIR/ios-test.log"
  local bundle="$LOG_DIR/ios.xcresult"

info "Ejecutando tests iOS en: $dest (puede tardar)..."
  if ! xcodebuild test \
      -project "$project" \
      -scheme "$scheme" \
      -destination "$dest" \
      -resultBundlePath "$bundle" \
      -parallel-testing-enabled NO \
      CODE_SIGNING_ALLOWED=NO \
      -quiet >"$log" 2>&1; then
    error "iOS: los tests fallaron o no pudieron ejecutarse (un build correcto NO basta). Últimas líneas:"
    tail -n 40 "$log" >&2
    KEEP_LOGS=1
    return 1
  fi

  # Comprobar que realmente se ejecutaron tests (0 tests no es "verde").
  local summary counts
  if ! summary="$(xcrun xcresulttool get test-results summary --path "$bundle" 2>>"$log")"; then
    error "iOS: no pude leer el resultado de los tests (xcresulttool)."
    KEEP_LOGS=1
    return 1
  fi
  if ! counts="$(printf '%s' "$summary" | python3 -c "$PY_IOS_RESULTS" 2>>"$log")"; then
    error "iOS: no pude determinar cuántos tests se ejecutaron. Revisa el log."
    KEEP_LOGS=1
    return 1
  fi
  local total="${counts% *}" failed="${counts#* }"
  if [ "$total" -eq 0 ]; then
    error "iOS: se ejecutaron 0 tests. Un gate sin tests no verifica nada."
    KEEP_LOGS=1
    return 1
  fi
  if [ "$failed" -gt 0 ]; then
    error "iOS: $failed de $total tests fallaron."
    KEEP_LOGS=1
    return 1
  fi
  success "Tests iOS en verde ($total tests)"
  return 0
}

run_android() {
  if [ ! -f "android/gradlew" ]; then
    error "Android: no existe android/gradlew."
    return 1
  fi
  if [ ! -x "android/gradlew" ]; then
    error "Android: android/gradlew no es ejecutable (chmod +x android/gradlew)."
    return 1
  fi

  local log="$LOG_DIR/android-test.log"

  info "Ejecutando unit tests Android (puede tardar)..."
  # cleanTest... fuerza a re-ejecutar los tests aunque Gradle los dé por "UP-TO-DATE".
  if ! (cd android && ./gradlew :app:cleanTestDebugUnitTest :app:testDebugUnitTest \
        --quiet --console=plain) >"$log" 2>&1; then
    error "Android: los unit tests fallaron o no pudieron ejecutarse. Últimas líneas:"
    tail -n 40 "$log" >&2
    KEEP_LOGS=1
    return 1
  fi

  local counts
  if ! counts="$(python3 -c "$PY_ANDROID_RESULTS" "$ANDROID_RESULTS_GLOB" 2>>"$log")"; then
    error "Android: no pude leer los resultados de los tests (XML)."
    KEEP_LOGS=1
    return 1
  fi
  local total failed skipped
  total="${counts%% *}"
  failed="$(echo "$counts" | cut -d' ' -f2)"
  skipped="${counts##* }"

  if [ "$total" -eq 0 ]; then
    error "Android: se ejecutaron 0 tests. Un gate sin tests no verifica nada."
    KEEP_LOGS=1
    return 1
  fi
  if [ "$failed" -gt 0 ]; then
    error "Android: $failed de $total tests fallaron."
    KEEP_LOGS=1
    return 1
  fi
  success "Unit tests Android en verde ($total tests, $skipped omitidos)"
  return 0
}

# ============================================
# Modo sensor suelto (--only): mismas comprobaciones, sin board ni gate
# ============================================

if [ -n "$ONLY" ]; then
  echo "🔬 Leafly — Sensor $ONLY"
  echo ""
  if [ "$ONLY" = "ios" ]; then run_ios; else run_android; fi
  rc=$?
  echo ""
  if [ "$rc" -eq 0 ]; then
    success "Sensor $ONLY en verde (esto NO es el gate completo)"
    exit 0
  fi
  error "Sensor $ONLY FALLIDO"
  exit 1
fi

# ============================================
# Gate completo
# ============================================

echo "🔒 Leafly — Gate de verificación dual"
echo ""

# --- Board coherente + feature objetivo ---
VALIDATION="$(TARGET="$TARGET" python3 -c "$PY_VALIDATE_BOARD")"
if [ $? -ne 0 ]; then
  error "Validación fallida (feature_list.json o feature objetivo): revisa los mensajes anteriores."
  exit 1
fi

if [ "$CHECK_BOARD" -eq 1 ]; then
  success "Board coherente: todas las specs existen y tienen Scenario"
  exit 0
fi

if [ "$VALIDATION" = "NONE" ]; then
  warn "No hay feature in_progress. Nada que verificar para done."
  exit 0
fi

FEATURE_ID="$(printf '%s\n' "$VALIDATION" | sed -n 's/^ID=//p')"
FEATURE_TITLE="$(printf '%s\n' "$VALIDATION" | sed -n 's/^TITLE=//p')"
FEATURE_STATUS="$(printf '%s\n' "$VALIDATION" | sed -n 's/^STATUS=//p')"
SPEC_PATH="$(printf '%s\n' "$VALIDATION" | sed -n 's/^SPEC=//p')"

info "Feature: $FEATURE_ID — $FEATURE_TITLE"
info "Status actual: $FEATURE_STATUS"
success "Board coherente y spec válida: $SPEC_PATH"
echo ""

info "Checklist dual (humano + tests):"
echo "  [ ] Comportamiento alineado con la spec en iOS"
echo "  [ ] Comportamiento alineado con la spec en Android"
echo "  [ ] Tests iOS en verde"
echo "  [ ] Tests Android en verde"
echo ""

IOS_OK=0
ANDROID_OK=0

run_ios && IOS_OK=1
echo ""
run_android && ANDROID_OK=1

echo ""
echo "============================================"

if [ "$IOS_OK" -eq 1 ] && [ "$ANDROID_OK" -eq 1 ]; then
  success "Gate PASADO para: $FEATURE_ID"
  echo ""
  echo "Puedes marcar la feature como done en feature_list.json"
  echo "después de confirmar el checklist dual de comportamiento."
  exit 0
fi

error "Gate FALLIDO — no marques done todavía"
[ "$IOS_OK" -eq 0 ] && error "  iOS no verificado"
[ "$ANDROID_OK" -eq 0 ] && error "  Android no verificado"
exit 1
