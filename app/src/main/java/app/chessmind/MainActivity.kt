package app.chessmind

import android.os.Bundle
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.Manifest
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.CenterFocusStrong
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.FlipCameraAndroid
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.automirrored.rounded.Redo
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.ViewInAr
import androidx.compose.material.icons.rounded.GridOn
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.AllInclusive
import androidx.compose.material.icons.rounded.Handshake
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.net.toUri
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import app.chessmind.domain.engine.AnalysisLevel
import app.chessmind.domain.model.Fen
import app.chessmind.domain.model.Piece
import app.chessmind.domain.model.PieceType
import app.chessmind.domain.model.Position
import app.chessmind.domain.model.Side
import app.chessmind.domain.model.Square
import app.chessmind.domain.model.ChessRules
import app.chessmind.domain.model.Move

private val LightInk = Color(0xFF102E4F)
private val SkyBackground = Color(0xFFE8F4FF)
private val NightBackground = Color(0xFF0D1828)
private val NightSurface = Color(0xFF17263A)
private val NightSurfaceHigh = Color(0xFF20344C)
private val BrandBlue = Color(0xFF356DFF)
private val Sunny = Color(0xFFFFC85A)
private val Coral = Color(0xFFFF8D78)
private val Purple = Color(0xFF9275E8)
private val BoardLight = Color(0xFFF4D9BC)
private val BoardDark = Color(0xFFC88F6A)
private val SpaceGrotesk = FontFamily(Font(R.font.space_grotesk))
private val JetBrainsMono = FontFamily(Font(R.font.jetbrains_mono))

private val Ink: Color @Composable get() = MaterialTheme.colorScheme.onBackground
private val Espresso: Color @Composable get() = MaterialTheme.colorScheme.onBackground
private val SurfaceDark: Color @Composable get() = MaterialTheme.colorScheme.surface
private val SurfaceLight: Color @Composable get() = MaterialTheme.colorScheme.surfaceVariant
private val Accent: Color @Composable get() = MaterialTheme.colorScheme.primary
private val Muted: Color @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant

private data class LeagueLook(val name: String, val minRating: Int, val art: Int, val color: Color)
private val LeagueLooks = listOf(
    LeagueLook("Iron", 0, R.drawable.league_iron, Color(0xFF66717E)),
    LeagueLook("Silver", 600, R.drawable.league_silver, Color(0xFF84A9C9)),
    LeagueLook("Gold", 800, R.drawable.league_gold, Color(0xFFF2B83D)),
    LeagueLook("Platinum", 1000, R.drawable.league_platinum, Color(0xFF55C8DB)),
    LeagueLook("Diamond", 1300, R.drawable.league_diamond, Color(0xFF5689FF)),
    LeagueLook("Master", 1600, R.drawable.league_master, Color(0xFF8D63E8)),
    LeagueLook("Grandmaster", 2000, R.drawable.league_grandmaster, Color(0xFFE25A4B)),
)
private fun leagueFor(rating: Int) = LeagueLooks.last { rating >= it.minRating }
private fun nextLeague(rating: Int) = LeagueLooks.firstOrNull { it.minRating > rating }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { ChessMindRoot() }
    }
}

@Composable
private fun ChessMindRoot(vm: MainViewModel = viewModel()) {
    val darkMode = vm.state.settings.darkMode
    val context = LocalContext.current
    SideEffect {
        val window = (context as MainActivity).window
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = !darkMode
            isAppearanceLightNavigationBars = !darkMode
        }
    }
    ChessMindTheme(darkMode) {
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onBackground) {
            ChessMindApp(vm)
        }
    }
}

@Composable
private fun ChessMindTheme(darkMode: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkMode) darkColorScheme(
            primary = Color(0xFF82A8FF),
            background = NightBackground,
            surface = NightSurface,
            surfaceVariant = NightSurfaceHigh,
            onPrimary = NightBackground,
            secondary = Coral,
            tertiary = Sunny,
            onBackground = Color(0xFFF2F7FF),
            onSurface = Color(0xFFF2F7FF),
            onSurfaceVariant = Color(0xFFAFC1D6),
        ) else lightColorScheme(
            primary = BrandBlue,
            background = SkyBackground,
            surface = Color(0xFFF8FBFF),
            onPrimary = Color.White,
            secondary = Coral,
            tertiary = Sunny,
            surfaceVariant = Color.White,
            onBackground = LightInk,
            onSurface = LightInk,
            onSurfaceVariant = Color(0xFF6F87A3),
        ),
        typography = androidx.compose.material3.Typography(
            displaySmall = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 40.sp, lineHeight = 43.sp),
            headlineLarge = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 36.sp),
            headlineMedium = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 26.sp, lineHeight = 30.sp),
            titleLarge = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 27.sp),
            titleMedium = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, lineHeight = 22.sp),
            bodyLarge = TextStyle(fontFamily = SpaceGrotesk, fontSize = 16.sp, lineHeight = 23.sp),
            bodyMedium = TextStyle(fontFamily = SpaceGrotesk, fontSize = 14.sp, lineHeight = 20.sp),
            labelLarge = TextStyle(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Bold, fontSize = 14.sp),
            labelMedium = TextStyle(fontFamily = JetBrainsMono, fontWeight = FontWeight.Medium, fontSize = 11.sp, letterSpacing = .5.sp),
        ),
        content = content,
    )
}

@Composable
fun ChessMindApp(vm: MainViewModel = viewModel()) {
    val state = vm.state
    if (state.screen == AppScreen.SPLASH) {
        SplashScreen(vm::finishSplash)
        return
    }
    BackHandler(enabled = state.screen !in setOf(AppScreen.HOME, AppScreen.ONBOARDING)) {
        vm.navigate(
            when (state.screen) {
                AppScreen.ANALYSIS -> AppScreen.LEVEL
                AppScreen.GAME -> AppScreen.PLAY_SELECT
                AppScreen.GAME_REVIEW -> AppScreen.GAME
                AppScreen.PLAY_SELECT -> AppScreen.HOME
                AppScreen.LEVEL -> AppScreen.SETUP
                AppScreen.PROFILE, AppScreen.SCANNER, AppScreen.IMAGE_REVIEW, AppScreen.SETUP, AppScreen.HISTORY,
                AppScreen.SETTINGS -> AppScreen.HOME
                else -> AppScreen.HOME
            }
        )
    }
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(Color.White.copy(if (state.settings.darkMode) .025f else .56f), radius = size.width * .72f, center = Offset(size.width * .13f, -size.height * .08f))
            drawCircle(BrandBlue.copy(if (state.settings.darkMode) .09f else .045f), radius = size.width * .64f, center = Offset(size.width * .98f, size.height * .42f))
        }
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
        ) { padding ->
            AnimatedContent(
                targetState = state.screen,
                transitionSpec = {
                    val duration = if (state.settings.animations) 220 else 0
                    fadeIn(tween(duration)) togetherWith fadeOut(tween(duration))
                },
                label = "screen",
                modifier = Modifier.fillMaxSize().padding(padding).statusBarsPadding(),
            ) { screen ->
                when (screen) {
                    AppScreen.SPLASH -> Unit
                    AppScreen.ONBOARDING -> OnboardingScreen(vm)
                    AppScreen.HOME -> HomeScreen(vm)
                    AppScreen.PROFILE -> ProfileScreen(vm)
                    AppScreen.SCANNER -> ScannerScreen(vm)
                    AppScreen.IMAGE_REVIEW -> ImageReviewScreen(vm)
                    AppScreen.SETUP -> SetupScreen(vm)
                    AppScreen.LEVEL -> LevelScreen(vm)
                    AppScreen.ANALYSIS -> AnalysisScreen(vm)
                    AppScreen.PLAY_SELECT -> PlaySelectScreen(vm)
                    AppScreen.GAME -> GameScreen(vm)
                    AppScreen.GAME_REVIEW -> GameReviewScreen(vm)
                    AppScreen.HISTORY -> HistoryScreen(vm)
                    AppScreen.SETTINGS -> SettingsScreen(vm)
                }
            }
        }
        if (state.screen in setOf(AppScreen.HOME, AppScreen.HISTORY)) {
            FloatingDock(state.screen, vm::navigate, Modifier.align(Alignment.BottomCenter))
        }
    }
}

@Composable
private fun SplashScreen(onFinished: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(1_350)
        onFinished()
    }
    Box(
        Modifier.fillMaxSize().background(
            Brush.linearGradient(listOf(Color(0xFF84B7E9), Color(0xFFB5A0DE), Color(0xFF55C1E9))),
        ),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.splash_art_portrait),
            contentDescription = "ChessMind",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxWidth().height(360.dp).scale(2.05f),
        )
    }
}

@Composable
private fun OnboardingScreen(vm: MainViewModel) {
    var page by remember { mutableStateOf(0) }
    val accent = Accent
    val content = listOf(
        Triple("SCAN", "Point. Capture. Solve.", "Use your camera or choose a board image from your gallery."),
        Triple("ANALYZE", "Find a stronger move.", "Choose a learning level and get legal candidates, lines, and grounded explanations."),
        Triple("EXPLORE", "Play through the line.", "Step forward, step back, or try your own legal moves on the analysis board."),
    )[page]
    Box(Modifier.fillMaxSize().padding(24.dp).statusBarsPadding()) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(listOf(Coral, Purple, accent)[page].copy(.18f), radius = size.width * .75f, center = Offset(size.width * .82f, size.height * .22f))
        }
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("CHESSMIND", color = Accent, letterSpacing = 2.sp, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                TextButton(onClick = vm::completeOnboarding) { Text("Skip", color = Muted) }
            }
            Spacer(Modifier.weight(.55f))
            Box(
                Modifier.fillMaxWidth().height(245.dp).align(Alignment.CenterHorizontally)
                    .shadow(22.dp, RoundedCornerShape(42.dp), spotColor = listOf(Coral, Sunny, accent)[page].copy(.28f))
                    .clip(RoundedCornerShape(42.dp)).background(listOf(Color(0xFFFFA08D), Color(0xFF8FB3FF), Color(0xFFFFD46B))[page]),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painterResource(listOf(R.drawable.art_scan_board, R.drawable.art_analysis, R.drawable.art_success)[page]),
                    contentDescription = null, contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize().padding(8.dp),
                )
            }
            Spacer(Modifier.weight(.45f))
            Text(content.first, color = Accent, letterSpacing = 1.8.sp, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text(content.second, fontSize = 34.sp, lineHeight = 37.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(top = 8.dp))
            Text(content.third, color = Muted, fontSize = 15.sp, lineHeight = 22.sp, modifier = Modifier.padding(top = 12.dp))
            Row(Modifier.padding(vertical = 22.dp), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                repeat(3) { index -> Box(Modifier.width(if (index == page) 28.dp else 7.dp).height(7.dp).clip(CircleShape).background(if (index == page) Accent else Color.White.copy(.18f))) }
            }
            Button(
                onClick = { if (page < 2) page++ else vm.completeOnboarding() }, modifier = Modifier.fillMaxWidth().height(58.dp),
                shape = RoundedCornerShape(20.dp), colors = ButtonDefaults.buttonColors(Sunny, Ink),
            ) { Text(if (page < 2) "Continue  →" else "Start playing", fontWeight = FontWeight.Bold) }
            Spacer(Modifier.navigationBarsPadding())
        }
    }
}

