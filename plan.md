# Plan: Material 3 Expressive Transition Animations

## Context

The app uses a **state-driven navigation pattern** (not NavHost) in `VaultApp.kt`. Screen swaps happen instantly via `if/else if` chains on boolean flags (`showSettings`, `showProfile`, `selectedEntry`, `editingEntry`). There are currently **zero transition animations** between screens. Existing animations are limited to a few `AnimatedVisibility` expand/collapse sections and one `AnimatedContent` slogan rotator.

**Compose BOM:** 2024.09.00 | **Material3:** 1.4.0 — full M3 motion APIs available.

**Goal:** Add M3-expressive transitions for screen changes, card appearances, and form reveals — without touching any functionality or navigation logic.

---

## Strategy

Wrap the screen-switching `if/else` chain in `VaultApp.kt` with `AnimatedContent`, using M3 expressive motion specs (emphasized easing, spring physics). Add staggered entrance animations to list items and cards. Enhance form field reveals with sequential slide-in animations.

All animations use `Modifier` additions or `AnimatedContent`/`AnimatedVisibility` wrappers — no structural changes to composables or their callback signatures.

---

## Part 1: Screen Transitions in VaultApp.kt

### What changes
Replace the `if/else if` chain (lines 277–404) with a single `AnimatedContent` keyed on the current "screen identity". This gives us enter/exit transitions between all unlocked screens.

### Screen key derivation
```kotlin
val currentScreen = when {
    editingEntry != null -> "edit"
    selectedEntry != null -> "detail"
    showProfile -> "profile"
    showSettings -> "settings"
    else -> "vault"
}
```

### Transition spec
Use M3 expressive forward/backward motion:
- **Forward** (vault → detail, detail → edit, vault → settings/profile): `fadeIn + slideInHorizontally(from end)` paired with `fadeOut + slideOutHorizontally(to start)`
- **Backward** (edit → detail, detail → vault, settings → vault): reverse direction
- Duration: **400ms** with `EmphasizedDecelerateEasing` (enter) and `EmphasizedAccelerateEasing` (exit) — standard M3 expressive timing
- Use `SizeTransform(clip = false)` to prevent clipping during transitions

### Screen depth ordering
```
vault(0) → detail(1) → edit(2)
vault(0) → settings(1)
vault(0) → profile(1)
```
Compare depths to decide forward vs backward animation direction.

### Files modified
- `VaultApp.kt` — wrap screen block in `AnimatedContent`, add screen key derivation and transition spec

---

## Part 2: Password List Item Staggered Entrance (VaultScreen.kt)

### What changes
Add staggered fade+slide entrance animation to `PasswordEntryItem` cards in the `LazyColumn`.

### Approach
Use `AnimatedVisibility` with `remember { MutableTransitionState(false) }` initialized to false, which auto-triggers the enter animation on first composition. Each item gets:
- `fadeIn(tween(300, delayMillis = index * 50))`
- `slideInVertically(tween(300, delayMillis = index * 50)) { it / 4 }`

Cap the stagger at index 8 so items far down the list don't have excessive delays.

### Files modified
- `VaultScreen.kt` — add enter animation to items in `LazyColumn`

---

## Part 3: Dashboard Stat Cards Staggered Entrance (VaultScreen.kt)

### What changes
Add sequential scale+fade entrance to the 4 stat cards in `DashboardStatsRow`.

### Approach
Each `GlassCard` in the `LazyRow` gets wrapped with `AnimatedVisibility` using `MutableTransitionState(false)`:
- `fadeIn(tween(400, delayMillis = index * 80))`
- `scaleIn(tween(400, delayMillis = index * 80), initialScale = 0.85f)`

### Files modified
- `VaultScreen.kt` — `DashboardStatsRow` composable

---

## Part 4: GlassCard Content Transitions (SettingsScreen)

### What changes
Add `animateContentSize()` to `GlassCard` sections that expand/collapse, so the card boundary animates smoothly when `AnimatedVisibility` children expand.

