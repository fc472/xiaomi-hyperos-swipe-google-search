# HyperOS Home Swipe-Up → Google Search

**English** | [繁體中文](README.zh-TW.md)

An LSPosed module that makes **swiping up on the home screen open Google search** (search box with the keyboard already shown) on **China-ROM HyperOS** devices, like the "Swipe up on the home screen to open search" option on Global ROM launchers.

No extra app, no HyperCeiler gesture, no Automate flow: the module hooks the system launcher (`com.miui.home`) directly.

> Tested on (CN ROM):
> - Xiaomi 13 Pro — HyperOS 3, Android 16 (KernelSU)
> - Xiaomi Pad 6S Pro — HyperOS 3
> - Redmi Note 11 Pro 5G — HyperOS 1.0, Android 13 (APatch)
>
> Launcher updates can rename the hooked methods; see [Troubleshooting](#troubleshooting).

---

## Why CN ROM doesn't have this

On Global ROMs, the swipe-up search is part of Xiaomi's launcher integration required by the Google Mobile Services agreement. The launcher just sends a search intent and the **Google app** shows its own search screen. CN ROMs don't have that integration, so the swipe-up opens Xiaomi's global search (or nothing), even when GMS has been enabled.

Since the search screen belongs to the Google app, the launcher only needs to send the same intent. This module does exactly that.

---

## Requirements

| Item | Requirement |
|---|---|
| ROM | HyperOS (CN ROM), phone or tablet. Tested on HyperOS 1.0 – 3 |
| Root | KernelSU / APatch / Magisk |
| Framework | LSPosed (or a fork, Xposed API ≥ 82) |
| Google app | `com.google.android.googlequicksearchbox` installed and enabled |
| Launcher | Classic home screen mode (no app drawer) — see [Notes](#notes) |

## Download

Get `HomeGSearch.apk` from the [**Releases**](../../releases/latest) page and check the SHA-256 in the release notes.

---

## Installation

1. **Remove anything else bound to the home swipe-up gesture.**
   If you used HyperCeiler for this, set its home swipe-up action back to *none*, or remove `com.miui.home` from HyperCeiler's LSPosed scope. Two modules hooking the same method run in no guaranteed order.
2. Install `HomeGSearch.apk`.
3. In LSPosed, enable **Home Swipe-Up Google Search**. The scope should contain only **System launcher (`com.miui.home`)**.
4. Restart the launcher so the hooks load (root, e.g. in Termux):
   ```sh
   su -c "am force-stop com.miui.home"
   ```
5. Swipe up on an empty part of the home screen.

### Migrating from older setups

| Previously used | What to do |
|---|---|
| G Search trampoline app + HyperCeiler gesture | Uninstall the trampoline app after confirming this module works |
| An earlier build of this module with a different package name | Uninstall the old build first, then install this one and re-enable it in LSPosed |

---

## How it works

All hooks run inside `com.miui.home`. Each hook is wrapped in its own `try/catch`: a missing method is skipped and logged, and a failed launch falls back to the stock behaviour.

| # | Hook | Effect |
|---|---|---|
| 1 | `launcher.Workspace#onVerticalGesture(int, MotionEvent)` | On swipe-up (`type == 10`), single finger, not in edit mode: send `android.search.action.GLOBAL_SEARCH` to the Google app and consume the gesture |
| 2 | `launcher.DeviceConfig#isGlobalSearchEnable`, `search.SearchEdgeLayout#isBottomGlobalSearchEnable`, `DeviceConfig#isGlobalSearchBottomEffectEnable` → `false` (fallback: `DeviceConfig#allowedSlidingUpToStartGolbalSearch`) | Turns off Xiaomi's own swipe-up global search so it doesn't compete |
| 3 | `launcher.allapps.LauncherMode#getSlideUpGesture` → `"no_action"` | Stops the stock swipe-up action from firing as well |

The intent is the same one the Global ROM launcher sends. You can test it without the module (root):

```sh
su -c "am start -a android.search.action.GLOBAL_SEARCH -p com.google.android.googlequicksearchbox"
```

---

## Troubleshooting

Check the module log:

```sh
su -c "grep -rh 'HomeGSearch' /data/adb/lspd/log/ | tail -20"
```

| Log / symptom | Meaning |
|---|---|
| `hooked com.miui.home.launcher.Workspace#onVerticalGesture` | Main hook is active |
| `skip ... NoSuchMethodError` | That method doesn't exist in your launcher version. Open an issue with the log and your launcher version |
| `launch failed, fallback to stock` | The Google app is missing, disabled, or blocked (e.g. by an app-hiding rule) |
| No `HomeGSearch` lines at all | Module not enabled, scope missing `com.miui.home`, or the launcher wasn't restarted |

Launcher version:

```sh
su -c "dumpsys package com.miui.home | grep versionName"
```

**Recovery:** if the launcher keeps crashing, disable the module in LSPosed. If you can't reach LSPosed, boot into your root solution's safe mode (for KernelSU: hold volume-down during boot), which disables all modules.

## Notes

- **App drawer mode:** if the home screen uses the app drawer, swipe-up opens the drawer and `onVerticalGesture` is never called. Switch to classic mode.
- **App-hiding modules:** the launcher must be able to see the Google app. Don't hide `com.google.android.googlequicksearchbox` from `com.miui.home`.
- **HyperOS ≠ MIUI:** hook targets were checked against HyperOS launcher code only. MIUI or AOSP launchers are not supported.

---

## Building

Plain Android SDK build-tools, no Gradle:

```sh
export ANDROID_HOME=/path/to/android-sdk
bash build.sh            # output: build/HomeGSearch.apk
```

The Xposed API is provided as compile-only stubs in `stubs/` and is not packaged.
GitHub Actions builds every push; pushing a `v*` tag publishes a release signed with the key stored in the repository secrets (`KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`).

## Credits

- Hook targets were located with reference to the `ScreenSwipe` rule in [HyperCeiler](https://github.com/ReChronoRain/HyperCeiler) (AGPL-3.0). No HyperCeiler code is included.
- [LSPosed](https://github.com/LSPosed/LSPosed)

## License

[MIT](LICENSE)

Not affiliated with Xiaomi or Google. Use at your own risk.
