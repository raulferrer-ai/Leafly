# Feature workflow (graph)

This document is the **explicit graph** for how a feature moves in Leafly.
The loop (build → test → fix) lives inside platform nodes.
The gate is the last automated lock before `done`.
What merges and what is tagged remains a **human** decision.

Related: `AGENTS.md` (rules, sensors, loop, DoD), `scripts/verify-feature.sh` (gate).

---

## Diagram

```text
                    ┌─────────────┐
                    │    spec     │
                    │  (.feature) │
                    └──────┬──────┘
                           │
                    ┌──────▼──────┐
                    │ in_progress │
                    │ feature_list│
                    └──────┬──────┘
                           │
              ┌────────────▼────────────┐
              │     implement iOS       │
              └────────────┬────────────┘
                           │
              ┌────────────▼────────────┐
              │      sensor iOS         │
              │  (xcodebuild test/build)│
              └────────────┬────────────┘
                     pass│ │fail
                         │ └──► back to implement iOS
                         │
              ┌────────────▼────────────┐
              │   implement Android     │
              └────────────┬────────────┘
                           │
              ┌────────────▼────────────┐
              │    sensor Android       │
              │ (./gradlew testDebug… ) │
              └────────────┬────────────┘
                     pass│ │fail
                         │ └──► back to implement Android
                         │
              ┌────────────▼────────────┐
              │   dual checklist        │  ← HUMAN checkpoint
              │  (same behaviour both)  │
              └────────────┬────────────┘
                           │
              ┌────────────▼────────────┐
              │         gate            │
              │ ./scripts/verify-feature│
              └────────────┬────────────┘
                     pass│ │fail
                         │ └──► do not set done; fix and re-enter
                         │
              ┌────────────▼────────────┐
              │         done            │
              │   feature_list.json     │
              └────────────┬────────────┘
                           │
              ┌────────────▼────────────┐
              │   commit / PR / merge   │  ← HUMAN (what ships)
              │   optional tag          │
              └─────────────────────────┘
```

One platform per agent session. Do not implement iOS and Android in the same session unless explicitly instructed.

---

## Nodes

| Node | Meaning | Exit condition |
|------|---------|----------------|
| **spec** | Gherkin (or agreed format) describes behaviour | Spec exists and is referenced from `feature_list.json` |
| **in_progress** | Exactly one active product feature | `status: in_progress` for that id only |
| **implement iOS** | Minimal change to satisfy the spec on iOS | Code compiles; ready to measure |
| **sensor iOS** | Feedback command for iOS | Test (preferred) or documented green build |
| **implement Android** | Same for Android | Code compiles; ready to measure |
| **sensor Android** | `./gradlew :app:testDebugUnitTest` (JDK 17+) | Tests green |
| **dual checklist** | Human compares behaviour and tone on both apps | Checklist accepted |
| **gate** | `./scripts/verify-feature.sh [id]` | Script reports PASSED |
| **done** | Feature closed in the board | `status: done` only after gate |
| **commit / merge / tag** | History and what the world sees | Human decision |

---

## Routes (edges)

- **Sensor fail → same implement node** (loop). Do not claim the platform is ready.
- **Gate fail → not done.** Fix platforms or environment (e.g. `JAVA_HOME`), re-run sensors and gate.
- **No skip:** do not jump from one platform green to `done` without the other platform, checklist, and gate.
- **Flywheel (side path):** clear deviation from spec/design system → fix code → optional entry in `progress/flywheel-log.md` → rule or test when the failure is repeatable.

---

## Checkpoints (automated)

1. Spec file present for the feature id.
2. iOS sensor green.
3. Android sensor green.
4. Gate PASSED.

---

## Suspend points (human only)

These are intentional stops. Agents do not decide them alone.

| Suspend | Who | Decision |
|---------|-----|----------|
| **Dual checklist** | Human | Same flows, same calm tone, same acceptance intent on both platforms |
| **Merge to main** | Human | This work belongs on the default branch |
| **Tag / release note** | Human | This commit is a named milestone (e.g. `v2.0-loop-mvp`) |
| **Scope change** | Human | Spec change mid-feature; rewrite spec before more code |

What **ships** to readers of the blog or consumers of the tag is always a human call—even when every sensor is green.

---

## Mapping to maturity levels

| Level | In this repo |
|-------|----------------|
| Harness | `AGENTS.md`, specs, design system, dual DoD |
| Loop | Sensor before "platform ready"; fix until green; then gate |
| Graph | **This file** — nodes, routes, checkpoints, suspend points |

Executable automation of every node (workflow engine) is optional. Following this graph in practice is mandatory for 2.x features.

---

## Commands (quick reference)

```bash
./init.sh
./scripts/verify-feature.sh
./scripts/verify-feature.sh <feature-id>

# Android sensor (JDK 17+)
cd android && ./gradlew :app:testDebugUnitTest

# iOS sensor (adjust simulator)
xcodebuild test \
  -project ios/Leafly/Leafly.xcodeproj \
  -scheme Leafly \
  -destination 'platform=iOS Simulator,name=iPhone 16'
```

### Make targets (executable graph)

```bash
make init
make sensor-ios
make sensor-android
make gate
make gate FEATURE=<id>
make verify FEATURE=<id>

Human suspend points (dual checklist, merge, tag) are not Make targets on purpose.
```

---

## Change policy

Changes to this graph should be deliberate: update this doc and, if needed, `AGENTS.md` in the same change set so agents and humans share one picture of the flow.
