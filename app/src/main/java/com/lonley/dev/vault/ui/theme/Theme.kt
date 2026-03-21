package com.lonley.dev.vault.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.lonley.dev.vault.model.AccentColor

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6750A4),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFEADDFF),
    onPrimaryContainer = Color(0xFF21005D),
    secondary = Color(0xFF625B71),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE8DEF8),
    onSecondaryContainer = Color(0xFF1D192B),
    tertiary = Color(0xFF7D5260),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFD8E4),
    onTertiaryContainer = Color(0xFF31111D),
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    background = Color(0xFFFFFBFE),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFBFE),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF79747E),
    outlineVariant = Color(0xFFCAC4D0),
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFD0BCFF),
    onPrimary = Color(0xFF381E72),
    primaryContainer = Color(0xFF4F378B),
    onPrimaryContainer = Color(0xFFEADDFF),
    secondary = Color(0xFFCCC2DC),
    onSecondary = Color(0xFF332D41),
    secondaryContainer = Color(0xFF4A4458),
    onSecondaryContainer = Color(0xFFE8DEF8),
    tertiary = Color(0xFFEFB8C8),
    onTertiary = Color(0xFF492532),
    tertiaryContainer = Color(0xFF633B48),
    onTertiaryContainer = Color(0xFFFFD8E4),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF9DEDC),
    background = Color(0xFF1C1B1F),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF1C1B1F),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF938F99),
    outlineVariant = Color(0xFF49454F),
)

// ── Accent color schemes ──

private fun blueLight() = lightColorScheme(
    primary = Color(0xFF4285F4), onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD3E3FD), onPrimaryContainer = Color(0xFF001D6C),
    secondary = Color(0xFF5B6B7D), onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFDFE3EB), onSecondaryContainer = Color(0xFF182633),
    tertiary = Color(0xFF7B5EA7), onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFEDDCFF), onTertiaryContainer = Color(0xFF2D0050),
    error = Color(0xFFB3261E), onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF9DEDC), onErrorContainer = Color(0xFF410E0B),
    background = Color(0xFFFFFBFE), onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFBFE), onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFE1E2EC), onSurfaceVariant = Color(0xFF44474F),
    outline = Color(0xFF74777F), outlineVariant = Color(0xFFC4C6D0),
)

private fun blueDark() = darkColorScheme(
    primary = Color(0xFFA8C7FA), onPrimary = Color(0xFF003DA5),
    primaryContainer = Color(0xFF1B66D0), onPrimaryContainer = Color(0xFFD3E3FD),
    secondary = Color(0xFFC3C7CF), onSecondary = Color(0xFF2D3B49),
    secondaryContainer = Color(0xFF435361), onSecondaryContainer = Color(0xFFDFE3EB),
    tertiary = Color(0xFFD6BAFF), onTertiary = Color(0xFF441F76),
    tertiaryContainer = Color(0xFF5E418E), onTertiaryContainer = Color(0xFFEDDCFF),
    error = Color(0xFFF2B8B5), onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18), onErrorContainer = Color(0xFFF9DEDC),
    background = Color(0xFF1C1B1F), onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF1C1B1F), onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF44474F), onSurfaceVariant = Color(0xFFC4C6D0),
    outline = Color(0xFF8E9099), outlineVariant = Color(0xFF44474F),
)

private fun tealLight() = lightColorScheme(
    primary = Color(0xFF009688), onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFA7F5EC), onPrimaryContainer = Color(0xFF002019),
    secondary = Color(0xFF4A6360), onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFCCE8E4), onSecondaryContainer = Color(0xFF05201D),
    tertiary = Color(0xFF48617A), onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFCEE5FF), onTertiaryContainer = Color(0xFF001D33),
    error = Color(0xFFB3261E), onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF9DEDC), onErrorContainer = Color(0xFF410E0B),
    background = Color(0xFFFFFBFE), onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFBFE), onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFDAE5E2), onSurfaceVariant = Color(0xFF3F4947),
    outline = Color(0xFF6F7977), outlineVariant = Color(0xFFBEC9C6),
)

