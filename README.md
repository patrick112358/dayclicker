# DayClicker

A minimal Android app for tracking days worked. One counter per job. Tap `+1` in the app or on a home-screen widget to log a day, optionally write a note. Manual monthly reset. History preserved.

This repo builds an installable debug APK automatically via GitHub Actions — no Android Studio, no local SDK, nothing to install.

---

## How to get your APK

### 1. Create a GitHub repo

- Go to https://github.com/new
- Name it anything (e.g. `dayclicker`)
- Make it **private** if you prefer
- **Do NOT** check "Add a README" — leave the repo empty
- Click **Create repository**

### 2. Upload these files

The easiest way from a phone or computer:

- On the empty repo page, click **"uploading an existing file"** in the quick-setup section
- Drag in **everything** from the zip (the contents — the `.github`, `app`, `gradle.properties`, etc., not the wrapping folder)
- Commit message: `initial commit`
- Click **Commit changes**

(If using a desktop with git: `git init && git add . && git commit -m "initial" && git push` after `git remote add`. Either works.)

### 3. Watch the build

- Click the **Actions** tab on your repo
- You should see a workflow run named **"Build APK"** running
- First build takes 5–8 minutes (downloads Gradle, the Android SDK platform, and all dependencies). Later builds are faster.
- When it finishes with a green check, click into the run
- Scroll to the **Artifacts** section at the bottom
- Download **DayClicker-debug-apk** (a zip)

### 4. Install on your Android phone

- Unzip the downloaded file — inside is `app-debug.apk`
- Transfer it to your phone (any method: AirDrop-equivalent, USB, email it to yourself, save to Drive, etc.)
- On your phone, open the APK file
- Android will warn about installing from an unknown source — approve it
- Open DayClicker

### 5. Add the widget

- Long-press on an empty area of your home screen
- Tap **Widgets**
- Find **DayClicker** in the list
- Drag a widget to your home screen
- The config screen will open — pick which counter the widget represents
- Tapping the widget now opens the note prompt

---

## What's included in v0

- Multiple counters, each with its own name and color
- `+1` button (with optional note prompt) and `−1` (silent)
- History with editable notes (long-press an entry)
- Manual "Reset to 0" button on the counter detail screen
- Home-screen widgets, one per counter
- Light/dark mode follows system

## What's NOT in v0 (intentional)

- Google Drive backup — coming next
- CSV export — coming next
- Auto-reset on the 1st of the month — coming next
- Release-signed APK (this is a debug-signed APK, fine for personal use)

---

## Rebuilding after edits

Any time you push a change to `main`, GitHub Actions rebuilds the APK automatically. Download the new artifact, install over the old version — your data is preserved (Android keeps app data across same-signature installs).

If you uninstall and reinstall, the data is gone (no Drive backup yet).

---

## Troubleshooting the build

If the Actions run fails:

1. Click into the failed run
2. Open the **build** job, then the **Build debug APK** step
3. Scroll for the actual error (usually the last red block)
4. Send the error to Claude — it's almost always a version mismatch or a missing file, and quick to fix

The most common failure modes for first-time builds:
- Missing `gradle.properties` (must be at the root)
- Wrong `applicationId` not matching package — both are `com.dayclicker.app`
- A renamed file that breaks an import

---

## Project layout

```
.github/workflows/build.yml      ← CI that builds the APK
app/build.gradle.kts             ← app module config
app/src/main/AndroidManifest.xml ← activities, widget receiver
app/src/main/java/com/dayclicker/app/
├── DayClickerApp.kt             ← Application class
├── data/                        ← Room DB, entities, repository
├── ui/                          ← Compose screens
│   ├── MainActivity.kt
│   ├── home/, detail/, edit/, note/, theme/
└── widget/                      ← Glance widget + config + receiver
```
