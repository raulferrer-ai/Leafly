# Leafly — executable workflow nodes (O4)
# Usage:
#   make init
#   make sensor-ios
#   make sensor-android
#   make gate
#   make gate FEATURE=watering-log
#   make sensors          # both platforms
#   make verify FEATURE=… # sensors + gate (still run dual checklist yourself)

.PHONY: init sensor-ios sensor-android sensors gate verify help

# Override if needed: make sensor-ios DEST='platform=iOS Simulator,name=iPhone 17 Pro'
DEST ?= platform=iOS Simulator,name=iPhone 16
IOS_PROJECT ?= ios/Leafly/Leafly.xcodeproj
IOS_SCHEME ?= Leafly
FEATURE ?=

help:
	@echo "Leafly workflow nodes"
	@echo "  make init              - harness structure check"
	@echo "  make sensor-ios        - iOS tests (or set DEST=...)"
	@echo "  make sensor-android    - Android unit tests (JDK 17+)"
	@echo "  make sensors           - iOS + Android sensors"
	@echo "  make gate              - dual gate (in_progress feature)"
	@echo "  make gate FEATURE=id   - dual gate for one feature"
	@echo "  make verify FEATURE=id - sensors then gate"
	@echo ""
	@echo "Human still required: dual UX checklist, merge, tag."

init:
	@chmod +x init.sh 2>/dev/null || true
	./init.sh

sensor-ios:
	@echo "→ sensor iOS (DEST=$(DEST))"
	xcodebuild test \
		-project $(IOS_PROJECT) \
		-scheme $(IOS_SCHEME) \
		-destination '$(DEST)' \
		-quiet
	@echo "✓ sensor iOS green"

sensor-android:
	@echo "→ sensor Android (need JDK 17+)"
	@java -version 2>&1 | head -1
	cd android && ./gradlew :app:testDebugUnitTest --quiet
	@echo "✓ sensor Android green"

sensors: sensor-ios sensor-android
	@echo "✓ both platform sensors green"

gate:
	@chmod +x scripts/verify-feature.sh 2>/dev/null || true
ifeq ($(FEATURE),)
	./scripts/verify-feature.sh
else
	./scripts/verify-feature.sh $(FEATURE)
endif

# Full automated path before human checklist + done
verify: sensors gate
	@echo "✓ verify path green — still do dual checklist, then mark done by hand"