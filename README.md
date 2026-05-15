# DayClicker

A minimal Android app for tracking days worked. One counter per job. Tap `+1` in the app or on a home-screen widget to log a day, optionally write a note. Manual monthly reset. History preserved.

This repo builds an installable debug APK automatically via GitHub Actions — no Android Studio, no local SDK, nothing to install.

-This app was built with Claude Code and a Human potato in the loop. 

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