@Composable
private fun HomeScreen(vm: MainViewModel) {
    LazyColumn(
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 118.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { vm.navigate(AppScreen.PROFILE) }, modifier = Modifier.size(52.dp)) {
                    UserAvatar(vm.state.profile.imageUri, vm.state.profile.name, Modifier.size(48.dp))
                }
                Spacer(Modifier.width(40.dp))
                IconButton(onClick = { vm.navigate(AppScreen.SETTINGS) }) { Icon(Icons.Rounded.Settings, contentDescription = "Settings", tint = Ink) }
            }
        }
        item {
            Column {
                Text("Hello, ${vm.state.profile.name.substringBefore(' ')}", style = MaterialTheme.typography.labelMedium, color = Accent)
                Text("What will you\nplay next?", style = MaterialTheme.typography.displaySmall, modifier = Modifier.padding(top = 6.dp))
            }
        }
        item {
            Box(Modifier.fillMaxWidth().height(326.dp).clickable { vm.navigate(AppScreen.SCANNER) }) {
                Box(
                    Modifier.fillMaxWidth().height(290.dp).align(Alignment.BottomCenter)
                        .shadow(11.dp, RoundedCornerShape(32.dp), spotColor = Color(0xFF78A6FF).copy(.32f))
                        .clip(RoundedCornerShape(32.dp)).background(Color(0xFF78A6FF)),
                )
                Image(
                    painter = painterResource(R.drawable.art_scan_board), contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.width(420.dp).height(350.dp).align(Alignment.TopEnd).offset(x = 40.dp, y = (-16).dp),
                )
                Text(
                    "SOLVE A\nPOSITION", style = MaterialTheme.typography.headlineMedium, color = Color.White,
                    modifier = Modifier.align(Alignment.TopStart).padding(start = 24.dp, top = 56.dp).width(170.dp),
                )
                PlayfulActionButton(
                    label = "Scan board", icon = Icons.Rounded.CameraAlt,
                    onClick = { vm.navigate(AppScreen.SCANNER) },
                    modifier = Modifier.align(Alignment.BottomStart).padding(22.dp),
                    colors = listOf(Color.White, Color(0xFFEAF2FF)), contentColor = BrandBlue,
                )
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                HomeActionCard("Play chess", "Friend or AI", leagueFor(vm.state.progress.rating).art, Color(0xFFFF8D78), Modifier.weight(1f), artSize = 172.dp) { vm.navigate(AppScreen.PLAY_SELECT) }
                HomeActionCard("Set position", "Build it yourself", R.drawable.art_setup_position_v2, Color(0xFFA78BFA), Modifier.weight(1f), artSize = 224.dp, artOffsetX = 12.dp, artOffsetY = (-38).dp) { vm.navigate(AppScreen.SETUP) }
            }
        }
        if (vm.state.history.isNotEmpty() || vm.state.matchHistory.isNotEmpty()) item {
            Card(
                onClick = { vm.navigate(AppScreen.HISTORY) }, shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            ) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Image(painterResource(R.drawable.art_history), null, contentScale = ContentScale.Crop, modifier = Modifier.size(62.dp))
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(if (vm.state.matchHistory.isNotEmpty()) "Review your latest match" else "Continue your last analysis", style = MaterialTheme.typography.titleMedium)
                        Text(
                            if (vm.state.matchHistory.isNotEmpty()) vm.state.matchHistory.first().result
                            else "${vm.state.history.first().bestMove} · ${vm.state.history.first().level}",
                            style = MaterialTheme.typography.bodyMedium, color = Muted, maxLines = 1,
                        )
                    }
                    Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = Muted)
                }
            }
        }
    }
}

@Composable
private fun HomeActionCard(
    title: String,
    subtitle: String,
    art: Int,
    color: Color,
    modifier: Modifier,
    artSize: androidx.compose.ui.unit.Dp = 158.dp,
    artOffsetX: androidx.compose.ui.unit.Dp = 20.dp,
    artOffsetY: androidx.compose.ui.unit.Dp = (-5).dp,
    onClick: () -> Unit,
) {
    Box(modifier.height(188.dp).clickable(onClick = onClick)) {
        Box(
            Modifier.fillMaxWidth().height(158.dp).align(Alignment.BottomCenter)
                .shadow(11.dp, RoundedCornerShape(27.dp), spotColor = color.copy(.32f))
                .clip(RoundedCornerShape(27.dp)).background(color),
        )
        Image(
            painterResource(art), null, contentScale = ContentScale.Fit,
            modifier = Modifier.size(artSize).align(Alignment.TopEnd).offset(x = artOffsetX, y = artOffsetY),
        )
        Column(Modifier.align(Alignment.BottomStart).padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = Color.White)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(.82f))
        }
    }
}

