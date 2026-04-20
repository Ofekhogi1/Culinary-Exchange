# CulinaryExchange

An Android culinary social feed app built with Kotlin, MVVM, Firebase, and Room.

## Setup

1. Create a Firebase project and enable:
   - Authentication (Email/Password)
   - Firestore Database
   - Storage
2. Download `google-services.json` from the Firebase Console and place it in the `app/` directory.
3. Open the project in Android Studio and sync Gradle.
4. Run on a device or emulator with internet access.

## Tech Stack

- Kotlin + Coroutines
- MVVM + Clean Architecture
- Firebase Auth, Firestore, Storage
- Room (local cache)
- Navigation Component + SafeArgs
- Retrofit (TheMealDB API)
- Glide
- Material3
