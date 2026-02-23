# Plan: Generated Password Display + Animated Bottom Bar Actions

## Overview

Three changes:
1. Show the generated password prominently on the VaultScreen below the heading
2. Add an animated download button to the bottom bar
3. Add an animated lock button to the bottom bar

---

## 1. Show Generated Password on VaultScreen

**Problem:** After generating a password, the user has no visible reminder of it on the main screen — it just gets copied to clipboard and pre-filled in the add sheet.

**Approach:** Surface `generatedPasswordForAdd` state onto the VaultScreen. When non-empty, show a dismissible glass card below the vault heading ("Your passwords, secured.") with the generated password displayed in monospace-style text, plus a copy button and dismiss (X) button.

### Files changed:

**`VaultScreen.kt` — `VaultScreen` composable**
- Add params: `generatedPassword: String = ""`, `onCopyGeneratedPassword: () -> Unit = {}`, `onDismissGeneratedPassword: () -> Unit = {}`
- Between the subtitle ("Your passwords, secured.") and the `Spacer(24.dp)` before the search bar, insert an `AnimatedVisibility` block (visible when `generatedPassword.isNotEmpty()`)
- Inside: a `GlassCard` with the credit-card gradient background containing:
  - Small label "Generated Password"
  - Row: password text (monospace, single line, ellipsis overflow) + copy `IconButton` + close `IconButton`
- Uses same `cardGradient` / `shineGradient` style as the filter pills

**`VaultApp.kt`**
- In the `VaultScreen(...)` call (line ~425), pass three new params:
  - `generatedPassword = generatedPasswordForAdd`
  - `onCopyGeneratedPassword = { copyToClipboard(generatedPasswordForAdd) }`
  - `onDismissGeneratedPassword = { generatedPasswordForAdd = "" }`

---

## 2. Animated Download Button in Bottom Bar

**Problem:** The download icon in `VaultBottomBar` is static.

**Approach:** Add a subtle continuous bounce animation. Use `rememberInfiniteTransition` + `animateFloat` to oscillate `translationY` from `0f` to `-4f` with `RepeatMode.Reverse` over ~800ms tween.

### File changed: `VaultScreen.kt` — `VaultBottomBar`

- Add a `rememberInfiniteTransition` inside `VaultBottomBar`
- Animate a `bounceOffset` float: `0f → -4f`, `tween(800)`, `RepeatMode.Reverse`
- Apply `Modifier.graphicsLayer { translationY = bounceOffset }` to the download `Icon` (not the IconButton — just the Icon inside it)

---

## 3. Animated Lock Button in Bottom Bar

**Problem:** The lock icon is static.

**Approach:** Add a gentle rotation wiggle. Use the same `rememberInfiniteTransition` to oscillate `rotationZ` between `-8f` and `8f` degrees over ~1200ms with `RepeatMode.Reverse`.

### File changed: `VaultScreen.kt` — `VaultBottomBar`

- Animate a `wiggleAngle` float: `-8f → 8f`, `tween(1200)`, `RepeatMode.Reverse`
- Apply `Modifier.graphicsLayer { rotationZ = wiggleAngle }` to the lock `Icon`

---

## Summary of file changes

| File | What changes |
|------|-------------|
| `VaultScreen.kt` | Add generated password card to `VaultScreen`; add bounce + wiggle animations to `VaultBottomBar` icons |
| `VaultApp.kt` | Pass `generatedPasswordForAdd` + copy/dismiss callbacks to `VaultScreen` |

## New imports needed in VaultScreen.kt

- `androidx.compose.animation.AnimatedVisibility`
- `androidx.compose.animation.core.rememberInfiniteTransition`
- `androidx.compose.animation.core.animateFloat`
- `androidx.compose.animation.core.infiniteRepeatable`
- `androidx.compose.animation.core.tween`
- `androidx.compose.animation.core.RepeatMode`
- `androidx.compose.ui.graphics.graphicsLayer`
- `androidx.compose.material.icons.filled.Close` (for dismiss button)
- `androidx.compose.ui.text.font.FontFamily` (for monospace password display)
