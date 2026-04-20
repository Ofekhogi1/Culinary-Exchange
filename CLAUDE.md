# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Android app ("CulinaryExchange") — a culinary social feed. Package: `com.example.webcellularapplication`. AGP 9.1.1, Kotlin 2.1.0, compileSdk 36, minSdk 24.

**Before the app compiles you must:**
1. Create a Firebase project, enable Auth (Email/Password), Firestore, and Storage.
2. Download `google-services.json` and place it in `app/`.

## Build commands

```bash
./gradlew assembleDebug
./gradlew assembleRelease
./gradlew test
./gradlew connectedAndroidTest          # requires device/emulator
./gradlew test --tests "com.example.webcellularapplication.ExampleUnitTest.*"
```

## Architecture

MVVM + Clean Architecture with a single `:app` module.

**Data flow:** Firebase/API → Repository → Room (cache) → ViewModel (Flow→LiveData) → Fragment

**Layer structure:**
- `model/` — plain Kotlin data classes (`Post`, `User`) mirroring Firestore documents
- `data/local/` — Room: `AppDatabase` (singleton), `entity/PostEntity`, `dao/PostDao`
- `data/remote/api/` — Retrofit client targeting TheMealDB (`https://www.themealdb.com/api/json/v1/1/`); used for random meal inspiration on the feed
- `data/repository/` — `PostRepository` (CRUD + Firebase Storage upload + Room cache), `AuthRepository` (Firebase Auth + Firestore user profile)
- `ui/auth/` — `LoginFragment`, `RegisterFragment`, shared `AuthViewModel`
- `ui/feed/` — `HomeFragment` + `FeedViewModel` (global feed, observes Room)
- `ui/post/` — `MyPostsFragment` + `AddPostFragment` + `PostViewModel` (user CRUD)
- `ui/profile/` — `ProfileFragment` + `ProfileViewModel`

**Navigation:** Single-activity (`MainActivity`). Nav graph start destination is `loginFragment`. Bottom nav (`homeFragment`, `myPostsFragment`, `profileFragment`) is hidden on auth screens. SafeArgs: `AddPostFragment` receives optional `postId: String?` to distinguish create vs. edit mode.

**Caching pattern:** `refreshAll*()` in repositories fetches from Firestore and `upsert`s into Room. Fragments only observe Room via `Flow` (never Firestore directly).

**Dependencies managed** via `gradle/libs.versions.toml` (version catalog). Add new libs there before referencing in `app/build.gradle.kts`. Room uses KSP; schema exported to `app/schemas/`.
