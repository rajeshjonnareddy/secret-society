package com.lonley.dev.vault.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import com.lonley.dev.vault.model.Network
import com.lonley.dev.vault.util.HapticHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkDropdown(
    selectedNetwork: Network?,
    onNetworkSelected: (Network) -> Unit,
    fieldShape: Shape,
    hapticsEnabled: Boolean = false,
    onInteraction: () -> Unit = {}
) {
    val view = LocalView.current
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            expanded = it
            onInteraction()
        }
    ) {
        OutlinedTextField(
            value = selectedNetwork?.label ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Network (Optional)") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
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
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            Network.entries.forEach { net ->
                DropdownMenuItem(
                    text = { Text(net.label) },
                    onClick = {
                        HapticHelper.performClick(view, hapticsEnabled)
                        onNetworkSelected(net)
                        expanded = false
                        onInteraction()
                    }
                )
            }
        }
    }
}

@Composable
fun SeedPhraseInput(
    words: List<String>,
    wordCount: Int,
    onWordChange: (index: Int, value: String) -> Unit,
    onWordCountChange: (Int) -> Unit,
    fieldShape: Shape,
    hapticsEnabled: Boolean = false,
    onInteraction: () -> Unit = {}
) {
    val view = LocalView.current

    Text(
        text = "Seed Phrase Word Count",
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(8.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = wordCount == 12,
            onClick = {
                HapticHelper.performClick(view, hapticsEnabled)
                onWordCountChange(12)
                onInteraction()
            },
            label = { Text("12 Words") }
        )
        FilterChip(
            selected = wordCount == 24,
            onClick = {
                HapticHelper.performClick(view, hapticsEnabled)
                onWordCountChange(24)
                onInteraction()
            },
            label = { Text("24 Words") }
        )
    }

    Spacer(modifier = Modifier.height(8.dp))
    val filledCount = words.take(wordCount).count { it.isNotBlank() }
    Text(
        text = "$filledCount / $wordCount words filled",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(8.dp))
    PassphraseWordFields(
        words = words,
        wordCount = wordCount,
        onWordChange = { index, value ->
            onWordChange(index, value)
            onInteraction()
        },
        fieldShape = fieldShape
    )
}