@Composable
private fun PlaySelectScreen(vm: MainViewModel) {
    val progress = vm.state.progress
    val league = leagueFor(progress.rating)
    LazyColumn(
        modifier = Modifier.navigationBarsPadding(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 0.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item { ScreenHeader("Play chess", onBack = { vm.navigate(AppScreen.HOME) }) }
        item {
            Card(
                shape = RoundedCornerShape(30.dp), colors = CardDefaults.cardColors(containerColor = league.color, contentColor = Color.White),
                modifier = Modifier.fillMaxWidth().height(210.dp).shadow(15.dp, RoundedCornerShape(30.dp), spotColor = league.color.copy(.4f)),
            ) {
                Box(Modifier.fillMaxSize()) {
                    Image(painterResource(league.art), null, contentScale = ContentScale.Fit, modifier = Modifier.size(210.dp).align(Alignment.CenterEnd).offset(x = 20.dp))
                    Column(Modifier.padding(24.dp).width(180.dp)) {
                        Text("YOUR LEAGUE", fontFamily = JetBrainsMono, fontSize = 10.sp, letterSpacing = 1.4.sp, fontWeight = FontWeight.Bold)
                        Text(league.name, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(top = 5.dp))
                        Text("${progress.rating} points", fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 5.dp))
                        Text("${progress.wins} wins · ${progress.games} games", color = Color.White.copy(.78f), fontSize = 12.sp, modifier = Modifier.padding(top = 7.dp))
                    }
                }
            }
        }
        item { Text("Choose your match", style = MaterialTheme.typography.titleLarge) }
        item {
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(SurfaceDark).padding(14.dp)) {
                Text("MATCH CLOCK", style = MaterialTheme.typography.labelMedium, color = Muted)
                Text("Each player gets the selected time", fontSize = 12.sp, color = Muted, modifier = Modifier.padding(top = 3.dp, bottom = 10.dp))
                TimeControlPicker(vm.state.selectedTimeMinutes, vm::setMatchMinutes)
            }
        }
        item {
            PlayModeCard(
                title = "VS AI", art = league.art,
                color = Color(0xFF6C91FF), icon = Icons.Rounded.SmartToy,
            ) { vm.startGame(GameMode.AI) }
        }
        item {
            PlayModeCard(
                title = "Play a friend", art = R.drawable.art_practice,
                color = Color(0xFF69D3AE), icon = Icons.Rounded.Groups,
            ) { vm.startGame(GameMode.FRIEND) }
        }
        item {
            Text("LEAGUE ROAD", style = MaterialTheme.typography.labelMedium, color = Muted, modifier = Modifier.padding(top = 8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(vertical = 7.dp)) {
                items(LeagueLooks) { item ->
                    val reached = progress.rating >= item.minRating
                    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(if (reached) item.color.copy(.18f) else SurfaceDark), modifier = Modifier.width(112.dp)) {
                        Column(Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Image(painterResource(item.art), null, contentScale = ContentScale.Fit, modifier = Modifier.size(72.dp).graphicsLayer { alpha = if (reached) 1f else .38f })
                            Text(item.name, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(if (item.minRating == 0) "Start" else "${item.minRating}+", color = Muted, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun TimeControlPicker(selected: Int, onSelect: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        listOf(3 to "3 min", 5 to "5 min", 10 to "10 min", 0 to "Unlimited").forEach { (minutes, label) ->
            Surface(
                onClick = { onSelect(minutes) }, modifier = Modifier.weight(1f).height(42.dp),
                shape = RoundedCornerShape(14.dp),
                color = if (selected == minutes) Accent else if (MaterialTheme.colorScheme.background == NightBackground) Color(0xFF263247) else Color(0xFFFFFBF4),
                contentColor = if (selected == minutes) Color.White else Ink,
                shadowElevation = if (selected == minutes) 4.dp else 0.dp,
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (minutes == 0) Icon(Icons.Rounded.AllInclusive, contentDescription = label, modifier = Modifier.size(22.dp))
                    else Text(label, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun PlayModeCard(title: String, art: Int, color: Color, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Box(Modifier.fillMaxWidth().height(178.dp).clickable(onClick = onClick)) {
        Box(
            Modifier.fillMaxWidth().height(154.dp).align(Alignment.BottomCenter)
                .shadow(10.dp, RoundedCornerShape(27.dp), spotColor = color.copy(.3f))
                .clip(RoundedCornerShape(27.dp)).background(color),
        )
        Image(painterResource(art), null, contentScale = ContentScale.Fit, modifier = Modifier.size(178.dp).align(Alignment.TopEnd).offset(x = 18.dp, y = (-3).dp))
        Row(Modifier.align(Alignment.CenterStart).padding(20.dp).width(188.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, modifier = Modifier.size(25.dp))
            Spacer(Modifier.width(9.dp))
            Text(title, fontSize = 21.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun GameScreen(vm: MainViewModel) {
    val state = vm.state
    var showResignConfirmation by remember { mutableStateOf(false) }
    val position = state.gamePosition
    val legal = ChessRules.legalMoves(position)
    val hints = state.gameSelected?.let { from -> legal.filter { it.from == from }.map { it.to }.toSet() }.orEmpty()
    val last = state.gameMoves.lastOrNull()?.move
    LaunchedEffect(state.aiThinking, state.gameMoves.size) {
        if (state.aiThinking) { delay(state.settings.aiMoveDelayMs.toLong()); vm.playAiMove() }
    }
    LaunchedEffect(state.gameResult, state.selectedTimeMinutes, state.clockStarted) {
        while (state.gameResult == null && state.selectedTimeMinutes > 0 && state.clockStarted) {
            delay(1_000)
            vm.tickGameClock()
        }
    }
    LazyColumn(
        modifier = Modifier.navigationBarsPadding(), contentPadding = PaddingValues(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { ScreenHeader(if (state.gameMode == GameMode.AI) "Vs adaptive AI" else "Friend match", onBack = { vm.navigate(AppScreen.PLAY_SELECT) }, trailing = if (state.gameMode == GameMode.AI) "${state.aiRating}" else "LOCAL") }
        item {
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(SurfaceDark).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                val league = leagueFor(if (state.gameMode == GameMode.AI) state.aiRating else state.progress.rating)
                Image(painterResource(league.art), null, modifier = Modifier.size(54.dp), contentScale = ContentScale.Fit)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(if (state.aiThinking && state.settings.showAiThinking) "Opponent is thinking…" else if (state.gameResult != null) state.gameResult else "${position.sideToMove.name.lowercase().replaceFirstChar { it.uppercase() }} to move", fontWeight = FontWeight.Bold)
                    Text(
                        if (!state.clockStarted && state.selectedTimeMinutes > 0) "Clock starts after White's first move"
                        else if (state.gameMode == GameMode.AI) "${league.name} rival · ${state.aiRating} points"
                        else "Pass the board after each move",
                        color = Muted, fontSize = 12.sp,
                    )
                }
                if (state.selectedTimeMinutes > 0) {
                    Column(horizontalAlignment = Alignment.End) {
                        ClockPill("White", state.whiteTimeSeconds, position.sideToMove == Side.WHITE)
                        Spacer(Modifier.height(5.dp))
                        ClockPill("Black", state.blackTimeSeconds, position.sideToMove == Side.BLACK)
                    }
                } else if (state.aiThinking && state.settings.showAiThinking) Text("•••", color = Accent, fontSize = 22.sp)
            }
        }
        item { BoardLookPicker(state.settings, vm::updateSettings) }
        item {
            ChessBoard(
                position, state.flipped, state.gameSelected, vm::tapGame, {},
                showCoordinates = state.settings.coordinates, hintSquares = if (state.settings.legalHints) hints else emptySet(),
                lastMove = if (state.settings.showLastMove) last?.let { it.from to it.to } else null, haptics = state.settings.haptics,
                highContrastBoard = state.settings.highContrastBoard, boardDepth = state.settings.boardDepth,
                pieceDepth = state.settings.pieceDepth, boardTheme = state.settings.boardTheme, pieceStyle = state.settings.pieceStyle,
                dragToMove = state.settings.dragToMove, onDragMove = vm::dragGame,
            )
        }
        if (state.settings.showMoveList && state.gameMoves.isNotEmpty()) item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                items(state.gameMoves.takeLast(16)) { record ->
                    Box(Modifier.clip(RoundedCornerShape(10.dp)).background(SurfaceDark).padding(horizontal = 10.dp, vertical = 7.dp)) {
                        Text(record.notation, fontFamily = JetBrainsMono, fontSize = 11.sp)
                    }
                }
            }
        }
        state.drawStatus?.let { message -> item {
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp)).background(Sunny.copy(.34f)).padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Rounded.Handshake, null, tint = Ink, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(9.dp)); Text(message, fontSize = 12.sp, color = Ink)
            }
        } }
        if (state.gameResult == null) item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                Button(
                    onClick = vm::offerDraw, enabled = state.gameMoves.isNotEmpty() && !state.aiThinking,
                    modifier = Modifier.weight(1f).height(50.dp), shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark, contentColor = Ink),
                ) { Icon(Icons.Rounded.Handshake, null); Spacer(Modifier.width(6.dp)); Text(if (state.gameMode == GameMode.AI) "Offer draw" else "Agree draw") }
                TextButton(
                    onClick = { if (state.settings.confirmResign) showResignConfirmation = true else vm.resignGame() },
                    modifier = Modifier.weight(1f).height(50.dp),
                ) { Icon(Icons.Rounded.Flag, null); Spacer(Modifier.width(6.dp)); Text("Resign") }
            }
        } else item {
            val currentLeague = leagueFor(state.progress.rating)
            val previousLeague = leagueFor(state.gameRatingBefore)
            val upcoming = nextLeague(state.progress.rating)
            Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(Sunny, LightInk)) {
                Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 19.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(painterResource(currentLeague.art), null, modifier = Modifier.size(76.dp), contentScale = ContentScale.Fit)
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text(state.gameResult, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                            Spacer(Modifier.height(5.dp))
                            Text(if (state.gameMode == GameMode.AI) "${if (state.lastRatingChange >= 0) "+" else ""}${state.lastRatingChange} points this match" else "Friendly match · unrated", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            if (state.gameMode == GameMode.AI) Text("${state.progress.rating} points · ${currentLeague.name} league", fontSize = 12.sp, color = LightInk.copy(.72f), modifier = Modifier.padding(top = 3.dp))
                        }
                    }
                    if (state.gameMode == GameMode.AI) {
                        if (previousLeague != currentLeague) Text("League up! Welcome to ${currentLeague.name}.", fontWeight = FontWeight.ExtraBold, color = BrandBlue, modifier = Modifier.padding(top = 9.dp))
                        Text(
                            upcoming?.let { "${it.minRating - state.progress.rating} points to ${it.name}" } ?: "Highest league reached",
                            fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                    Box(Modifier.fillMaxWidth().padding(top = 14.dp).height(1.dp).background(LightInk.copy(.12f)))
                    Text("Review ideas for White and Black", fontSize = 12.sp, modifier = Modifier.padding(top = 12.dp))
                }
            }
            Button(onClick = vm::buildGameReview, modifier = Modifier.fillMaxWidth().height(58.dp), shape = RoundedCornerShape(19.dp)) {
                Icon(Icons.Rounded.Psychology, null); Spacer(Modifier.width(7.dp)); Text("Analyze both sides")
            }
        }
        item { Spacer(Modifier.height(20.dp)) }
    }
    if (showResignConfirmation) AlertDialog(
        onDismissRequest = { showResignConfirmation = false },
        title = { Text("Resign this game?") },
        text = { Text("The current side will lose the match.") },
        confirmButton = { TextButton(onClick = { showResignConfirmation = false; vm.resignGame() }) { Text("Resign", color = Coral) } },
        dismissButton = { TextButton(onClick = { showResignConfirmation = false }) { Text("Keep playing") } },
    )
    if (state.aiDrawOffer) AlertDialog(
        onDismissRequest = { vm.respondToAiDraw(false) },
        icon = { Icon(Icons.Rounded.Handshake, null, tint = Accent) },
        title = { Text("The AI offers a draw") },
        text = { Text("The material and position are balanced. Would you like to end the game as a draw?") },
        confirmButton = { TextButton(onClick = { vm.respondToAiDraw(true) }) { Text("Accept draw") } },
        dismissButton = { TextButton(onClick = { vm.respondToAiDraw(false) }) { Text("Play on") } },
    )
}

@Composable
private fun ClockPill(label: String, seconds: Int, active: Boolean) {
    val minutes = seconds / 60
    val remainder = seconds % 60
    Row(
        Modifier.clip(RoundedCornerShape(10.dp)).background(if (active) Accent else MaterialTheme.colorScheme.background)
            .padding(horizontal = 9.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label.take(1), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (active) Color.White.copy(.76f) else Muted)
        Spacer(Modifier.width(5.dp))
        Text("$minutes:${remainder.toString().padStart(2, '0')}", fontFamily = JetBrainsMono, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (active) Color.White else Ink)
    }
}

@Composable
private fun GameReviewScreen(vm: MainViewModel) {
    val state = vm.state
    var index by remember(state.gameReview.size) { mutableStateOf(0) }
    if (state.reviewLoading) {
        ReviewLoadingScreen(state.reviewProgress, state.reviewTotal) { vm.navigate(AppScreen.GAME) }
        return
    }
    val review = state.gameReview.getOrNull(index)
    LazyColumn(modifier = Modifier.navigationBarsPadding(), contentPadding = PaddingValues(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { ScreenHeader("Game review", onBack = { vm.navigate(AppScreen.GAME) }, trailing = "BOTH SIDES") }
        item {
            ChessBoard(
                review?.position ?: state.gamePosition, false, null, {}, {},
                showCoordinates = state.settings.coordinates,
                lastMove = if (state.settings.showLastMove) state.gameMoves.getOrNull(index)?.move?.let { it.from to it.to } else null,
                highContrastBoard = state.settings.highContrastBoard, boardDepth = state.settings.boardDepth,
                pieceDepth = state.settings.pieceDepth, boardTheme = state.settings.boardTheme, pieceStyle = state.settings.pieceStyle,
            )
        }
        item {
            Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(SurfaceDark)) {
                Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { if (index > 0) index-- }, enabled = index > 0) { Icon(Icons.Rounded.SkipPrevious, "Previous move") }
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("MOVE ${review?.ply ?: 0} OF ${state.gameReview.size}", style = MaterialTheme.typography.labelMedium, color = Accent)
                        Text("${review?.side?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: ""}: ${review?.move ?: ""}", fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = { if (index < state.gameReview.lastIndex) index++ }, enabled = index < state.gameReview.lastIndex) { Icon(Icons.Rounded.SkipNext, "Next move") }
                }
            }
        }
        review?.let { item {
            Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(if (it.verdict == "Best move") Color(0xFFBCEBD8) else Color(0xFFFFE4A8), LightInk)) {
                Column(Modifier.fillMaxWidth().padding(18.dp)) {
                    Text(it.verdict.uppercase(), style = MaterialTheme.typography.labelMedium)
                    Text(if (it.verdict == "Best move") "Nice work — the coach agrees." else "Played ${it.move}. The coach preferred ${it.bestMove}.", fontSize = 17.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 7.dp))
                }
            }
        } }
        item { Spacer(Modifier.height(18.dp)) }
    }
}

@Composable
private fun PlayfulActionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    colors: List<Color> = listOf(Color(0xFF5B8CFF), Color(0xFF2E63F6)),
    contentColor: Color = Color.White,
) {
    Box(
        modifier.height(56.dp).shadow(11.dp, RoundedCornerShape(19.dp), spotColor = colors.last().copy(.45f))
            .clip(RoundedCornerShape(19.dp)).background(Brush.verticalGradient(colors)).clickable(onClick = onClick),
    ) {
        Box(Modifier.fillMaxWidth().height(2.dp).background(Color.White.copy(.36f)))
        Row(Modifier.fillMaxSize().padding(horizontal = 18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Icon(icon, null, tint = contentColor, modifier = Modifier.size(23.dp))
            Spacer(Modifier.width(9.dp))
            Text(label, color = contentColor, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(9.dp))
            Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, tint = contentColor, modifier = Modifier.size(21.dp))
        }
    }
}

@Composable
private fun UserAvatar(uri: String?, name: String, modifier: Modifier = Modifier) {
    Box(modifier.clip(CircleShape).background(Sunny), contentAlignment = Alignment.Center) {
        if (uri != null) AndroidView(
            factory = { imageContext -> ImageView(imageContext).apply { scaleType = ImageView.ScaleType.CENTER_CROP } },
            update = { it.setImageURI(uri.toUri()) }, modifier = Modifier.fillMaxSize(),
        ) else Text(name.take(1).uppercase(), color = Espresso, style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
private fun ProfileScreen(vm: MainViewModel) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(vm.state.profile.name) }
    var imageUri by remember { mutableStateOf(vm.state.profile.imageUri) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            runCatching { context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            imageUri = it.toString()
        }
    }
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp).navigationBarsPadding()) {
        ScreenHeader("Your profile", onBack = { vm.navigate(AppScreen.HOME) })
        Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFC85A)), modifier = Modifier.fillMaxWidth().height(150.dp)) {
            Box(Modifier.fillMaxSize()) {
                Image(painterResource(R.drawable.art_profile), null, contentScale = ContentScale.Fit, modifier = Modifier.size(180.dp).align(Alignment.CenterEnd).offset(x = 18.dp, y = 7.dp))
                Column(Modifier.padding(20.dp).width(180.dp)) {
                    Text("YOUR CHESS STORY", style = MaterialTheme.typography.labelMedium, color = LightInk.copy(.66f))
                    Text("Play like a champion", style = MaterialTheme.typography.headlineMedium, color = LightInk, modifier = Modifier.padding(top = 6.dp))
                }
            }
        }
        Spacer(Modifier.height(18.dp))
        Box(Modifier.align(Alignment.CenterHorizontally)) {
            UserAvatar(imageUri, name, Modifier.size(132.dp))
            IconButton(onClick = { picker.launch(arrayOf("image/*")) }, modifier = Modifier.align(Alignment.BottomEnd).background(SurfaceLight, RoundedCornerShape(14.dp))) {
                Icon(Icons.Rounded.AddPhotoAlternate, contentDescription = "Choose profile photo")
            }
        }
        Text("PROFILE NAME", style = MaterialTheme.typography.labelMedium, color = Muted, modifier = Modifier.padding(top = 32.dp, bottom = 8.dp))
        OutlinedTextField(value = name, onValueChange = { name = it.take(32) }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(18.dp))
        Spacer(Modifier.weight(1f))
        Button(onClick = { vm.updateProfile(name, imageUri); vm.navigate(AppScreen.HOME) }, modifier = Modifier.fillMaxWidth().height(58.dp), shape = RoundedCornerShape(20.dp), colors = ButtonDefaults.buttonColors(Sunny, Espresso)) {
            Text("Save profile", style = MaterialTheme.typography.labelLarge)
        }
        Spacer(Modifier.height(18.dp))
    }
}

