# Plan: Crypto Wallet Entry Type + Complementary Card Accent Colors

## Feature 1: Crypto Wallet Entry Type

### Concept
Add `CryptoWallet` as a third `EntryType` alongside `Password` and `Passphrase`. A crypto wallet entry stores seed phrases, wallet addresses, network info, and optional private keys — all encrypted locally like existing entries.

### Data Model Changes

**`model/EntryType.kt`** — Add new enum value:
```kotlin
enum class EntryType { Password, Passphrase, CryptoWallet }
```

**`model/PasswordEntry.kt`** — Add crypto-specific nullable fields:
```kotlin
val walletAddress: String? = null,       // Public wallet address (e.g. 0x...)
val network: CryptoNetwork? = null,      // Bitcoin, Ethereum, Solana, etc.
val privateKey: String? = null,          // Optional private key
val walletName: String? = null,          // e.g. "MetaMask", "Ledger", "Trust Wallet"
```
The existing `password` field stores the seed phrase (same as Passphrase type). The existing `phraseWordCount` (12/24) is reused.

**New file `model/CryptoNetwork.kt`**:
```kotlin
enum class CryptoNetwork(val label: String, val symbol: String) {
    Bitcoin("Bitcoin", "BTC"),
    Ethereum("Ethereum", "ETH"),
    Solana("Solana", "SOL"),
    Polygon("Polygon", "MATIC"),
    BNBChain("BNB Chain", "BNB"),
    Avalanche("Avalanche", "AVAX"),
    Cardano("Cardano", "ADA"),
    XRP("XRP", "XRP"),
    Other("Other", "")
}
```

### Repository Changes

**`repository/VaultRepository.kt`**:
- `saveVault()`: Add `put("walletAddress", ...)`, `put("network", ...)`, `put("privateKey", ...)`, `put("walletName", ...)`
- `parseEntries()`: Read with `optString` + null fallback, parse `CryptoNetwork` enum same pattern as `PlanType`

### ViewModel Changes

**`viewmodel/VaultViewModel.kt`**:
- `addPassword()`: Accept 4 new crypto params, pass to `PasswordEntry` constructor
- `updatePassword()`: Copy 4 new crypto fields during entry replacement

### UI Changes

**`views/AddPasswordSheet.kt`**:
- Add third `FilterChip` in type selector: "Crypto Wallet"
- When `CryptoWallet` selected, show:
  - `walletName` text field (e.g. "MetaMask", "Ledger") with wallet icon
  - `CryptoNetwork` chip row (scrollable, 9 options)
  - Seed phrase fields (reuse `PassphraseWordFields` + 12/24 word count selector)
  - `walletAddress` text field (public address) with copy icon
  - Optional `privateKey` masked password field (visibility toggle)
- Hide `username`, `email`, `website` fields when CryptoWallet is selected
- Form validation: `name` + all seed words required; walletAddress/privateKey optional
- Update `onConfirm` callback to pass new fields

**`views/PasswordDetailDialog.kt`**:
- `PasswordDetailScreen`: When `CryptoWallet`:
  - Show wallet name + network as header subtitle (e.g. "MetaMask · Ethereum")
  - Wallet address row with copy button
  - Seed phrase grid (reuse `PassphraseGridSection` with blur)
  - Private key row (masked + visibility toggle + copy) — only if present
  - Hide username/email rows
- `PasswordEditScreen`: Mirror AddPasswordSheet's crypto fields, pre-populate from entry

**`views/VaultScreen.kt`**:
- `PasswordEntryItem` card: When crypto wallet, show `"{network.symbol} · {walletName}"` where username normally goes
- Add `"Crypto"` filter pill: `FilterPill("Crypto", Icons.Outlined.AccountBalanceWallet, count = passwordEntries.count { it.entryType == EntryType.CryptoWallet })`
- Add filter case in `filteredEntries`: `"Crypto" -> entry.entryType == EntryType.CryptoWallet`

