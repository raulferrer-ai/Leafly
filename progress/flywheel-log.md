# Flywheel log — Leafly

Registro de fallos convertidos (o pendientes de convertir) en reglas o sensores.

<!-- Las entradas nuevas van arriba -->

### 2026-11-21 — Cierre de las 12 features sin validación automática de paridad ni chequeo global de cierre

- **Síntoma:** las 12 features de `feature_list.json` figuran en estado `done`, pero `./scripts/verify-feature.sh` sin argumentos avisa de que no hay feature `in_progress` y no existe una comprobación automática que audite la paridad de tests o reglas entre iOS y Android más allá de que cada suite pase por separado.
- **Causa probable:** el gate se diseñó para validar la feature activa (`in_progress`) o una feature concreta por ID, y la equivalencia fina entre plataformas dependía del checklist humano.
- **Corrección en código:** se añade validación del tablero en `init.sh` (`--check-board`), mensajes claros cuando todas las features están en `done` y documentación explícita del alcance real del gate y del loop.
- **Cambio en el harness:**
  - [x] Nueva regla en `AGENTS.md` (distinción entre protocolo de loop y comprobaciones ejecutables)
  - [ ] Ajuste de spec
  - [ ] Nuevo test o aserción (sensor): pendiente (`scripts/check-parity.sh` o `--check-done`)
  - [ ] Nada
- **Texto de la regla o del test (propuesto):** «El gate verifica que el tablero sea coherente y que los sensores de ambas plataformas ejecuten tests en verde; la equivalencia de comportamiento y tono se revisa en el checklist dual y se documenta en la matriz de paridad.»

### 2026-11-21 — Paridad de tests iOS/Android sin comprobar (ABIERTA)

- **Síntoma:** `android/app/src/test` tiene 11 ficheros `*Test.kt` (uno es el `ExampleUnitTest` de plantilla) y `ios` tiene 17 `*Tests.swift` (6 son UI tests). En Android no hay unit tests equivalentes a `SettingsViewModelTests`, `NotificationPreferencesTests` ni `SwiftDataPlantRepositoryTests` (repositorio). Nada lo detecta: la DoD dual depende de que una persona lo compare.
- **Causa probable:** sensor inexistente; la paridad solo se revisa en el checklist humano.
- **Corrección en código:** pendiente (añadir los tests equivalentes en Android o documentar por qué no aplican).
- **Cambio en el harness:**
  - [ ] Nueva regla en `AGENTS.md`
  - [ ] Ajuste de spec
  - [ ] Nuevo test o aserción (sensor): **pendiente**, `scripts/check-parity.sh`
  - [ ] Nada
- **Texto de la regla o del test (propuesto):** «Toda feature `done` debe tener tests con el nombre de cada Scenario de su `.feature` en iOS y en Android; si un Scenario no aplica a una plataforma, se declara en `feature_list.json` con el motivo.» El script falla si falta alguno.

### 2026-11-21 — El gate podía dar verde sin tests

- **Síntoma:** `scripts/verify-feature.sh` daba iOS por bueno si `xcodebuild test` fallaba pero `xcodebuild build` compilaba, y también si faltaba `xcodebuild` o `gradlew`. Los errores se ocultaban con `2>/dev/null`. No consta si llegó a ocurrir en alguna feature.
- **Causa probable:** sensor permisivo y una regla que lo legitimaba (`AGENTS.md`: «mínimo aceptable: `xcodebuild build` en verde»).
- **Corrección en código:** endurecimiento de `scripts/verify-feature.sh` en modo *fail-closed* y creación de `scripts/selftest-gate.sh`.
- **Cambio en el harness:**
  - [x] Nueva regla en `AGENTS.md` (un build en verde no es un sensor válido)
  - [ ] Ajuste de spec
  - [x] Nuevo test o aserción (sensor): gate *fail-closed* y `scripts/selftest-gate.sh` en el job `harness` del CI.
  - [ ] Nada
- **Texto de la regla o del test:** «Un `xcodebuild build` en verde NO es un sensor válido. Si los tests no se pueden ejecutar, la feature no está verificada en iOS. El sensor falla también si se ejecutan 0 tests.»

### 2026-11-21 — El CI de iOS solo compilaba

- **Síntoma:** el job `ios` ejecutaba únicamente `xcodebuild build` y el gate no corría en CI. Los tests de iOS no se ejecutaban en CI en ningún momento, y el CI de Android sí corría tests: dos plataformas, dos niveles de exigencia.
- **Causa probable:** sensor inexistente en CI para iOS.
- **Corrección en código:** actualización de `.github/workflows/ci.yml` para ejecutar `make check-sim` y `make sensor-ios` en `macos-26`, `make sensor-android` en Android y un job agregador `Gate`.
- **Cambio en el harness:**
  - [ ] Nueva regla en `AGENTS.md`
  - [ ] Ajuste de spec
  - [x] Nuevo test o aserción (sensor): jobs `ios` (`make sensor-ios`) y `android` (`make sensor-android`) y job agregador `Gate`.
  - [ ] Nada
- **Texto de la regla o del test:** «`Gate` es el check requerido en `main`; solo está en verde si `harness`, `android` e `ios` lo están.»

### 2026-11-21 — Simulador definido en cinco sitios con dos valores distintos

- **Síntoma:** el `Makefile`, `AGENTS.md`, `README.md` y `docs/workflow.md` usaban «iPhone 16»; el gate usaba «iPhone 17 Pro»; el CI compilaba para un destino genérico. Un cambio de simulador exigía tocar cinco sitios y ya habían divergido.
- **Causa probable:** configuración copiada en lugar de referenciada.
- **Corrección en código:** centralización de `DEST`, `IOS_PROJECT` y `IOS_SCHEME` en el `Makefile` y lectura vía `make -s print-...` en scripts y CI.
- **Cambio en el harness:**
  - [x] Nueva regla en `AGENTS.md`
  - [ ] Ajuste de spec
  - [x] Nuevo test o aserción (sensor): comprobación de fuente única en CI y casos del autotest del gate (`selftest-gate.sh`).
  - [ ] Nada
- **Texto de la regla o del test:** «El simulador, el proyecto y el scheme se definen solo en el `Makefile` (`DEST`). No los copies a otros archivos.»

### 2026-11-21 — Archivos de IDE y de usuario versionados

- **Síntoma:** `android/.idea/` (incluido `awsToolkit.xml`) y `xcuserdata/<usuario>.xcuserdatad/` están en git desde los primeros commits. No existía un `.gitignore` real (solo contenía `docs/prompts.md`).
- **Causa probable:** regla ausente y sensor inexistente.
- **Corrección en código:** `.gitignore` completo y `git rm -r --cached` sobre esos archivos. El historial anterior no se reescribe (decisión aparte).
- **Cambio en el harness:**
  - [x] Nueva regla en `AGENTS.md` (higiene del repositorio y reglas de trabajo)
  - [ ] Ajuste de spec
  - [x] Nuevo test o aserción (sensor): paso «No IDE or user files tracked in git» del job `harness` del CI.
  - [ ] Nada
- **Texto de la regla o del test:** `git ls-files | grep -E '(^|/)(xcuserdata|\.idea)(/|$)|\.xcuserstate$|(^|/)\.DS_Store$|(^|/)local\.properties$'` — si hay coincidencias, el CI falla.