### Approach
Add `Modifier.animateContentSize(animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f))` to the inner `Column` of each `GlassCard` that contains expandable content in SettingsScreen (theme mode, font size, scroll vibrations sections).

### Files modified
- `SettingsScreen.kt` — add `animateContentSize` to 2 expandable GlassCard Column modifiers (Appearance card, Experience card)

---

## Part 5: Form Field Sequential Entrance (PasswordEditScreen, AddPasswordSheet)

### What changes
Add staggered slide-up entrance for form fields when the edit screen or add sheet first appears.

### Approach
For `PasswordEditScreen`: Each `OutlinedTextField` and section gets wrapped in an `AnimatedVisibility` with `MutableTransitionState(false)`:
- `fadeIn(tween(300, delayMillis = fieldIndex * 60))`
- `slideInVertically(tween(300, delayMillis = fieldIndex * 60)) { it / 3 }`

Same approach for `AddPasswordContent` in `AddPasswordSheet.kt`.

### Files modified
- `PasswordDetailDialog.kt` (contains `PasswordEditScreen`) — add staggered entrance to form fields
- `AddPasswordSheet.kt` (contains `AddPasswordContent`) — add staggered entrance to form fields

---

## Part 6: HomeScreen Feature Card Entrance

### What changes
Add a subtle scale+fade entrance animation to the `OutlinedCard` feature list on `HomeScreen`.

### Approach
Wrap the `OutlinedCard` in an `AnimatedVisibility` using `MutableTransitionState(false)`:
- `fadeIn(tween(500))`
- `slideInVertically(tween(500, easing = EmphasizedDecelerateEasing)) { it / 4 }`

Also stagger the two CTA buttons at the bottom with a slight delay.

### Files modified
- `HomeScreen.kt` — add entrance animation to feature card and CTA buttons

---

## Files Modified Summary

| File | Changes |
|------|---------|
| `VaultApp.kt` | Wrap screen chain in `AnimatedContent` with M3 expressive transitions |
| `VaultScreen.kt` | Staggered entrance for password list items + dashboard stat cards |
| `SettingsScreen.kt` | `animateContentSize` on expandable GlassCard sections |
| `PasswordDetailDialog.kt` | Staggered form field entrance in `PasswordEditScreen` |
| `AddPasswordSheet.kt` | Staggered form field entrance in `AddPasswordContent` |
| `HomeScreen.kt` | Feature card + CTA button entrance animations |

---

## What is NOT changed
- No navigation logic changes — all `if/else` conditions, `BackHandler`, state variables remain identical
- No callback signatures modified
- No new dependencies needed — all APIs available in current Compose BOM
- No functionality affected — animations are purely visual additions
- No changes to `GlassCard` composable itself (animations applied at call sites)

---

## Animation Specs Reference

| Animation | Duration | Easing | Notes |
|-----------|----------|--------|-------|
| Screen transitions | 400ms | EmphasizedDecelerate (enter) / EmphasizedAccelerate (exit) | M3 standard |
| List item stagger | 300ms + 50ms/item delay | EaseOut | Capped at 8 items |
| Stat card stagger | 400ms + 80ms/card delay | EaseOut + scaleIn(0.85) | 4 cards total |
| Card content resize | Spring(0.8, 300) | Spring physics | For expand/collapse |
| Form field stagger | 300ms + 60ms/field delay | EaseOut | ~6 fields |
| Feature card entrance | 500ms | EmphasizedDecelerate | Single card |

---

## Verification

1. `gradlew.bat assembleDebug` — compiles with no errors
2. Manual test: navigate between all screens, verify smooth transitions
3. Verify back navigation still works correctly (BackHandler unaffected)
4. Verify add password sheet still opens/closes properly
5. Verify settings expand/collapse sections animate smoothly
6. Verify no visual glitches or clipping during transitions
