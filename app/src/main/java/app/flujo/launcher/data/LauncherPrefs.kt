package app.flujo.launcher.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** Ajustes del launcher. Cada cambio se guarda al instante y la interfaz se actualiza sola. */
class LauncherPrefs(context: Context) {
    private val sp = context.getSharedPreferences("flujo", Context.MODE_PRIVATE)

    var showIcons by mutableStateOf(sp.getBoolean("icons", true)); private set
    var favCount by mutableStateOf(sp.getInt("fav_count", 5)); private set
    var accent by mutableStateOf(sp.getLong("accent", ACCENTS[0])); private set
    /** 0 = automático, 1 = oscuro, 2 = claro. */
    var themeMode by mutableStateOf(sp.getInt("theme", 0)); private set
    var doubleTapLock by mutableStateOf(sp.getBoolean("double_tap", false)); private set
    var favorites by mutableStateOf(readList("favs")); private set
    var hidden by mutableStateOf(readList("hidden").toSet()); private set

    val seeded: Boolean get() = sp.getBoolean("seeded", false)

    fun setShowIcons(v: Boolean) { showIcons = v; sp.edit().putBoolean("icons", v).apply() }
    fun setFavCount(v: Int) { favCount = v.coerceIn(3, 5); sp.edit().putInt("fav_count", favCount).apply() }
    fun setAccent(v: Long) { accent = v; sp.edit().putLong("accent", v).apply() }
    fun setThemeMode(v: Int) { themeMode = v; sp.edit().putInt("theme", v).apply() }
    fun setDoubleTapLock(v: Boolean) { doubleTapLock = v; sp.edit().putBoolean("double_tap", v).apply() }

    fun seedFavorites(list: List<String>) {
        favorites = list
        sp.edit().putString("favs", list.joinToString(",")).putBoolean("seeded", true).apply()
    }

    fun toggleFavorite(pkg: String) {
        favorites = if (pkg in favorites) favorites - pkg else favorites + pkg
        sp.edit().putString("favs", favorites.joinToString(",")).apply()
    }

    fun toggleHidden(pkg: String) {
        hidden = if (pkg in hidden) hidden - pkg else hidden + pkg
        sp.edit().putString("hidden", hidden.joinToString(",")).apply()
    }

    /** Cuántas veces abriste una app, para ordenar los resultados de búsqueda. */
    fun count(pkg: String): Int = sp.getInt("n_$pkg", 0)
    fun bump(pkg: String) { sp.edit().putInt("n_$pkg", count(pkg) + 1).apply() }

    private fun readList(key: String): List<String> =
        sp.getString(key, null)?.split(",")?.filter { it.isNotBlank() } ?: emptyList()

    companion object {
        val ACCENTS = listOf(0xFFA9CCFFL, 0xFF9BE3C4L, 0xFFF2C46DL, 0xFFF29BB0L)
    }
}
