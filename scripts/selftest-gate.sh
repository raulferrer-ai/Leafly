#!/usr/bin/env bash
# ============================================
# Leafly — Autotest del gate ("el sensor del sensor")
#
# Demuestra que scripts/verify-feature.sh FALLA cuando debe fallar:
# tests en rojo, 0 tests, herramienta ausente, board roto, config iOS ausente…
# Usa herramientas simuladas (xcodebuild, xcrun, gradlew): funciona en
# Linux y macOS, sin simuladores ni Android SDK.
#
# Uso: ./scripts/selftest-gate.sh        (también lo ejecuta el CI)
# ============================================

set -u

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
T="$(mktemp -d "${TMPDIR:-/tmp}/leafly-selftest.XXXXXX")"
trap 'rm -rf "$T"' EXIT

BASH_BIN="$(command -v bash)"
PASS=0
FAILN=0

# --- Feature de prueba: la primera 'done' del board real ---
FID="$(python3 -c "
import json
d = json.load(open('$ROOT/feature_list.json'))
print(next((f['id'] for f in d if f['status'] == 'done'), d[0]['id']))
")"
IOS_PROJECT_DIR="$(make -s -C "$ROOT" print-IOS_PROJECT)"
MAKE_DEST="$(make -s -C "$ROOT" print-DEST)"

# --- Herramientas simuladas ---
STUBS="$T/stubs"
mkdir -p "$STUBS"

cat > "$STUBS/xcodebuild" <<'STUB'
#!/bin/bash
echo "$@" >> "${STUB_LOG:-/dev/null}"
if [ "${STUB_XCB_RC:-0}" -ne 0 ]; then
  echo "error: Unable to find a destination matching the provided destination specifier"
  exit "$STUB_XCB_RC"
fi
exit 0
STUB

cat > "$STUBS/xcrun" <<'STUB'
#!/bin/bash
if [ -n "${STUB_XCRESULT+x}" ]; then
  printf '%s' "$STUB_XCRESULT"
else
  printf '%s' '{"totalTestCount":17,"failedTests":0}'
fi
STUB
chmod +x "$STUBS/xcodebuild" "$STUBS/xcrun"