private fun tealDark() = darkColorScheme(
    primary = Color(0xFF80CBC4), onPrimary = Color(0xFF00363D),
    primaryContainer = Color(0xFF004F46), onPrimaryContainer = Color(0xFFA7F5EC),
    secondary = Color(0xFFB1CCC8), onSecondary = Color(0xFF1C3532),
    secondaryContainer = Color(0xFF334B48), onSecondaryContainer = Color(0xFFCCE8E4),
    tertiary = Color(0xFFB0CAE6), onTertiary = Color(0xFF19334B),
    tertiaryContainer = Color(0xFF304962), onTertiaryContainer = Color(0xFFCEE5FF),
    error = Color(0xFFF2B8B5), onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18), onErrorContainer = Color(0xFFF9DEDC),
    background = Color(0xFF1C1B1F), onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF1C1B1F), onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF3F4947), onSurfaceVariant = Color(0xFFBEC9C6),
    outline = Color(0xFF899391), outlineVariant = Color(0xFF3F4947),
)

private fun greenLight() = lightColorScheme(
    primary = Color(0xFF4CAF50), onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFC8E6C9), onPrimaryContainer = Color(0xFF002106),
    secondary = Color(0xFF52634F), onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD5E8CF), onSecondaryContainer = Color(0xFF101F10),
    tertiary = Color(0xFF38656A), onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFBCEBF0), onTertiaryContainer = Color(0xFF002023),
    error = Color(0xFFB3261E), onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF9DEDC), onErrorContainer = Color(0xFF410E0B),
    background = Color(0xFFFFFBFE), onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFBFE), onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFDEE5D9), onSurfaceVariant = Color(0xFF424940),
    outline = Color(0xFF72796F), outlineVariant = Color(0xFFC2C9BD),
)

private fun greenDark() = darkColorScheme(
    primary = Color(0xFFA5D6A7), onPrimary = Color(0xFF003910),
    primaryContainer = Color(0xFF1B5E20), onPrimaryContainer = Color(0xFFC8E6C9),
    secondary = Color(0xFFB9CCB4), onSecondary = Color(0xFF253423),
    secondaryContainer = Color(0xFF3B4B38), onSecondaryContainer = Color(0xFFD5E8CF),
    tertiary = Color(0xFFA0CFD4), onTertiary = Color(0xFF00363A),
    tertiaryContainer = Color(0xFF1F4D51), onTertiaryContainer = Color(0xFFBCEBF0),
    error = Color(0xFFF2B8B5), onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18), onErrorContainer = Color(0xFFF9DEDC),
    background = Color(0xFF1C1B1F), onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF1C1B1F), onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF424940), onSurfaceVariant = Color(0xFFC2C9BD),
    outline = Color(0xFF8C9388), outlineVariant = Color(0xFF424940),
)

private fun yellowLight() = lightColorScheme(
    primary = Color(0xFFF9A825), onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFE082), onPrimaryContainer = Color(0xFF261A00),
    secondary = Color(0xFF6B5E3F), onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFF4E2BB), onSecondaryContainer = Color(0xFF241B04),
    tertiary = Color(0xFF4B6546), onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFCDEBC4), onTertiaryContainer = Color(0xFF072108),
    error = Color(0xFFB3261E), onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF9DEDC), onErrorContainer = Color(0xFF410E0B),
    background = Color(0xFFFFFBFE), onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFBFE), onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFECE1CF), onSurfaceVariant = Color(0xFF4D4639),
    outline = Color(0xFF7F7667), outlineVariant = Color(0xFFD0C5B4),
)

private fun yellowDark() = darkColorScheme(
    primary = Color(0xFFFFD54F), onPrimary = Color(0xFF3F2E00),
    primaryContainer = Color(0xFFC49000), onPrimaryContainer = Color(0xFFFFE082),
    secondary = Color(0xFFD7C6A1), onSecondary = Color(0xFF3A3015),
    secondaryContainer = Color(0xFF52472A), onSecondaryContainer = Color(0xFFF4E2BB),
    tertiary = Color(0xFFB1CFA9), onTertiary = Color(0xFF1D361B),
    tertiaryContainer = Color(0xFF334D30), onTertiaryContainer = Color(0xFFCDEBC4),
    error = Color(0xFFF2B8B5), onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18), onErrorContainer = Color(0xFFF9DEDC),
    background = Color(0xFF1C1B1F), onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF1C1B1F), onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF4D4639), onSurfaceVariant = Color(0xFFD0C5B4),
    outline = Color(0xFF998F80), outlineVariant = Color(0xFF4D4639),
)

