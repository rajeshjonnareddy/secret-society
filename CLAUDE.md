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

**Single Activity + Compose state-driven navigation:**

```
MainActivity (entry point, sets up Scaffold + VaultTheme)
  └── VaultApp (state-driven screen container)
        └── VaultUiState: Initializing → Locked → PromptUnlock → Loading → Unlocked → Error
              └── AnimatedContent switches between screens: vault, detail, edit, profile, settings, recovery, changePassword
```

- `MainActivity.kt` — sole Activity, hosts the Compose UI tree with edge-to-edge rendering
- `VaultApp.kt` — state-driven navigation via `VaultUiState`. The `Unlocked` branch uses `AnimatedContent` with a `screenKey` string to switch between vault, detail, edit, profile, settings, recovery, and changePassword screens. Bottom sheets are used for add-password and create-vault flows
- `views/` — screen-level composables; screens take callback lambdas for navigation actions
- `ui/theme/` — Material 3 theming with dynamic color (Android 12+), dark mode support, custom animated gradient background, and per-accent contrasting heading colors via `LocalVaultColors`

## Key Patterns

- **Stateless composables with callback params** — screens receive navigation callbacks rather than holding NavController references
- **Centralized dependency versions** — all versions managed in `gradle/libs.versions.toml`
- **ViewModel for vault state** — `VaultViewModel` manages password entries and vault file I/O; screens collect `StateFlow`
- **Security-conscious defaults** — backup and data extraction rules are configured to be empty (no cloud backup), aligning with the local-first philosophy
- **VaultLogger** — `util/VaultLogger.kt` writes to both logcat and `vault.log` file in `context.filesDir`. Initialize with `VaultLogger.init(context)` in `MainActivity.onCreate()`. Use `VaultLogger.d/i/w/e(area, message)` throughout

## Entry Types

Three entry types defined in `model/EntryType.kt`:
- **Password** — standard credential with username/password
- **CryptoWallet** (displayed as "Digital Wallet") — stores wallet address, seed phrase, network, and exchange
- **Passphrase** — legacy type; existing entries are auto-migrated to `CryptoWallet` on load in `VaultRepository.parseEntries()`. Not shown in add/edit UI

Wallet-specific fields in `PasswordEntry`: `walletAddress`, `seedPhrase`, `network` (enum), `exchange` — all nullable.

## Bottom Sheet Pattern

ModalBottomSheets (add password, create vault) use swipe-dismiss prevention:
```kotlin
val sheetState = rememberModalBottomSheetState(
    skipPartiallyExpanded = true,
    confirmValueChange = { it != SheetValue.Hidden } // blocks swipe-to-dismiss
)
```
Dismissal is done by toggling the show state (`showSheet = false`) which removes the sheet from composition. Never call `sheetState.hide()` with this pattern — it deadlocks since `confirmValueChange` blocks the `Hidden` transition.

## Theme System

- `LocalGlassColors` — provides glass card background/border colors (frosted glass look)
- `LocalVaultColors` — provides `cardHeading` color for password card entry name headings (contrasting hue per accent color)
- Heading colors are defined as dark/light pairs in `Color.kt` and resolved by `headingColorForAccent()` in `Theme.kt`
- For `AccentColor.Auto` with dynamic colors: `colorScheme.tertiary` is used as the heading color

## Shared Composables

- `WalletComponents.kt` — `NetworkDropdown` (network selector) and `SeedPhraseInput` (word count chips + word grid + counter), shared between AddPasswordSheet and PasswordEditScreen
- `PassphraseWordFields` in `AddPasswordSheet.kt` — low-level word grid with multi-word paste support
- `GlassCard` in `VaultScreen.kt` — reusable glass-styled container
- `PasswordTextField` — password field with visibility toggle

## Screen Layout Rules

- **MainActivity owns the outer Scaffold** — it handles system bar insets (edge-to-edge). All inner screens must NOT add their own system bar padding.
- **Inner Scaffolds must use `contentWindowInsets = WindowInsets(0, 0, 0, 0)`** — prevents double padding from nested Scaffolds. Only use an inner Scaffold when the screen needs a FAB or bottom bar.
- **Consistent top spacing** — all screens start content with `Spacer(modifier = Modifier.height(12.dp))` after the outer Scaffold padding. This is the only vertical gap between system bar and content. Do NOT add extra top padding.
- **Horizontal padding** — all screen content uses `24.dp` horizontal padding, matching HomeScreen's layout.
- **Brand heading** — screen titles use `MaterialTheme.typography.displayLarge`, `FontWeight.Bold`, `MaterialTheme.colorScheme.primary`, left-aligned. Same style as HomeScreen's "Vault" brand text.

## Package Structure

All source code lives under `com.lonley.dev.vault` in `app/src/main/java/`.

## Build Configuration

- Gradle 9.1.0 (Kotlin DSL), AGP 9.0.1, Kotlin 2.2.10
- Compose BOM 2024.09.00
- Java 11 target
- Non-transitive R classes enabled
