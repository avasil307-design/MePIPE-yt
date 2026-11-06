package app.mepipe.yt

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MePipeRoot() }
    }
}

private data class ThemePalette(
    val id: String,
    val name: String,
    val description: String,
    val swatches: List<Color>,
    val light: androidx.compose.material3.ColorScheme,
    val dark: androidx.compose.material3.ColorScheme,
)

private fun themePalette(
    id: String,
    name: String,
    description: String,
    primary: Color,
    primaryContainer: Color,
    secondary: Color,
    secondaryContainer: Color,
    tertiary: Color,
    tertiaryContainer: Color,
    background: Color,
    surfaceVariant: Color,
    darkPrimary: Color,
    darkPrimaryContainer: Color,
    darkSecondary: Color,
    darkSecondaryContainer: Color,
    darkTertiary: Color,
    darkTertiaryContainer: Color,
    darkBackground: Color,
    darkSurfaceVariant: Color,
): ThemePalette {
    val lightText = Color(0xFF191D16)
    val darkText = Color(0xFFE1E4D9)
    val light = lightColorScheme(
        primary = primary,
        onPrimary = Color.White,
        primaryContainer = primaryContainer,
        onPrimaryContainer = primary,
        secondary = secondary,
        onSecondary = Color.White,
        secondaryContainer = secondaryContainer,
        onSecondaryContainer = Color(0xFF20251D),
        tertiary = tertiary,
        onTertiary = Color.White,
        tertiaryContainer = tertiaryContainer,
        onTertiaryContainer = Color(0xFF20251D),
        background = background,
        onBackground = lightText,
        surface = background,
        onSurface = lightText,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = Color(0xFF44483F),
        surfaceTint = primary,
        inverseSurface = Color(0xFF2E322A),
        inverseOnSurface = Color(0xFFEEF2E8),
        outline = Color(0xFF74796E),
        outlineVariant = surfaceVariant,
        surfaceBright = background,
        surfaceDim = surfaceVariant,
        surfaceContainerLowest = Color.White,
        surfaceContainerLow = surfaceVariant,
        surfaceContainer = surfaceVariant,
        surfaceContainerHigh = surfaceVariant,
        surfaceContainerHighest = surfaceVariant,
    )
    val dark = darkColorScheme(
        primary = darkPrimary,
        onPrimary = Color(0xFF18220F),
        primaryContainer = darkPrimaryContainer,
        onPrimaryContainer = Color(0xFFE2F5D2),
        secondary = darkSecondary,
        onSecondary = Color(0xFF192017),
        secondaryContainer = darkSecondaryContainer,
        onSecondaryContainer = Color(0xFFE0E8D7),
        tertiary = darkTertiary,
        onTertiary = Color(0xFF10211F),
        tertiaryContainer = darkTertiaryContainer,
        onTertiaryContainer = Color(0xFFD8F0EB),
        background = darkBackground,
        onBackground = darkText,
        surface = darkBackground,
        onSurface = darkText,
        surfaceVariant = darkSurfaceVariant,
        onSurfaceVariant = Color(0xFFC4C8BC),
        surfaceTint = darkPrimary,
        inverseSurface = Color(0xFFE1E4D9),
        inverseOnSurface = Color(0xFF2E322A),
        outline = Color(0xFF8E9486),
        outlineVariant = darkSurfaceVariant,
        surfaceBright = Color(0xFF373B33),
        surfaceDim = darkBackground,
        surfaceContainerLowest = Color(0xFF0C0F0A),
        surfaceContainerLow = darkBackground,
        surfaceContainer = darkSurfaceVariant,
        surfaceContainerHigh = darkSurfaceVariant,
        surfaceContainerHighest = darkSurfaceVariant,
    )
    return ThemePalette(id, name, description, listOf(primary, secondary, tertiary, primaryContainer), light, dark)
}

