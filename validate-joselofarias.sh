#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
fail=0
check() { if eval "$2"; then printf 'PASS  %s\n' "$1"; else printf 'FAIL  %s\n' "$1"; fail=1; fi; }
check 'flavor joselofarias' "grep -q 'joselofarias {' app/build.gradle"
check 'Play flavor removed' "! grep -q 'applicationIdSuffix \".play\"' app/build.gradle"
check 'Play source removed' "test ! -d app/src/play"
check 'JoseloFarias source set' "test -d app/src/joselofarias"
check 'unlock flag renamed' "grep -q JOSELOFARIAS_UNLOCKED app/src/main/java/app/simple/inure/preferences/TrialPreferences.kt"
check 'selected-folder preference' "grep -q SCAN_FOLDERS app/src/main/java/app/simple/inure/preferences/ApkBrowserPreferences.kt"
check 'no absolute-root APK scan' "! grep -q 'Environment.getExternalStorageDirectory(), SDCard' app/src/main/java/app/simple/inure/viewmodels/panels/ApkBrowserViewModel.kt"
check 'folder picker present' "grep -q OpenDocumentTree app/src/main/java/app/simple/inure/dialogs/apks/ApksMenu.kt"
check 'build task renamed' "grep -q assembleJoselofariasDebug build-joselofarias.sh"
check 'proot-aware build launcher' "grep -q 'proot-distro login debian' build-joselofarias.sh"
check 'no stale GitHub dependency configuration' "! grep -qE '^[[:space:]]*github(Implementation|Api|CompileOnly|RuntimeOnly|Kapt|Ksp|AnnotationProcessor)' app/build.gradle"
check 'no stale community marker' "! grep -RqsE 'COMMUNITY_UNLOCKED|Inure Community|-community' app build-joselofarias.sh FORK_NOTICE.md"
python3 - <<'PY'
import xml.etree.ElementTree as ET
for p in [
'app/src/main/res/layout/dialog_menu_apk_browser.xml',
'app/src/main/res/values/strings.xml',
'app/src/main/res/values-es-rES/strings.xml',
'app/src/main/res/drawable/ic_launcher_background.xml']:
    ET.parse(p)
    print('PASS  XML', p)
PY
exit "$fail"
