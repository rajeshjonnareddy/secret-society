# Plan: Fix renewal date calculation + price currency input

## Problem 1: Incorrect next renewal date calculation

**Current behavior:** `nextRenewalDate()` adds a fixed number of days (30 for Monthly, 365 for Yearly) repeatedly from the start date. This drifts over time — "Monthly" isn't always 30 days, and "Yearly" isn't always 365 days.

**Desired behavior:** Use proper calendar math. From the start date, calculate the **same day of the next month** (or next year for yearly, next week for weekly) relative to today. E.g., if start date is Jan 31, monthly renewal → Feb 28, Mar 31, Apr 30, etc.

### Changes

**File: `model/PlanType.kt`**
- Remove `periodDays` from enum (no longer needed)
- Rewrite `nextRenewalDate()` using `java.time.LocalDate`:
  1. Convert `startDate` (epoch millis) to `LocalDate`
  2. Get today as `LocalDate`
  3. Starting from the start date, advance by the plan period (`.plusWeeks(1)`, `.plusMonths(1)`, `.plusYears(1)`) until we land on a date >= today
  4. Convert back to epoch millis and return
- This handles month-end edge cases correctly (Java's `plusMonths` clamps to last valid day)

---

## Problem 2: Notification schedule — 5 days before, daily, then 3x on renewal day

**Current behavior:** `RenewalCheckWorker` runs once per day and only notifies within a 3-day window. Sends one notification per check. No multiple notifications on renewal day.

**Desired behavior:**
- Notifications start **5 days before** renewal (days 5, 4, 3, 2, 1 before)
- On renewal day itself, send **3 notifications** (morning, afternoon, evening)
- Worker already runs daily via WorkManager — change the window from 3 days to 5 days

### Changes

**File: `notification/RenewalCheckWorker.kt`**
- Change window from `threeDaysMs` (3 days) to `fiveDaysMs` (5 days)
- When `daysUntil == 0` (renewal day), send 3 notifications with distinct IDs and different messages:
  - "Morning reminder: {name} renews today!"
  - "Afternoon reminder: {name} renews today!"
  - "Evening reminder: Don't forget — {name} renews today!"
- For days 1–5 before, send one notification as before (already works)

**File: `notification/VaultNotificationHelper.kt`**
- Add a `sendRenewalDayNotifications()` method that sends 3 notifications with different text and unique notification IDs

**File: `viewmodel/VaultViewModel.kt` `syncReminders()`**
- No structural change needed — it stores `nextRenewalDate()` which will now return correct calendar-based dates

---

## Problem 3: Price text field — currency-style input (right-to-left digits, always 2 decimals)

**Current behavior:** Free-form decimal input. User types "99" and gets "99". Manual dot placement.

**Desired behavior:** Currency-style input where digits fill from the right with an implied 2-decimal-place format:
- Type `9` → `0.09`
- Type `99` → `0.99`
- Type `999` → `9.99`
- Type `9999` → `99.99`
- Type `99999` → `999.99`
- Backspace removes the last digit and shifts right

### Changes

**File: `views/AddPasswordSheet.kt`** — Price `OutlinedTextField` (~line 339)
- Replace `onValueChange` logic with currency formatter:
  1. Strip all non-digit characters from input
  2. Limit to max 7 digits (99999.99 = 7 digits)
  3. Store raw digits in state, display formatted: insert decimal 2 places from the right
  4. Use a `VisualTransformation` or format the display string directly
- Approach: store `price` as raw digit string (e.g. "999"), display as formatted ("9.99")
- On save, the formatted value is what gets passed to `onConfirm`

**File: `views/PasswordDetailDialog.kt`** — Edit screen price `OutlinedTextField` (~line 888)
- Same currency formatting logic
- On init, convert existing `entry.price` (e.g. "9.99") back to raw digits ("999")

### Helper function (shared between both files)
- Add a `formatCurrencyInput(rawDigits: String): String` utility either as a top-level function in one of the files or a small helper
- Logic: pad to at least 3 chars, insert "." before last 2 digits, strip unnecessary leading zeros

---

## Files to modify

| File | Change |
|------|--------|
| `model/PlanType.kt` | Remove `periodDays`, rewrite `nextRenewalDate()` with `LocalDate` calendar math |
| `notification/RenewalCheckWorker.kt` | Expand window to 5 days; send 3 notifications on renewal day |
| `notification/VaultNotificationHelper.kt` | Support multiple renewal-day notification messages |
| `views/AddPasswordSheet.kt` | Currency-style price input formatting |
| `views/PasswordDetailDialog.kt` | Currency-style price input formatting (edit screen) |

---

## Verification

- `gradlew.bat assembleDebug` compiles
- Monthly renewal from Jan 31 → Feb 28 (not March 2)
- Yearly renewal from Feb 29 2024 → Feb 28 2025
- Price field: typing "12345" displays "123.45"
- Backspace on "123.45" → "12.34"
- Notifications fire at days 5, 4, 3, 2, 1 before renewal + 3x on renewal day
