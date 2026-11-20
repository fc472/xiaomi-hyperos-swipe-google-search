# 小米陸版ROM 桌面上滑開啟 Google 搜尋

[English](README.md) | **繁體中文**

國際版桌面有「桌面上滑開啟搜尋」的功能，上滑會直接跳出 Google 搜尋框並彈出鍵盤。陸版 ROM 即使補上 GMS，上滑仍然是小米自己的全域搜尋。

這個 LSPosed 模組（HomeGSearch）直接 hook 系統桌面，讓陸版也有一樣的效果，不需要 HyperCeiler 手勢或另外安裝跳板 App。

## 實測

* 小米 13 Pro 陸版（HyperOS 3）
* 小米平板 6S Pro 陸版（HyperOS 3）
* 紅米 Note 11 Pro 5G 陸版（HyperOS 1.0）

## 需求

* 已 root（KernelSU、APatch、Magisk 皆可）
* 已安裝 LSPosed
* 已安裝 Google App
* 桌面使用經典模式（沒有抽屜），抽屜模式下上滑會被抽屜接走

## 安裝

**1. 安裝 HomeGSearch.apk**

到 [Releases](../../releases/latest) 下載 HomeGSearch.apk 並安裝。

**2. 在 LSPosed 啟用模組**

打開 LSPosed → 模組 → Home Swipe-Up Google Search → 開啟模組，作用域只勾選「系統桌面」。

**3. 重啟桌面**

重新開機，或在 Termux 執行 `su -c "am force-stop com.miui.home"`。之後在桌面空白處上滑即可。

附註：如果之前用 HyperCeiler 設定過桌面上滑動作，要先改回「無」，否則兩個模組會搶同一個手勢。

如果桌面異常閃退，到 LSPosed 停用此模組即可恢復。

其他機型不能用的話，請開 issue 並附上 LSPosed log（含 `HomeGSearch` 的那幾行）。

## 其他

* APK 由 GitHub Actions 從本 repo 原始碼編譯並簽章。
* Hook 點參考 [HyperCeiler](https://github.com/ReChronoRain/HyperCeiler) 的 ScreenSwipe 規則定位，未包含其程式碼。
* 授權：[MIT](LICENSE)。與小米、Google 無關，使用風險自負。
