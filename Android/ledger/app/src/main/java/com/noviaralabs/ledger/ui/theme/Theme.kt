package com.noviaralabs.ledger.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Charcoal = Color(0xFF1E1E1C)   // primary text, nav, buttons, dark surfaces
val Sand = Color(0xFFD8CBB3)       // accents, selected states, highlights
val Cream = Color(0xFFF3EEE3)      // app background
val WarmWhite = Color(0xFFFFFDFA)  // cards / transaction surfaces
val Taupe = Color(0xFF777168)      // secondary text, labels, metadata

val CreditGreen = Color(0xFF2E7D32)
val DebitRed = Color(0xFFC62828)

private val scheme = lightColorScheme(
    primary = Charcoal,
    onPrimary = Cream,
    primaryContainer = Sand,
    onPrimaryContainer = Charcoal,
    secondary = Sand,
    onSecondary = Charcoal,
    secondaryContainer = Sand,
    onSecondaryContainer = Charcoal,
    background = Cream,
    onBackground = Charcoal,
    surface = WarmWhite,
    onSurface = Charcoal,
    surfaceVariant = Cream,
    onSurfaceVariant = Taupe,
    outline = Taupe,
    outlineVariant = Sand,
    surfaceContainerLowest = WarmWhite,
    surfaceContainerLow = WarmWhite,
    surfaceContainer = WarmWhite,
    surfaceContainerHigh = WarmWhite,
    surfaceContainerHighest = Cream,
    error = DebitRed
)

@Composable
fun LedgerTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = scheme, content = content)
}
