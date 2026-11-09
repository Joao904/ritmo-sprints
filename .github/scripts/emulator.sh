#!/usr/bin/env bash
# Executado dentro de projects/team-09/app com o emulador já ligado.
set -euo pipefail

OUT="$GITHUB_WORKSPACE/ci-out"
APP=br.unemat.ritmo
RUNNER=br.unemat.ritmo.test/androidx.test.runner.AndroidJUnitRunner
mkdir -p "$OUT/evidence"

adb shell setprop persist.sys.timezone America/Cuiaba || true
adb shell getprop ro.product.model | tee "$OUT/device.txt"
adb shell getprop ro.build.version.release | tee -a "$OUT/device.txt"
adb shell getprop ro.build.version.sdk | tee -a "$OUT/device.txt"

# 1) Todos os testes instrumentados (as classes *Evidence são ignoradas aqui).
./gradlew connectedDebugAndroidTest --console=plain 2>&1 | tee "$OUT/connected-tests.txt"
find app/build/outputs/androidTest-results -name 'TEST-*.xml' -exec cp {} "$OUT/" \; || true

# 2) Evidências: cada passo roda em um processo novo do app.
./gradlew installDebug installDebugAndroidTest --console=plain
LOG="$OUT/evidence-run.txt"
: > "$LOG"
for step in $EVIDENCE_STEPS; do
  adb shell am force-stop "$APP"
  echo "== $step  ($(date -u +%H:%M:%S) UTC)" | tee -a "$LOG"
  echo "processo do app antes do passo: $(adb shell pidof $APP || echo 'nenhum (app fechado)')" | tee -a "$LOG"
  result=$(adb shell am instrument -w -e evidence true -e class "$APP.$step" "$RUNNER")
  echo "$result" | tee -a "$LOG"
  echo "$result" | grep -q "OK (1 test)" || { echo "Passo falhou: $step"; exit 1; }
done

for f in $(adb shell run-as "$APP" ls files/evidence | tr -d '\r'); do
  adb exec-out run-as "$APP" cat "files/evidence/$f" > "$OUT/evidence/$f"
done
ls -la "$OUT/evidence"