private fun orangeLight() = lightColorScheme(
    primary = Color(0xFFFF9800), onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFDDB3), onPrimaryContainer = Color(0xFF2A1800),
    secondary = Color(0xFF715B3F), onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFDDEBB), onSecondaryContainer = Color(0xFF281804),
    tertiary = Color(0xFF56633B), onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFD9E9B6), onTertiaryContainer = Color(0xFF141F01),
    error = Color(0xFFB3261E), onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF9DEDC), onErrorContainer = Color(0xFF410E0B),
    background = Color(0xFFFFFBFE), onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFBFE), onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFF0E0CF), onSurfaceVariant = Color(0xFF51453A),
    outline = Color(0xFF837568), outlineVariant = Color(0xFFD5C4B4),
)

private fun orangeDark() = darkColorScheme(
    primary = Color(0xFFFFB74D), onPrimary = Color(0xFF462B00),
    primaryContainer = Color(0xFFCC7A00), onPrimaryContainer = Color(0xFFFFDDB3),
    secondary = Color(0xFFDFC2A0), onSecondary = Color(0xFF3F2D15),
    secondaryContainer = Color(0xFF58442A), onSecondaryContainer = Color(0xFFFDDEBB),
    tertiary = Color(0xFFBDCD9C), onTertiary = Color(0xFF293412),
    tertiaryContainer = Color(0xFF3F4B25), onTertiaryContainer = Color(0xFFD9E9B6),
    error = Color(0xFFF2B8B5), onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18), onErrorContainer = Color(0xFFF9DEDC),
    background = Color(0xFF1C1B1F), onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF1C1B1F), onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF51453A), onSurfaceVariant = Color(0xFFD5C4B4),
    outline = Color(0xFF9D8E7F), outlineVariant = Color(0xFF51453A),
)

private fun purpleLight() = lightColorScheme(
    primary = Color(0xFF9C27B0), onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFF3DAFF), onPrimaryContainer = Color(0xFF36003E),
    secondary = Color(0xFF6B5870), onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFF3DBF7), onSecondaryContainer = Color(0xFF25162A),
    tertiary = Color(0xFF815249), onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFDAD4), onTertiaryContainer = Color(0xFF33110B),
    error = Color(0xFFB3261E), onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF9DEDC), onErrorContainer = Color(0xFF410E0B),
    background = Color(0xFFFFFBFE), onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFBFE), onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFEBDFEA), onSurfaceVariant = Color(0xFF4C444D),
    outline = Color(0xFF7D747D), outlineVariant = Color(0xFFCFC3CE),
)

private fun purpleDark() = darkColorScheme(
    primary = Color(0xFFE1BEE7), onPrimary = Color(0xFF560064),
    primaryContainer = Color(0xFF7B1FA2), onPrimaryContainer = Color(0xFFF3DAFF),
    secondary = Color(0xFFD7BFDB), onSecondary = Color(0xFF3B2B40),
    secondaryContainer = Color(0xFF524157), onSecondaryContainer = Color(0xFFF3DBF7),
    tertiary = Color(0xFFF5B7AD), onTertiary = Color(0xFF4C251F),
    tertiaryContainer = Color(0xFF663B33), onTertiaryContainer = Color(0xFFFFDAD4),
    error = Color(0xFFF2B8B5), onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18), onErrorContainer = Color(0xFFF9DEDC),
    background = Color(0xFF1C1B1F), onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF1C1B1F), onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF4C444D), onSurfaceVariant = Color(0xFFCFC3CE),
    outline = Color(0xFF988D98), outlineVariant = Color(0xFF4C444D),
)

private fun grayLight() = lightColorScheme(
    primary = Color(0xFF607D8B), onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFCFE4EE), onPrimaryContainer = Color(0xFF0E1D24),
    secondary = Color(0xFF4F6068), onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD2E4ED), onSecondaryContainer = Color(0xFF0B1D24),
    tertiary = Color(0xFF605B7D), onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFE6DFFF), onTertiaryContainer = Color(0xFF1C1736),
    error = Color(0xFFB3261E), onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF9DEDC), onErrorContainer = Color(0xFF410E0B),
    background = Color(0xFFFFFBFE), onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFBFE), onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFDCE4E8), onSurfaceVariant = Color(0xFF40484C),
    outline = Color(0xFF70787C), outlineVariant = Color(0xFFC0C8CC),
)

