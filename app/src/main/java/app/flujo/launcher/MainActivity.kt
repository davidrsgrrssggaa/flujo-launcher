package app.flujo.launcher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import app.flujo.launcher.data.AppEntry
import app.flujo.launcher.data.AppRepository
import app.flujo.launcher.data.LauncherPrefs
import app.flujo.launcher.ui.FlujoApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private lateinit var prefs: LauncherPrefs
    private lateinit var repo: AppRepository
    private var apps by mutableStateOf<List<AppEntry>>(emptyList())
    private var homeSignal by mutableIntStateOf(0)

    private val packageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) = reload()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        prefs = LauncherPrefs(this)
        repo = AppRepository(this)
        reload()

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addDataScheme("package")
        }
        ContextCompat.registerReceiver(this, packageReceiver, filter, ContextCompat.RECEIVER_EXPORTED)

        setContent { FlujoApp(apps = apps, prefs = prefs, repo = repo, homeSignal = homeSignal) }
    }

    /** Pulsar el botón de inicio estando ya en el launcher vuelve a la pantalla principal. */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        homeSignal++
    }

    override fun onDestroy() {
        unregisterReceiver(packageReceiver)
        super.onDestroy()
    }

    private fun reload() {
        lifecycleScope.launch {
            // Fase 1: nombres, para que la pantalla aparezca enseguida.
            val list = withContext(Dispatchers.Default) { repo.load() }
            apps = list
            if (!prefs.seeded && list.isNotEmpty()) {
                prefs.seedFavorites(repo.defaultFavorites(list))
            }
            // Fase 2: iconos, dibujados una vez y en paralelo.
            apps = withContext(Dispatchers.Default) { repo.withIcons(list) }
        }
    }
}
