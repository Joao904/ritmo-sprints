package br.unemat.ritmo.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Forest = Color(0xFF183D35)
val Lime = Color(0xFFDAEDAC)
val Paper = Color(0xFFF6F7F2)
val Muted = Color(0xFF53635B)

@Composable
fun RitmoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Forest, onPrimary = Color.White,
            primaryContainer = Lime, onPrimaryContainer = Forest,
            secondaryContainer = Lime, onSecondaryContainer = Forest,
            background = Paper, onBackground = Forest,
            surface = Paper, onSurface = Forest,
            surfaceVariant = Color(0xFFE8ECE2), onSurfaceVariant = Muted,
            outline = Color(0xFF747F74)
        ),
        typography = Typography(
            headlineLarge = TextStyle(fontFamily = FontFamily.Serif, fontSize = 38.sp, lineHeight = 42.sp),
            headlineMedium = TextStyle(fontFamily = FontFamily.Serif, fontSize = 30.sp, lineHeight = 36.sp),
            titleLarge = TextStyle(fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.SemiBold)
        ),
        content = content
    )
}