@Composable
private fun ScannerScreen(vm: MainViewModel) {
    val accent = Accent
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var granted by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var captureError by remember { mutableStateOf<String?>(null) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted = it }
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { vm.reviewImage(it.toString()) }
    }
    LaunchedEffect(Unit) { if (!granted) permissionLauncher.launch(Manifest.permission.CAMERA) }

    Box(Modifier.fillMaxSize().background(Color(0xFF09111F))) {
        if (granted) {
            AndroidView(
                factory = { cameraContext ->
                    PreviewView(cameraContext).also { previewView ->
                        previewView.scaleType = PreviewView.ScaleType.FILL_CENTER
                        val future = ProcessCameraProvider.getInstance(cameraContext)
                        future.addListener({
                            val provider = future.get()
                            val preview = Preview.Builder().build().also { it.surfaceProvider = previewView.surfaceProvider }
                            val capture = ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build()
                            imageCapture = capture
                            provider.unbindAll()
                            provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, capture)
                        }, ContextCompat.getMainExecutor(cameraContext))
                    }
                },
                modifier = Modifier.fillMaxSize(),
            )
            Canvas(Modifier.fillMaxSize().padding(horizontal = 28.dp, vertical = 154.dp)) {
                val side = minOf(size.width, size.height)
                val left = (size.width - side) / 2f
                val top = (size.height - side) / 2f
                drawRoundRect(Color.Black.copy(.18f), Offset(left, top), Size(side, side), CornerRadius(28.dp.toPx()))
                drawRoundRect(accent.copy(.38f), Offset(left, top), Size(side, side), CornerRadius(28.dp.toPx()), style = Stroke(1.dp.toPx()))
                val corner = 36.dp.toPx()
                val width = 4.dp.toPx()
                listOf(
                    Offset(left, top) to Offset(left + corner, top), Offset(left, top) to Offset(left, top + corner),
                    Offset(left + side, top) to Offset(left + side - corner, top), Offset(left + side, top) to Offset(left + side, top + corner),
                    Offset(left, top + side) to Offset(left + corner, top + side), Offset(left, top + side) to Offset(left, top + side - corner),
                    Offset(left + side, top + side) to Offset(left + side - corner, top + side), Offset(left + side, top + side) to Offset(left + side, top + side - corner),
                ).forEach { (start, end) -> drawLine(accent, start, end, width, StrokeCap.Round) }
            }
            Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding(), horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                    color = Color(0xFF111D31).copy(.92f), contentColor = Color.White,
                    shape = RoundedCornerShape(24.dp), shadowElevation = 8.dp,
                ) {
                    Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { vm.navigate(AppScreen.HOME) }) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                        }
                        Column(Modifier.weight(1f)) {
                            Text("Scan a position", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                            Text("Line up every corner of the chessboard", color = Color.White.copy(.68f), fontSize = 11.sp)
                        }
                        Icon(Icons.Rounded.CenterFocusStrong, contentDescription = null, tint = Sunny, modifier = Modifier.size(26.dp))
                        Spacer(Modifier.width(8.dp))
                    }
                }
                Spacer(Modifier.weight(1f))
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(16.dp), color = Color(0xFF111D31).copy(.94f),
                    contentColor = Color.White, shape = RoundedCornerShape(28.dp), shadowElevation = 12.dp,
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Ready when the board is sharp", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("Avoid glare and keep pieces fully visible.", color = Color.White.copy(.64f), fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
                        captureError?.let { Text(it, color = Coral, fontSize = 11.sp, modifier = Modifier.padding(top = 6.dp)) }
                        Row(Modifier.fillMaxWidth().padding(top = 14.dp), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                            ScannerActionButton("Gallery", Icons.Rounded.PhotoLibrary, Color(0xFFEAF2FF), BrandBlue, Modifier.weight(1f)) {
                                imagePicker.launch(arrayOf("image/*"))
                            }
                            ScannerActionButton("Capture", Icons.Rounded.CameraAlt, Sunny, Espresso, Modifier.weight(1.15f)) {
                                val capture = imageCapture ?: return@ScannerActionButton
                                captureError = null
                                val file = File(context.cacheDir, "scan-${System.currentTimeMillis()}.jpg")
                                capture.takePicture(
                                    ImageCapture.OutputFileOptions.Builder(file).build(), ContextCompat.getMainExecutor(context),
                                    object : ImageCapture.OnImageSavedCallback {
                                        override fun onImageSaved(output: ImageCapture.OutputFileResults) = vm.reviewImage(Uri.fromFile(file).toString())
                                        override fun onError(exception: ImageCaptureException) { captureError = "Could not capture. Please try once more." }
                                    },
                                )
                            }
                            ScannerActionButton("Manual", Icons.Rounded.GridOn, Color(0xFFDDF8EE), Color(0xFF176B52), Modifier.weight(1f)) {
                                vm.navigate(AppScreen.SETUP)
                            }
                        }
                    }
                }
            }
        } else {
            Surface(
                modifier = Modifier.align(Alignment.Center).padding(24.dp), shape = RoundedCornerShape(30.dp),
                color = Color(0xFF111D31), contentColor = Color.White, shadowElevation = 16.dp,
            ) {
                Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Rounded.CenterFocusStrong, contentDescription = null, tint = Sunny, modifier = Modifier.size(64.dp))
                    Text("Allow camera access", fontSize = 23.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(top = 16.dp))
                    Text("The camera is used only while this screen is open. Images stay on your device.", color = Color.White.copy(.66f), textAlign = TextAlign.Center, modifier = Modifier.padding(top = 8.dp, bottom = 18.dp))
                    Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(17.dp), colors = ButtonDefaults.buttonColors(Sunny, Espresso)) { Text("Open camera", fontWeight = FontWeight.Bold) }
                    Row(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ScannerActionButton("Gallery", Icons.Rounded.PhotoLibrary, Color(0xFFEAF2FF), BrandBlue, Modifier.weight(1f)) { imagePicker.launch(arrayOf("image/*")) }
                        ScannerActionButton("Manual", Icons.Rounded.GridOn, Color(0xFFDDF8EE), Color(0xFF176B52), Modifier.weight(1f)) { vm.navigate(AppScreen.SETUP) }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScannerActionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    background: Color,
    content: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Surface(onClick = onClick, modifier = modifier.height(52.dp), shape = RoundedCornerShape(17.dp), color = background, contentColor = content) {
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(19.dp))
            Spacer(Modifier.width(6.dp))
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ImageReviewScreen(vm: MainViewModel) {
    val uri = vm.state.importedImage
    var rotation by remember(uri) { mutableStateOf(0f) }
    Column(Modifier.fillMaxSize().padding(horizontal = 18.dp).navigationBarsPadding()) {
        ScreenHeader("Review image", onBack = { vm.navigate(AppScreen.HOME) }, trailing = "LOCAL")
        Box(Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(28.dp)).background(Color.Black)) {
            if (uri != null) AndroidView(
                factory = { imageContext -> ImageView(imageContext).apply { scaleType = ImageView.ScaleType.CENTER_CROP; setImageURI(uri.toUri()) } },
                update = { it.setImageURI(uri.toUri()) },
                modifier = Modifier.fillMaxSize().graphicsLayer { rotationZ = rotation },
            )
            Canvas(Modifier.fillMaxSize().padding(24.dp)) {
                for (index in 0..8) {
                    val x = size.width * index / 8f
                    val y = size.height * index / 8f
                    drawLine(Color.White.copy(.26f), Offset(x, 0f), Offset(x, size.height), 1.dp.toPx())
                    drawLine(Color.White.copy(.26f), Offset(0f, y), Offset(size.width, y), 1.dp.toPx())
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Center the board in the grid", color = Muted, fontSize = 13.sp)
            TextButton(onClick = { rotation = (rotation + 90f) % 360f }) { Text("Rotate 90°", color = Accent) }
        }
        Text("Automatic piece recognition is not bundled yet. Continue to an empty board and enter the detected position manually.", color = Muted, fontSize = 12.sp, lineHeight = 17.sp, modifier = Modifier.padding(bottom = 12.dp))
        Button(
            onClick = vm::startImageCorrection, modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(20.dp), colors = ButtonDefaults.buttonColors(Sunny, Ink),
        ) { Text("Correct position manually  →", fontWeight = FontWeight.Bold) }
        Spacer(Modifier.height(18.dp))
    }
}

@Composable
private fun SetupScreen(vm: MainViewModel) {
    val state = vm.state
    var showDetails by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp).navigationBarsPadding()) {
        ScreenHeader("Set up position", onBack = { vm.navigate(AppScreen.HOME) })
        state.fenError?.let { message ->
            Row(
                Modifier.fillMaxWidth().padding(bottom = 8.dp).clip(RoundedCornerShape(14.dp))
                    .background(Coral.copy(.14f)).padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(message, color = Ink, fontSize = 11.sp, modifier = Modifier.weight(1f))
                Text("×", color = Coral, fontSize = 20.sp, modifier = Modifier.clickable(onClick = vm::dismissFenError))
            }
        }
        BoardLookPicker(state.settings, vm::updateSettings)
        Spacer(Modifier.height(10.dp))
        ChessBoard(
            position = state.position,
            flipped = state.flipped,
            selected = state.selected,
            onTap = vm::tapSquare,
            onLongPressFallback = vm::erase,
            showCoordinates = state.settings.coordinates,
            highContrastBoard = state.settings.highContrastBoard,
            boardDepth = state.settings.boardDepth,
            pieceDepth = state.settings.pieceDepth,
            boardTheme = state.settings.boardTheme,
            pieceStyle = state.settings.pieceStyle,
            haptics = state.settings.haptics,
            dragToMove = state.settings.dragToMove,
            onDragMove = vm::dragSetupPiece,
        )
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = vm::toggleTurn) { Text(if (state.position.sideToMove == Side.WHITE) "White to move" else "Black to move", color = Accent) }
            Row {
                IconButton(onClick = vm::flip) { Icon(Icons.Rounded.FlipCameraAndroid, contentDescription = "Flip board") }
                IconButton(onClick = vm::clear) { Icon(Icons.Rounded.DeleteSweep, contentDescription = "Clear board") }
                IconButton(onClick = vm::reset) { Icon(Icons.Rounded.Refresh, contentDescription = "Reset board") }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            TextButton(onClick = vm::undo, enabled = state.canUndo) { Icon(Icons.AutoMirrored.Rounded.Undo, null); Spacer(Modifier.width(5.dp)); Text("Undo") }
            TextButton(onClick = vm::redo, enabled = state.canRedo) { Icon(Icons.AutoMirrored.Rounded.Redo, null); Spacer(Modifier.width(5.dp)); Text("Redo") }
        }
        TextButton(onClick = { showDetails = true }, modifier = Modifier.fillMaxWidth()) {
            Text("Castling ${state.position.castling.asFen()}  ·  En passant ${state.position.enPassant?.algebraic ?: "-"}", color = Muted, fontSize = 12.sp)
        }
        Spacer(Modifier.height(8.dp))
        PlayfulActionButton(
            label = "Analyze position", icon = Icons.Rounded.CenterFocusStrong,
            onClick = { vm.proceedToLevel() }, modifier = Modifier.fillMaxWidth(),
            colors = listOf(Color(0xFFFFD778), Color(0xFFFFB83E)), contentColor = LightInk,
        )
        Spacer(Modifier.height(14.dp))
    }
    if (showDetails) PositionDetailsDialog(
        castling = state.position.castling.asFen(),
        enPassant = state.position.enPassant?.algebraic ?: "-",
        error = state.fenError,
        onToggle = vm::toggleCastling,
        onDismiss = { showDetails = false; vm.dismissFenError() },
        onSaveEnPassant = { if (vm.setEnPassant(it)) showDetails = false },
    )
}

