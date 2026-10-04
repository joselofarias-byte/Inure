#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"

fail=0
check() {
    if eval "$2"; then
        printf 'PASS  %s\n' "$1"
    else
        printf 'FAIL  %s\n' "$1"
        fail=1
    fi
}

check 'upstream base 107.2.4' "grep -q 'versionCode 10724' app/build.gradle && grep -q 'versionName \"build107.2.4\"' app/build.gradle"
check 'flavor joselofarias' "grep -q 'joselofarias {' app/build.gradle"
check 'independent package suffix' "grep -q 'applicationIdSuffix \".joselofarias\"' app/build.gradle"
check 'GitHub/FOSS source reuse' "grep -q 'joselofarias.setRoot(\"src/github\")' app/build.gradle"
check 'Play flavor removed' "! grep -q 'applicationIdSuffix \".play\"' app/build.gradle"
check 'Play source removed' "test ! -d app/src/play"
check 'commercial Purchase removed' "test ! -f app/src/github/java/app/simple/inure/dialogs/app/Purchase.kt"
check 'commercial LicenseKey removed' "test ! -f app/src/github/java/app/simple/inure/dialogs/app/LicenseKey.kt"
check 'commercial Gumroad authenticator removed' "test ! -f app/src/github/java/app/simple/inure/viewmodels/autheticators/GumroadLicenceAuthenticatorViewModel.kt"
check 'Unlocker receiver removed' "test ! -f app/src/main/java/app/simple/inure/receivers/LicenceVerificationReceiver.kt"
check 'Full compatibility gate enabled' "grep -q 'isAppFullVersionEnabled(): Boolean = true' app/src/main/java/app/simple/inure/preferences/TrialPreferences.kt"
check 'selected-folder preference' "grep -q CUSTOM_APK_PATHS app/src/main/java/app/simple/inure/preferences/ApkBrowserPreferences.kt"
check 'folder picker present' "grep -q OpenDocumentTree app/src/main/java/app/simple/inure/dialogs/apks/ApksMenu.kt"
check 'extra terminal keys present' "grep -q 'key_ctrl' app/src/main/res/layout/activity_terminal.xml"
check 'build task renamed' "grep -q assembleJoselofariasDebug build-joselofarias.sh"
check 'proot-aware build launcher' "grep -q 'proot-distro login debian' build-joselofarias.sh"

python3 - <<'PY'
import xml.etree.ElementTree as ET
for p in [
    'app/src/main/res/layout/dialog_menu_apk_browser.xml',
    'app/src/main/res/layout/activity_terminal.xml',
    'app/src/main/res/values/strings.xml',
    'app/src/main/res/values-es-rES/strings.xml',
]:
    ET.parse(p)
    print('PASS  XML', p)
PY

exit "$fail"
