#!/usr/bin/env bash
# Build HomeGSearch.apk with plain Android SDK build-tools (no Gradle).
#
# Requirements: JDK 17+, Android SDK with build-tools (d8, aapt2, zipalign, apksigner)
# and at least one platform (android.jar). ANDROID_HOME or ANDROID_SDK_ROOT must be set.
#
# Signing (optional, for a stable release key):
#   KEYSTORE_FILE      path to .jks
#   KEYSTORE_PASSWORD  keystore password
#   KEY_ALIAS          key alias
#   KEY_PASSWORD       key password (defaults to KEYSTORE_PASSWORD)
# Without these, a throwaway key is generated in build/ (fine for testing only;
# APKs signed with different keys cannot be installed over each other).
set -euo pipefail
cd "$(dirname "$0")"

SDK="${ANDROID_HOME:-${ANDROID_SDK_ROOT:?Set ANDROID_HOME or ANDROID_SDK_ROOT}}"
BT="${BUILD_TOOLS:-$(ls -1 "$SDK/build-tools" | sort -V | tail -1)}"
PLAT="${PLATFORM:-$(ls -1 "$SDK/platforms" | sort -V | tail -1)}"
B="$SDK/build-tools/$BT"
JAR="$SDK/platforms/$PLAT/android.jar"
echo "build-tools: $BT | platform: $PLAT"

rm -rf build
mkdir -p build/stubs build/classes build/dex

# 1) Compile (stubs are compile-only and are NOT packaged)
javac --release 8 -nowarn -classpath "$JAR" -d build/stubs $(find stubs -name '*.java')
javac --release 8 -nowarn -classpath "$JAR:build/stubs" -d build/classes $(find app/src/main/java -name '*.java')

# 2) Dex
"$B/d8" --release --min-api 30 --lib "$JAR" --classpath build/stubs \
    --output build/dex $(find build/classes -name '*.class')

# 3) Resources + manifest + assets (xposed_init)
"$B/aapt2" compile --dir app/src/main/res -o build/res.zip
"$B/aapt2" link -I "$JAR" --manifest app/src/main/AndroidManifest.xml \
    -A app/src/main/assets -o build/unsigned.apk build/res.zip
(cd build/dex && zip -q -j ../unsigned.apk classes.dex)

# 4) Align
"$B/zipalign" -f -p 4 build/unsigned.apk build/aligned.apk

# 5) Sign
if [[ -n "${KEYSTORE_FILE:-}" ]]; then
    KS="$KEYSTORE_FILE"; KSPASS="${KEYSTORE_PASSWORD:?}"; ALIAS="${KEY_ALIAS:?}"
    KEYPASS="${KEY_PASSWORD:-$KSPASS}"
else
    echo "WARNING: no KEYSTORE_FILE set, signing with a throwaway key"
    KS=build/throwaway.jks; KSPASS=android; ALIAS=throwaway; KEYPASS=android
    keytool -genkeypair -keystore "$KS" -storepass "$KSPASS" -keypass "$KEYPASS" \
        -alias "$ALIAS" -keyalg RSA -keysize 2048 -validity 10000 \
        -dname "CN=HomeGSearch throwaway" >/dev/null 2>&1
fi
"$B/apksigner" sign --ks "$KS" --ks-pass "pass:$KSPASS" --ks-key-alias "$ALIAS" \
    --key-pass "pass:$KEYPASS" --out build/HomeGSearch.apk build/aligned.apk
"$B/apksigner" verify --print-certs build/HomeGSearch.apk | grep -E "Signer #1 certificate (DN|SHA-256)"

ls -l build/HomeGSearch.apk
sha256sum build/HomeGSearch.apk
