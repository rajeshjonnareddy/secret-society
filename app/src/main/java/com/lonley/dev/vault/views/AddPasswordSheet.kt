package com.lonley.dev.vault.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Password
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Subscriptions
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lonley.dev.vault.model.EntryType
import com.lonley.dev.vault.model.Network
import com.lonley.dev.vault.model.PlanType
import com.lonley.dev.vault.ui.theme.VaultTheme
import com.lonley.dev.vault.util.HapticHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPasswordContent(
    onConfirm: (
        name: String,
        username: String,
        email: String,
        password: String,
        website: String,
        comments: String,
        isSubscription: Boolean,
        planType: PlanType?,
        price: String?,
        subscriptionEmail: String?,
        startDate: Long?,
        reminderEnabled: Boolean,
        entryType: EntryType,
        phraseWordCount: Int?,
        walletAddress: String?,
        seedPhrase: String?,
        network: Network?,
        exchange: String?
    ) -> Unit,
    onCancel: () -> Unit,
    hapticsEnabled: Boolean = false,
    initialPassword: String = "",
    onInteraction: () -> Unit = {}
) {
    val view = LocalView.current
    val focusManager = LocalFocusManager.current
    val dateFocusRequester = remember { FocusRequester() }
    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember(initialPassword) { mutableStateOf(initialPassword) }
    var website by remember { mutableStateOf("") }
    var comments by remember { mutableStateOf("") }
    var nameDirty by remember { mutableStateOf(false) }
    var passwordDirty by remember { mutableStateOf(false) }

    // Entry type fields
    var selectedEntryType by remember { mutableStateOf(EntryType.Password) }

    // Digital wallet fields
    var walletAddress by remember { mutableStateOf("") }
    var seedPhraseWords by remember { mutableStateOf(List(24) { "" }) }
    var selectedSeedWordCount by remember { mutableStateOf(12) }
    var selectedNetwork by remember { mutableStateOf<Network?>(null) }
    var networkDropdownExpanded by remember { mutableStateOf(false) }
    var exchange by remember { mutableStateOf("") }

    // Subscription fields
    var isSubscription by remember { mutableStateOf(false) }
    var selectedPlanType by remember { mutableStateOf<PlanType?>(null) }
    var price by remember { mutableStateOf("") }
    var subscriptionEmail by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf<Long?>(null) }
    var reminderEnabled by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showGeneratorDialog by remember { mutableStateOf(false) }

    val hasSeedPhrase = seedPhraseWords.take(selectedSeedWordCount).all { it.isNotBlank() }
    val isFormValid = name.isNotBlank() && when (selectedEntryType) {
        EntryType.Password -> password.isNotBlank()
        EntryType.CryptoWallet -> walletAddress.isNotBlank() || hasSeedPhrase
        else -> false
    }
    val fieldShape = MaterialTheme.shapes.extraLarge

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Text(
            text = when {
                selectedEntryType == EntryType.CryptoWallet -> "Add Digital Wallet"
                isSubscription -> "Add Subscription"
                else -> "Add Password"
            },
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = when {
                selectedEntryType == EntryType.CryptoWallet -> "Store a wallet address and recovery phrase."
                isSubscription -> "Track a new subscription."
                else -> "Save a new credential to your vault."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Entry type selector
        Text(
            text = "Type",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = selectedEntryType == EntryType.Password,
                onClick = {
                    HapticHelper.performClick(view, hapticsEnabled)
                    selectedEntryType = EntryType.Password
                    onInteraction()
                },
                label = { Text("Password") }
            )
            FilterChip(
                selected = selectedEntryType == EntryType.CryptoWallet,
                onClick = {
                    HapticHelper.performClick(view, hapticsEnabled)
                    selectedEntryType = EntryType.CryptoWallet
                    password = ""
                    onInteraction()
                },
                label = { Text("Digital Wallet") }
            )
        }

        AnimatedVisibility(visible = selectedEntryType == EntryType.CryptoWallet) {
            Column {
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = walletAddress,
                    onValueChange = { walletAddress = it; onInteraction() },
                    label = { Text("Wallet Address") },
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = fieldShape
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; onInteraction() },
                    label = { Text("Name") },
                    singleLine = true,
                    isError = nameDirty && name.isBlank(),
                    supportingText = if (nameDirty && name.isBlank()) {
                        { Text("Name is required") }
                    } else null,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Badge,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { if (!it.isFocused) nameDirty = true },
                    shape = fieldShape
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Seed Phrase Word Count",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedSeedWordCount == 12,
                        onClick = {
                            HapticHelper.performClick(view, hapticsEnabled)
                            selectedSeedWordCount = 12
                            onInteraction()
                        },
                        label = { Text("12 Words") }
                    )
                    FilterChip(
                        selected = selectedSeedWordCount == 24,
                        onClick = {
                            HapticHelper.performClick(view, hapticsEnabled)
                            selectedSeedWordCount = 24
                            onInteraction()
                        },
                        label = { Text("24 Words") }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                val seedFilledCount = seedPhraseWords.take(selectedSeedWordCount).count { it.isNotBlank() }
                Text(
                    text = "$seedFilledCount / $selectedSeedWordCount words filled",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                PassphraseWordFields(
                    words = seedPhraseWords,
                    wordCount = selectedSeedWordCount,
                    onWordChange = { index, value ->
                        seedPhraseWords = seedPhraseWords.toMutableList().also { it[index] = value }
                        onInteraction()
                    },
                    fieldShape = fieldShape
                )

                Spacer(modifier = Modifier.height(12.dp))

                ExposedDropdownMenuBox(
                    expanded = networkDropdownExpanded,
                    onExpandedChange = {
                        networkDropdownExpanded = it
                        onInteraction()
                    }
                ) {
                    OutlinedTextField(
                        value = selectedNetwork?.label ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Network (Optional)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = networkDropdownExpanded) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Language,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        shape = fieldShape
                    )
                    ExposedDropdownMenu(
                        expanded = networkDropdownExpanded,
                        onDismissRequest = { networkDropdownExpanded = false }
                    ) {
                        Network.entries.forEach { net ->
                            DropdownMenuItem(
                                text = { Text(net.label) },
                                onClick = {
                                    HapticHelper.performClick(view, hapticsEnabled)
                                    selectedNetwork = net
                                    networkDropdownExpanded = false
                                    onInteraction()
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = exchange,
                    onValueChange = { exchange = it; onInteraction() },
                    label = { Text("Exchange (Optional)") },
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Language,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = fieldShape
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedEntryType != EntryType.CryptoWallet) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it; onInteraction() },
                label = { Text("Name") },
                singleLine = true,
                isError = nameDirty && name.isBlank(),
                supportingText = if (nameDirty && name.isBlank()) {
                    { Text("Name is required") }
                } else null,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Badge,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { if (!it.isFocused) nameDirty = true },
                shape = fieldShape
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it; onInteraction() },
            label = { Text("Username (Optional)") },
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = fieldShape
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it; onInteraction() },
            label = { Text("Email (Optional)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Email,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = fieldShape
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (selectedEntryType == EntryType.Password) {
            PasswordTextField(
                value = password,
                onValueChange = { password = it; onInteraction() },
                label = "Password",
                isError = passwordDirty && password.isBlank(),
                supportingText = if (passwordDirty && password.isBlank()) {
                    { Text("Password is required") }
                } else null,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                extraTrailingIcon = {
                    IconButton(onClick = {
                        HapticHelper.performClick(view, hapticsEnabled)
                        showGeneratorDialog = true
                    }) {
                        Icon(
                            imageVector = Icons.Outlined.Password,
                            contentDescription = "Generate Password",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { if (!it.isFocused) passwordDirty = true },
                shape = fieldShape
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = website,
            onValueChange = { website = it; onInteraction() },
            label = { Text("Website (Optional)") },
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Language,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = fieldShape
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = comments,
            onValueChange = { comments = it; onInteraction() },
            label = { Text("Comments (Optional)") },
            singleLine = false,
            minLines = 2,
            maxLines = 4,
            leadingIcon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Notes,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = fieldShape
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Subscription toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Subscriptions,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Subscription",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Switch(
                checked = isSubscription,
                onCheckedChange = {
                    HapticHelper.performClick(view, hapticsEnabled)
                    isSubscription = it
                    onInteraction()
                }
            )
        }

        AnimatedVisibility(visible = isSubscription) {
            Column {
                Spacer(modifier = Modifier.height(12.dp))

                // Plan type chips
                Text(
                    text = "Plan Type",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PlanType.entries.forEach { plan ->
                        FilterChip(
                            selected = selectedPlanType == plan,
                            onClick = {
                                HapticHelper.performClick(view, hapticsEnabled)
                                selectedPlanType = plan
                                onInteraction()
                            },
                            label = { Text(plan.label) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                val priceDisplayText = if (price.isEmpty()) "" else {
                    val cents = price.toLongOrNull() ?: 0L
                    "%.2f".format(cents / 100.0)
                }
                OutlinedTextField(
                    value = TextFieldValue(
                        text = priceDisplayText,
                        selection = TextRange(priceDisplayText.length)
                    ),
                    onValueChange = { newValue ->
                        onInteraction()
                        val digitsOnly = newValue.text.filter { it.isDigit() }
                        if (digitsOnly.length <= 7) {
                            price = digitsOnly
                        }
                    },
                    label = { Text("Price (Optional)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.AttachMoney,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = fieldShape
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = subscriptionEmail,
                    onValueChange = { subscriptionEmail = it; onInteraction() },
                    label = { Text("Subscription Email (Optional)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Email,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = fieldShape
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Start date button
                val dateText = if (startDate != null) {
                    val sdf = java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.getDefault())
                    sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
                    sdf.format(java.util.Date(startDate!!))
                } else "Select Start Date"

                OutlinedTextField(
                    value = dateText,
                    onValueChange = {},
                    label = { Text("Start Date") },
                    readOnly = true,
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.CalendarToday,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(dateFocusRequester)
                        .onFocusChanged { if (it.isFocused) showDatePicker = true },
                    shape = fieldShape
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Reminder toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Remind before renewal",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Switch(
                        checked = reminderEnabled,
                        onCheckedChange = {
                            HapticHelper.performClick(view, hapticsEnabled)
                            reminderEnabled = it
                            onInteraction()
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FilledTonalButton(
                onClick = {
                    HapticHelper.performClick(view, hapticsEnabled)
                    onCancel()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Text(
                    text = "Cancel",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Button(
                onClick = {
                    HapticHelper.performClick(view, hapticsEnabled)
                    val isWallet = selectedEntryType == EntryType.CryptoWallet
                    val finalPassword = if (isWallet) {
                        seedPhraseWords.take(selectedSeedWordCount).joinToString(" ") { it.trim() }
                    } else password
                    onConfirm(
                        name, username, email, finalPassword, website, comments,
                        isSubscription, selectedPlanType,
                        price.ifBlank { null },
                        subscriptionEmail.ifBlank { null },
                        startDate, reminderEnabled,
                        selectedEntryType,
                        null,
                        if (isWallet) walletAddress else null,
                        if (isWallet) seedPhraseWords.take(selectedSeedWordCount).joinToString(" ") { it.trim() } else null,
                        if (isWallet) selectedNetwork else null,
                        if (isWallet) exchange.ifBlank { null } else null
                    )
                },
                enabled = isFormValid,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Text(
                    text = "Add",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    // Password generator dialog
    if (showGeneratorDialog) {
        PasswordGeneratorDialog(
            onDismiss = { showGeneratorDialog = false },
            onAddPassword = { generated ->
                password = generated
                showGeneratorDialog = false
            },
            hapticsEnabled = hapticsEnabled,
            onInteraction = onInteraction
        )
    }

    // Date picker dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = startDate ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = {
                showDatePicker = false
                focusManager.clearFocus()
            },
            confirmButton = {
                TextButton(onClick = {
                    startDate = datePickerState.selectedDateMillis
                    showDatePicker = false
                    focusManager.clearFocus()
                    onInteraction()
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDatePicker = false
                    focusManager.clearFocus()
                }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun PassphraseWordFields(
    words: List<String>,
    wordCount: Int,
    onWordChange: (index: Int, value: String) -> Unit,
    fieldShape: androidx.compose.ui.graphics.Shape = MaterialTheme.shapes.medium
) {
    val columns = 3
    val rows = wordCount / columns
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        for (row in 0 until rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (col in 0 until columns) {
                    val index = row * columns + col
                    OutlinedTextField(
                        value = words[index],
                        onValueChange = { value ->
                            val pastedWords = value.trim().split("\\s+".toRegex())
                            if (pastedWords.size > 1) {
                                // Pasted a multi-word phrase — distribute across fields
                                pastedWords.forEachIndexed { offset, word ->
                                    val targetIndex = index + offset
                                    if (targetIndex < wordCount) {
                                        onWordChange(targetIndex, word)
                                    }
                                }
                            } else {
                                onWordChange(index, value.filter { !it.isWhitespace() })
                            }
                        },
                        label = { Text("${index + 1}.") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = fieldShape,
                        textStyle = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AddPasswordContentPreview() {
    VaultTheme {
        AddPasswordContent(
            onConfirm = { _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _ -> },
            onCancel = {}
        )
    }
}
