package app.flujo.launcher.ui

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.flujo.launcher.LockService
import app.flujo.launcher.data.LauncherPrefs

@Composable
fun SettingsScreen(prefs: LauncherPrefs, onClose: () -> Unit) {
    val c = LocalFlujo.current
    val context = LocalContext.current

    Column(
        Modifier
            .fillMaxSize()
            .background(c.bg)
            .windowInsetsPadding(WindowInsets.systemBars)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
    ) {
        Spacer(Modifier.height(24.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Ajustes",
                color = c.fg,
                fontSize = 30.sp,
                fontWeight = FontWeight.Light,
                modifier = Modifier.weight(1f),
            )
            Text(
                "CERRAR",
                color = c.accent,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                letterSpacing = 1.sp,
                modifier = Modifier.clickable(onClick = onClose).padding(8.dp),
            )
        }
        Spacer(Modifier.height(12.dp))

        SettingRow("Iconos", "Junto al nombre") {
            FlujoSwitch(prefs.showIcons) { prefs.setShowIcons(it) }
        }
        SettingRow("Favoritas", null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StepButton("−") { prefs.setFavCount(prefs.favCount - 1) }
                Text(
                    "${prefs.favCount}",
                    color = c.fg,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 12.dp),
                )
                StepButton("+") { prefs.setFavCount(prefs.favCount + 1) }
            }
        }
        SettingRow("Acento", null) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                LauncherPrefs.ACCENTS.forEach { value ->
                    val selected = prefs.accent == value
                    Box(
                        Modifier
                            .size(24.dp)
                            .border(2.dp, if (selected) c.fg else Color.Transparent, CircleShape)
                            .padding(3.dp)
                            .clip(CircleShape)
                            .background(Color(value))
                            .clickable { prefs.setAccent(value) },
                    )
                }
            }
        }
        SettingRow("Tema", null) {
            val shape = RoundedCornerShape(10.dp)
            Row(
                Modifier
                    .clip(shape)
                    .background(c.card)
                    .border(1.dp, c.line, shape)
                    .padding(2.dp),
            ) {
                listOf("Auto", "Oscuro", "Claro").forEachIndexed { index, label ->
                    Text(
                        label,
                        color = if (prefs.themeMode == index) c.fg else c.dim,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (prefs.themeMode == index) c.line else Color.Transparent)
                            .clickable { prefs.setThemeMode(index) }
                            .padding(horizontal = 9.dp, vertical = 6.dp),
                    )
                }
            }
        }
        SettingRow("Doble toque", "Bloquear pantalla") {
            FlujoSwitch(prefs.doubleTapLock) { on ->
                prefs.setDoubleTapLock(on)
                if (on && LockService.instance == null) {
                    context.startActivity(
                        Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                    )
                }
            }
        }
        SettingRow("Apps ocultas", "Solo se abren buscando") {
            Text("${prefs.hidden.size}", color = c.fg, fontFamily = FontFamily.Monospace, fontSize = 14.sp)
        }
        SettingRow(
            "Launcher predeterminado",
            "Abre los ajustes de Android",
            onClick = {
                runCatching {
                    context.startActivity(
                        Intent(Settings.ACTION_HOME_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                    )
                }
            },
        ) {
            Text("→", color = c.dim, fontSize = 16.sp)
        }
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun SettingRow(
    title: String,
    subtitle: String?,
    onClick: (() -> Unit)? = null,
    trailing: @Composable () -> Unit,
) {
    val c = LocalFlujo.current
    Box(Modifier.fillMaxWidth().height(1.dp).background(c.line))
    Row(
        Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = c.fg, fontSize = 16.sp)
            if (subtitle != null) Text(subtitle, color = c.dim, fontSize = 12.sp)
        }
        Spacer(Modifier.width(12.dp))
        trailing()
    }
}

@Composable
private fun StepButton(label: String, onClick: () -> Unit) {
    val c = LocalFlujo.current
    val shape = RoundedCornerShape(8.dp)
    Box(
        Modifier
            .size(28.dp)
            .clip(shape)
            .border(1.dp, c.line, shape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = c.fg, fontFamily = FontFamily.Monospace, fontSize = 14.sp)
    }
}
