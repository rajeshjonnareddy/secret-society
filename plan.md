# Plan: Fix PasswordDetailDialog Theme/Accent + Sharp Button Colors

## Root Cause Analysis

From the screenshots:
- **Vault screen**: Yellow accent applied correctly (themed by `MainActivity`'s `VaultTheme`)
- **Settings screen**: Yellow accent applied correctly (same parent `VaultTheme`)
- **PasswordDetailDialog** (view + edit): Shows default purple/blue accent, flat dark background (no animated gradient blobs)

The Compose `Dialog` composable creates a **separate window** with its own composition tree — it does **NOT** inherit `MaterialTheme` or `CompositionLocal` values from the parent. The `PasswordDetailDialog` wraps its content in its own `VaultTheme(themeMode, accentColor)` to compensate.

**Previous code** passed only `themeMode` (dark/light was correct) but NOT `accentColor` — so it defaulted to `AccentColor.Auto`, which resolves to default M3 purple on older devices or wallpaper-based dynamic colors on Android 12+.

**My last round of changes** already fixed this by passing the full `settingsState` (which includes `accentColor`) to the dialog. However, **the build was assembled but likely not installed** on the device (`assembleDebug` ≠ `installDebug`).

## Steps

### 1. Install the latest build on device
Run `gradlew.bat installDebug` to push the APK with the accent fix to the device.

### 2. Verify PasswordDetailDialog theming
After install, open a password entry — it should now show:
- Yellow title text (uses `MaterialTheme.colorScheme.primary`)
- Yellow animated gradient blobs in background
- Yellow-toned glass cards and icon tints
- Correct accent on all buttons

### 3. Fix "blurred/not sharp" button colors
The screenshots show washed-out button colors. Even with the correct yellow theme, Material 3's `FilledTonalButton` uses `secondaryContainer` which is a muted dark brown (`#52472A` in yellow-dark). To make buttons crisper:

**File: `views/PasswordDetailDialog.kt`**

| Button | Current color source | Fix |
|--------|---------------------|-----|
| Edit Entry (`Button`) | `primary` (#FFD54F) | Already sharp — no change needed |
| Delete Entry (`FilledTonalButton`) | Custom error colors | Already correct — no change needed |
| Cancel (edit mode, `FilledTonalButton`) | `secondaryContainer` (muted brown) | Override to use `surfaceVariant` so it's neutral-toned and reads as secondary |
| Save (edit mode, `Button`) | `primary` (#FFD54F) | Already sharp — no change needed |

**File: `views/AddPasswordSheet.kt`**

| Button | Current color source | Fix |
|--------|---------------------|-----|
| Cancel (`FilledTonalButton`) | `secondaryContainer` (muted) | Same override as above |
| Add (`Button`) | `primary` when enabled | Already sharp |

**File: `views/CreateVaultDrawer.kt`**

| Button | Current color source | Fix |
|--------|---------------------|-----|
| Cancel (`FilledTonalButton`) | `secondaryContainer` (muted) | Same override |
| Create Vault (`Button`) | `primary` | Already sharp |

**File: `views/HomeScreen.kt`**

| Button | Current color source | Fix |
|--------|---------------------|-----|
| Create New Vault (`Button`) | `primary` | Already sharp |
| Open Existing Vault (`FilledTonalButton`) | `secondaryContainer` (muted) | Same override |

The fix for FilledTonalButton: use `surfaceContainerHighest` for container and `onSurface` for content — this gives a clean, neutral secondary button that works across all accent colors.

### 4. Files to modify

| File | Change |
|------|--------|
| `views/PasswordDetailDialog.kt` | Override `FilledTonalButton` colors for Cancel button |
| `views/AddPasswordSheet.kt` | Override `FilledTonalButton` colors for Cancel button |
| `views/CreateVaultDrawer.kt` | Override `FilledTonalButton` colors for Cancel button |
| `views/HomeScreen.kt` | Override `FilledTonalButton` colors for Open Existing Vault button |

### 5. Verify
- `gradlew.bat installDebug`
- Open password detail → should show yellow accent, animated background, sharp buttons
- Open edit mode → same
- Open Add Password sheet → sharp Cancel/Add buttons
- Open Create Vault sheet → sharp Cancel/Create buttons
- Try different accent colors in Settings → all screens update consistently
