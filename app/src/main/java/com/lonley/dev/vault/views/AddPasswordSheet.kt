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
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
        reminderEnabled: Boolean
    ) -> Unit,
    onCancel: () -> Unit,
    hapticsEnabled: Boolean = false,
    initialPassword: String = "",
    onInteraction: () -> Unit = {}
) {
    val view = LocalView.current
    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember(initialPassword) { mutableStateOf(initialPassword) }
    var website by remember { mutableStateOf("") }
    var comments by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var nameDirty by remember { mutableStateOf(false) }
    var passwordDirty by remember { mutableStateOf(false) }

    // Subscription fields
    var isSubscription by remember { mutableStateOf(false) }
    var selectedPlanType by remember { mutableStateOf<PlanType?>(null) }
    var price by remember { mutableStateOf("") }
    var subscriptionEmail by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf<Long?>(null) }
    var reminderEnabled by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showGeneratorDialog by remember { mutableStateOf(false) }

    val isFormValid = name.isNotBlank() && password.isNotBlank()
    val fieldShape = MaterialTheme.shapes.extraLarge

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Text(
            text = if (isSubscription) "Add Subscription" else "Add Password",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = if (isSubscription) "Track a new subscription." else "Save a new credential to your vault.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

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

        OutlinedTextField(
            value = password,
            onValueChange = { password = it; onInteraction() },
            label = { Text("Password") },
            singleLine = true,
            isError = passwordDirty && password.isBlank(),
            supportingText = if (passwordDirty && password.isBlank()) {
                { Text("Password is required") }
            } else null,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingIcon = {
                Row {
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
                    IconButton(onClick = {
                        HapticHelper.performClick(view, hapticsEnabled)
                        passwordVisible = !passwordVisible
                    }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { if (!it.isFocused) passwordDirty = true },
            shape = fieldShape
        )

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
                    onConfirm(
                        name, username, email, password, website, comments,
                        isSubscription, selectedPlanType,
                        price.ifBlank { null },
                        subscriptionEmail.ifBlank { null },
                        startDate, reminderEnabled
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
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    startDate = datePickerState.selectedDateMillis
                    showDatePicker = false
                    onInteraction()
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AddPasswordContentPreview() {
    VaultTheme {
        AddPasswordContent(
            onConfirm = { _, _, _, _, _, _, _, _, _, _, _, _ -> },
            onCancel = {}
        )
    }
}
