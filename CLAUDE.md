# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Vault is a local-first encryption password manager for Android. Built with Kotlin and Jetpack Compose, targeting API 24+ (Android 7.0) through API 36 (Android 15). The app emphasizes offline security — no cloud, no leaks.

**Strictly Kotlin + Jetpack Compose.** All UI must use Compose — no XML layouts, no Views. All code must be Kotlin — no Java.

## Build Commands

```bash
# Build (Windows - use gradlew.bat)
gradlew.bat assembleDebug
gradlew.bat assembleRelease

# Install on connected device/emulator
gradlew.bat installDebug

# Unit tests
gradlew.bat test

# Single test class
gradlew.bat test --tests "com.lonley.dev.vault.ExampleUnitTest"

# Instrumented tests (requires device/emulator)
gradlew.bat connectedAndroidTest

# Clean
gradlew.bat clean
```

## Architecture

**Single Activity + Compose Navigation pattern:**

```
MainActivity (entry point, sets up Scaffold + VaultTheme)
  └── VaultApp (NavHost container, defines routes)
        └── Routes: HOME, SIGN_UP, UPLOAD_VAULT
              └── Screen composables (e.g., HomeScreen)
```

- `MainActivity.kt` — sole Activity, hosts the Compose UI tree with edge-to-edge rendering
- `VaultApp.kt` — navigation graph using `NavHost`; routes defined in `Routes` object. Only HOME is implemented; SIGN_UP and UPLOAD_VAULT are stubbed
- `views/` — screen-level composables; HomeScreen takes callback lambdas for navigation actions
- `ui/theme/` — Material 3 theming with dynamic color (Android 12+), dark mode support, and a custom animated gradient background

## Key Patterns

- **Stateless composables with callback params** — screens receive navigation callbacks rather than holding NavController references
- **Centralized dependency versions** — all versions managed in `gradle/libs.versions.toml`
- **No state management layer yet** — no ViewModels, Room, or DI framework; local `mutableStateOf` used directly in composables
- **Security-conscious defaults** — backup and data extraction rules are configured to be empty (no cloud backup), aligning with the local-first philosophy

## Package Structure

All source code lives under `com.lonley.dev.vault` in `app/src/main/java/`.

## Build Configuration

- Gradle 9.1.0 (Kotlin DSL), AGP 9.0.0, Kotlin 2.0.21
- Compose BOM 2024.09.00
- Java 11 target
- Non-transitive R classes enabled
