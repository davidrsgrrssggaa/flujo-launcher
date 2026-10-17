package app.flujo.launcher.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.flujo.launcher.data.AppEntry
import app.flujo.launcher.data.AppRepository
import app.flujo.launcher.data.LauncherPrefs
import app.flujo.launcher.data.normalize
import kotlinx.coroutines.launch

sealed interface DrawerRow {
    data class Header(val letter: Char) : DrawerRow
    data class Item(val app: AppEntry) : DrawerRow
}

private fun letterOf(normLabel: String): Char {
    val ch = normLabel.firstOrNull() ?: return '#'
    return if (ch in 'a'..'z') ch.uppercaseChar() else '#'
}

@Composable
fun DrawerScreen(
    apps: List<AppEntry>,
    prefs: LauncherPrefs,
    repo: AppRepository,
    focusSearch: Boolean,
    onLaunch: (AppEntry) -> Unit,
) {
    val c = LocalFlujo.current
    var query by remember { mutableStateOf("") }
    val term = normalize(query.trim())

    val rows: List<DrawerRow> = remember(apps, term, prefs.hidden) {
        if (term.isEmpty()) {
            val out = mutableListOf<DrawerRow>()
            var last = ' '
            apps.filter { it.packageName !in prefs.hidden }.forEach { app ->
                val letter = letterOf(app.normLabel)
                if (letter != last) {
                    out += DrawerRow.Header(letter)
                    last = letter
                }
                out += DrawerRow.Item(app)
            }
            out
        } else {
            val matches = apps.filter { it.normLabel.contains(term) }
            // Las veces abierta se leen una sola vez por búsqueda, no en cada comparación.
            val counts = matches.associate { it.packageName to prefs.count(it.packageName) }
            matches
                .sortedWith(
                    compareByDescending<AppEntry> { it.normLabel.startsWith(term) }
                        .thenByDescending { counts[it.packageName] ?: 0 }
                        .thenBy { it.normLabel },
                )
                .map { DrawerRow.Item(it) }
        }
    }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { if (focusSearch) focus.requestFocus() }

    val headerIndex = remember(rows) {
        rows.withIndex()
            .filter { it.value is DrawerRow.Header }
            .associate { (it.value as DrawerRow.Header).letter to it.index }
    }
    val current by remember(rows) {
        derivedStateOf {
            when (val r = rows.getOrNull(listState.firstVisibleItemIndex)) {
                is DrawerRow.Header -> r.letter
                is DrawerRow.Item -> letterOf(r.app.normLabel)
                null -> null
            }
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(c.bg)
            .windowInsetsPadding(WindowInsets.systemBars)
            .imePadding(),
    ) {
        val pill = RoundedCornerShape(24.dp)
        Row(
            Modifier
                .padding(horizontal = 20.dp, vertical = 14.dp)
                .fillMaxWidth()
                .height(48.dp)
                .clip(pill)
                .background(c.card)
                .border(1.dp, c.line, pill)
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SearchGlyph(c.dim)
            Spacer(Modifier.width(10.dp))
            BasicTextField(
                value = query,
                onValueChange = { query = it },
                singleLine = true,
                textStyle = TextStyle(color = c.fg, fontSize = 16.sp),
                cursorBrush = SolidColor(c.accent),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                keyboardActions = KeyboardActions(
                    onGo = {
                        rows.filterIsInstance<DrawerRow.Item>().singleOrNull()?.let { onLaunch(it.app) }
                    },
                ),
                modifier = Modifier.weight(1f).focusRequester(focus),
                decorationBox = { inner ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (query.isEmpty()) {
                            Text("Buscar en ${apps.size} apps", color = c.dim, fontSize = 16.sp)
                        }
                        inner()
                    }
                },
            )
            if (query.isNotEmpty()) {
                Text(
                    "✕",
                    color = c.dim,
                    modifier = Modifier.clickable { query = "" }.padding(start = 8.dp),
                )
            }
        }

        Row(Modifier.fillMaxWidth().weight(1f)) {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).padding(start = 24.dp, end = if (term.isEmpty()) 0.dp else 24.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                if (rows.isEmpty()) {
                    item(key = "empty") {
                        Text(
                            "Sin resultados para “$query”.",
                            color = c.dim,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 20.dp),
                        )
                    }
                }
                items(
                    rows,
                    key = {
                        when (it) {
                            is DrawerRow.Header -> "h${it.letter}"
                            is DrawerRow.Item -> it.app.packageName + "/" + it.app.activityName
                        }
                    },
                ) { row ->
                    when (row) {
                        is DrawerRow.Header -> Text(
                            row.letter.toString(),
                            color = c.accent,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            letterSpacing = 1.5.sp,
                            modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
                        )
                        is DrawerRow.Item -> AppRow(row.app, prefs, repo, term.isNotEmpty(), onLaunch)
                    }
                }
            }
            if (term.isEmpty()) {
                Scrubber(headerIndex.keys.sorted(), current) { letter ->
                    headerIndex[letter]?.let { index -> scope.launch { listState.scrollToItem(index) } }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppRow(
    app: AppEntry,
    prefs: LauncherPrefs,
    repo: AppRepository,
    searching: Boolean,
    onLaunch: (AppEntry) -> Unit,
) {
    val c = LocalFlujo.current
    var menu by remember { mutableStateOf(false) }
    val isHidden = app.packageName in prefs.hidden
    val isFavorite = app.packageName in prefs.favorites

    Column {
        Row(
            Modifier
                .fillMaxWidth()
                .combinedClickable(onClick = { onLaunch(app) }, onLongClick = { menu = true })
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (prefs.showIcons) {
                AppIcon(app, 28.dp)
                Spacer(Modifier.width(14.dp))
            }
            Text(
                app.label,
                color = c.fg,
                fontSize = 22.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = (-0.2).sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )
            if (isHidden && searching) {
                Text(
                    "  oculta",
                    color = c.dim,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                )
            }
        }
        DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
            DropdownMenuItem(
                text = { Text(if (isFavorite) "Quitar de favoritas" else "Fijar como favorita") },
                onClick = { menu = false; prefs.toggleFavorite(app.packageName) },
            )
            DropdownMenuItem(
                text = { Text(if (isHidden) "Mostrar en la lista" else "Ocultar") },
                onClick = { menu = false; prefs.toggleHidden(app.packageName) },
            )
            DropdownMenuItem(
                text = { Text("Información") },
                onClick = { menu = false; repo.showInfo(app.packageName) },
            )
            DropdownMenuItem(
                text = { Text("Desinstalar") },
                onClick = { menu = false; repo.uninstall(app.packageName) },
            )
        }
    }
}

@Composable
private fun Scrubber(letters: List<Char>, current: Char?, onPick: (Char) -> Unit) {
    val c = LocalFlujo.current
    var height by remember { mutableIntStateOf(1) }

    fun pick(y: Float) {
        if (letters.isEmpty()) return
        val index = (y / height * letters.size).toInt().coerceIn(0, letters.lastIndex)
        onPick(letters[index])
    }

    Column(
        Modifier
            .width(36.dp)
            .fillMaxHeight()
            .padding(vertical = 8.dp)
            .onSizeChanged { height = it.height.coerceAtLeast(1) }
            .pointerInput(letters) { detectTapGestures { offset -> pick(offset.y) } }
            .pointerInput(letters) {
                detectVerticalDragGestures(
                    onDragStart = { offset -> pick(offset.y) },
                    onVerticalDrag = { change, _ -> pick(change.position.y) },
                )
            },
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        letters.forEach { letter ->
            val selected = letter == current
            Text(
                letter.toString(),
                color = if (selected) c.bg else c.dim,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                modifier = Modifier
                    .background(if (selected) c.accent else Color.Transparent, RoundedCornerShape(4.dp))
                    .padding(horizontal = 5.dp),
            )
        }
    }
}
