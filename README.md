# 🛠️ DevToggle

**DevToggle** is a lightweight, modern Android utility built with Jetpack Compose & Material 3 that lets you instantly toggle **Developer Options** on and off from your app, your **Quick Settings Tiles**, or a **Home Screen Widget** — with zero root required.

---

## ⚡ Features

- **Instant 1-Tap Toggle**: Enable or disable Developer Options on the fly without navigating deep into Android Settings.
- **Quick Settings Tile**: Add the tile to your notification shade for instant access anytime.
- **Home Screen App Widget**: Interactive widget with real-time status updates.
- **Ultra Lightweight**: Shrunk with R8 down to < 1 MB.
- **Zero Root Needed**: Operates via Android's `WRITE_SECURE_SETTINGS` permission.

---

## 📱 Tutorial: Setup Without a PC (Using Shizuku + aShell)

Android protects system settings with the `WRITE_SECURE_SETTINGS` permission. You can grant this permission directly on your device without ever touching a computer by using **Shizuku** and **aShell**.

### 1. Prerequisites (Downloads)

1. **Shizuku**: Download from [Google Play](https://play.google.com/store/apps/details?id=moe.shizuku.privileged.api) or [GitHub Releases](https://github.com/RikkaApps/Shizuku/releases).
2. **aShell** (or aShell You): Download from [F-Droid](https://f-droid.org/packages/in.sunilpaulmathew.ashell/) or [GitHub Releases](https://github.com/sunilpaulmathew/ashell/releases).
3. **DevToggle**: Installed on your phone.

---

### 2. Step-by-Step Guide

#### Step 1: Start Shizuku (Android 11+)

1. Connect your phone to any **Wi-Fi network** (or turn on a Wi-Fi Hotspot on another device and connect to it).
2. Open your phone's **Settings > Developer Options**.
3. Scroll down and enable **Wireless debugging**.
4. Tap directly on the text **"Wireless debugging"** to open its sub-menu.
5. Tap **"Pair device with pairing code"**. A 6-digit Wi-Fi pairing code and port number will appear.
6. Open the notification drawer: tap the **Shizuku "Pairing service found"** notification, tap **Enter pairing code**, and enter the 6 digits.
7. Once paired, return to the **Shizuku** app and tap **Start** under *Start via Wireless debugging*.
8. You should see: **"Shizuku is running (Version ...)"** in green.

> *Note for MIUI / HyperOS users:* In Developer Options, also enable **"USB debugging (Security settings)"** to allow permission grants.

---

#### Step 2: Open aShell & Authorize Shizuku

1. Launch the **aShell** app on your device.
2. A Shizuku authorization dialog will pop up: tap **"Allow all the time"**.
3. aShell will connect and display the interactive terminal prompt.

---

#### Step 3: Grant Permission to DevToggle

Copy and paste the following command into **aShell** and press the **Execute (Run)** button:

```sh
pm grant com.example android.permission.WRITE_SECURE_SETTINGS
```

If successful, the terminal will return to a clean prompt with no error output.

---

#### Step 4: Verify in DevToggle

1. Open **DevToggle**.
2. The permission card at the top will now show a **green checkmark**: *"Permission Granted"*.
3. The main switch, Quick Settings tile, and Home screen widget are now fully active!

---

## 💻 Alternative: Setup via PC (Standard ADB)

If you have a computer with `adb` installed, connect your phone via USB cable with **USB Debugging** enabled and run:

```bash
adb shell pm grant com.example android.permission.WRITE_SECURE_SETTINGS
```

---

## 🎛️ How to Add Quick Settings Tile & Widget

### Adding the Quick Settings Tile:
1. Swipe down twice from the top of your screen to fully open the Quick Settings panel.
2. Tap the **Edit (pencil)** icon.
3. Scroll down to find the **"Developer Options"** tile from DevToggle.
4. Drag and drop it into your active tiles list.

### Adding the Home Screen Widget:
1. Long-press an empty area on your phone's home screen.
2. Tap **Widgets** and search for **DevToggle**.
3. Drag the **Developer Options** widget onto your home screen.

---

## ❓ Troubleshooting & FAQ

- **Q: Does this survive a phone reboot?**  
  **A:** Yes! Android persists the `WRITE_SECURE_SETTINGS` permission across restarts. You only need to run the `pm grant` command once.

- **Q: Permission denied error in aShell?**  
  **A:** Ensure Shizuku is currently running in the background. On Xiaomi / Redmi / POCO devices running MIUI or HyperOS, you must toggle on *"USB debugging (Security settings)"* in Developer Options (requires an active Mi Account and SIM card).

- **Q: How to revoke the permission?**  
  **A:** In aShell or PC ADB, run:
  ```sh
  pm revoke com.example android.permission.WRITE_SECURE_SETTINGS
  ```
  Or simply uninstall DevToggle.
