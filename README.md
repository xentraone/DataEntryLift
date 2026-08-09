# Lift Work Diary (DataEntryLift)

A very simple Android app that replaces the Excel sheet used for daily lift
maintenance entries. Made for easy, entry-level use:

- **Every day**: tap **＋ New Entry**, pick the JOB, UNIT, DX LOADER and hours
  from dropdown lists (all values from the original sheet are preloaded —
  KONE, D4046, D4662…, P01–P04, DX serial numbers, NOR 2/3/8/call back).
  New values that get typed once are remembered and offered next time.
- **Sundays**: tap the **Holiday** button — it fills the row automatically.
- **End of the week** (8th, 16th, 24th and month end): tap **Report (PNG)**,
  check the preview and tap **Save PNG & Share** to send it on WhatsApp.
  The report looks like the original Excel sheet (yellow columns, red values,
  merged dates, TOTAL HOURS row).

All entries are stored in a local database **on the phone** — no internet
needed. Every report PNG is also saved to **Pictures/DataEntryLift** on the
phone so old reports stay available in the Gallery.

## Get the app

Each push builds a debug APK on GitHub Actions and publishes it on the
[**debug-apk release page**](../../releases/tag/debug-apk).
Download `app-debug.apk` on the phone, open it and allow installation.

Requires Android 8.0 or newer.

## Try it with test data

In the app menu (⋮ top right) choose **Load sample week** — it inserts the
exact week 01-08-2026 → 08-08-2026 from the original sheet (totals: NOR 56,
OT1 4). Then open **Report (PNG)** and pick that week to see the output.

## Build locally

```bash
./gradlew assembleDebug
# APK at app/build/outputs/apk/debug/app-debug.apk
```
