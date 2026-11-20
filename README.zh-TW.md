# HyperOS 桌面上滑 → Google 搜尋

[English](README.md) | **繁體中文**

一個 LSPosed 模組：讓**國行 HyperOS** 裝置在**桌面上滑時直接開啟 Google 搜尋**（搜尋框 + 鍵盤自動彈出），效果等同國際版桌面的「在桌面上滑以開啟搜尋」。

不需要額外的 App、不需要 HyperCeiler 手勢、也不需要 Automate 流程：模組直接 hook 系統桌面（`com.miui.home`）。

> 測試環境（皆為國行 ROM）：
> - Xiaomi 13 Pro：HyperOS 3、Android 16（KernelSU）
> - Xiaomi Pad 6S Pro：HyperOS 3
> - Redmi Note 11 Pro 5G：HyperOS 1.0、Android 13（APatch）
>
> 桌面更新可能改掉被 hook 的方法名稱，請見[疑難排解](#疑難排解)。

---

## 國行為什麼沒有這個功能

國際版的上滑搜尋，是小米依 Google Mobile Services 授權協議在自家桌面做的整合：桌面只負責送出搜尋 Intent，畫面其實是 **Google app** 自己的搜尋頁。國行沒有這段整合，所以即使補上 GMS，上滑仍然是小米全域搜尋（或沒有動作）。

既然搜尋畫面屬於 Google app，桌面只要送出同一個 Intent 就好，這個模組做的就是這件事。

---

## 需求

| 項目 | 需求 |
|---|---|
| ROM | HyperOS 國行，手機或平板。已在 HyperOS 1.0 – 3 測試 |
| Root | KernelSU / APatch / Magisk |
| 框架 | LSPosed（或其分支，Xposed API ≥ 82） |
| Google app | `com.google.android.googlequicksearchbox` 已安裝且未停用 |
| 桌面 | 經典模式（不使用抽屜），見[注意事項](#注意事項) |

## 下載

到 [**Releases**](../../releases/latest) 下載 `HomeGSearch.apk`，並比對 release 說明中的 SHA-256。

---

## 安裝

1. **先移除其他綁在「桌面上滑」的設定。**
   如果之前用 HyperCeiler 做這件事，把它的桌面上滑動作設回「無」，或在 LSPosed 把 `com.miui.home` 從 HyperCeiler 的作用域移除。兩個模組 hook 同一個方法時，執行順序沒有保證。
2. 安裝 `HomeGSearch.apk`。
3. 在 LSPosed 啟用 **Home Swipe-Up Google Search**，作用域只勾 **系統桌面（`com.miui.home`）**。
4. 重啟桌面讓 hook 載入（需 root，例如在 Termux）：
   ```sh
   su -c "am force-stop com.miui.home"
   ```
5. 在桌面空白處上滑測試。

### 從舊做法轉移

| 之前用的 | 處理方式 |
|---|---|
| G Search 跳板 App + HyperCeiler 手勢 | 確認本模組正常後，移除跳板 App |
| 套件名稱不同的舊版本模組 | 先移除舊版，再安裝本版並在 LSPosed 重新啟用 |

---

## 運作原理

所有 hook 都在 `com.miui.home` 內執行，每個 hook 各自包在 `try/catch`：方法不存在就跳過並記錄 log；送 Intent 失敗則退回桌面原本的行為。

| # | Hook | 作用 |
|---|---|---|
| 1 | `launcher.Workspace#onVerticalGesture(int, MotionEvent)` | 上滑（`type == 10`）、單指、非編輯模式時，送出 `android.search.action.GLOBAL_SEARCH` 給 Google app，並攔下原本的手勢 |
| 2 | `launcher.DeviceConfig#isGlobalSearchEnable`、`search.SearchEdgeLayout#isBottomGlobalSearchEnable`、`DeviceConfig#isGlobalSearchBottomEffectEnable` → `false`（找不到時改 hook `DeviceConfig#allowedSlidingUpToStartGolbalSearch`） | 關掉小米自己的上滑全域搜尋，避免搶手勢 |
| 3 | `launcher.allapps.LauncherMode#getSlideUpGesture` → `"no_action"` | 避免原生上滑動作同時觸發 |

送出的 Intent 跟國際版桌面相同。不裝模組也可以先測試（需 root）：

```sh
su -c "am start -a android.search.action.GLOBAL_SEARCH -p com.google.android.googlequicksearchbox"
```

---

## 疑難排解

查看模組 log：

```sh
su -c "grep -rh 'HomeGSearch' /data/adb/lspd/log/ | tail -20"
```

| Log / 現象 | 意思 |
|---|---|
| `hooked com.miui.home.launcher.Workspace#onVerticalGesture` | 主要 hook 已生效 |
| `skip ... NoSuchMethodError` | 你的桌面版本沒有這個方法，請附上 log 和桌面版本開 issue |
| `launch failed, fallback to stock` | Google app 沒裝、被停用，或被擋（例如隱藏應用規則） |
| 完全沒有 `HomeGSearch` 字樣 | 模組沒啟用、作用域沒勾 `com.miui.home`，或桌面沒重啟 |

查桌面版本：

```sh
su -c "dumpsys package com.miui.home | grep versionName"
```

**救援：** 桌面一直崩潰時，到 LSPosed 停用本模組。如果連 LSPosed 都進不去，用 root 方案的安全模式開機（KernelSU：開機時按住音量下鍵），會停用所有模組。

## 注意事項

- **抽屜模式：** 桌面開了抽屜時，上滑會打開抽屜，`onVerticalGesture` 不會被呼叫。請切回經典模式。
- **隱藏應用模組：** 桌面必須看得到 Google app，不要對 `com.miui.home` 隱藏 `com.google.android.googlequicksearchbox`。
- **HyperOS ≠ MIUI：** hook 點只對照過 HyperOS 桌面程式碼，不支援 MIUI 或 AOSP 桌面。

---

## 自行編譯

使用 Android SDK build-tools，不需要 Gradle：

```sh
export ANDROID_HOME=/path/to/android-sdk
bash build.sh            # 輸出：build/HomeGSearch.apk
```

Xposed API 以 compile-only stub 放在 `stubs/`，不會被打包進 APK。
GitHub Actions 會在每次 push 時編譯；推送 `v*` tag 時，用 repository secrets（`KEYSTORE_BASE64`、`KEYSTORE_PASSWORD`、`KEY_ALIAS`、`KEY_PASSWORD`）裡的金鑰簽章並發布 release。

## 致謝

- Hook 點參考 [HyperCeiler](https://github.com/ReChronoRain/HyperCeiler)（AGPL-3.0）的 `ScreenSwipe` 規則定位，本專案未包含 HyperCeiler 的程式碼。
- [LSPosed](https://github.com/LSPosed/LSPosed)

## 授權

[MIT](LICENSE)

與小米、Google 無關，使用風險自負。
