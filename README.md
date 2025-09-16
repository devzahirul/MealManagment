# MealManage (Android)

[![CI](https://github.com/devzahirul/MealManagment/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/devzahirul/MealManagment/actions/workflows/ci.yml?query=branch%3Amain)
![Tests](https://raw.githubusercontent.com/devzahirul/MealManagment/main/badges/tests.svg)
![Coverage](https://raw.githubusercontent.com/devzahirul/MealManagment/main/badges/coverage.svg)
![Domain](https://raw.githubusercontent.com/devzahirul/MealManagment/main/badges/coverage-domain.svg)
![Core](https://raw.githubusercontent.com/devzahirul/MealManagment/main/badges/coverage-core.svg)
![Data](https://raw.githubusercontent.com/devzahirul/MealManagment/main/badges/coverage-data.svg)
![App](https://raw.githubusercontent.com/devzahirul/MealManagment/main/badges/coverage-app.svg)

MealManage helps households or small dorm groups track daily meals, per-user costs, and shared balances in real time. The app is built entirely with Kotlin, Jetpack Compose, Hilt, and Firebase.

---

## Table of Contents
1. [Highlights](#highlights)
2. [Architecture & Design](#architecture--design)
   - [Layered view](#layered-view)
   - [Module responsibilities](#module-responsibilities)
   - [Quality attributes](#quality-attributes)
   - [Use case walkthrough](#use-case-walkthrough)
3. [Project Setup](#project-setup)
4. [Running & Testing](#running--testing)
5. [Continuous Integration](#continuous-integration)
6. [Troubleshooting](#troubleshooting)
7. [Resources](#resources)

---

## Highlights

- **Composable UI**: Jetpack Compose screens for meals, costs, dashboard, profile, and authentication.
- **Real-time data**: Firebase Authentication + Cloud Firestore provide live updates with resilient error handling.
- **Domain-driven design**: Clear separation between presentation, business logic, infrastructure, and platform abstractions.
- **Quality gates**: 100% line coverage enforced by Kover; GitHub Actions runs full test + badge generation pipelines.

---

## Architecture & Design

### Layered view

```
┌────────────┐      ┌────────────┐      ┌──────────┐      ┌──────────────┐
│  app       │ ───▶ │  domain    │ ───▶ │   data   │ ───▶ │ Firebase SDKs│
│ (Compose + │ uses │ (use cases │ uses │(repos +   │ hits │ Auth & Firestore
│ ViewModels)│      │ + contracts│      │ data src) │      │ via adapters) │
└────────────┘      └────────────┘      └──────────┘      └──────────────┘
        ▲                    ▲                   ▲
        └─────── core abstractions (DateProvider, Dispatchers) ───────┘
```

Dependency always flows inward: UI depends on use cases; use cases depend on repository interfaces; repositories delegate to data sources that wrap Firebase. The `core` module shares non-Android utilities (time/dispatchers) across layers.

### Module responsibilities

| Module  | Purpose | Key contents |
| --- | --- | --- |
| `:app` | Presentation & DI | Compose screens, Hilt ViewModels, module wiring, navigation | 
| `:domain` | Pure business logic | Models, `Result`/`DomainError`, repository interfaces, use cases, `MonthRangeCalculator` |
| `:data` | Infrastructure adapters | Repository implementations, Firebase data sources (`AuthDataSource`, `MealDataSource`, `CostDataSource`, `UserDataSource`), error mappers |
| `:core` | Platform abstractions | `DateProvider`, dispatcher qualifiers, coroutine dispatcher provider |

### Quality attributes

- **Scalable**: Adding features means composing new use cases and data source methods rather than editing existing flows. Firebase adapters are isolated; migrating to another backend is localized.
- **Testable**: Every layer has interfaces and 100% line coverage enforced by `./gradlew koverVerify`. Unit tests in `data/src/test/...` use in-memory fakes to validate success and error paths without network access.
- **Maintainable**: DI modules (`app/di`, `data/di`) centralize wiring. Errors are normalized via `DomainError`, so the UI renders consistent feedback. Core abstractions prevent Android leakage into domain/data layers.

### Use case walkthrough: Monthly dashboard refresh

1. `HomeViewModel.refreshAll()` obtains the active month from `MonthRangeCalculator` and calls two use cases: `GetTotalCostForRange` and `GetTotalMealsForRange`.
2. Each use case delegates to a domain repository interface (`CostRepository`, `MealRepository`). No Firebase logic leaks into the ViewModel.
3. The data layer repositories execute on the injected IO dispatcher, fan out to Firebase data sources, and map failures to `DomainError` (`IndexRequired`, `Auth`, etc.).
4. Results are emitted back up the stack via `Result.Success`/`Result.Error`, allowing the ViewModel to render totals or user-friendly errors. Because use cases are shared, other screens (e.g., per-user breakdown) stay consistent by reusing the same orchestration.

---

## Project Setup

1. **Clone & open**
   ```bash
   git clone https://github.com/devzahirul/MealManagment.git
   cd MealManagment
   ```
   Open the project in Android Studio (Hedgehog or newer).

2. **Firebase**
   - Create a Firebase project and add an Android app with package `com.ugo.mhews.mealmanage`.
   - Download `google-services.json` and drop it into `app/google-services.json`.

3. **Enable services**
   - Authentication: Email/Password provider.
   - Firestore: Production or test mode (your choice).

4. **Deploy rules & indexes** (optional but recommended)
   ```bash
   firebase use <your-project-id>
   firebase deploy --only firestore:rules
   firebase deploy --only firestore:indexes
   ```

5. **Run the app**
   Select a device/emulator in Android Studio and hit **Run**.

---

## Running & Testing

- All JVM + Android unit tests:
  ```bash
  ./gradlew :domain:test :core:test :data:testDebugUnitTest :app:testDebugUnitTest
  ```
- Module-specific:
  ```bash
  ./gradlew :data:testDebugUnitTest   # data layer
  ./gradlew :app:testDebugUnitTest   # ViewModels
  ```
- Coverage verification (fails below 100% measured line coverage):
  ```bash
  ./gradlew koverVerify
  ```
  XML reports are emitted to `build/reports/kover/report.xml` (aggregated) and per-module equivalents.

---

## Continuous Integration

GitHub Actions workflow: `.github/workflows/ci.yml`

- Configures JDK 17, Android SDK, and Gradle build cache.
- Runs the full test suite, generates Kover XML coverage, and enforces quality gates.
- Publishes test summaries and uploads coverage artifacts.
- On `main`, updates README badges (tests + coverage) via an automated PR if direct commits are not allowed.

---

## Troubleshooting

| Issue | Cause | Fix |
| --- | --- | --- |
| `DomainError.IndexRequired` | Firestore query needs an index | Deploy provided `firestore.indexes.json` |
| Blank dashboard | Missing or incorrect `google-services.json` | Re-download from Firebase console |
| CI coverage failure | New code lacks tests | Add/extend unit tests and rerun `./gradlew koverVerify` |

---

## Resources

- [Jetpack Compose docs](https://developer.android.com/jetpack/compose)
- [Firebase Auth & Firestore KTX](https://firebase.google.com/docs/reference/kotlin)
- [Kover Gradle Plugin](https://github.com/Kotlin/kotlinx-kover)
- [Hilt dependency injection](https://dagger.dev/hilt/)

Happy cooking & coding! 🍽️