private fun grayDark() = darkColorScheme(
    primary = Color(0xFFB0BEC5), onPrimary = Color(0xFF1D333B),
    primaryContainer = Color(0xFF37565F), onPrimaryContainer = Color(0xFFCFE4EE),
    secondary = Color(0xFFB6C8D1), onSecondary = Color(0xFF21333A),
    secondaryContainer = Color(0xFF374950), onSecondaryContainer = Color(0xFFD2E4ED),
    tertiary = Color(0xFFCAC3E8), onTertiary = Color(0xFF312C4C),
    tertiaryContainer = Color(0xFF484364), onTertiaryContainer = Color(0xFFE6DFFF),
    error = Color(0xFFF2B8B5), onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18), onErrorContainer = Color(0xFFF9DEDC),
    background = Color(0xFF1C1B1F), onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF1C1B1F), onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF40484C), onSurfaceVariant = Color(0xFFC0C8CC),
    outline = Color(0xFF8A9296), outlineVariant = Color(0xFF40484C),
)

private fun redLight() = lightColorScheme(
    primary = Color(0xFFE53935), onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFDAD6), onPrimaryContainer = Color(0xFF410002),
    secondary = Color(0xFF775652), onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFDAD6), onSecondaryContainer = Color(0xFF2C1513),
    tertiary = Color(0xFF755A2F), onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFDDB3), onTertiaryContainer = Color(0xFF281800),
    error = Color(0xFFB3261E), onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF9DEDC), onErrorContainer = Color(0xFF410E0B),
    background = Color(0xFFFFFBFE), onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFBFE), onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFF5DDDA), onSurfaceVariant = Color(0xFF534341),
    outline = Color(0xFF857370), outlineVariant = Color(0xFFD8C2BF),
)

private fun redDark() = darkColorScheme(
    primary = Color(0xFFFFB4AB), onPrimary = Color(0xFF690005),
    primaryContainer = Color(0xFFC62828), onPrimaryContainer = Color(0xFFFFDAD6),
    secondary = Color(0xFFE7BDB8), onSecondary = Color(0xFF442927),
    secondaryContainer = Color(0xFF5D3F3B), onSecondaryContainer = Color(0xFFFFDAD6),
    tertiary = Color(0xFFE5C18D), onTertiary = Color(0xFF412D05),
    tertiaryContainer = Color(0xFF5B431A), onTertiaryContainer = Color(0xFFFFDDB3),
    error = Color(0xFFF2B8B5), onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18), onErrorContainer = Color(0xFFF9DEDC),
    background = Color(0xFF1C1B1F), onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF1C1B1F), onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF534341), onSurfaceVariant = Color(0xFFD8C2BF),
    outline = Color(0xFFA08C8A), outlineVariant = Color(0xFF534341),
)

private fun pinkLight() = lightColorScheme(
    primary = Color(0xFFE91E63), onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFD9E2), onPrimaryContainer = Color(0xFF3E001D),
    secondary = Color(0xFF74565F), onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFD9E2), onSecondaryContainer = Color(0xFF2B151C),
    tertiary = Color(0xFF7C5635), onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFDCC2), onTertiaryContainer = Color(0xFF2E1500),
    error = Color(0xFFB3261E), onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF9DEDC), onErrorContainer = Color(0xFF410E0B),
    background = Color(0xFFFFFBFE), onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFBFE), onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFF2DDE1), onSurfaceVariant = Color(0xFF514347),
    outline = Color(0xFF837377), outlineVariant = Color(0xFFD5C2C6),
)

private fun pinkDark() = darkColorScheme(
    primary = Color(0xFFFFB1C8), onPrimary = Color(0xFF650033),
    primaryContainer = Color(0xFFC2185B), onPrimaryContainer = Color(0xFFFFD9E2),
    secondary = Color(0xFFE3BDC6), onSecondary = Color(0xFF422931),
    secondaryContainer = Color(0xFF5B3F47), onSecondaryContainer = Color(0xFFFFD9E2),
    tertiary = Color(0xFFEFBD94), onTertiary = Color(0xFF48290C),
    tertiaryContainer = Color(0xFF623F20), onTertiaryContainer = Color(0xFFFFDCC2),
    error = Color(0xFFF2B8B5), onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18), onErrorContainer = Color(0xFFF9DEDC),
    background = Color(0xFF1C1B1F), onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF1C1B1F), onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF514347), onSurfaceVariant = Color(0xFFD5C2C6),
    outline = Color(0xFF9E8C90), outlineVariant = Color(0xFF514347),
)

