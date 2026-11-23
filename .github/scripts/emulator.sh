#!/usr/bin/env bash
# Executado dentro de projects/team-09/app com o emulador já ligado.
set -euo pipefail

OUT="$GITHUB_WORKSPACE/ci-out"
APP=br.unemat.ritmo
RUNNER=br.unemat.ritmo.test/androidx.test.runner.AndroidJUnitRunner
mkdir -p "$OUT/evidence"

adb shell getprop ro.product.model | tee "$OUT/device.txt"
adb shell getprop ro.build.version.release | tee -a "$OUT/device.txt"
adb shell getprop ro.build.version.sdk | tee -a "$OUT/device.txt"

# Janelas de erro do sistema (ex.: "System UI não está respondendo") roubam o foco e derrubam os testes.
adb shell settings put global hide_error_dialogs 1 || true
adb shell input keyevent 82 || true

# 1) Todos os testes instrumentados (as classes *Evidence são ignoradas aqui).
status=0
./gradlew connectedDebugAndroidTest --console=plain 2>&1 | tee "$OUT/connected-tests.txt" || status=$?
find app/build/outputs/androidTest-results -name 'TEST-*.xml' -exec cp {} "$OUT/" \; || true
[ "$status" -eq 0 ] || { echo "Testes instrumentados falharam"; exit "$status"; }

# 2) Evidências: cada passo roda em um processo novo do app.
# O fuso de Mato Grosso só é aplicado aqui, para as datas do histórico saírem no horário local
# (trocá-lo antes dos testes deixou alguns deles instáveis).
adb shell cmd alarm set-timezone America/Cuiaba || true
adb shell date | tee "$OUT/device-date.txt"
./gradlew installDebug installDebugAndroidTest --console=plain

# Sprint 07: abertura limpa (dados apagados), voltar na tela raiz e registro de travamentos.
adb logcat -b crash -c || true
LAUNCH="$OUT/clean-launch.txt"
: > "$LAUNCH"
adb shell pm clear "$APP" | tee -a "$LAUNCH"
echo "== am start -W (abertura a frio, sem dados)" | tee -a "$LAUNCH"
adb shell am start -W -n "$APP/.MainActivity" | tee -a "$LAUNCH"
sleep 3
echo "== atividade em primeiro plano depois de abrir" | tee -a "$LAUNCH"
adb shell dumpsys activity activities | grep -E "topResumedActivity|mResumedActivity" | head -n 2 | tee -a "$LAUNCH" || true
echo "processo do app: $(adb shell pidof $APP || echo 'nenhum')" | tee -a "$LAUNCH"
echo "== tecla Voltar na tela inicial" | tee -a "$LAUNCH"
adb shell input keyevent 4
sleep 3
adb shell dumpsys activity activities | grep -E "topResumedActivity|mResumedActivity" | head -n 2 | tee -a "$LAUNCH" || true
echo "processo do app: $(adb shell pidof $APP || echo 'nenhum')" | tee -a "$LAUNCH"
adb shell am force-stop "$APP"
adb shell pm clear "$APP" | tee -a "$LAUNCH"

LOG="$OUT/evidence-run.txt"
: > "$LOG"
for step in $EVIDENCE_STEPS; do
  adb shell am force-stop "$APP"
  # Sprint 06: cada passo começa sem a permissão de notificação, para a janela do sistema aparecer.
  adb shell pm revoke "$APP" android.permission.POST_NOTIFICATIONS 2>/dev/null || true
  adb shell pm clear-permission-flags "$APP" android.permission.POST_NOTIFICATIONS user-set user-fixed 2>/dev/null || true
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

# Travamentos registrados pelo Android durante toda a execução (vazio = nenhum).
adb logcat -b crash -d > "$OUT/crash-log.txt" || true
echo "linhas no log de travamentos: $(wc -l < "$OUT/crash-log.txt")"
