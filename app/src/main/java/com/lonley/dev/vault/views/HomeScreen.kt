package com.lonley.dev.vault.views

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.lonley.dev.vault.ui.theme.VaultTheme // Assuming VaultTheme is in this path

@Composable
fun HomeScreen(
    onUploadClick: () -> Unit = { /* TODO: Implement navigation to upload screen */ },
    onCreateNewClick: () -> Unit = { /* TODO: Implement navigation to create new screen */ }
) {

    val sloganParts = listOf("Data", "Device", "Rules")
    var currentSloganIndex by remember { mutableStateOf(0) }

    // Animate the index change every few seconds
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(1500) // Change word every 1.5 seconds
            currentSloganIndex = (currentSloganIndex + 1) % sloganParts.size
        }
    }


    VaultTheme { // Apply the VaultTheme
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp), // Increased padding for a more spacious feel
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center // Center content vertically
        ) {
            // App Logo
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground), // Replace with your actual logo
                contentDescription = "App Logo",
                modifier = Modifier
                    .height(150.dp) // Slightly larger logo
                    .width(150.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Your ",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
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
                        // Use secondary color from theme, or define a fallback
                        color = MaterialTheme.colorScheme.secondary,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold, // Keep emphasis on these words
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
                Text(
                    text = "!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Descriptive Text (Modernized with AnnotatedString)
            Text(
                text = buildAnnotatedString {
                    append("Experience true security with ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold)) {
                        append("local-first encryption")
                    }
                    append(". No cloud, no leaks—just your device and your password.")
                },
                style = MaterialTheme.typography.bodyMedium, // Use a standard body text style
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(48.dp)) // More space before buttons

            // Create New Vault Button (Primary Action)
            Button(
                onClick = onCreateNewClick,
                modifier = Modifier
                    .fillMaxWidth() // Make button wider
                    .height(56.dp), // Standard button height
                shape = MaterialTheme.shapes.medium // Use theme's medium shape
            ) {
                Text(
                    "Create New Vault",
                    style = MaterialTheme.typography.titleMedium, // Slightly larger text for button
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Upload Existing Vault Button (Secondary Action)
            Button(
                onClick = onUploadClick,
                modifier = Modifier
                    .fillMaxWidth() // Make button wider
                    .height(56.dp),
                shape = MaterialTheme.shapes.medium,
                // Optionally, style this as a secondary button if your theme supports it,
                // e.g., using outlinedButton or different colors.
                // For now, it's styled similarly to the primary for simplicity.
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
