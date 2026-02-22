# Vault

**Local-first encryption password manager for Android.**

No cloud. No analytics. No leaks. Your passwords stay on your device.

---

## Features

- **Offline-only** — all data stays on-device, never touches a server
- **Strong encryption** — AES-256-GCM, AES-256-CBC, and ChaCha20-Poly1305
- **Argon2id key derivation** — memory-hard protection against brute force
- **Subscription tracking** — track recurring services with price and plan type
- **Favorites & filtering** — organize entries with favorites, search, and category filters
- **Auto-lock** — vault locks automatically on idle, app close, or background
- **Haptic feedback** — configurable per-screen scroll vibrations
- **Material 3 theming** — dynamic color (Android 12+), dark mode, 12 accent colors
- **Glass UI** — frosted-glass card design language throughout the app
- **Vault export** — download your encrypted vault file at any time

## Screenshots

<!-- Add screenshots here -->

## Installation

Vault is distributed as a sideloaded APK:

1. Download the latest APK from [Releases](#)
2. On your Android device, enable **Install from unknown sources** for your browser/file manager
3. Open the APK and install
4. Create a new vault with a master password

**Requirements:** Android 7.0+ (API 24)

## How It Works

1. **Create a vault** — choose a master password and encryption algorithm
2. **Add entries** — store passwords with username, website, notes, and subscription info
3. **Auto-lock** — the vault locks when you leave the app or after an idle timeout
4. **Export anytime** — download your encrypted vault file for backup

## Security Model

- All encryption/decryption happens on-device
- Master password never stored — only the Argon2id-derived key is held in memory
- No cloud sync, no telemetry, no analytics
- Android backup and data extraction disabled
- Vault file is AES-256/ChaCha20 encrypted at rest

---

## Development

### Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin 2.0 |
| UI | Jetpack Compose (Material 3) |
| Build | Gradle 9.1 (Kotlin DSL), AGP 9.0 |
| Min SDK | 24 (Android 7.0) |
| Target SDK | 36 (Android 15) |
| Key derivation | Argon2id via argon2kt |
| Architecture | Single Activity + Compose Navigation |

### Architecture

```
MainActivity (entry point, Scaffold + VaultTheme)
  └── VaultApp (NavHost, defines routes)
        └── Screen composables (VaultScreen, SettingsScreen, etc.)
              └── ViewModels (VaultViewModel manages state via StateFlow)
                    └── Repository + Crypto layer
```

**Single Activity pattern** — `MainActivity` owns the outer Scaffold with edge-to-edge rendering. All screens are Compose functions that receive navigation callbacks as lambdas.

### Package Structure

| Package | Purpose |
|---------|---------|
| `crypto/` | Encryption/decryption (AES-GCM, AES-CBC, ChaCha20-Poly1305) |
| `data/` | Persistence (SharedPreferences, settings state) |
| `model/` | Data classes (PasswordEntry, SettingsState, enums) |
| `notification/` | Reminder notifications and scheduling |
| `repository/` | Vault file I/O and data access |
| `ui/theme/` | Material 3 theming, colors, typography, glass effects |
| `util/` | Helpers (haptics, logging, time formatting) |
| `viewmodel/` | ViewModels (vault state, settings) |
| `views/` | Screen-level composables |

### Build Commands

```bash
# Debug build
gradlew.bat assembleDebug

# Release build
gradlew.bat assembleRelease

# Install on connected device/emulator
gradlew.bat installDebug

# Run unit tests
gradlew.bat test

# Run instrumented tests (requires device/emulator)
gradlew.bat connectedAndroidTest

# Clean
gradlew.bat clean
```

### Key Design Decisions

- **Stateless composables** — screens receive callbacks, never hold NavController references
- **Glass UI cards** — reusable `GlassCard` composable with frosted-glass background and border
- **Inner Scaffolds use zero insets** — prevents double padding from nested Scaffolds
- **VaultLogger** — dual logging to logcat and file for debugging
- **Centralized versions** — all dependency versions in `gradle/libs.versions.toml`

### Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/my-feature`)
3. Make your changes — Kotlin + Jetpack Compose only, no XML layouts or Java
4. Run `gradlew.bat assembleDebug` to verify the build
5. Submit a pull request

---

Built with Kotlin and Jetpack Compose.