private fun indigoLight() = lightColorScheme(
    primary = Color(0xFF3F51B5), onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFDEE0FF), onPrimaryContainer = Color(0xFF00105C),
    secondary = Color(0xFF5C5D72), onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE1E0F9), onSecondaryContainer = Color(0xFF191A2C),
    tertiary = Color(0xFF78536B), onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFD8EE), onTertiaryContainer = Color(0xFF2E1126),
    error = Color(0xFFB3261E), onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF9DEDC), onErrorContainer = Color(0xFF410E0B),
    background = Color(0xFFFFFBFE), onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFBFE), onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFE3E1EC), onSurfaceVariant = Color(0xFF46464F),
    outline = Color(0xFF767680), outlineVariant = Color(0xFFC7C5D0),
)

private fun indigoDark() = darkColorScheme(
    primary = Color(0xFFBAC3FF), onPrimary = Color(0xFF08218A),
    primaryContainer = Color(0xFF283593), onPrimaryContainer = Color(0xFFDEE0FF),
    secondary = Color(0xFFC5C4DD), onSecondary = Color(0xFF2E2F42),
    secondaryContainer = Color(0xFF454559), onSecondaryContainer = Color(0xFFE1E0F9),
    tertiary = Color(0xFFE8B9D5), onTertiary = Color(0xFF46263C),
    tertiaryContainer = Color(0xFF5F3C53), onTertiaryContainer = Color(0xFFFFD8EE),
    error = Color(0xFFF2B8B5), onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18), onErrorContainer = Color(0xFFF9DEDC),
    background = Color(0xFF1C1B1F), onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF1C1B1F), onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF46464F), onSurfaceVariant = Color(0xFFC7C5D0),
    outline = Color(0xFF90909A), outlineVariant = Color(0xFF46464F),
)

private fun brownLight() = lightColorScheme(
    primary = Color(0xFF795548), onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFDDD1), onPrimaryContainer = Color(0xFF2E150B),
    secondary = Color(0xFF765749), onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFDDD1), onSecondaryContainer = Color(0xFF2C150C),
    tertiary = Color(0xFF636032), onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFEAE5AA), onTertiaryContainer = Color(0xFF1E1C00),
    error = Color(0xFFB3261E), onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF9DEDC), onErrorContainer = Color(0xFF410E0B),
    background = Color(0xFFFFFBFE), onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFBFE), onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFF5DED5), onSurfaceVariant = Color(0xFF53443D),
    outline = Color(0xFF85736C), outlineVariant = Color(0xFFD8C2BA),
)

private fun brownDark() = darkColorScheme(
    primary = Color(0xFFD7BCA9), onPrimary = Color(0xFF3B2317),
    primaryContainer = Color(0xFF5D4037), onPrimaryContainer = Color(0xFFFFDDD1),
    secondary = Color(0xFFE7BDB0), onSecondary = Color(0xFF442A1F),
    secondaryContainer = Color(0xFF5D4034), onSecondaryContainer = Color(0xFFFFDDD1),
    tertiary = Color(0xFFCDC990), onTertiary = Color(0xFF343209),
    tertiaryContainer = Color(0xFF4B491E), onTertiaryContainer = Color(0xFFEAE5AA),
    error = Color(0xFFF2B8B5), onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18), onErrorContainer = Color(0xFFF9DEDC),
    background = Color(0xFF1C1B1F), onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF1C1B1F), onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF53443D), onSurfaceVariant = Color(0xFFD8C2BA),
    outline = Color(0xFFA08D85), outlineVariant = Color(0xFF53443D),
)

internal fun accentColorScheme(accent: AccentColor, dark: Boolean): ColorScheme {
    return when (accent) {
        AccentColor.Blue -> if (dark) blueDark() else blueLight()
        AccentColor.Teal -> if (dark) tealDark() else tealLight()
        AccentColor.Green -> if (dark) greenDark() else greenLight()
        AccentColor.Yellow -> if (dark) yellowDark() else yellowLight()
        AccentColor.Orange -> if (dark) orangeDark() else orangeLight()
        AccentColor.Purple -> if (dark) purpleDark() else purpleLight()
        AccentColor.Gray -> if (dark) grayDark() else grayLight()
        AccentColor.Red -> if (dark) redDark() else redLight()
        AccentColor.Pink -> if (dark) pinkDark() else pinkLight()
        AccentColor.Indigo -> if (dark) indigoDark() else indigoLight()
        AccentColor.Brown -> if (dark) brownDark() else brownLight()
        AccentColor.Auto -> if (dark) DarkColorScheme else LightColorScheme
    }
}