@Composable
private fun PiecePalette(selected: Piece?, onSelect: (Piece?) -> Unit) {
    Column {
        Text("PIECES", fontSize = 11.sp, letterSpacing = 1.4.sp, color = Muted)
        Spacer(Modifier.height(6.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            item {
                PaletteCell(null, selected == null) { onSelect(null) }
            }
            items(Side.entries.flatMap { side -> PieceType.entries.map { Piece(it, side) } }) { piece ->
                PaletteCell(piece, selected == piece) { onSelect(piece) }
            }
        }
    }
}

@Composable
private fun PaletteCell(piece: Piece?, active: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.size(44.dp).clip(RoundedCornerShape(12.dp))
            .background(if (active) Accent else SurfaceLight)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (piece == null) Text("↕", fontSize = 25.sp, color = if (active) Color.White else Ink)
        else ChessPiece(piece, depth = true, Modifier.size(38.dp).padding(3.dp))
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun ChessBoard(
    position: Position,
    flipped: Boolean,
    selected: Square?,
    onTap: (Square) -> Unit,
    onLongPressFallback: (Square) -> Unit,
    showCoordinates: Boolean = true,
    hintSquares: Set<Square> = emptySet(),
    lastMove: Pair<Square, Square>? = null,
    haptics: Boolean = false,
    highContrastBoard: Boolean = false,
    boardDepth: Boolean = false,
    pieceDepth: Boolean = true,
    boardTheme: Int = 0,
    pieceStyle: Int = 0,
    dragToMove: Boolean = false,
    onDragMove: (Square, Square) -> Unit = { _, _ -> },
) {
    val haptic = LocalHapticFeedback.current
    val palette = when (boardTheme.coerceIn(0, 3)) {
        1 -> Color(0xFFEEEED2) to Color(0xFF769656)
        2 -> Color(0xFFD8E8F4) to Color(0xFF4D7EA8)
        3 -> Color(0xFFE6E6E6) to Color(0xFF5D6470)
        else -> BoardLight to BoardDark
    }
    val lightColor = if (highContrastBoard) Color(0xFFF4F0DF) else palette.first
    val darkColor = if (highContrastBoard) Color(0xFF6E8060) else palette.second
    val dragModifier = if (dragToMove) Modifier.pointerInput(position, flipped) {
        var dragFrom: Square? = null
        var dragTo: Square? = null
        fun squareAt(point: Offset): Square? {
            if (point.x !in 0f..size.width.toFloat() || point.y !in 0f..size.height.toFloat()) return null
            val displayFile = (point.x / (size.width / 8f)).toInt().coerceIn(0, 7)
            val displayRank = (point.y / (size.height / 8f)).toInt().coerceIn(0, 7)
            val file = if (flipped) 7 - displayFile else displayFile
            val rank = if (flipped) displayRank else 7 - displayRank
            return Square.of(file, rank)
        }
        detectDragGestures(
            onDragStart = { point -> dragFrom = squareAt(point); dragTo = dragFrom },
            onDragEnd = {
                val from = dragFrom
                val to = dragTo
                if (from != null && to != null && from != to) {
                    if (haptics) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onDragMove(from, to)
                }
                dragFrom = null
                dragTo = null
            },
            onDragCancel = { dragFrom = null; dragTo = null },
            onDrag = { change, _ -> dragTo = squareAt(change.position); change.consume() },
        )
    } else Modifier
    Box(
        Modifier.fillMaxWidth().aspectRatio(1f)
            .shadow(if (boardDepth) 12.dp else 6.dp, RoundedCornerShape(26.dp), spotColor = Color.Black.copy(.16f))
            .clip(RoundedCornerShape(26.dp)).background(SurfaceLight).padding(7.dp),
    ) {
        Column(Modifier.fillMaxSize().then(dragModifier).clip(RoundedCornerShape(18.dp))) {
            for (displayRank in 0..7) {
                Row(Modifier.weight(1f)) {
                    for (displayFile in 0..7) {
                        val file = if (flipped) 7 - displayFile else displayFile
                        val rank = if (flipped) displayRank else 7 - displayRank
                        val square = Square.of(file, rank)!!
                        val light = (file + rank) % 2 == 1
                        val isSelected = selected == square
                        val isLastMove = square == lastMove?.first || square == lastMove?.second
                        val isHint = square in hintSquares
                        val squareColor = if (isSelected) Coral else if (isLastMove) Sunny else if (light) lightColor else darkColor
                        Box(
                            Modifier.weight(1f).fillMaxHeight()
                                .semantics {
                                    val piece = position[square]
                                    contentDescription = if (piece == null) "${square.algebraic}, empty" else "${square.algebraic}, ${piece.side.name.lowercase()} ${piece.type.name.lowercase()}"
                                }
                                .background(squareColor)
                                .combinedClickable(
                                    onClick = { if (haptics) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove); onTap(square) },
                                    onLongClick = { if (haptics) haptic.performHapticFeedback(HapticFeedbackType.LongPress); onLongPressFallback(square) },
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            position[square]?.let { piece ->
                                ChessPiece(piece, pieceDepth, Modifier.fillMaxSize().padding(4.dp), pieceStyle)
                            }
                            if (isHint) Box(Modifier.size(if (position[square] == null) 10.dp else 36.dp).clip(CircleShape).background(Espresso.copy(if (position[square] == null) .48f else .08f)).then(if (position[square] == null) Modifier else Modifier.border(3.dp, Espresso.copy(.52f), CircleShape)))
                            if (showCoordinates && displayFile == 0) Text(
                                "${rank + 1}", fontSize = 9.sp, color = (if (light) darkColor else lightColor).copy(.8f),
                                modifier = Modifier.align(Alignment.TopStart).padding(2.dp),
                            )
                            if (showCoordinates && displayRank == 7) Text(
                                "${('a'.code + file).toChar()}", fontSize = 9.sp, color = (if (light) darkColor else lightColor).copy(.8f),
                                modifier = Modifier.align(Alignment.BottomEnd).padding(2.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChessPiece(piece: Piece, depth: Boolean, modifier: Modifier = Modifier, style: Int = 0) {
    Image(
        painter = painterResource(pieceDrawable(style, piece)),
        contentDescription = "${piece.side.name.lowercase()} ${piece.type.name.lowercase()}",
        contentScale = ContentScale.Fit,
        modifier = modifier,
    )
}

private fun pieceDrawable(style: Int, piece: Piece): Int {
    val key = "${if (piece.side == Side.WHITE) 'w' else 'b'}${piece.type.fen.lowercaseChar()}"
    return when (style.coerceIn(0, 4)) {
        1 -> when (key) {
            "wk" -> R.drawable.piece_fantasy_wk; "wq" -> R.drawable.piece_fantasy_wq
            "wr" -> R.drawable.piece_fantasy_wr; "wb" -> R.drawable.piece_fantasy_wb
            "wn" -> R.drawable.piece_fantasy_wn; "wp" -> R.drawable.piece_fantasy_wp
            "bk" -> R.drawable.piece_fantasy_bk; "bq" -> R.drawable.piece_fantasy_bq
            "br" -> R.drawable.piece_fantasy_br; "bb" -> R.drawable.piece_fantasy_bb
            "bn" -> R.drawable.piece_fantasy_bn; else -> R.drawable.piece_fantasy_bp
        }
        2 -> when (key) {
            "wk" -> R.drawable.piece_spatial_wk; "wq" -> R.drawable.piece_spatial_wq
            "wr" -> R.drawable.piece_spatial_wr; "wb" -> R.drawable.piece_spatial_wb
            "wn" -> R.drawable.piece_spatial_wn; "wp" -> R.drawable.piece_spatial_wp
            "bk" -> R.drawable.piece_spatial_bk; "bq" -> R.drawable.piece_spatial_bq
            "br" -> R.drawable.piece_spatial_br; "bb" -> R.drawable.piece_spatial_bb
            "bn" -> R.drawable.piece_spatial_bn; else -> R.drawable.piece_spatial_bp
        }
        3 -> when (key) {
            "wk" -> R.drawable.piece_celtic_wk; "wq" -> R.drawable.piece_celtic_wq
            "wr" -> R.drawable.piece_celtic_wr; "wb" -> R.drawable.piece_celtic_wb
            "wn" -> R.drawable.piece_celtic_wn; "wp" -> R.drawable.piece_celtic_wp
            "bk" -> R.drawable.piece_celtic_bk; "bq" -> R.drawable.piece_celtic_bq
            "br" -> R.drawable.piece_celtic_br; "bb" -> R.drawable.piece_celtic_bb
            "bn" -> R.drawable.piece_celtic_bn; else -> R.drawable.piece_celtic_bp
        }
        4 -> when (key) {
            "wk" -> R.drawable.piece_chessnut_wk; "wq" -> R.drawable.piece_chessnut_wq
            "wr" -> R.drawable.piece_chessnut_wr; "wb" -> R.drawable.piece_chessnut_wb
            "wn" -> R.drawable.piece_chessnut_wn; "wp" -> R.drawable.piece_chessnut_wp
            "bk" -> R.drawable.piece_chessnut_bk; "bq" -> R.drawable.piece_chessnut_bq
            "br" -> R.drawable.piece_chessnut_br; "bb" -> R.drawable.piece_chessnut_bb
            "bn" -> R.drawable.piece_chessnut_bn; else -> R.drawable.piece_chessnut_bp
        }
        else -> when (key) {
            "wk" -> R.drawable.piece_rhosgfx_wk; "wq" -> R.drawable.piece_rhosgfx_wq
            "wr" -> R.drawable.piece_rhosgfx_wr; "wb" -> R.drawable.piece_rhosgfx_wb
            "wn" -> R.drawable.piece_rhosgfx_wn; "wp" -> R.drawable.piece_rhosgfx_wp
            "bk" -> R.drawable.piece_rhosgfx_bk; "bq" -> R.drawable.piece_rhosgfx_bq
            "br" -> R.drawable.piece_rhosgfx_br; "bb" -> R.drawable.piece_rhosgfx_bb
            "bn" -> R.drawable.piece_rhosgfx_bn; else -> R.drawable.piece_rhosgfx_bp
        }
    }
}

@Composable
private fun BoardLookPicker(settings: app.chessmind.data.UserSettings, onUpdate: (app.chessmind.data.UserSettings) -> Unit) {
    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = SurfaceDark)) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 11.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Palette, contentDescription = null, tint = Accent, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("BOARD", color = Muted, fontFamily = JetBrainsMono, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(55.dp))
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                listOf(BoardLight to BoardDark, Color(0xFFEEEED2) to Color(0xFF769656), Color(0xFFD8E8F4) to Color(0xFF4D7EA8), Color(0xFFE6E6E6) to Color(0xFF5D6470)).forEachIndexed { index, colors ->
                    Box(
                        Modifier.weight(1f).height(32.dp).clip(RoundedCornerShape(9.dp))
                            .border(if (settings.boardTheme == index) 2.dp else 0.dp, Accent, RoundedCornerShape(10.dp))
                            .clickable { onUpdate(settings.copy(boardTheme = index)) },
                    ) {
                        Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(colors.first, colors.first, colors.second, colors.second))))
                    }
                }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.ViewInAr, contentDescription = null, tint = Accent, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("PIECES", color = Muted, fontFamily = JetBrainsMono, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(55.dp))
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                repeat(5) { index ->
                    Box(
                        Modifier.weight(1f).height(39.dp).clip(RoundedCornerShape(11.dp))
                            .background(if (settings.pieceStyle == index) Accent.copy(.14f) else Color.Transparent)
                            .border(if (settings.pieceStyle == index) 1.5.dp else 0.dp, Accent, RoundedCornerShape(11.dp))
                            .clickable { onUpdate(settings.copy(pieceStyle = index)) },
                        contentAlignment = Alignment.Center,
                    ) { ChessPiece(Piece(PieceType.KNIGHT, if (index % 2 == 0) Side.WHITE else Side.BLACK), false, Modifier.size(34.dp), index) }
                }
                }
            }
        }
    }
}

@Composable
private fun FenDialog(error: String?, onDismiss: () -> Unit, onLoad: (String) -> Unit) {
    var text by remember { mutableStateOf(Fen.encode(Position.START)) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Load FEN") },
        text = {
            Column {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Position") },
                    minLines = 3,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None),
                )
                if (error != null) Text(error, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
            }
        },
        confirmButton = { TextButton(onClick = { onLoad(text) }) { Text("Load") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun PgnDialog(error: String?, onDismiss: () -> Unit, onLoad: (String) -> Unit) {
    var text by remember { mutableStateOf("[Event \"Imported game\"]\n\n1. e4 e5 2. Nf3 Nc6 *") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Load PGN") },
        text = {
            Column {
                OutlinedTextField(value = text, onValueChange = { text = it }, label = { Text("Game notation") }, minLines = 7)
                if (error != null) Text(error, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
            }
        },
        confirmButton = { TextButton(onClick = { onLoad(text) }) { Text("Load final position") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun PositionDetailsDialog(
    castling: String,
    enPassant: String,
    error: String?,
    onToggle: (Char) -> Unit,
    onDismiss: () -> Unit,
    onSaveEnPassant: (String) -> Unit,
) {
    var ep by remember(enPassant) { mutableStateOf(enPassant) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Position details") },
        text = {
            Column {
                Text("Castling rights", color = Muted, fontSize = 12.sp)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    listOf('K', 'Q', 'k', 'q').forEach { symbol ->
                        val active = symbol in castling
                        TextButton(onClick = { onToggle(symbol) }, modifier = Modifier.weight(1f)) {
                            Text(symbol.toString(), color = if (active) Sunny else Muted, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                OutlinedTextField(value = ep, onValueChange = { ep = it }, label = { Text("En-passant square or -") })
                if (error != null) Text(error, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
            }
        },
        confirmButton = { TextButton(onClick = { onSaveEnPassant(ep) }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun LevelScreen(vm: MainViewModel) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).navigationBarsPadding()) {
        ScreenHeader("Choose analysis", onBack = { vm.navigate(AppScreen.SETUP) })
        Text("Choose your\nkind of answer", style = MaterialTheme.typography.headlineLarge)
        Text("Pick the depth and teaching style that feels right.", style = MaterialTheme.typography.bodyLarge, color = Muted, modifier = Modifier.padding(top = 8.dp, bottom = 20.dp))
        AnalysisLevel.entries.forEach { level ->
            LevelCard(level) { vm.chooseLevel(level) }
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun LevelCard(level: AnalysisLevel, onClick: () -> Unit) {
    val look = when (level) {
        AnalysisLevel.BEGINNER -> Triple(Color(0xFF74DDB5), R.drawable.art_practice, "Clear & friendly")
        AnalysisLevel.INTERMEDIATE -> Triple(Color(0xFFFFCF62), R.drawable.art_analysis, "Balanced coaching")
        AnalysisLevel.MASTER -> Triple(Color(0xFFA98AF4), R.drawable.art_success, "Plans & candidates")
        AnalysisLevel.GOD -> Triple(Color(0xFFFF8D78), R.drawable.art_setup_board, "Maximum engine depth")
    }
    Card(
        onClick = onClick, shape = RoundedCornerShape(25.dp),
        colors = CardDefaults.cardColors(look.first, if (level == AnalysisLevel.INTERMEDIATE) LightInk else Color.White),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier.fillMaxWidth().height(116.dp).shadow(10.dp, RoundedCornerShape(25.dp), spotColor = look.first.copy(.28f)),
    ) {
        Box(Modifier.fillMaxSize()) {
            Image(painterResource(look.second), null, contentScale = ContentScale.Fit, modifier = Modifier.size(142.dp).align(Alignment.CenterEnd).offset(x = 17.dp, y = 2.dp))
            Column(Modifier.align(Alignment.CenterStart).padding(start = 20.dp).width(205.dp)) {
                Text(level.title, fontSize = 21.sp, fontWeight = FontWeight.ExtraBold)
                Text(look.third, fontSize = 13.sp, color = LocalContentColor.current.copy(.78f), modifier = Modifier.padding(top = 4.dp))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                    Text(when (level) { AnalysisLevel.BEGINNER -> "QUICK"; AnalysisLevel.INTERMEDIATE -> "SMART"; AnalysisLevel.MASTER -> "DEEP"; AnalysisLevel.GOD -> "MAX" }, fontSize = 9.sp, letterSpacing = 1.2.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(5.dp)); Icon(Icons.Rounded.ChevronRight, null, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun AnalysisScreen(vm: MainViewModel) {
    val state = vm.state
    val result = state.result
    if (result == null) {
        LaunchedEffect(state.level) { vm.analyze(state.level) }
        AnalyzingScreen(state.level, vm::stopAnalysis)
        return
    }
    var exploredPositions by remember(result) { mutableStateOf(listOf(state.position)) }
    var selected by remember(result) { mutableStateOf<Square?>(null) }
    var pvIndex by remember(result) { mutableStateOf(0) }
    var followingLine by remember(result) { mutableStateOf(true) }
    var autoPlay by remember(result) { mutableStateOf(false) }
    var coachSide by remember(result) { mutableStateOf(if (state.flipped) Side.BLACK else Side.WHITE) }
    val boardPosition = exploredPositions.last()
    val lastMove = if (exploredPositions.size > 1) findTransitionMove(exploredPositions[exploredPositions.lastIndex - 1], boardPosition) else null
    val hints = selected?.let { from -> ChessRules.legalMoves(boardPosition).filter { it.from == from }.map { it.to }.toSet() }.orEmpty()

    fun play(move: Move, followsPv: Boolean) {
        exploredPositions = exploredPositions + ChessRules.apply(boardPosition, move)
        selected = null
        followingLine = followsPv
        if (followsPv) {
            pvIndex++
            if (pvIndex >= result.principalVariationUci.size) autoPlay = false
        }
    }

    LaunchedEffect(autoPlay, pvIndex, exploredPositions) {
        if (!autoPlay) return@LaunchedEffect
        delay(1_000)
        val uci = result.principalVariationUci.getOrNull(pvIndex)
        val move = uci?.let(::parseUciMove)
        val legal = ChessRules.legalMoves(exploredPositions.last())
        if (move != null && move in legal) play(move, true) else autoPlay = false
    }

    LazyColumn(modifier = Modifier.navigationBarsPadding(), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
        item { ScreenHeader("Analysis", onBack = { vm.navigate(AppScreen.LEVEL) }, trailing = state.level.title) }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                BoardLookPicker(state.settings, vm::updateSettings)
            }
        }
        item {
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(SurfaceDark).padding(5.dp), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                listOf(Side.WHITE to "White", Side.BLACK to "Black").forEach { (side, label) ->
                    val active = coachSide == side
                    Row(
                        Modifier.weight(1f).height(42.dp).clip(RoundedCornerShape(14.dp)).background(if (active) Accent else Color.Transparent)
                            .clickable { coachSide = side; if (state.flipped != (side == Side.BLACK)) vm.flip() }.padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center,
                    ) {
                        ChessPiece(Piece(PieceType.PAWN, side), true, Modifier.size(29.dp), state.settings.pieceStyle)
                        Spacer(Modifier.width(5.dp)); Text(label, color = if (active) Color.White else Ink, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
        item {
            AnimatedContent(targetState = boardPosition, transitionSpec = { fadeIn(tween(320)) togetherWith fadeOut(tween(220)) }, label = "boardMove") { shownPosition ->
            ChessBoard(
                shownPosition, state.flipped, selected,
                onTap = { square ->
                    val current = selected
                    if (current == null) {
                        if (boardPosition[square]?.side == boardPosition.sideToMove) selected = square
                    } else if (current == square) selected = null
                    else {
                        val move = ChessRules.legalMoves(boardPosition).firstOrNull { it.from == current && it.to == square && (it.promotion == null || it.promotion == PieceType.QUEEN) }
                        if (move != null) play(move, false) else selected = null
                    }
                },
                onLongPressFallback = {},
                showCoordinates = state.settings.coordinates,
                hintSquares = if (state.settings.legalHints) hints else emptySet(),
                lastMove = if (state.settings.showLastMove) lastMove?.let { it.from to it.to } else null,
                haptics = state.settings.haptics,
                highContrastBoard = state.settings.highContrastBoard,
                boardDepth = state.settings.boardDepth,
                pieceDepth = state.settings.pieceDepth,
                boardTheme = state.settings.boardTheme,
                pieceStyle = state.settings.pieceStyle,
                dragToMove = state.settings.dragToMove,
                onDragMove = { from, to ->
                    val move = ChessRules.legalMoves(boardPosition).firstOrNull { it.from == from && it.to == to && (it.promotion == null || it.promotion == PieceType.QUEEN) }
                    if (move != null) play(move, false)
                },
            )
            }
        }
        item {
            Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = SurfaceDark)) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    IconButton(onClick = {
                        autoPlay = false
                        if (exploredPositions.size > 1) {
                            exploredPositions = exploredPositions.dropLast(1)
                            if (followingLine && pvIndex > 0) pvIndex--
                            selected = null
                        }
                    }, enabled = exploredPositions.size > 1) { Icon(Icons.Rounded.SkipPrevious, contentDescription = "Previous move") }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(if (followingLine) "EXPLORE BEST LINE" else "FREE PLAY", style = MaterialTheme.typography.labelMedium, color = Accent)
                        Text(if (followingLine) "Move $pvIndex of ${result.principalVariationUci.size}" else "Try any legal move", style = MaterialTheme.typography.bodyMedium, color = Muted)
                    }
                    FilledIconButton(onClick = {
                        val uci = result.principalVariationUci.getOrNull(pvIndex)
                        val move = uci?.let { parseUciMove(it) }
                        if (move != null && move in ChessRules.legalMoves(boardPosition)) play(move, true)
                    }, enabled = followingLine && pvIndex < result.principalVariationUci.size, colors = IconButtonDefaults.filledIconButtonColors(containerColor = Accent)) { Icon(Icons.Rounded.SkipNext, contentDescription = "Next move") }
                }
                Row(Modifier.fillMaxWidth().padding(start = 12.dp, end = 12.dp, bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = { autoPlay = !autoPlay }, enabled = followingLine && (pvIndex < result.principalVariationUci.size || autoPlay), modifier = Modifier.weight(1f), shape = RoundedCornerShape(15.dp)) {
                        Icon(if (autoPlay) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, null); Spacer(Modifier.width(5.dp)); Text(if (autoPlay) "Pause" else "Auto line")
                    }
                    TextButton(onClick = { autoPlay = false; exploredPositions = listOf(state.position); selected = null; pvIndex = 0; followingLine = true }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Rounded.Refresh, null); Spacer(Modifier.width(6.dp)); Text("Reset")
                    }
                }
                TextButton(onClick = { vm.analyzeFrom(boardPosition) }, modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
                    Icon(Icons.Rounded.SmartToy, null); Spacer(Modifier.width(6.dp)); Text("Ask coach from here")
                }
            }
        }
        item {
            GlassCard {
                Text("BEST MOVE", style = MaterialTheme.typography.labelMedium, color = Muted)
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(result.bestMove, style = MaterialTheme.typography.displaySmall, color = Accent)
                        Text(result.evaluation, style = MaterialTheme.typography.titleLarge)
                        Text("Depth ${result.depth}", style = MaterialTheme.typography.labelMedium, color = Muted)
                    }
                    Image(painterResource(R.drawable.art_success), null, contentScale = ContentScale.Crop, modifier = Modifier.size(112.dp).offset(x = 12.dp))
                }
            }
        }
        item {
            GlassCard {
                Text("WHY THIS MOVE?", style = MaterialTheme.typography.labelMedium, color = Muted)
                Spacer(Modifier.height(10.dp))
                Text(result.explanation, style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(10.dp))
                result.reasons.forEach { Text("•  $it", color = Ink.copy(.78f), fontSize = 14.sp, modifier = Modifier.padding(vertical = 2.dp)) }
            }
        }
        item {
            GlassCard {
                Text("BEST LINE", style = MaterialTheme.typography.labelMedium, color = Muted)
                Spacer(Modifier.height(9.dp))
                Text(result.principalVariation.joinToString("  "), fontFamily = JetBrainsMono, fontSize = 14.sp, lineHeight = 21.sp)
            }
        }
        if (result.candidates.size > 1) item {
            GlassCard {
                Text("CANDIDATES", style = MaterialTheme.typography.labelMedium, color = Muted)
                result.candidates.forEachIndexed { index, candidate ->
                    Row(Modifier.fillMaxWidth().padding(top = 11.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("${index + 1}", color = Muted, modifier = Modifier.width(24.dp))
                        Text(candidate.move, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        Text(candidate.label, color = Muted, fontSize = 12.sp)
                        Spacer(Modifier.width(10.dp))
                        Text(candidate.evaluation, color = Accent)
                    }
                }
            }
        }
        item { Spacer(Modifier.height(12.dp)) }
    }
}

@Composable
private fun AnalyzingScreen(level: AnalysisLevel, onStop: () -> Unit) {
    var stage by remember { mutableStateOf(0) }
    val stages = listOf("Starting the local engine", "Checking tactics and captures", "Building a clear coach answer")
    LaunchedEffect(Unit) {
        while (true) { delay(900); stage = (stage + 1) % stages.size }
    }
    val transition = rememberInfiniteTransition(label = "analysis")
    val pulse by transition.animateFloat(
        initialValue = .96f, targetValue = 1.04f,
        animationSpec = infiniteRepeatable(tween(1100, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "pulse",
    )
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(260.dp).clip(RoundedCornerShape(54.dp)).background(Color(0xFFBBD9FF)), contentAlignment = Alignment.Center) {
                Image(painterResource(R.drawable.art_analysis), contentDescription = null, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize().padding(4.dp).scale(pulse))
            }
            Text("Finding the clearest line…", style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 28.dp))
            Text("${level.title} analysis", style = MaterialTheme.typography.labelMedium, color = Accent, modifier = Modifier.padding(top = 8.dp))
            Text(stages[stage], color = Muted, fontSize = 13.sp, modifier = Modifier.padding(top = 7.dp))
            Row(Modifier.padding(top = 22.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(3) { index -> Box(Modifier.size((9 + index * 2).dp).scale(if (index == 1) pulse else 1f).clip(CircleShape).background(listOf(Coral, Sunny, Accent)[index])) }
            }
            TextButton(onClick = onStop, modifier = Modifier.padding(top = 20.dp)) { Icon(Icons.Rounded.Stop, null); Spacer(Modifier.width(6.dp)); Text("Stop analysis") }
        }
    }
}

@Composable
private fun ReviewLoadingScreen(progress: Int, total: Int, onStop: () -> Unit) {
    val fraction = if (total == 0) 0f else progress.toFloat() / total
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(painterResource(R.drawable.art_analysis), null, contentScale = ContentScale.Fit, modifier = Modifier.size(230.dp))
            Text("Reviewing both sides", style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
            Text("Move ${progress.coerceAtMost(total)} of $total", color = Muted, modifier = Modifier.padding(top = 8.dp))
            LinearProgressIndicator(
                progress = { fraction },
                modifier = Modifier.fillMaxWidth().padding(top = 18.dp).height(9.dp).clip(CircleShape),
                color = Accent,
                trackColor = SurfaceDark,
            )
            Text("Deep checks are time-capped so this always finishes.", color = Muted, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 13.dp))
            TextButton(onClick = onStop, modifier = Modifier.padding(top = 14.dp)) { Icon(Icons.Rounded.Stop, null); Spacer(Modifier.width(6.dp)); Text("Close review") }
        }
    }
}

private fun parseUciMove(uci: String): Move? {
    if (uci.length !in 4..5) return null
    val from = Square.parse(uci.substring(0, 2)) ?: return null
    val to = Square.parse(uci.substring(2, 4)) ?: return null
    val promotion = uci.getOrNull(4)?.let { symbol -> PieceType.entries.firstOrNull { it.fen == symbol.lowercaseChar() } }
    return Move(from, to, promotion)
}

private fun findTransitionMove(before: Position, after: Position): Move? = ChessRules.legalMoves(before).firstOrNull { ChessRules.apply(before, it) == after }

@Composable
private fun PracticeScreen(vm: MainViewModel) {
    val state = vm.state
    val puzzle = vm.currentPuzzle()
    LazyColumn(
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("DAILY TRAINING", color = Accent, fontSize = 10.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold)
                    Text(puzzle.title, fontSize = 29.sp, fontWeight = FontWeight.ExtraBold)
                    Text(puzzle.subtitle, color = Muted, fontSize = 13.sp)
                }
                Box(Modifier.clip(RoundedCornerShape(16.dp)).background(Sunny).padding(horizontal = 13.dp, vertical = 10.dp)) {
                    Text("🔥 ${state.practiceStats.streak}", color = Ink, fontWeight = FontWeight.Bold)
                }
            }
        }
        item {
            val hints = if (state.settings.legalHints && state.practiceSelected != null) {
                app.chessmind.domain.model.ChessRules.legalMoves(state.practicePosition)
                    .filter { it.from == state.practiceSelected }.map { it.to }.toSet()
            } else emptySet()
            ChessBoard(
                state.practicePosition, false, state.practiceSelected, vm::tapPractice, {},
                showCoordinates = state.settings.coordinates,
                hintSquares = hints,
                haptics = state.settings.haptics,
                highContrastBoard = state.settings.highContrastBoard,
                boardDepth = state.settings.boardDepth,
                pieceDepth = state.settings.pieceDepth,
                boardTheme = state.settings.boardTheme,
                pieceStyle = state.settings.pieceStyle,
                dragToMove = state.settings.dragToMove,
                onDragMove = vm::dragPractice,
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatPill("Solved", state.practiceStats.solved.toString(), Modifier.weight(1f))
                val accuracy = if (state.practiceStats.solved == 0) 0 else state.practiceStats.correct * 100 / state.practiceStats.solved
                StatPill("Accuracy", "$accuracy%", Modifier.weight(1f))
                StatPill("Best", state.practiceStats.bestStreak.toString(), Modifier.weight(1f))
            }
        }
        item {
            GlassCard {
                Text(if (state.practiceResult == null) "HINT" else "RESULT", color = Accent, fontSize = 10.sp, letterSpacing = 1.4.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(7.dp))
                Text(state.practiceResult ?: puzzle.hint, fontSize = 15.sp, lineHeight = 21.sp)
                if (state.practiceResult != null) {
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = vm::nextPuzzle, colors = ButtonDefaults.buttonColors(Sunny, Ink), shape = RoundedCornerShape(14.dp)) { Text("Next position  →", fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

@Composable
private fun StatPill(label: String, value: String, modifier: Modifier) {
    Column(modifier.clip(RoundedCornerShape(18.dp)).background(Color.White.copy(.08f)).padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Sunny)
        Text(label, fontSize = 9.sp, color = Muted, letterSpacing = .6.sp)
    }
}

@Composable
private fun SavedScreen(vm: MainViewModel) {
    val positions = vm.state.savedPositions
    LazyColumn(
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text("SAVED LIBRARY", color = Accent, fontSize = 10.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold)
            Text("Your positions", fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
            Text("${positions.size} stored locally", color = Muted, fontSize = 13.sp)
        }
        if (positions.isEmpty()) item {
            GlassCard {
                Text("◇", fontSize = 44.sp, color = Purple)
                Text("Nothing saved yet", fontSize = 19.sp, fontWeight = FontWeight.Bold)
                Text("Save a position from an analysis result and it will appear here.", color = Muted, lineHeight = 19.sp)
            }
        }
        items(positions, key = { it.id }) { item ->
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(SurfaceDark)
                    .border(1.dp, Color.White.copy(.08f), RoundedCornerShape(22.dp)).clickable { vm.openSaved(item) }.padding(15.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(58.dp).clip(RoundedCornerShape(16.dp)).background(Brush.linearGradient(listOf(Purple, Color(0xFF5B43B5)))), contentAlignment = Alignment.Center) {
                    Text("♞", fontSize = 35.sp, color = Color.White)
                }
                Spacer(Modifier.width(13.dp))
                Column(Modifier.weight(1f)) {
                    Text(item.name, fontWeight = FontWeight.Bold)
                    Text("${item.bestMove}  •  ${item.evaluation}", color = Sunny, fontSize = 13.sp)
                    Text(item.fen.take(28) + "…", color = Muted, fontSize = 10.sp, maxLines = 1)
                }
                TextButton(onClick = { vm.deleteSaved(item.id) }) { Text("×", color = Coral, fontSize = 22.sp) }
            }
        }
    }
}

@Composable
private fun HistoryScreen(vm: MainViewModel) {
    val analyses = vm.state.history
    val matches = vm.state.matchHistory
    LazyColumn(modifier = Modifier.navigationBarsPadding(), contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
        item { ScreenHeader("History", onBack = { vm.navigate(AppScreen.HOME) }, trailing = "${matches.size + analyses.size} LOCAL") }
        if (matches.isEmpty() && analyses.isEmpty()) item {
            Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = SurfaceDark)) {
                Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(painterResource(R.drawable.art_empty_history), null, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxWidth().height(210.dp))
                    Text("Every game tells a story", style = MaterialTheme.typography.titleMedium)
                    Text("AI matches, friend games, and completed analyses will appear here.", color = Muted, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 5.dp))
                }
            }
        }
        if (matches.isNotEmpty()) item {
            Text("MATCHES", style = MaterialTheme.typography.labelMedium, color = Muted, modifier = Modifier.padding(top = 4.dp, bottom = 2.dp))
        }
        items(matches, key = { it.id }) { item ->
            val isAi = item.mode == GameMode.AI.name
            val league = leagueFor(item.opponentRating ?: item.ratingBefore)
            Card(shape = RoundedCornerShape(23.dp), colors = CardDefaults.cardColors(containerColor = SurfaceDark), modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(68.dp).clip(RoundedCornerShape(19.dp)).background(if (isAi) league.color.copy(.22f) else Color(0xFF69D3AE).copy(.25f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(painterResource(if (isAi) league.art else R.drawable.art_practice), null, contentScale = ContentScale.Fit, modifier = Modifier.size(78.dp))
                    }
                    Spacer(Modifier.width(13.dp))
                    Column(Modifier.weight(1f)) {
                        Text(item.result, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, maxLines = 2)
                        Text(
                            if (isAi) "AI · ${league.name} · ${item.opponentRating} pts" else "Local friend match",
                            color = Muted, fontSize = 11.sp, modifier = Modifier.padding(top = 3.dp),
                        )
                        Text(
                            "${if (item.timeMinutes == 0) "Unlimited" else "${item.timeMinutes} min"} · ${item.moves} moves · ${formatHistoryDate(item.createdAt)}",
                            color = Muted.copy(.78f), fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                    if (isAi) {
                        Surface(shape = RoundedCornerShape(12.dp), color = if (item.ratingChange >= 0) Color(0xFF69D3AE).copy(.25f) else Coral.copy(.16f)) {
                            Text(
                                if (item.ratingChange >= 0) "+${item.ratingChange}" else "${item.ratingChange}",
                                color = if (item.ratingChange >= 0) Color(0xFF176B52) else Coral,
                                fontFamily = JetBrainsMono, fontWeight = FontWeight.Bold, fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 7.dp),
                            )
                        }
                    }
                }
            }
        }
        if (analyses.isNotEmpty()) item {
            Text("POSITION ANALYSES", style = MaterialTheme.typography.labelMedium, color = Muted, modifier = Modifier.padding(top = 10.dp, bottom = 2.dp))
        }
        items(analyses, key = { it.id }) { item ->
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(SurfaceDark)
                    .clickable { vm.openHistory(item) }.padding(15.dp), verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(painterResource(R.drawable.art_history), null, contentScale = ContentScale.Crop, modifier = Modifier.size(58.dp))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(item.bestMove, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(item.level, color = Muted, fontSize = 12.sp)
                    Text(item.fen.take(34) + "…", color = Muted.copy(.72f), fontSize = 9.sp, maxLines = 1)
                }
                Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = Accent)
            }
        }
    }
}

private fun formatHistoryDate(timestamp: Long): String =
    SimpleDateFormat("d MMM · h:mm a", Locale.getDefault()).format(Date(timestamp))

@Composable
private fun SettingsScreen(vm: MainViewModel) {
    val context = LocalContext.current
    val settings = vm.state.settings
    var showClearHistory by remember { mutableStateOf(false) }
    val backupPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            runCatching { context.contentResolver.openInputStream(it)?.bufferedReader()?.use { reader -> reader.readText() } }
                .getOrNull()?.let(vm::importData)
        }
    }
    LazyColumn(contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 0.dp, bottom = 36.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { ScreenHeader("Settings", onBack = { vm.navigate(AppScreen.HOME) }) }
        item {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Accent),
                modifier = Modifier.fillMaxWidth().height(132.dp),
            ) {
                Box(Modifier.fillMaxSize()) {
                    Canvas(Modifier.fillMaxSize()) {
                        drawCircle(Color.White.copy(.13f), size.width * .34f, Offset(size.width * .94f, size.height * .05f))
                        drawCircle(Sunny.copy(.28f), size.width * .18f, Offset(size.width * .78f, size.height * .92f))
                    }
                    Image(painterResource(R.drawable.art_settings), null, contentScale = ContentScale.Fit, modifier = Modifier.align(Alignment.CenterEnd).size(155.dp).offset(x = 25.dp, y = 8.dp))
                    Column(Modifier.padding(22.dp).width(205.dp)) {
                        Text("MAKE IT YOURS", color = Sunny, style = MaterialTheme.typography.labelMedium)
                        Text("A board that feels like you", color = Color.White, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(top = 7.dp))
                    }
                }
            }
        }
        item {
            SettingsSection("Appearance", Icons.Rounded.Palette) {
                SettingChoice("Theme", Icons.Rounded.LightMode, if (settings.darkMode) "Dark" else "Light") {
                    ChoicePill("Light", !settings.darkMode) { vm.updateSettings(settings.copy(darkMode = false)) }
                    ChoicePill("Dark", settings.darkMode) { vm.updateSettings(settings.copy(darkMode = true)) }
                }
                SettingInfo(Icons.Rounded.GridOn, "Board & pieces", "Four board palettes and five filled piece sets")
                BoardLookPicker(settings, vm::updateSettings)
                SettingToggle(Icons.Rounded.AutoAwesome, "Animations", "Smooth and responsive", settings.animations) { vm.updateSettings(settings.copy(animations = it)) }
            }
        }
        item {
            SettingsSection("Board controls", Icons.Rounded.TouchApp) {
                SettingToggle(Icons.Rounded.TouchApp, "Drag to move", "Slide pieces directly between squares", settings.dragToMove) { vm.updateSettings(settings.copy(dragToMove = it)) }
                SettingToggle(Icons.Rounded.GridOn, "Coordinates", "Show files and ranks", settings.coordinates) { vm.updateSettings(settings.copy(coordinates = it)) }
                SettingToggle(Icons.Rounded.Visibility, "Legal move hints", "Reveal valid destinations", settings.legalHints) { vm.updateSettings(settings.copy(legalHints = it)) }
                SettingToggle(Icons.Rounded.History, "Last move highlight", "Keep the previous move visible", settings.showLastMove) { vm.updateSettings(settings.copy(showLastMove = it)) }
                SettingToggle(Icons.Rounded.Vibration, "Haptic feedback", "A soft response on every move", settings.haptics) { vm.updateSettings(settings.copy(haptics = it)) }
                SettingToggle(Icons.Rounded.AutoAwesome, "High contrast", "Extra separation between squares", settings.highContrastBoard) { vm.updateSettings(settings.copy(highContrastBoard = it)) }
            }
        }
        item {
            SettingsSection("Matches", Icons.Rounded.Groups) {
                SettingInfo(Icons.Rounded.History, "Default clock", if (settings.defaultTimeMinutes == 0) "Unlimited" else "${settings.defaultTimeMinutes} minutes per side")
                Spacer(Modifier.height(8.dp))
                TimeControlPicker(settings.defaultTimeMinutes) { vm.updateSettings(settings.copy(defaultTimeMinutes = it)) }
                SettingToggle(Icons.Rounded.FlipCameraAndroid, "Auto-flip friend board", "Turn the board after every local move", settings.autoFlipFriend) { vm.updateSettings(settings.copy(autoFlipFriend = it)) }
                SettingToggle(Icons.Rounded.Visibility, "Move list", "Show notation during a match", settings.showMoveList) { vm.updateSettings(settings.copy(showMoveList = it)) }
                SettingToggle(Icons.Rounded.Flag, "Confirm resignation", "Prevent accidental match losses", settings.confirmResign) { vm.updateSettings(settings.copy(confirmResign = it)) }
            }
        }
        item {
            SettingsSection("AI opponent", Icons.Rounded.SmartToy) {
                SettingInfo(Icons.Rounded.Psychology, "Rival strength", when (settings.aiStrength) { -1 -> "Gentler opponents and more natural mistakes"; 1 -> "Stronger opponents with cleaner play"; else -> "Adapts just below your current league" })
                Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ChoicePill("Gentle", settings.aiStrength == -1, Modifier.weight(1f)) { vm.updateSettings(settings.copy(aiStrength = -1)) }
                    ChoicePill("Adaptive", settings.aiStrength == 0, Modifier.weight(1f)) { vm.updateSettings(settings.copy(aiStrength = 0)) }
                    ChoicePill("Tough", settings.aiStrength == 1, Modifier.weight(1f)) { vm.updateSettings(settings.copy(aiStrength = 1)) }
                }
                SettingChoice("AI move pace", Icons.Rounded.SmartToy, when (settings.aiMoveDelayMs) { 350 -> "Fast"; 1_000 -> "Calm"; else -> "Natural" }) {
                    ChoicePill("Fast", settings.aiMoveDelayMs == 350) { vm.updateSettings(settings.copy(aiMoveDelayMs = 350)) }
                    ChoicePill("Natural", settings.aiMoveDelayMs == 650) { vm.updateSettings(settings.copy(aiMoveDelayMs = 650)) }
                    ChoicePill("Calm", settings.aiMoveDelayMs == 1_000) { vm.updateSettings(settings.copy(aiMoveDelayMs = 1_000)) }
                }
                SettingToggle(Icons.Rounded.AutoAwesome, "Varied openings", "Avoid repeating the same early replies", settings.aiVariedOpenings) { vm.updateSettings(settings.copy(aiVariedOpenings = it)) }
                SettingToggle(Icons.Rounded.Visibility, "Thinking status", "Show when the opponent is calculating", settings.showAiThinking) { vm.updateSettings(settings.copy(showAiThinking = it)) }
                SettingInfo(Icons.Rounded.Handshake, "Draw behavior", when (settings.aiDrawPolicy) { 0 -> "Only in long, completely level games"; 2 -> "More willing in balanced positions"; else -> "Offers and accepts fair practical draws" })
                Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ChoicePill("Rare", settings.aiDrawPolicy == 0, Modifier.weight(1f)) { vm.updateSettings(settings.copy(aiDrawPolicy = 0)) }
                    ChoicePill("Fair", settings.aiDrawPolicy == 1, Modifier.weight(1f)) { vm.updateSettings(settings.copy(aiDrawPolicy = 1)) }
                    ChoicePill("Often", settings.aiDrawPolicy == 2, Modifier.weight(1f)) { vm.updateSettings(settings.copy(aiDrawPolicy = 2)) }
                }
            }
        }
        item {
            SettingsSection("Analysis", Icons.Rounded.Analytics) {
                SettingInfo(Icons.Rounded.Speed, "Adaptive engine", "Balanced automatically for speed, depth, and battery")
                SettingInfo(Icons.Rounded.Info, "ChessMind 4.0.2", "Stockfish 19 on supported ARM devices")
            }
        }
        item {
            SettingsSection("Your data", Icons.Rounded.History) {
                SettingAction(
                    icon = Icons.Rounded.DeleteSweep, title = "Clear history",
                    subtitle = "Remove match and analysis history", tint = Coral,
                    onClick = { showClearHistory = true },
                )
                SettingAction(
                    icon = Icons.Rounded.AutoAwesome, title = "Export backup",
                    subtitle = "Share your profile, settings, games, and analyses",
                    onClick = {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/json"
                            putExtra(Intent.EXTRA_TEXT, vm.exportData())
                            putExtra(Intent.EXTRA_SUBJECT, "ChessMind backup")
                        }
                        context.startActivity(Intent.createChooser(intent, "Export ChessMind data"))
                    },
                )
                SettingAction(
                    icon = Icons.Rounded.PhotoLibrary, title = "Import backup",
                    subtitle = "Restore a ChessMind JSON backup", tint = Accent,
                    onClick = { backupPicker.launch("application/json") },
                )
            }
            vm.state.fenError?.let { error -> Text(error, color = Coral, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp)) }
        }
        item {
            Row(Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 18.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Text("Made With 💙 By ", color = Muted, fontSize = 12.sp)
                Text(
                    "Veil", color = Accent, fontWeight = FontWeight.Bold, fontSize = 12.sp,
                    modifier = Modifier.clickable { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://venusapp.in"))) },
                )
            }
        }
    }
    if (showClearHistory) {
        AlertDialog(
            onDismissRequest = { showClearHistory = false },
            title = { Text("Clear all history?") },
            text = { Text("This removes completed matches and position analyses from this device. Your rating and settings stay intact.") },
            confirmButton = {
                TextButton(onClick = { vm.clearHistory(); showClearHistory = false }) { Text("Clear", color = Coral, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { showClearHistory = false }) { Text("Keep history") } },
        )
    }
}

@Composable
private fun SettingsSection(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(horizontal = 18.dp, vertical = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = Accent, modifier = Modifier.size(25.dp))
                Spacer(Modifier.width(10.dp))
                Text(title, style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(6.dp))
            content()
        }
    }
}

@Composable
private fun SettingChoice(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    choices: @Composable RowScope.() -> Unit,
) {
    Row(Modifier.fillMaxWidth().padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = Muted, modifier = Modifier.size(23.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(value, color = Muted, fontSize = 11.sp)
        }
        Row(
            Modifier.clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.background.copy(.72f)).padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp), content = choices,
        )
    }
}

@Composable
private fun ChoicePill(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val restingColor = if (MaterialTheme.colorScheme.background == NightBackground) Color(0xFF263247) else Color(0xFFFFFBF4)
    Surface(
        onClick = onClick, modifier = modifier,
        color = if (selected) Accent else restingColor,
        contentColor = if (selected) Color.White else Ink,
        shape = RoundedCornerShape(11.dp),
        shadowElevation = if (selected) 3.dp else 0.dp,
    ) { Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp)) }
}

