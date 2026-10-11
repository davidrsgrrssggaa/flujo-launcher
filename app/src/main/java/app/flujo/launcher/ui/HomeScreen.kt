package app.flujo.launcher.ui

import android.text.format.DateFormat
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.flujo.launcher.data.AppEntry
import app.flujo.launcher.data.AppRepository
import app.flujo.launcher.data.LauncherPrefs
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    apps: List<AppEntry>,
    prefs: LauncherPrefs,
    repo: AppRepository,
    onLaunch: (AppEntry) -> Unit,
    onOpenDrawer: (Boolean) -> Unit,
    onOpenSettings: () -> Unit,
    onLock: () -> Unit,
) {
    val c = LocalFlujo.current
    val byPackage = remember(apps) { apps.associateBy { it.packageName } }
    val favorites = prefs.favorites.mapNotNull { byPackage[it] }.take(prefs.favCount)

    Column(
        Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                var total = 0f
                detectVerticalDragGestures(
                    onDragStart = { total = 0f },
                    onDragEnd = { if (total < -140f) onOpenDrawer(false) },
                    onVerticalDrag = { _, delta -> total += delta },
                )
            }
            .pointerInput(prefs.doubleTapLock) {
                detectTapGestures(
                    onDoubleTap = { if (prefs.doubleTapLock) onLock() },
                    onLongPress = { onOpenSettings() },
                )
            }
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(horizontal = 24.dp),
    ) {
        Spacer(Modifier.height(40.dp))
        Clock()
        Spacer(Modifier.weight(1f))

        if (favorites.isEmpty()) {
            Text(
                "Mantén pulsada una app en el cajón para fijarla aquí.",
                color = c.dim,
                fontSize = 14.sp,
            )
        }
        favorites.forEach { app -> FavoriteRow(app, prefs, repo, onLaunch) }

        Spacer(Modifier.height(20.dp))
        SearchPill { onOpenDrawer(true) }
        Text(
            "↑  TODAS LAS APPS",
            color = c.dim,
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            letterSpacing = 1.5.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        )
    }
}

@Composable
private fun Clock() {
    val c = LocalFlujo.current
    val context = LocalContext.current
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = LocalDateTime.now()
            delay(60_000L - System.currentTimeMillis() % 60_000L)
        }
    }
    val pattern = if (DateFormat.is24HourFormat(context)) "HH:mm" else "h:mm"
    val time = now.format(DateTimeFormatter.ofPattern(pattern))
    val date = now.format(DateTimeFormatter.ofPattern("EEEE · d MMM", Locale.getDefault())).uppercase()

    Column {
        Text(
            time,
            color = c.fg,
            fontSize = 84.sp,
            lineHeight = 84.sp,
            fontWeight = FontWeight.ExtraLight,
            letterSpacing = (-3).sp,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            date,
            color = c.dim,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            letterSpacing = 1.sp,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FavoriteRow(
    app: AppEntry,
    prefs: LauncherPrefs,
    repo: AppRepository,
    onLaunch: (AppEntry) -> Unit,
) {
    val c = LocalFlujo.current
    var menu by remember { mutableStateOf(false) }
    Column {
        Row(
            Modifier
                .fillMaxWidth()
                .combinedClickable(onClick = { onLaunch(app) }, onLongClick = { menu = true })
                .padding(vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (prefs.showIcons) {
                AppIcon(app, 26.dp)
                Spacer(Modifier.width(14.dp))
            }
            Text(
                app.label,
                color = c.fg,
                fontSize = 30.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = (-0.3).sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
            DropdownMenuItem(
                text = { Text("Quitar de favoritas") },
                onClick = { menu = false; prefs.toggleFavorite(app.packageName) },
            )
            DropdownMenuItem(
                text = { Text("Información") },
                onClick = { menu = false; repo.showInfo(app.packageName) },
            )
        }
    }
}
