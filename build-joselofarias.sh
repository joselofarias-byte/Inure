#!/usr/bin/env bash
set -euo pipefail

REPO="$(cd "$(dirname "$0")" && pwd)"
TERMUX_HOME="/data/data/com.termux/files/home"
DOWNLOAD="$TERMUX_HOME/storage/shared/Download"
STAMP="$(date +%Y%m%d-%H%M%S)"
REPORT="${REPORT:-$DOWNLOAD/GPT-5.6-Thinking_2026-07-18_Inure_JoseloFarias_Build_${STAMP}.txt}"

if [[ ! -f /etc/debian_version ]]; then
    if ! command -v proot-distro >/dev/null 2>&1; then
        printf 'ERROR: proot-distro no está disponible.\n' >&2
        exit 1
    fi
    exec proot-distro login debian -- env REPO="$REPO" REPORT="$REPORT" bash -lc \
        'cd "$REPO" && exec bash ./build-joselofarias.sh'
fi

cd "$REPO"
chmod +x gradlew
mkdir -p "$(dirname "$REPORT")"

{
    printf 'Repositorio: %s\n' "$REPO"
    printf 'Fecha: %s\n' "$(date '+%Y-%m-%d %H:%M:%S %z')"
    printf 'Java:\n'
    java -version
    printf '\n=== VALIDACION ===\n'
    ./validate-joselofarias.sh
    printf '\n=== BUILD ===\n'
} 2>&1 | tee "$REPORT"

set +e
set -o pipefail
./gradlew --no-daemon clean :app:assembleJoselofariasDebug 2>&1 | tee -a "$REPORT"
RC=${PIPESTATUS[0]}
set -e

{
    printf '\nEXIT_CODE=%s\n' "$RC"
    printf '\n=== APK GENERADO ===\n'
    APK="$(find app/build/outputs/apk/joselofarias/debug -maxdepth 1 -type f -name '*.apk' -print -quit 2>/dev/null || true)"
    if [[ -n "$APK" ]]; then
        ls -lh "$APK"
        OUT="$DOWNLOAD/Inure-JoseloFarias-build107.1.0-debug.apk"
        cp -f "$APK" "$OUT"
        printf 'APK_COPIADO=%s\n' "$OUT"
    else
        printf 'APK no generado.\n'
    fi
    printf 'REPORTE=%s\n' "$REPORT"
} 2>&1 | tee -a "$REPORT"

exit "$RC"