private val themePalettes = listOf(
    themePalette(
        "plant", "Plant Life", "Листва, шалфей и прохладный акцент",
        Color(0xFF386A20), Color(0xFFB8F397), Color(0xFF52634A), Color(0xFFD6E8C8),
        Color(0xFF386663), Color(0xFFBCEBE6), Color(0xFFF6F8F0), Color(0xFFE1E4D9),
        Color(0xFF9FD67E), Color(0xFF285019), Color(0xFFBACBB0), Color(0xFF3C4B34),
        Color(0xFFA0CFCA), Color(0xFF1F4E4B), Color(0xFF11140F), Color(0xFF41483B),
    ),
    themePalette(
        "sage", "Шалфей", "Спокойная зелень с небесно-голубым оттенком",
        Color(0xFF486351), Color(0xFFC9E8CB), Color(0xFF59665A), Color(0xFFDDE8D9),
        Color(0xFF526477), Color(0xFFD5E3F7), Color(0xFFF5F8F3), Color(0xFFE1E8DF),
        Color(0xFFAAD3B1), Color(0xFF31503B), Color(0xFFBBCBB8), Color(0xFF3D5141),
        Color(0xFFB3CBE3), Color(0xFF374A5D), Color(0xFF101510), Color(0xFF3D463E),
    ),
    themePalette(
        "citrus", "Цитрус", "Оливковый лайм и тёплый янтарь",
        Color(0xFF626400), Color(0xFFE9EF9D), Color(0xFF665F43), Color(0xFFEAE2C5),
        Color(0xFF84532A), Color(0xFFFFDDB9), Color(0xFFFBF9EF), Color(0xFFE9E5D2),
        Color(0xFFD1D978), Color(0xFF494B00), Color(0xFFD0C6A5), Color(0xFF4E472F),
        Color(0xFFF2BB86), Color(0xFF683D16), Color(0xFF15140D), Color(0xFF484536),
    ),
    themePalette(
        "ocean", "Бирюза", "Глубокая зелень и чистая бирюза",
        Color(0xFF006A60), Color(0xFF9CF2E1), Color(0xFF4A635E), Color(0xFFD0E8E1),
        Color(0xFF436278), Color(0xFFCDE5FF), Color(0xFFF0F8F6), Color(0xFFDCE8E4),
        Color(0xFF80D5C5), Color(0xFF005047), Color(0xFFB4CCC4), Color(0xFF344B45),
        Color(0xFFB1CBE4), Color(0xFF304A60), Color(0xFF0E1514), Color(0xFF374744),
    ),
)

@Composable
private fun MePipeRoot() {
    val context = LocalContext.current
    val preferences = remember { context.getSharedPreferences("mepipe_settings", Context.MODE_PRIVATE) }
    var paletteId by remember { mutableStateOf(preferences.getString("palette", "plant") ?: "plant") }
    val palette = themePalettes.firstOrNull { it.id == paletteId } ?: themePalettes.first()

    MePipeTheme(palette) {
        MePipeApp(palette) { selected ->
            paletteId = selected.id
            preferences.edit().putString("palette", selected.id).apply()
        }
    }
}

