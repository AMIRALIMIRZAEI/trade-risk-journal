package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
  primary = IndigoPrimary,
  onPrimary = SurfaceWhite,
  primaryContainer = IndigoLightBg,
  onPrimaryContainer = IndigoDark,
  secondary = EmeraldGreen,
  onSecondary = SurfaceWhite,
  secondaryContainer = EmeraldGreenBg,
  onSecondaryContainer = EmeraldGreenDark,
  tertiary = CrimsonRed,
  onTertiary = SurfaceWhite,
  tertiaryContainer = CrimsonRedBg,
  onTertiaryContainer = CrimsonRedDark,
  background = LightBg,
  onBackground = TextPrimary,
  surface = SurfaceWhite,
  onSurface = TextPrimary,
  surfaceVariant = SurfaceSubtle,
  onSurfaceVariant = TextSecondary,
  outline = BorderSubtle,
  error = CrimsonRed,
  onError = SurfaceWhite
)

@Composable
fun MyApplicationTheme(
  content: @Composable () -> Unit
) {
  val colorScheme = LightColorScheme
  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as? Activity)?.window
      if (window != null) {
        window.statusBarColor = LightBg.toArgb()
        window.navigationBarColor = LightBg.toArgb()
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = true
      }
    }
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
