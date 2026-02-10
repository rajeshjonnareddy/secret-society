package com.lonley.dev.vault.views

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lonley.dev.vault.R
import com.lonley.dev.vault.ui.theme.VaultTheme

@Composable
fun HomeScreen(
    onUploadClick: () -> Unit = { /* TODO: Implement navigation to upload screen */ },
    onCreateNewClick: () -> Unit = { /* TODO: Implement navigation to create new screen */ }
) {
    // State for the animated slogan parts
    val sloganParts = listOf("Data", "Device", "Rules")
    var currentSloganIndex by remember { mutableStateOf(0) }

    // Animate the index change every few seconds
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(2500) // Change word every 2.5 seconds
            currentSloganIndex = (currentSloganIndex + 1) % sloganParts.size
        }
    }

    // Apply the VaultTheme. The background color is handled within VaultTheme itself.
    VaultTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Logo
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "App Logo",
                modifier = Modifier
                    .height(150.dp)
                    .width(150.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Animated Slogan Section
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Your ",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground, // Use onBackground for text on background
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                // Animated text part
                AnimatedContent(
                    targetState = sloganParts[currentSloganIndex],
                    label = "sloganTransition",
                    transitionSpec = {
                        // Define slide and fade transitions
                        slideInVertically { height -> height } + fadeIn() togetherWith
                                slideOutVertically { height -> -height } + fadeOut()
                    }
                ) { targetState ->
                    Text(
                        text = targetState,
                        // Use secondary color from theme
                        color = MaterialTheme.colorScheme.secondary,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
                Text(
                    text = "!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground, // Use onBackground for text on background
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Descriptive Text
            Text(
                text = buildAnnotatedString {
                    append("Experience true security with ")
                    withStyle(style = SpanStyle(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.tertiary // Using tertiary for highlight
                    )) {
                        append("local-first encryption")
                    }
                    append(". No cloud, no leaks—just your device and your password.")
                },
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground, // Use onBackground for text on background
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Create New Vault Button (Primary Action)
            Button(
                onClick = onCreateNewClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors( // Explicitly define button colors
                    containerColor = MaterialTheme.colorScheme.primary, // Use primary for container
                    contentColor = MaterialTheme.colorScheme.onPrimary // Use onPrimary for text
                )
            ) {
                Text(
                    "Create New Vault",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Upload Existing Vault Button (Secondary Action)
            Button(
                onClick = onUploadClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors( // Explicitly define button colors
                    containerColor = MaterialTheme.colorScheme.secondaryContainer, // Use secondaryContainer for a distinct look
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer // Use onSecondaryContainer for text
                )
            ) {
                Text(
                    "Open Your Vault",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_NO)
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}
