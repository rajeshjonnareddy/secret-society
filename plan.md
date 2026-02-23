# Plan: 4 Changes

## 1. Add X button to PasswordGeneratorDialog

**File:** `views/PasswordGeneratorDialog.kt`

Change the dialog header from just a title to a `Row` with the title on the left and a close `IconButton` (`Icons.Default.Close`) on the right, calling `onDismiss`.

---

## 2. Reorder FAB icons: generator before settings

**File:** `views/VaultScreen.kt`

Swap the Password icon button and the Settings icon button in the FAB row so the order becomes:
Lock, Download, Profile, **Password**, **Settings**

---

## 3. Price field: proper currency input ($00.00)

**Files:** `views/AddPasswordSheet.kt` and `views/PasswordDetailDialog.kt` (PasswordEditScreen has the same price field)

- Add input filtering via `onValueChange` that only allows digits and a single decimal point, with max 2 decimal places
- The existing `AttachMoney` leading icon already shows `$`

---

## 4. Fixed FAB row across all pages (content-only transitions)

Currently each screen (VaultScreen, UserProfileScreen, SettingsScreen) duplicates the FAB row, and `AnimatedContent` in `VaultApp.kt` transitions the entire screen including the FAB.

### Approach

Extract a shared `VaultBottomBar` composable and place it outside the `AnimatedContent` in `VaultApp.kt`, so it stays fixed while only the page content animates.

**Steps:**

1. **Extract `VaultBottomBar`** into `views/VaultScreen.kt` — a reusable glass FAB row that accepts callbacks and a `currentScreen` param to highlight the active icon.

2. **Remove the FAB row from VaultScreen, UserProfileScreen, and SettingsScreen** — add bottom spacer (~100.dp) so content doesn't sit behind the fixed FAB.

3. **Restructure VaultApp.kt Unlocked block** — place `AnimatedContent` and `VaultBottomBar` as siblings inside a `Box`. The FAB is shown for vault/profile/settings screens, hidden for detail/edit (which keep their own bottom buttons).

4. **Primary action button adapts per screen:**
   - vault: Add (+) icon, opens AddPasswordSheet
   - profile: Home icon, back to vault
   - settings: Home icon, back to vault

5. **Detail/Edit screens** keep their own bottom buttons unchanged.

### Files modified

| File | Change |
|------|--------|
| `views/VaultScreen.kt` | Extract `VaultBottomBar`; remove FAB from `VaultScreen`; add bottom spacer |
| `views/UserProfileScreen.kt` | Remove FAB row; remove nav callback params; add bottom spacer |
| `views/SettingsScreen.kt` | Remove FAB row; remove nav callback params; add bottom spacer |
| `VaultApp.kt` | Place `VaultBottomBar` outside `AnimatedContent`; wire callbacks; update screen calls |