@Composable
private fun SettingToggle(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = Muted, modifier = Modifier.size(23.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = Muted, fontSize = 12.sp)
        }
        val restingColor = if (MaterialTheme.colorScheme.background == NightBackground) Color(0xFF263247) else Color(0xFFFFFBF4)
        Switch(
            checked = checked, onCheckedChange = onChecked,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White, checkedTrackColor = Accent, checkedBorderColor = Color.Transparent,
                uncheckedThumbColor = Muted, uncheckedTrackColor = restingColor, uncheckedBorderColor = Color.Transparent,
            ),
        )
    }
}

@Composable
private fun SettingAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    tint: Color = Ink,
    onClick: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().padding(top = 4.dp).clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick)
            .padding(horizontal = 0.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(23.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, color = Ink)
            Text(subtitle, color = Muted, fontSize = 12.sp)
        }
        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = Muted, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun SettingInfo(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String) {
    Row(Modifier.fillMaxWidth().padding(top = 13.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = Muted, modifier = Modifier.size(23.dp))
        Spacer(Modifier.width(12.dp))
        Column { Text(title, fontWeight = FontWeight.SemiBold); Text(subtitle, color = Muted, fontSize = 12.sp) }
    }
}

@Composable
private fun ScreenHeader(title: String, onBack: () -> Unit, trailing: String? = null) {
    Row(Modifier.fillMaxWidth().height(68.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back") }
        Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
        if (trailing != null) Text(trailing.uppercase(), style = MaterialTheme.typography.labelMedium, color = Accent)
    }
}

@Composable
private fun GlassCard(content: @Composable ColumnScope.() -> Unit) {
    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = SurfaceDark), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp), content = content)
    }
}

@Composable
private fun FloatingDock(active: AppScreen, onNavigate: (AppScreen) -> Unit, modifier: Modifier = Modifier) {
    val haptic = LocalHapticFeedback.current
    Surface(
        modifier.navigationBarsPadding().padding(bottom = 14.dp).width(226.dp).height(68.dp).shadow(18.dp, RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(30.dp), color = SurfaceLight, tonalElevation = 6.dp,
    ) {
        Row(Modifier.padding(7.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            listOf(
                Triple(AppScreen.HOME, "Home", Icons.Rounded.Home),
                Triple(AppScreen.HISTORY, "History", Icons.Rounded.History),
            ).forEach { (screen, label, icon) ->
                val selected = active == screen
                Surface(
                    onClick = { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove); onNavigate(screen) },
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    shape = RoundedCornerShape(21.dp),
                    color = if (selected) Accent else Color.Transparent,
                    contentColor = if (selected) Color.White else Muted,
                    shadowElevation = if (selected) 7.dp else 0.dp,
                ) {
                    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        Icon(icon, contentDescription = label, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(7.dp))
                        Text(label, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