**`VaultApp.kt`**:
- Pass new crypto fields through navigation callbacks (add/edit flows)

### Icon
Use `Icons.Outlined.AccountBalanceWallet` for crypto wallet entries and filter pill (already in material-icons-extended).

---

## Feature 2: Complementary Card Accent Color

### Problem
Every heading, icon, and interactive element uses `colorScheme.primary` — the user's chosen accent. This creates a monochrome look where card entry names blend into the theme chrome.

### Approach: Use Material 3's `tertiary` color
Every accent color scheme already defines a `tertiary` that's intentionally complementary to `primary`. No new color logic needed — just use what's already there.

| User's Accent | Primary (theme/icons) | Tertiary (card headings) |
|---|---|---|
| Blue | Blue (#4285F4) | Purple (#7B5EA7) |
| Teal | Teal (#009688) | Blue-gray (#48617A) |
| Green | Green (#4CAF50) | Teal (#38656A) |
| Red | Red (#E53935) | Amber (#755A2F) |
| Purple | Purple (#9C27B0) | Rose (#815249) |
| Orange | Orange (#FF9800) | Olive (#56633B) |
| Pink | Pink (#E91E63) | Brown (#7C5635) |
| Indigo | Indigo (#3F51B5) | Rose (#78536B) |
| Yellow | Yellow (#F9A825) | Green (#4B6546) |
| Gray | Gray (#607D8B) | Lavender (#605B7D) |
| Brown | Brown (#795548) | Olive (#636032) |

### Implementation

**`ui/theme/Theme.kt`** — New composition local:
```kotlin
data class CardAccentColors(
    val heading: Color,
    val headingVariant: Color
)

val LocalCardAccent = staticCompositionLocalOf {
    CardAccentColors(heading = Color.Unspecified, headingVariant = Color.Unspecified)
}
```

In `VaultTheme`:
```kotlin
val cardAccent = CardAccentColors(
    heading = colorScheme.tertiary,
    headingVariant = colorScheme.onTertiaryContainer
)
CompositionLocalProvider(
    LocalGlassColors provides glassColors,
    LocalCardAccent provides cardAccent
) { ... }
```

**`views/VaultScreen.kt`** — Swap colors in `PasswordEntryItem`:
- Entry name `Text` → `LocalCardAccent.current.heading` (was `colorScheme.primary`)
- Passphrase label → `LocalCardAccent.current.heading`

**Keep `colorScheme.primary` for:**
- Vault title heading (brand identity)
- All icons and icon buttons
- Filter pills, search bar, action buttons
- Detail/edit screen field icons

This creates a two-tone palette where card names pop against the theme chrome.

---

## File Change Summary

| File | Crypto Wallet | Card Accent |
|---|---|---|
| `model/EntryType.kt` | Add `CryptoWallet` | — |
| `model/PasswordEntry.kt` | Add 4 fields | — |
| `model/CryptoNetwork.kt` | **New file** | — |
| `repository/VaultRepository.kt` | Serialize/parse 4 fields | — |
| `viewmodel/VaultViewModel.kt` | Pass 4 fields in add/update | — |
| `views/AddPasswordSheet.kt` | Crypto wallet form UI | — |
| `views/PasswordDetailDialog.kt` | Crypto detail + edit views | — |
| `views/VaultScreen.kt` | Crypto card display + filter | Heading color → tertiary |
| `ui/theme/Theme.kt` | — | `LocalCardAccent` + provide |
| `VaultApp.kt` | Pass fields in callbacks | — |

## Verification
- `gradlew.bat assembleDebug` passes after each feature
- Add a crypto wallet entry → seed phrase grid, wallet address, network all saved and displayed
- Filter by "Crypto" → shows only wallet entries
- Card headings use tertiary (complementary) color, icons/buttons stay primary
- Both dark and light modes look correct