enum class ThemeMode { System, Light, Dark }

data class GlassColors(
    val background: Color,
    val border: Color
)

val LocalGlassColors = staticCompositionLocalOf {
    GlassColors(
        background = Color.Unspecified,
        border = Color.Unspecified
    )
}

data class VaultColors(
    val cardHeading: Color
)

val LocalVaultColors = staticCompositionLocalOf {
    VaultColors(cardHeading = Color.Unspecified)
}

internal fun headingColorForAccent(accent: AccentColor, dark: Boolean): Color {
    return when (accent) {
        AccentColor.Blue -> if (dark) HeadingBlueDark else HeadingBlueLight
        AccentColor.Teal -> if (dark) HeadingTealDark else HeadingTealLight
        AccentColor.Green -> if (dark) HeadingGreenDark else HeadingGreenLight
        AccentColor.Yellow -> if (dark) HeadingYellowDark else HeadingYellowLight
        AccentColor.Orange -> if (dark) HeadingOrangeDark else HeadingOrangeLight
        AccentColor.Purple -> if (dark) HeadingPurpleDark else HeadingPurpleLight
        AccentColor.Gray -> if (dark) HeadingGrayDark else HeadingGrayLight
        AccentColor.Red -> if (dark) HeadingRedDark else HeadingRedLight
        AccentColor.Pink -> if (dark) HeadingPinkDark else HeadingPinkLight
        AccentColor.Indigo -> if (dark) HeadingIndigoDark else HeadingIndigoLight
        AccentColor.Brown -> if (dark) HeadingBrownDark else HeadingBrownLight
        AccentColor.Auto -> Color.Unspecified // resolved after colorScheme is created
    }
}

@Composable
fun VaultTheme(
    themeMode: ThemeMode = ThemeMode.System,
    accentColor: AccentColor = AccentColor.Auto,
    fontScale: Float = 1.0f,
    darkTheme: Boolean = when (themeMode) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    },
    content: @Composable () -> Unit
) {
    val colorScheme = when (accentColor) {
        AccentColor.Auto -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                accentColorScheme(AccentColor.Blue, darkTheme)
            }
        }
        else -> accentColorScheme(accentColor, darkTheme)
    }

    val view = LocalView.current
    val activity = view.context as? Activity
    if (!view.isInEditMode && activity != null) {
        SideEffect {
            val window = activity.window
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    // Swap: old app bg tones → glass cards, old glass tones → app bg
    val glassColors = if (darkTheme) {
        GlassColors(
            background = colorScheme.primary.copy(alpha = 0.06f)
                .compositeOver(Color(0xFF101018)),
            border = Color.White.copy(alpha = 0.06f)
        )
    } else {
        GlassColors(
            background = colorScheme.primary.copy(alpha = 0.04f)
                .compositeOver(Color(0xFFF4F4F8)),
            border = colorScheme.outlineVariant.copy(alpha = 0.25f)
        )
    }

    val headingColor = if (accentColor == AccentColor.Auto) {
        colorScheme.tertiary
    } else {
        headingColorForAccent(accentColor, darkTheme)
    }
    val vaultColors = VaultColors(cardHeading = headingColor)

    val typography = scaledTypography(fontScale)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
    ) {
        CompositionLocalProvider(
            LocalGlassColors provides glassColors,
            LocalVaultColors provides vaultColors
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        if (darkTheme) {
                            // Dark: frosted-surface base
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.04f)
                                        .compositeOver(Color(0xFF0E0E14)),
                                    Color.White.copy(alpha = 0.06f)
                                        .compositeOver(Color(0xFF0C0C11)),
                                    Color(0xFF0A0A0F)
                                )
                            )
                        } else {
                            // Light: bright frosted white
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.White,
                                    Color.White.copy(alpha = 0.85f)
                                        .compositeOver(Color(0xFFF8F8FC)),
                                    Color(0xFFFCFCFF)
                                )
                            )
                        }
                    )
            ) {
                content()
            }
        }
    }
}