# PATH sin xcodebuild: solo enlaces a lo imprescindible
NOBIN="$T/nobin"
mkdir -p "$NOBIN"
for tool in bash env dirname mktemp python3 sed cut tail make rm cat grep head tr sort chmod mkdir cp; do
  path="$(command -v "$tool" 2>/dev/null)"
  case "$path" in /*) ln -s "$path" "$NOBIN/$tool" ;; esac
done
# make puede ejecutar 'printf' sin shell: necesita un printf externo en el PATH
for candidate in /usr/bin/printf /bin/printf; do
  [ -x "$candidate" ] && ln -s "$candidate" "$NOBIN/printf" && break
done

P_WITH="$STUBS:$PATH"
P_WITHOUT="$NOBIN"

# --- Repo de juguete ---
R="$T/repo"
mk_repo() {
  rm -rf "$R"
  mkdir -p "$R/scripts" "$R/android" "$R/$IOS_PROJECT_DIR"
  cp "$ROOT/AGENTS.md" "$ROOT/feature_list.json" "$ROOT/Makefile" "$R/"
  cp -r "$ROOT/features" "$R/"
  cp "$ROOT/scripts/verify-feature.sh" "$R/scripts/"
  cat > "$R/android/gradlew" <<'STUB'
#!/bin/bash
D=app/build/test-results/testDebugUnitTest
rm -rf "$D"; mkdir -p "$D"
echo "<testsuite name=\"a\" tests=\"${STUB_AND_TESTS:-11}\" failures=\"${STUB_AND_FAIL:-0}\" errors=\"0\" skipped=\"1\"></testsuite>" > "$D/TEST-a.xml"
exit "${STUB_AND_RC:-0}"
STUB
  chmod +x "$R/android/gradlew" "$R/scripts/verify-feature.sh"
}

# gate <PATH> [VAR=valor …] -- <args del script>
gate() {
  local p="$1"; shift
  local envs=()
  while [ "$#" -gt 0 ] && [ "$1" != "--" ]; do envs+=("$1"); shift; done
  shift
  env -u DEST -u IOS_PROJECT -u IOS_SCHEME PATH="$p" ${envs[@]+"${envs[@]}"} \
    "$BASH_BIN" "$R/scripts/verify-feature.sh" "$@"
}

# check <nombre> <rc esperado> <comando…>
check() {
  local name="$1" want="$2"; shift 2
  local out rc
  out="$("$@" 2>&1)"; rc=$?
  if [ "$rc" -eq "$want" ]; then
    PASS=$((PASS + 1)); echo "  ✓ $name"
  else
    FAILN=$((FAILN + 1)); echo "  ✗ $name (esperaba exit $want, obtuvo $rc)"
    printf '%s\n' "$out" | tail -n 8 | sed 's/^/      /'
  fi
}

# check_msg <nombre> <rc esperado> <texto que debe aparecer> <comando…>
# Como check, pero además exige el MOTIVO del fallo (evita "fallar por casualidad").
check_msg() {
  local name="$1" want="$2" pattern="$3"; shift 3
  local out rc
  out="$("$@" 2>&1)"; rc=$?
  if [ "$rc" -eq "$want" ] && printf '%s' "$out" | grep -qF -- "$pattern"; then
    PASS=$((PASS + 1)); echo "  ✓ $name"
  else
    FAILN=$((FAILN + 1)); echo "  ✗ $name (esperaba exit $want y el texto «$pattern»; exit=$rc)"
    printf '%s\n' "$out" | tail -n 8 | sed 's/^/      /'
  fi
}

# assert <nombre> <comando que debe salir 0>
assert() {
  local name="$1"; shift
  if "$@" >/dev/null 2>&1; then
    PASS=$((PASS + 1)); echo "  ✓ $name"
  else
    FAILN=$((FAILN + 1)); echo "  ✗ $name"
  fi
}

mutate_board() { # mutate_board <python-statement sobre d (lista)>
  python3 - "$1" "$R/feature_list.json" <<'PY'
import json, sys
stmt, path = sys.argv[1], sys.argv[2]
d = json.load(open(path))
exec(stmt)
json.dump(d, open(path, "w"))
PY
}

echo "🧪 Autotest del gate (feature de prueba: $FID)"
echo ""

mk_repo
echo "Camino feliz"
check "todo verde → PASA"                                   0 gate "$P_WITH" -- "$FID"
: > "$T/xcb.log"
gate "$P_WITH" STUB_LOG="$T/xcb.log" -- "$FID" >/dev/null 2>&1
assert "el simulador sale del Makefile (única fuente)"       grep -qF -- "$MAKE_DEST" "$T/xcb.log"
: > "$T/xcb.log"
gate "$P_WITH" STUB_LOG="$T/xcb.log" DEST="platform=iOS Simulator,name=Override" -- "$FID" >/dev/null 2>&1
assert "DEST del entorno tiene prioridad sobre el Makefile"  grep -qF -- "name=Override" "$T/xcb.log"

echo "iOS"
check_msg "sin xcodebuild → FALLA (no 'ok con aviso')"     1 "xcodebuild no está disponible" gate "$P_WITHOUT" -- "$FID"
check_msg "xcodebuild falla → FALLA y muestra el error real" 1 "Unable to find a destination" gate "$P_WITH" STUB_XCB_RC=65 -- "$FID"
check_msg "0 tests ejecutados → FALLA"                       1 "iOS: se ejecutaron 0 tests" gate "$P_WITH" 'STUB_XCRESULT={"totalTestCount":0,"failedTests":0}' -- "$FID"
check_msg "tests fallidos → FALLA"                           1 "2 de 17 tests fallaron" gate "$P_WITH" 'STUB_XCRESULT={"totalTestCount":17,"failedTests":2}' -- "$FID"
check_msg "resultado ilegible → FALLA (fail-closed)"         1 "no pude determinar" gate "$P_WITH" 'STUB_XCRESULT={"foo":1}' -- "$FID"

echo "Android"
check_msg "gradle falla → FALLA"                             1 "unit tests fallaron" gate "$P_WITH" STUB_AND_RC=1 -- "$FID"
check_msg "0 tests ejecutados → FALLA"                       1 "Android: se ejecutaron 0 tests" gate "$P_WITH" STUB_AND_TESTS=0 -- "$FID"
check_msg "XML con fallos aunque gradle devuelva 0 → FALLA"  1 "3 de 11 tests fallaron" gate "$P_WITH" STUB_AND_FAIL=3 -- "$FID"

echo "Configuración"
rm -f "$R/Makefile"
check_msg "sin DEST ni Makefile → FALLA"                     1 "falta la configuración" gate "$P_WITH" -- "$FID"
cp "$ROOT/Makefile" "$R/Makefile"

echo "Board y argumentos"
mutate_board "d[1]['id'] = d[0]['id']"
check_msg "id duplicado → FALLA"                             1 "id duplicado" gate "$P_WITH" -- "$FID"
mk_repo
mutate_board "d[0]['spec'] = 'features/no-existe.feature'"
check_msg "spec inexistente → FALLA"                         1 "la spec no existe" gate "$P_WITH" -- "$FID"
mk_repo
check_msg "feature inexistente → FALLA"                      1 "no existe en feature_list" gate "$P_WITH" -- "no-existe"
rm -f "$T/PWNED"
check "id con inyección → FALLA sin ejecutar código"        1 gate "$P_WITH" -- "x\"; import os; os.system(\"touch $T/PWNED\"); \""
assert "…y no se creó el fichero de la inyección"           test ! -e "$T/PWNED"

echo "Modos sueltos"
check "--only ios verde → 0"                                0 gate "$P_WITH" -- --only ios
check "--only android con 0 tests → 1"                      1 gate "$P_WITH" STUB_AND_TESTS=0 -- --only android
check "--only + feature-id → uso incorrecto (2)"            2 gate "$P_WITH" -- --only ios "$FID"
check "--check-board con board válido → 0"                  0 gate "$P_WITHOUT" -- --check-board
printf '{bad' > "$R/feature_list.json"
check "--check-board con JSON roto → 1"                     1 gate "$P_WITHOUT" -- --check-board

echo ""
TOTAL=$((PASS + FAILN))
if [ "$FAILN" -eq 0 ]; then
  echo "✓ Autotest del gate: $PASS/$TOTAL casos correctos"
  exit 0
fi
echo "✗ Autotest del gate: $FAILN de $TOTAL casos FALLARON" >&2
exit 1
