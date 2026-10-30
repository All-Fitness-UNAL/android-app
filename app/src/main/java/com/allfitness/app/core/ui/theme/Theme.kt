package com.allfitness.app.core.ui.theme

import android.provider.Settings
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect

object Spacing {
    val hairline = 1.dp
    val lineWidth = 3.dp
    val xs = 4.dp
    val s = 8.dp
    val sm = 12.dp
    val stationSize = 12.dp
    val m = 16.dp
    val stationToday = 16.dp
    val l = 24.dp
    val xl = 32.dp
    val xxl = 48.dp
    val routeRowHeight = 56.dp
    val imageSize = 64.dp
    val chartHeight = 180.dp
    val chartPoint = 8.dp
    val contentMaxWidth = 640.dp
}

object AppTheme {
    val colors: AppColors
        @Composable @ReadOnlyComposable get() = LocalAppColors.current
    val reducedMotion: Boolean
        @Composable @ReadOnlyComposable get() = LocalReducedMotion.current
}

@Composable
fun AllFitnessTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val resolver = LocalContext.current.contentResolver
    fun readReduced() =
        isReducedMotion(Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f))
    var reduced by remember { mutableStateOf(readReduced()) }
    // El ajuste se cambia fuera de la app: se vuelve a leer al volver a ella.
    LifecycleResumeEffect(resolver) {
        reduced = readReduced()
        onPauseOrDispose {}
    }
    CompositionLocalProvider(
        LocalAppColors provides if (dark) DarkAppColors else LightAppColors,
        LocalReducedMotion provides reduced,
    ) {
        MaterialTheme(
            colorScheme = if (dark) DarkColors else LightColors,
            typography = AppTypography,
            shapes = AppShapes,
            content = content,
        )
    }
}