@Composable
private fun MePipeTheme(palette: ThemePalette, content: @Composable () -> Unit) {
    val dark = androidx.compose.foundation.isSystemInDarkTheme()
    MaterialTheme(colorScheme = if (dark) palette.dark else palette.light, content = content)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MePipeApp(
    currentPalette: ThemePalette,
    onPaletteSelected: (ThemePalette) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableIntStateOf(0) }
    var query by remember { mutableStateOf("популярное") }
    var searchOpen by remember { mutableStateOf(false) }
    var results by remember { mutableStateOf<List<VideoResult>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var searchError by remember { mutableStateOf<String?>(null) }
    var activeVideo by remember { mutableStateOf<VideoResult?>(null) }
    var playbackSource by remember { mutableStateOf<PlaybackSource?>(null) }
    var isResolvingVideo by remember { mutableStateOf(false) }
    var playbackError by remember { mutableStateOf<String?>(null) }

    suspend fun searchYouTube(term: String) {
        val cleanTerm = term.trim()
        if (cleanTerm.isBlank()) {
            searchError = "Введите название видео или канала"
            return
        }
        query = cleanTerm
        searchOpen = false
        activeVideo = null
        isLoading = true
        searchError = null
        results = emptyList()
        try {
            results = withContext(Dispatchers.IO) { YouTubeRepository.search(cleanTerm) }
        } catch (exception: Exception) {
            searchError = exception.message?.takeIf { it.isNotBlank() }
                ?: "Не удалось получить результаты. Проверьте подключение и попробуйте ещё раз."
        } finally {
            isLoading = false
        }
    }

    fun openVideo(video: VideoResult) {
        activeVideo = video
        playbackSource = null
        playbackError = null
        isResolvingVideo = true
        scope.launch {
            try {
                playbackSource = withContext(Dispatchers.IO) {
                    YouTubeRepository.playbackSource(video.url)
                }
            } catch (exception: Exception) {
                playbackError = exception.message?.takeIf { it.isNotBlank() }
                    ?: "Не удалось получить видеопоток с YouTube."
            } finally {
                isResolvingVideo = false
            }
        }
    }

    LaunchedEffect(Unit) { searchYouTube("популярное") }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    if (activeVideo != null) {
                        IconButton(onClick = { activeVideo = null }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                        }
                    }
                },
                title = {
                    if (activeVideo != null) {
                        Text(activeVideo!!.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(width = 31.dp, height = 24.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp))
                            }
                            Spacer(Modifier.width(8.dp))
                            Text("MePIPE", fontWeight = FontWeight.Bold, letterSpacing = (-0.7).sp, fontSize = 21.sp)
                        }
                    }
                },
                actions = {
                    if (activeVideo == null) {
                        IconButton(onClick = { searchOpen = !searchOpen; selectedTab = 1 }) {
                            Icon(Icons.Default.Search, contentDescription = "Поиск на YouTube")
                        }
                        IconButton(onClick = { selectedTab = 2 }) {
                            Icon(Icons.Default.Person, contentDescription = "Темы и настройки")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                val tabs = listOf("Главная", "Поиск", "Вы")
                val icons = listOf(Icons.Default.Home, Icons.Default.Search, Icons.Default.Person)
                tabs.forEachIndexed { index, label ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = {
                            selectedTab = index
                            if (index == 0) {
                                activeVideo = null
                                searchOpen = false
                                scope.launch { searchYouTube("популярное") }
                            }
                            if (index == 1) {
                                activeVideo = null
                                searchOpen = true
                            }
                            if (index == 2) { activeVideo = null; searchOpen = false }
                        },
                        icon = { Icon(icons[index], contentDescription = label) },
                        label = { Text(label, maxLines = 1) },
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(bottom = 18.dp),
        ) {
            when {
                selectedTab == 2 -> item { ThemeSettings(currentPalette, onPaletteSelected) }
                activeVideo != null -> {
                    val video = activeVideo!!
                    item {
                        Column {
                            when {
                                playbackSource != null -> VideoPlayer(playbackSource!!)
                                isResolvingVideo -> LoadingPanel("Получаю видеопоток с YouTube…")
                                playbackError != null -> ErrorPanel(playbackError!!, onRetry = { openVideo(video) })
                            }
                            Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                                Text(video.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(7.dp))
                                Text(video.channel, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(video.metadata, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
                else -> {
                    if (searchOpen) {
                        item {
                            OutlinedTextField(
                                value = query,
                                onValueChange = { query = it },
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 5.dp),
                                placeholder = { Text("Искать видео на YouTube") },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                trailingIcon = {
                                    IconButton(onClick = { scope.launch { searchYouTube(query) } }) {
                                        Icon(Icons.Default.Search, contentDescription = "Начать поиск")
                                    }
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(28.dp),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(onSearch = { scope.launch { searchYouTube(query) } }),
                            )
                        }
                    }
                    if (selectedTab == 0 && !searchOpen) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp, vertical = 5.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                listOf("популярное", "музыка", "технологии", "новости", "игры", "наука").forEach { suggestion ->
                                    val selected = query.equals(suggestion, ignoreCase = true)
                                    AssistChip(
                                        onClick = { scope.launch { searchYouTube(suggestion) } },
                                        label = { Text(suggestion.replaceFirstChar { it.uppercase() }) },
                                        colors = AssistChipDefaults.assistChipColors(
                                            containerColor = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.surfaceContainerHigh,
                                            labelColor = if (selected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurface,
                                        ),
                                        border = null,
                                    )
                                }
                            }
                        }
                    }
                    item {
                        Column(Modifier.fillMaxWidth().padding(start = 18.dp, end = 12.dp, top = 17.dp, bottom = 8.dp)) {
                            Text("Результаты YouTube", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            if (query.isNotBlank()) {
                                Text("Запрос: $query", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    when {
                        isLoading -> item { LoadingPanel("Ищем видео на YouTube…") }
                        searchError != null -> item {
                            ErrorPanel(searchError!!, onRetry = { scope.launch { searchYouTube(query) } })
                        }
                        results.isEmpty() -> item {
                            Text("По этому запросу ничего не найдено", modifier = Modifier.padding(24.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        else -> itemsIndexed(results) { _, video -> VideoCard(video, onClick = { openVideo(video) }) }
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeSettings(
    currentPalette: ThemePalette,
    onPaletteSelected: (ThemePalette) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 20.dp, bottom = 28.dp)) {
        Text(
            "Вы",
            modifier = Modifier.padding(horizontal = 20.dp),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            "Настройки оформления",
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(22.dp))
        Text(
            "Цветовая тема",
            modifier = Modifier.padding(horizontal = 20.dp),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            "Выберите сочетание цветов Material 3",
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 5.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(10.dp))
        themePalettes.forEach { palette ->
            val selected = palette.id == currentPalette.id
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 5.dp)
                    .clickable { onPaletteSelected(palette) },
                shape = RoundedCornerShape(22.dp),
                color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                tonalElevation = if (selected) 2.dp else 0.dp,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 15.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            palette.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            palette.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                            palette.swatches.forEach { swatch ->
                                Surface(modifier = Modifier.size(20.dp), shape = CircleShape, color = swatch) { }
                            }
                        }
                    }
                    if (selected) {
                        Text("✓", modifier = Modifier.padding(start = 12.dp), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
        }
        Text(
            "Выбор сохраняется на устройстве и применяется в светлой и тёмной теме.",
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun VideoCard(video: VideoResult, onClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(bottom = 18.dp)) {
        Box(
            modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
        ) {
            if (video.thumbnailUrl != null) {
                AsyncImage(
                    model = video.thumbnailUrl,
                    contentDescription = "Превью: ${video.title}",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }
            if (video.duration.isNotBlank()) {
                Surface(
                    modifier = Modifier.align(Alignment.BottomEnd).padding(9.dp),
                    color = Color.Black.copy(alpha = 0.82f),
                    shape = RoundedCornerShape(5.dp),
                ) {
                    Text(video.duration, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp), color = Color.White, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth().padding(start = 12.dp, end = 4.dp, top = 10.dp), verticalAlignment = Alignment.Top) {
            Surface(modifier = Modifier.size(40.dp), shape = CircleShape, color = MaterialTheme.colorScheme.tertiaryContainer) {
                Box(contentAlignment = Alignment.Center) {
                    Text(video.channel.take(1).uppercase(), color = MaterialTheme.colorScheme.onTertiaryContainer, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }
            }
            Column(modifier = Modifier.weight(1f).padding(start = 11.dp, end = 6.dp)) {
                Text(video.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(4.dp))
                Text("${video.channel} · ${video.metadata}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Icon(Icons.Default.PlayCircleOutline, contentDescription = "Смотреть", modifier = Modifier.padding(9.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun LoadingPanel(message: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CircularProgressIndicator()
        Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ErrorPanel(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text("Не удалось загрузить YouTube", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        TextButton(onClick = onRetry) { Text("Повторить") }
    }
}

@Composable
private fun VideoPlayer(source: PlaybackSource) {
    val context = LocalContext.current
    val player = remember(context) {
        val httpFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 Chrome/130.0.0.0 Mobile Safari/537.36")
            .setDefaultRequestProperties(mapOf("Referer" to "https://www.youtube.com/"))
        val dataSourceFactory = DefaultDataSource.Factory(context, httpFactory)
        val mediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory)
        ExoPlayer.Builder(context).setMediaSourceFactory(mediaSourceFactory).build()
    }

    DisposableEffect(player) {
        onDispose { player.release() }
    }
    LaunchedEffect(source) {
        val mediaItem = MediaItem.Builder().setUri(source.url).apply {
            source.mimeType?.let(::setMimeType)
        }.build()
        player.setMediaItem(mediaItem)
        player.prepare()
        player.playWhenReady = true
    }
    val exoPlayer = player
    AndroidView(
        modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f)
            .background(Color.Black),
        factory = { viewContext ->
            PlayerView(viewContext).apply {
                useController = true
                this.player = exoPlayer
            }
        },
        update = { it.player = exoPlayer },
    )
}
