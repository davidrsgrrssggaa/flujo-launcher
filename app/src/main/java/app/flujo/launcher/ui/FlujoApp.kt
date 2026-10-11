package app.flujo.launcher.ui

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import app.flujo.launcher.LockService
import app.flujo.launcher.data.AppEntry
import app.flujo.launcher.data.AppRepository
import app.flujo.launcher.data.LauncherPrefs

enum class Screen { Home, Drawer, Settings }

@Composable
fun FlujoApp(apps: List<AppEntry>, prefs: LauncherPrefs, repo: AppRepository, homeSignal: Int) {
    val dark = when (prefs.themeMode) {
        1 -> true
        2 -> false
        else -> isSystemInDarkTheme()
    }
    val colors = flujoColors(dark, Color(prefs.accent))
    val context = LocalContext.current
    val view = LocalView.current

    SideEffect {
        val window = (view.context as Activity).window
        val controller = WindowCompat.getInsetsController(window, view)
        controller.isAppearanceLightStatusBars = !dark
        controller.isAppearanceLightNavigationBars = !dark
    }

    var screen by remember { mutableStateOf(Screen.Home) }
    var focusSearch by remember { mutableStateOf(false) }

    LaunchedEffect(homeSignal) { screen = Screen.Home }
    BackHandler(enabled = screen != Screen.Home) { screen = Screen.Home }

    val launch: (AppEntry) -> Unit = { app ->
        prefs.bump(app.packageName)
        repo.launch(app)
        screen = Screen.Home
    }
    val lock: () -> Unit = {
        if (!LockService.lock()) {
            Toast.makeText(
                context,
                "Activa Flujo en Accesibilidad para bloquear con doble toque",
                Toast.LENGTH_LONG,
            ).show()
        }
    }

    CompositionLocalProvider(LocalFlujo provides colors) {
        Box(Modifier.fillMaxSize().background(colors.bg)) {
            HomeScreen(
                apps = apps,
                prefs = prefs,
                repo = repo,
                onLaunch = launch,
                onOpenDrawer = { withKeyboard -> focusSearch = withKeyboard; screen = Screen.Drawer },
                onOpenSettings = { screen = Screen.Settings },
                onLock = lock,
            )
            AnimatedVisibility(
                visible = screen == Screen.Drawer,
                enter = slideInVertically { it / 3 } + fadeIn(),
                exit = slideOutVertically { it / 3 } + fadeOut(),
            ) {
                DrawerScreen(apps = apps, prefs = prefs, repo = repo, focusSearch = focusSearch, onLaunch = launch)
            }
            AnimatedVisibility(
                visible = screen == Screen.Settings,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                SettingsScreen(prefs = prefs, onClose = { screen = Screen.Home })
            }
        }
    }
}
