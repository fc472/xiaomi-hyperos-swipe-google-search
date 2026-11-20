# Xiaomi CN ROM: Swipe Up on Home Screen to Open Google Search

**English** | [繁體中文](README.zh-TW.md)

Global ROM launchers have "Swipe up on the home screen to open search", which opens the Google search box with the keyboard shown. On China ROMs, swiping up still opens Xiaomi's own global search, even with GMS enabled.

This LSPosed module (HomeGSearch) hooks the system launcher so China ROMs get the same behaviour. No HyperCeiler gesture or extra launcher app needed.

## Tested on

* Xiaomi 13 Pro, CN ROM (HyperOS 3)
* Xiaomi Pad 6S Pro, CN ROM (HyperOS 3)
* Redmi Note 11 Pro 5G, CN ROM (HyperOS 1.0)

## Requirements

* Root (KernelSU, APatch or Magisk)
* LSPosed
* Google app installed
* Classic home screen (no app drawer); in drawer mode the swipe-up opens the drawer instead

## Installation

**1. Install HomeGSearch.apk**

Download it from [Releases](../../releases/latest).

**2. Enable the module in LSPosed**

LSPosed → Modules → Home Swipe-Up Google Search → enable. Scope: System launcher only.

**3. Restart the launcher**

Reboot, or run `su -c "am force-stop com.miui.home"` in Termux. Then swipe up on an empty area of the home screen.

Note: if you set a home swipe-up action in HyperCeiler before, set it back to none, or the two modules will fight over the gesture.

If the launcher keeps crashing, disable this module in LSPosed.

If it doesn't work on your device, open an issue with your LSPosed log (lines containing `HomeGSearch`).

## Other

* The APK is built and signed by GitHub Actions from the source in this repo.
* Hook targets were located with reference to the ScreenSwipe rule in [HyperCeiler](https://github.com/ReChronoRain/HyperCeiler); no HyperCeiler code is included.
* License: [MIT](LICENSE). Not affiliated with Xiaomi or Google. Use at your own risk.
