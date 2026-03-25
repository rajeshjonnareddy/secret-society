package com.lonley.dev.vault.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.lonley.dev.vault.model.Network

@Composable
fun AddPortfolioContent(
    onConfirm: (
        tokenName: String,
        tokenSymbol: String,
        tokenAmount: String,
        tokenValueUsd: String,
        network: Network?,
        l2Network: String?
    ) -> Unit,
    onCancel: () -> Unit,
    hapticsEnabled: Boolean = false,
    onInteraction: () -> Unit = {}
) {
    var tokenName by remember { mutableStateOf("") }
    var tokenSymbol by remember { mutableStateOf("") }
    var tokenAmount by remember { mutableStateOf("") }
    var tokenValueUsd by remember { mutableStateOf("") }
    var selectedNetwork by remember { mutableStateOf<Network?>(null) }
    var l2Network by remember { mutableStateOf("") }
    var showErrors by remember { mutableStateOf(false) }

    val fieldShape = MaterialTheme.shapes.medium
    val showL2 = selectedNetwork == Network.Ethereum

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp)
    ) {
        Text(
            text = "Add Investment",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = tokenName,
            onValueChange = {
                tokenName = it
                onInteraction()
            },
            label = { Text("Token Name") },
            placeholder = { Text("e.g. Ethereum") },
            singleLine = true,
            isError = showErrors && tokenName.isBlank(),
            modifier = Modifier.fillMaxWidth(),
            shape = fieldShape
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = tokenSymbol,
            onValueChange = {
                tokenSymbol = it.uppercase()
                onInteraction()
            },
            label = { Text("Symbol") },
            placeholder = { Text("e.g. ETH") },
            singleLine = true,
            isError = showErrors && tokenSymbol.isBlank(),
            modifier = Modifier.fillMaxWidth(),
            shape = fieldShape
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = tokenAmount,
            onValueChange = {
                tokenAmount = it
                onInteraction()
            },
            label = { Text("Amount Held") },
            placeholder = { Text("e.g. 2.5") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            isError = showErrors && (tokenAmount.isBlank() || tokenAmount.toDoubleOrNull() == null),
            modifier = Modifier.fillMaxWidth(),
            shape = fieldShape
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = tokenValueUsd,
            onValueChange = {
                tokenValueUsd = it
                onInteraction()
            },
            label = { Text("USD Value per Token") },
            placeholder = { Text("e.g. 3500.00") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            isError = showErrors && (tokenValueUsd.isBlank() || tokenValueUsd.toDoubleOrNull() == null),
            modifier = Modifier.fillMaxWidth(),
            shape = fieldShape
        )
        Spacer(modifier = Modifier.height(12.dp))

        NetworkDropdown(
            selectedNetwork = selectedNetwork,
            onNetworkSelected = {
                selectedNetwork = it
                onInteraction()
            },
            fieldShape = fieldShape,
            hapticsEnabled = hapticsEnabled,
            onInteraction = onInteraction
        )

        if (showL2) {
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = l2Network,
                onValueChange = {
                    l2Network = it
                    onInteraction()
                },
                label = { Text("L2 Network (optional)") },
                placeholder = { Text("e.g. Arbitrum, Base, Optimism") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = fieldShape
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                onInteraction()
                if (tokenName.isBlank() || tokenSymbol.isBlank() ||
                    tokenAmount.toDoubleOrNull() == null ||
                    tokenValueUsd.toDoubleOrNull() == null
                ) {
                    showErrors = true
                    return@Button
                }
                onConfirm(
                    tokenName.trim(),
                    tokenSymbol.trim(),
                    tokenAmount.trim(),
                    tokenValueUsd.trim(),
                    selectedNetwork,
                    l2Network.trim().ifBlank { null }
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add to Portfolio")
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = {
                onInteraction()
                onCancel()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancel")
        }
    }
}
