# MealManage (Android)

MealManage is an Android app built with Kotlin and Jetpack Compose to help small groups track daily meals and shared costs. It uses Firebase Authentication and Cloud Firestore for sign‑in, storage, and realtime updates.

## Features

- Email/password sign‑in and sign‑up.
- Meals calendar per user with day badges and colors:
  - Yellow for current/future days with meals, green for past days with meals.
  - Tap a current/future day to edit your count.
  - Tap a past day to view all users’ counts.
  - Realtime updates with a resilient fallback to keep the calendar populated.
- Costs tracking: add costs, monthly totals, and per‑user breakdowns.
- Home dashboard:
  - Month total cost and computed meal rate (cost ÷ total meals).
  - Today’s meals (total and top users).
  - Month by user: costs, meals, expected vs actual (balance).
- Profile: set a display name and sign out.

## Tech Stack

- Kotlin, Jetpack Compose (Material 3)
- Firebase: Authentication, Cloud Firestore (KTX SDKs, BoM)
- Gradle Android Plugin 8.5, Kotlin 1.9, Compose BOM 2024.04.01
- Min SDK 24, Target/Compile SDK 34

## Project Structure

- `app/src/main/java/com/ugo/mhews/mealmanage/ui` — Compose screens
  - `HomeScreen.kt`, `MealScreen.kt`, `CostAddScreen.kt`, `ProfileScreen.kt`, `LoginScreen.kt`
- `app/src/main/java/com/ugo/mhews/mealmanage/data` — Repositories for Firebase access
  - `MealRepository.kt`, `CostRepository.kt`, `UserRepository.kt`, `AuthRepository.kt`, `FirestoreProvider.kt`
- `app/src/main/java/com/ugo/mhews/mealmanage/ui/theme` — Theme (Material 3 light color scheme)
- Firestore security and indexes: `firestore.rules`, `firestore.indexes.json`

## Firestore Data Model

- Users: `Users/{uid}`
  - Fields: `name: string`, `email: string`
- Meals by user (subcollection): `Meals/{uid}/days/{YYYY-MM-DD}`
  - Fields: `uid: string`, `date: string (YYYY-MM-DD)`, `count: number`
- Costs: `AddCost/{autoId}`
  - Fields: `uid: string`, `name: string`, `cost: number`, `timestamp: number (ms)`

Indexes (included):
- Collection group `days` — single‑field index on `date` (ASCENDING) for range queries.
- `AddCost` composite index on `(uid ASC, timestamp ASC)`.

## Prerequisites

- Android Studio (Hedgehog or newer) with Android SDK 34.
- Java toolchain via Android Studio (project uses Java 8 bytecode + desugaring).
- A Firebase project with Authentication and Firestore enabled.
- Firebase CLI if you want to deploy rules/indexes from the repo.

## Setup

1) Clone the repo
- `git clone https://github.com/devzahirul/MealManagment.git`
- Open in Android Studio.

2) Firebase project and app
- Create a Firebase project in the console.
- Add an Android app with package name `com.ugo.mhews.mealmanage`.
- Download `google-services.json` and place it at `app/google-services.json` (replace if present).

3) Enable providers and services
- Authentication: enable Email/Password.
- Firestore: create a database (Production or Test mode as you prefer).

4) Apply security rules and indexes
- Rules: review `firestore.rules` and deploy to your project.
- Indexes: deploy `firestore.indexes.json` to enable collection‑group queries on `days.date`.
- Using Firebase CLI:
  - `firebase use <your-project-id>`
  - `firebase deploy --only firestore:rules`
  - `firebase deploy --only firestore:indexes`

5) Build & run
- From Android Studio: select a device and press Run.
- CLI: `./gradlew assembleDebug` (and install the APK on a device/emulator).

6) Sign up and sign in
- On first launch, use the Sign Up tab to create an account, then sign in.

## How the Meals Calendar Works

- Shows 6 weeks (42 days) covering the selected month.
- Badges appear on days where your meal count > 0.
- Colors: yellow for current/future days with meals, green for past days with meals, subtle highlight for today.
- Past days: opens a dialog listing all users’ counts.
- Current/future: opens an editor to increment/decrement and save your count.
- Data flows:
  - On month change, the screen proactively fetches the month’s meals.
  - A realtime listener keeps the calendar in sync; if it fails (e.g., indexing delay), a fallback one‑shot fetch runs and a snackbar shows the error.

## Troubleshooting

- Missing index (collection group `days`):
  - Symptom: Firestore error mentioning `FAILED_PRECONDITION` and an index requirement on `days` and `date`.
  - Fix: deploy indexes with `firebase deploy --only firestore:indexes`.

- No badges/colors visible:
  - Ensure you’re signed in and have saved a non‑zero meal count for some days.
  - The calendar auto‑loads on month change; a snackbar appears if realtime updates fail temporarily.

- Auth errors:
  - Rules require `request.auth != null`. Sign in first.
  - If using Test mode rules, consider tightening to the provided rules when you go to production.

- `google-services.json`:
  - Replace the included file with your own from Firebase. Avoid committing your project’s credentials to a public repo.

## Scripts

- `scripts/deploy_firestore_rules.sh` — example shell script to deploy Firestore rules (requires Firebase CLI).

## Contributing

- Branch from `main` (e.g., `feature/...`), open a Pull Request.
- Keep changes focused and include a short rationale in the PR description.

## Recent Changes

- Calendar badges reliability: proactive month load, resilient realtime listener, corrected Firestore queries for collection‑group ranges.

