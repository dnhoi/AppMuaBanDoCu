package com.example.appmuabandocu.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Blue_Brand_Light,
    onPrimary = Color.White,
    secondary = Coral_Orange,
    onSecondary = Color.White,
    tertiary = Blue_Navy,
    background = Color(0xFF0F172A),
    surface = Color(0xFF1E293B)
)

private val LightColorScheme = lightColorScheme(
    primary = Blue_text,           // 30% Xanh dương Navy (#1E3A8A)
    onPrimary = Color.White,
    secondary = Coral_Orange,      // 10% Cam san hô (#FF5722)
    onSecondary = Color.White,
    tertiary = Blue_Brand_Light,   // #0D6EFD
    background = Background_Light, // 60% Trắng tinh (#FFFFFF)
    surface = Color.White,         // Trắng tinh (#FFFFFF)
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Surface_Light
)

@Composable
fun AppMuaBanDoCuTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Tắt dynamicColor mặc định để giữ trọn vẹn bộ nhận diện màu 60-30-10
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}