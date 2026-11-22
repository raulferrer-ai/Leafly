# Leafly — executable workflow nodes (O4)
#
# Usage:
#   make init
#   make check-sim            # comprueba que el simulador de DEST existe
#   make sensor-ios
#   make sensor-android
#   make sensors              # both platforms
#   make gate                 # dual gate (in_progress feature)
#   make gate FEATURE=<id>    # dual gate for one feature
#   make verify               # alias of gate (the gate already runs both sensors)

.PHONY: init check-sim sensor-ios sensor-android sensors gate verify help

# ---------------------------------------------------------------
# ÚNICA fuente de verdad de la configuración iOS.
# scripts/verify-feature.sh, el CI y la documentación leen estos
# valores de aquí (make -s print-DEST). No los copies a ningún otro
# archivo: para cambiar de simulador edita SOLO la línea DEST,
# o úsalo puntualmente: make sensor-ios DEST='platform=iOS Simulator,name=…'
# ---------------------------------------------------------------
DEST ?= platform=iOS Simulator,name=iPhone 17 Pro
IOS_PROJECT ?= ios/Leafly/Leafly.xcodeproj
IOS_SCHEME ?= Leafly
FEATURE ?=

export DEST IOS_PROJECT IOS_SCHEME

# make -s print-DEST  → imprime el valor (lo usan el gate y el CI)
print-%:
	@printf '%s\n' '$($*)'

help:
	@echo "Leafly workflow nodes"
	@echo "  make init              - harness structure check"
	@echo "  make check-sim         - the iOS simulator in DEST exists on this Mac"
	@echo "  make sensor-ios        - iOS tests (simulator comes from DEST in this Makefile)"
	@echo "  make sensor-android    - Android unit tests (JDK 17+)"
	@echo "  make sensors           - iOS + Android sensors"
	@echo "  make gate              - dual gate (in_progress feature)"
	@echo "  make gate FEATURE=id   - dual gate for one feature"
	@echo "  make verify            - same as gate"
	@echo ""
	@echo "Human still required: dual UX checklist, merge, tag."

init:
	@chmod +x init.sh 2>/dev/null || true
	./init.sh

check-sim:
	@name=$$(printf '%s' '$(DEST)' | sed -n 's/.*name=\([^,]*\).*/\1/p'); \
	if xcrun simctl list devices available | grep -E "^ +$$name \(" >/dev/null; then \
	  echo "✓ simulator available: $$name"; \
	else \
	  echo "✗ simulator '$$name' not found. Available devices:"; \
	  xcrun simctl list devices available; \
	  echo "Edit DEST in the Makefile (single source) or run: make … DEST='platform=iOS Simulator,name=<device>'"; \
	  exit 1; \
	fi

loop-ios:
	@chmod +x scripts/sensor-loop.sh scripts/verify-feature.sh 2>/dev/null || true
	@./scripts/sensor-loop.sh ios

loop-android:
	@chmod +x scripts/sensor-loop.sh scripts/verify-feature.sh 2>/dev/null || true
	@./scripts/sensor-loop.sh android

# Sensors use the same code (and the same checks: 0 tests = failure) as the gate.
sensor-ios:
	@chmod +x scripts/verify-feature.sh 2>/dev/null || true
	@./scripts/verify-feature.sh --only ios

sensor-android:
	@chmod +x scripts/verify-feature.sh 2>/dev/null || true
	@java -version 2>&1 | head -1
	@./scripts/verify-feature.sh --only android

sensors: sensor-ios sensor-android
	@echo "✓ both platform sensors green"

gate:
	@chmod +x scripts/verify-feature.sh 2>/dev/null || true
ifeq ($(FEATURE),)
	./scripts/verify-feature.sh
else
	./scripts/verify-feature.sh $(FEATURE)
endif

# The gate already runs both platform sensors; running `sensors` first would
# execute every test twice.
verify: gate
	@echo "✓ verify path green — still do dual checklist, then mark done by hand"
