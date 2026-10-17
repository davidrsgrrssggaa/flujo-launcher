package app.flujo.launcher.data

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import java.text.Normalizer
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

@Immutable
data class AppEntry(
    val label: String,
    /** Etiqueta ya sin acentos y en minúsculas, calculada una sola vez para buscar y ordenar. */
    val normLabel: String,
    val packageName: String,
    val activityName: String,
    val icon: ImageBitmap? = null,
)

/** Minúsculas y sin acentos, para buscar "camara" y encontrar "Cámara". */
fun normalize(s: String): String =
    Normalizer.normalize(s, Normalizer.Form.NFD).replace(MARKS, "").lowercase()

private val MARKS = Regex("\\p{Mn}+")

class AppRepository(private val context: Context) {
    private val pm: PackageManager = context.packageManager

    /** Lista rápida: solo nombres, sin iconos. Permite pintar la pantalla enseguida. */
    @Suppress("DEPRECATION")
    fun load(): List<AppEntry> {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return pm.queryIntentActivities(intent, 0)
            .filter { it.activityInfo.packageName != context.packageName }
            .map {
                val label = it.loadLabel(pm).toString()
                AppEntry(
                    label = label,
                    normLabel = normalize(label),
                    packageName = it.activityInfo.packageName,
                    activityName = it.activityInfo.name,
                )
            }
            .distinctBy { it.packageName + "/" + it.activityName }
            .sortedBy { it.normLabel }
    }

    /** Segunda fase: dibuja los iconos una sola vez, en paralelo y fuera del hilo principal. */
    suspend fun withIcons(apps: List<AppEntry>): List<AppEntry> = coroutineScope {
        apps.map { app ->
            async {
                val bitmap = runCatching {
                    pm.getActivityIcon(ComponentName(app.packageName, app.activityName))
                        .toBitmap(ICON_PX, ICON_PX)
                        .asImageBitmap()
                }.getOrNull()
                app.copy(icon = bitmap)
            }
        }.awaitAll()
    }

    fun launch(app: AppEntry) {
        val intent = Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_LAUNCHER)
            .setClassName(app.packageName, app.activityName)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        runCatching { context.startActivity(intent) }
    }

    fun showInfo(packageName: String) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(intent) }
    }

    fun uninstall(packageName: String) {
        val intent = Intent(Intent.ACTION_DELETE, Uri.parse("package:$packageName"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(intent) }
    }

    /** Teléfono, mensajes, cámara, música y mapas según las apps predeterminadas del móvil. */
    @Suppress("DEPRECATION")
    fun defaultFavorites(installed: List<AppEntry>): List<String> {
        val known = installed.map { it.packageName }.toSet()
        val probes = listOf(
            Intent(Intent.ACTION_DIAL),
            Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_MESSAGING),
            Intent(MediaStore.ACTION_IMAGE_CAPTURE),
            Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_MUSIC),
            Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_MAPS),
        )
        return probes
            .mapNotNull { pm.resolveActivity(it, PackageManager.MATCH_DEFAULT_ONLY)?.activityInfo?.packageName }
            .filter { it in known }
            .distinct()
    }

    private companion object {
        const val ICON_PX = 96
    }
}
