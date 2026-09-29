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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CameraAlt
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
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.scale
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
import app.chessmind.domain.engine.AnalysisLevel
import app.chessmind.domain.model.Fen
import app.chessmind.domain.model.Piece
import app.chessmind.domain.model.PieceType
import app.chessmind.domain.model.Position
import app.chessmind.domain.model.Side
import app.chessmind.domain.model.Square
import app.chessmind.domain.model.ChessRules
import app.chessmind.domain.model.Move

private val Ink = Color(0xFF211A17)
private val SurfaceDark = Color(0xFF302824)
private val SurfaceLight = Color(0xFF443A34)
private val Accent = Color(0xFF9DB58C)
private val Sunny = Color(0xFFF0C66B)
private val Coral = Color(0xFFE58E72)
private val Purple = Color(0xFF9A899E)
private val Muted = Color(0xFFC5B9AE)
private val BoardLight = Color(0xFFF0E3CB)
private val BoardDark = Color(0xFF8FA17C)
private val Espresso = Color(0xFF3B2A22)
private val SpaceGrotesk = FontFamily(Font(R.font.space_grotesk))
private val JetBrainsMono = FontFamily(Font(R.font.jetbrains_mono))

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }
        setContent { ChessMindTheme { CompositionLocalProvider(LocalContentColor provides Color.White) { ChessMindApp() } } }
    }
}

@Composable
private fun ChessMindTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Accent,
            background = Ink,
            surface = SurfaceDark,
            onPrimary = Ink,
            secondary = Coral,
            tertiary = Sunny,
            surfaceVariant = SurfaceLight,
            onBackground = Color(0xFFFFF8EF),
            onSurface = Color(0xFFFFF8EF),
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
    BackHandler(enabled = state.screen !in setOf(AppScreen.HOME, AppScreen.ONBOARDING)) {
        vm.navigate(
            when (state.screen) {
                AppScreen.ANALYSIS -> AppScreen.LEVEL
                AppScreen.LEVEL -> AppScreen.SETUP
                AppScreen.PROFILE, AppScreen.SCANNER, AppScreen.IMAGE_REVIEW, AppScreen.SETUP, AppScreen.HISTORY,
                AppScreen.SETTINGS -> AppScreen.HOME
                else -> AppScreen.HOME
            }
        )
    }
    Box(Modifier.fillMaxSize().background(Ink)) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(Coral.copy(.07f), radius = size.width * .72f, center = Offset(size.width * .13f, -size.height * .08f))
            drawCircle(Accent.copy(.05f), radius = size.width * .64f, center = Offset(size.width * .98f, size.height * .42f))
        }
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
        ) { padding ->
            AnimatedContent(
                targetState = state.screen,
                transitionSpec = {
                    val duration = if (state.settings.animations) 150 else 0
                    fadeIn(tween(duration)) togetherWith fadeOut(tween(duration))
                },
                label = "screen",
                modifier = Modifier.fillMaxSize().padding(padding).statusBarsPadding(),
            ) { screen ->
                when (screen) {
                    AppScreen.ONBOARDING -> OnboardingScreen(vm)
                    AppScreen.HOME -> HomeScreen(vm)
                    AppScreen.PROFILE -> ProfileScreen(vm)
                    AppScreen.SCANNER -> ScannerScreen(vm)
                    AppScreen.IMAGE_REVIEW -> ImageReviewScreen(vm)
                    AppScreen.SETUP -> SetupScreen(vm)
                    AppScreen.LEVEL -> LevelScreen(vm)
                    AppScreen.ANALYSIS -> AnalysisScreen(vm)
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
private fun OnboardingScreen(vm: MainViewModel) {
    var page by remember { mutableStateOf(0) }
    val content = listOf(
        Triple("SCAN", "Point. Capture. Solve.", "Use your camera or choose a board image from your gallery."),
        Triple("ANALYZE", "Find a stronger move.", "Choose a learning level and get legal candidates, lines, and grounded explanations."),
        Triple("EXPLORE", "Play through the line.", "Step forward, step back, or try your own legal moves on the analysis board."),
    )[page]
    Box(Modifier.fillMaxSize().padding(24.dp).statusBarsPadding()) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(listOf(Coral, Purple, Accent)[page].copy(.18f), radius = size.width * .75f, center = Offset(size.width * .82f, size.height * .22f))
        }
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("CHESSMIND", color = Accent, letterSpacing = 2.sp, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                TextButton(onClick = vm::completeOnboarding) { Text("Skip", color = Muted) }
            }
            Spacer(Modifier.weight(.55f))
            Box(
                Modifier.size(190.dp).align(Alignment.CenterHorizontally).shadow(26.dp, RoundedCornerShape(54.dp))
                    .clip(RoundedCornerShape(54.dp)).background(listOf(Coral, Sunny, Accent)[page]),
                contentAlignment = Alignment.Center,
            ) { Text(listOf("⌗", "♞", "♟")[page], color = Color.White, fontSize = 112.sp) }
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
                Text("CHESSMIND", style = MaterialTheme.typography.labelMedium, color = Muted)
                FilledIconButton(onClick = { vm.navigate(AppScreen.SETTINGS) }, colors = IconButtonDefaults.filledIconButtonColors(containerColor = SurfaceLight, contentColor = Color.White)) {
                    Icon(Icons.Rounded.Settings, contentDescription = "Settings")
                }
            }
        }
        item {
            Column {
                Text("Hello, ${vm.state.profile.name.substringBefore(' ')}", style = MaterialTheme.typography.labelMedium, color = Accent)
                Text("What will you\nplay next?", style = MaterialTheme.typography.displaySmall, modifier = Modifier.padding(top = 6.dp))
            }
        }
        item {
            Card(
                onClick = { vm.navigate(AppScreen.SCANNER) },
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1D7B5), contentColor = Espresso),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Box(Modifier.fillMaxWidth().height(300.dp)) {
                    Image(
                        painter = painterResource(R.drawable.chess_hero), contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxWidth().height(220.dp).align(Alignment.TopCenter).padding(horizontal = 2.dp),
                    )
                    Column(Modifier.align(Alignment.BottomStart).padding(22.dp)) {
                        Text("SCAN & SOLVE", style = MaterialTheme.typography.labelMedium, color = Espresso.copy(.65f))
                        Text("Turn a board into\na clear plan.", style = MaterialTheme.typography.headlineMedium, color = Espresso)
                    }
                    FilledIconButton(
                        onClick = { vm.navigate(AppScreen.SCANNER) },
                        modifier = Modifier.align(Alignment.BottomEnd).padding(22.dp).size(54.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = Espresso, contentColor = Color(0xFFFFF8EF)),
                    ) { Icon(Icons.Rounded.CameraAlt, contentDescription = "Scan board") }
                }
            }
        }
        item {
            Text("Choose a starting point", style = MaterialTheme.typography.titleMedium)
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                HomeActionCard("Scan board", "Camera or photo", Icons.Rounded.CenterFocusStrong, Coral, Modifier.weight(1f)) { vm.navigate(AppScreen.SCANNER) }
                HomeActionCard("Set position", "Build it yourself", Icons.Rounded.Edit, Accent, Modifier.weight(1f)) { vm.navigate(AppScreen.SETUP) }
            }
        }
        if (vm.state.history.isNotEmpty()) item {
            Card(
                onClick = { vm.navigate(AppScreen.HISTORY) }, shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            ) {
                Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(48.dp).clip(CircleShape).background(Sunny), contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.History, contentDescription = null, tint = Espresso)
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Continue your last analysis", style = MaterialTheme.typography.titleMedium)
                        Text("${vm.state.history.first().bestMove} · ${vm.state.history.first().level}", style = MaterialTheme.typography.bodyMedium, color = Muted)
                    }
                    Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = Muted)
                }
            }
        }
    }
}

@Composable
private fun HomeActionCard(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = modifier.height(144.dp), shape = RoundedCornerShape(26.dp), colors = CardDefaults.cardColors(containerColor = color, contentColor = Espresso)) {
        Column(Modifier.fillMaxSize().padding(18.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(30.dp))
            Column { Text(title, style = MaterialTheme.typography.titleMedium); Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = Espresso.copy(.66f)) }
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
        Spacer(Modifier.height(22.dp))
        Box(Modifier.align(Alignment.CenterHorizontally)) {
            UserAvatar(imageUri, name, Modifier.size(132.dp))
            FilledIconButton(onClick = { picker.launch(arrayOf("image/*")) }, modifier = Modifier.align(Alignment.BottomEnd), colors = IconButtonDefaults.filledIconButtonColors(containerColor = Coral, contentColor = Espresso)) {
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
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var granted by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted = it }
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { vm.reviewImage(it.toString()) }
    }
    LaunchedEffect(Unit) { if (!granted) permissionLauncher.launch(Manifest.permission.CAMERA) }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
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
            Canvas(Modifier.fillMaxSize().padding(horizontal = 28.dp, vertical = 126.dp)) {
                val side = minOf(size.width, size.height)
                val left = (size.width - side) / 2f
                val top = (size.height - side) / 2f
                drawRoundRect(Color.White.copy(.12f), Offset(left, top), Size(side, side), CornerRadius(28.dp.toPx()), style = Stroke(2.dp.toPx()))
                val corner = 36.dp.toPx()
                val width = 4.dp.toPx()
                listOf(
                    Offset(left, top) to Offset(left + corner, top), Offset(left, top) to Offset(left, top + corner),
                    Offset(left + side, top) to Offset(left + side - corner, top), Offset(left + side, top) to Offset(left + side, top + corner),
                    Offset(left, top + side) to Offset(left + corner, top + side), Offset(left, top + side) to Offset(left, top + side - corner),
                    Offset(left + side, top + side) to Offset(left + side - corner, top + side), Offset(left + side, top + side) to Offset(left + side, top + side - corner),
                ).forEach { (start, end) -> drawLine(Accent, start, end, width, StrokeCap.Round) }
            }
            Column(Modifier.fillMaxSize().padding(20.dp).statusBarsPadding().navigationBarsPadding(), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(44.dp).clip(CircleShape).background(Color.Black.copy(.42f)).clickable { vm.navigate(AppScreen.HOME) }, contentAlignment = Alignment.Center) { Text("×", fontSize = 25.sp) }
                    Spacer(Modifier.weight(1f))
                    Box(Modifier.clip(RoundedCornerShape(18.dp)).background(Color.Black.copy(.46f)).padding(horizontal = 14.dp, vertical = 9.dp)) {
                        Text("ALIGN THE BOARD", fontSize = 10.sp, letterSpacing = 1.2.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.weight(1f)); Spacer(Modifier.size(44.dp))
                }
                Spacer(Modifier.weight(1f))
                Text("Keep all four corners inside the frame", fontSize = 13.sp, modifier = Modifier.clip(RoundedCornerShape(14.dp)).background(Color.Black.copy(.46f)).padding(horizontal = 14.dp, vertical = 8.dp))
                Spacer(Modifier.height(18.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                    FilledIconButton(
                        onClick = { imagePicker.launch(arrayOf("image/*")) }, modifier = Modifier.size(54.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = SurfaceDark.copy(.9f), contentColor = Color.White),
                    ) { Icon(Icons.Rounded.PhotoLibrary, contentDescription = "Choose image from gallery") }
                    Box(
                        Modifier.size(76.dp).semantics { contentDescription = "Capture chess board" }.clip(CircleShape).background(Color.White).border(6.dp, Color.White.copy(.35f), CircleShape).clickable {
                            val capture = imageCapture ?: return@clickable
                            val file = File(context.cacheDir, "scan-${System.currentTimeMillis()}.jpg")
                            capture.takePicture(
                                ImageCapture.OutputFileOptions.Builder(file).build(), ContextCompat.getMainExecutor(context),
                                object : ImageCapture.OnImageSavedCallback {
                                    override fun onImageSaved(output: ImageCapture.OutputFileResults) = vm.reviewImage(Uri.fromFile(file).toString())
                                    override fun onError(exception: ImageCaptureException) = Unit
                                },
                            )
                        },
                    )
                    Spacer(Modifier.size(54.dp))
                }
                Spacer(Modifier.height(24.dp))
            }
        } else {
            Column(Modifier.align(Alignment.Center).padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("⌗", fontSize = 58.sp, color = Accent)
                Text("Camera access is needed", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("ChessMind only uses the camera while this scanner is open.", color = Muted, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 12.dp))
                Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }, colors = ButtonDefaults.buttonColors(Sunny, Ink)) { Text("Allow camera") }
                TextButton(onClick = { imagePicker.launch(arrayOf("image/*")) }) { Icon(Icons.Rounded.PhotoLibrary, null); Spacer(Modifier.width(7.dp)); Text("Choose from gallery") }
                TextButton(onClick = { vm.navigate(AppScreen.SETUP) }) { Text("Set up manually") }
            }
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
    var showFen by remember { mutableStateOf(false) }
    var showPgn by remember { mutableStateOf(false) }
    var showDetails by remember { mutableStateOf(false) }
    val context = LocalContext.current
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp).navigationBarsPadding()) {
        ScreenHeader("Set up position", onBack = { vm.navigate(AppScreen.HOME) })
        state.fenError?.let { message ->
            Row(
                Modifier.fillMaxWidth().padding(bottom = 8.dp).clip(RoundedCornerShape(14.dp))
                    .background(Coral.copy(.14f)).padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(message, color = Color.White, fontSize = 11.sp, modifier = Modifier.weight(1f))
                Text("×", color = Coral, fontSize = 20.sp, modifier = Modifier.clickable(onClick = vm::dismissFenError))
            }
        }
        ChessBoard(
            position = state.position,
            flipped = state.flipped,
            selected = state.selected,
            onTap = vm::tapSquare,
            onLongPressFallback = vm::erase,
            showCoordinates = state.settings.coordinates,
            highContrastBoard = state.settings.highContrastBoard,
            haptics = state.settings.haptics,
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
        PiecePalette(state.palettePiece, vm::setPalette)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = { showFen = true }, modifier = Modifier.weight(1f)) { Text("Load FEN") }
            TextButton(onClick = { showPgn = true }, modifier = Modifier.weight(1f)) { Text("Load PGN") }
            TextButton(onClick = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("ChessMind FEN", Fen.encode(state.position)))
            }, modifier = Modifier.weight(1f)) { Text("Copy FEN") }
        }
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = { vm.proceedToLevel() },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Sunny, contentColor = Ink),
        ) { Text("Analyze position  →", fontWeight = FontWeight.Bold) }
        Spacer(Modifier.height(14.dp))
    }
    if (showFen) FenDialog(
        error = state.fenError,
        onDismiss = { showFen = false; vm.dismissFenError() },
        onLoad = { if (vm.loadFen(it)) showFen = false },
    )
    if (showPgn) PgnDialog(
        error = state.fenError,
        onDismiss = { showPgn = false; vm.dismissFenError() },
        onLoad = { if (vm.loadPgn(it)) showPgn = false },
    )
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
                PaletteCell("↕", selected == null) { onSelect(null) }
            }
            items(Side.entries.flatMap { side -> PieceType.entries.map { Piece(it, side) } }) { piece ->
                PaletteCell(piece.symbol, selected == piece) { onSelect(piece) }
            }
        }
    }
}

@Composable
private fun PaletteCell(text: String, active: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.size(44.dp).clip(RoundedCornerShape(12.dp))
            .background(if (active) Accent else SurfaceLight)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Text(text, fontSize = 27.sp, color = if (active) Ink else Color.White) }
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
) {
    val haptic = LocalHapticFeedback.current
    val lightColor = if (highContrastBoard) Color(0xFFF4F0DF) else BoardLight
    val darkColor = if (highContrastBoard) Color(0xFF6E8060) else BoardDark
    Box(
        Modifier.fillMaxWidth().aspectRatio(1f).shadow(18.dp, RoundedCornerShape(26.dp), spotColor = Color.Black.copy(.34f))
            .clip(RoundedCornerShape(26.dp)).background(Espresso).padding(8.dp),
    ) {
        Column(Modifier.fillMaxSize().clip(RoundedCornerShape(18.dp))) {
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
                        Box(
                            Modifier.weight(1f).fillMaxHeight()
                                .semantics {
                                    val piece = position[square]
                                    contentDescription = if (piece == null) "${square.algebraic}, empty" else "${square.algebraic}, ${piece.side.name.lowercase()} ${piece.type.name.lowercase()}"
                                }
                                .background(if (isSelected) Coral else if (isLastMove) Sunny else if (light) lightColor else darkColor)
                                .combinedClickable(
                                    onClick = { if (haptics) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove); onTap(square) },
                                    onLongClick = { if (haptics) haptic.performHapticFeedback(HapticFeedbackType.LongPress); onLongPressFallback(square) },
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            position[square]?.let { piece ->
                                Text(
                                    piece.symbol,
                                    fontFamily = FontFamily.Serif,
                                    fontSize = 35.sp,
                                    color = if (piece.side == Side.WHITE) Color(0xFFFFF8EA) else Color(0xFF261C17),
                                    style = TextStyle(shadow = Shadow(Color.Black.copy(.28f), Offset(0f, 2f), 2.2f)),
                                )
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
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp).navigationBarsPadding()) {
        ScreenHeader("Choose analysis", onBack = { vm.navigate(AppScreen.SETUP) })
        Text("Choose your\nkind of answer", style = MaterialTheme.typography.headlineLarge)
        Text("From a simple idea to the deepest practical line.", style = MaterialTheme.typography.bodyLarge, color = Muted, modifier = Modifier.padding(top = 8.dp, bottom = 24.dp))
        AnalysisLevel.entries.forEach { level ->
            LevelCard(level) { vm.chooseLevel(level) }
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun LevelCard(level: AnalysisLevel, onClick: () -> Unit) {
    val cardColor = when (level) {
        AnalysisLevel.BEGINNER -> Color(0xFFC5D4B8)
        AnalysisLevel.INTERMEDIATE -> Color(0xFFE8C9AD)
        AnalysisLevel.MASTER -> Color(0xFFD9B5A8)
        AnalysisLevel.GOD -> Color(0xFFEBCB79)
    }
    Card(onClick = onClick, shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(cardColor, Espresso), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(46.dp).clip(CircleShape).background(Espresso.copy(.10f)), contentAlignment = Alignment.Center) {
                Text(when (level) { AnalysisLevel.BEGINNER -> "1"; AnalysisLevel.INTERMEDIATE -> "2"; AnalysisLevel.MASTER -> "3"; AnalysisLevel.GOD -> "∞" }, color = Espresso, style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(level.title, color = Espresso, style = MaterialTheme.typography.titleMedium)
                Text(level.subtitle, color = Espresso.copy(.66f), style = MaterialTheme.typography.bodyMedium)
            }
            Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = Espresso)
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
    val boardPosition = exploredPositions.last()
    val lastMove = if (exploredPositions.size > 1) findTransitionMove(exploredPositions[exploredPositions.lastIndex - 1], boardPosition) else null
    val hints = selected?.let { from -> ChessRules.legalMoves(boardPosition).filter { it.from == from }.map { it.to }.toSet() }.orEmpty()

    fun play(move: Move, followsPv: Boolean) {
        exploredPositions = exploredPositions + ChessRules.apply(boardPosition, move)
        selected = null
        followingLine = followsPv
        if (followsPv) pvIndex++
    }

    LazyColumn(modifier = Modifier.navigationBarsPadding(), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
        item { ScreenHeader("Analysis", onBack = { vm.navigate(AppScreen.LEVEL) }, trailing = state.level.title) }
        item {
            ChessBoard(
                boardPosition, state.flipped, selected,
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
                lastMove = lastMove?.let { it.from to it.to },
                haptics = state.settings.haptics,
                highContrastBoard = state.settings.highContrastBoard,
            )
        }
        item {
            Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = SurfaceDark)) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    IconButton(onClick = {
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
                    IconButton(onClick = {
                        val uci = result.principalVariationUci.getOrNull(pvIndex)
                        val move = uci?.let { parseUciMove(it) }
                        if (move != null && move in ChessRules.legalMoves(boardPosition)) play(move, true)
                    }, enabled = followingLine && pvIndex < result.principalVariationUci.size) { Icon(Icons.Rounded.SkipNext, contentDescription = "Next move") }
                }
                Row(Modifier.fillMaxWidth().padding(start = 12.dp, end = 12.dp, bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    TextButton(onClick = { exploredPositions = listOf(state.position); selected = null; pvIndex = 0; followingLine = true }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Rounded.Refresh, null); Spacer(Modifier.width(6.dp)); Text("Reset")
                    }
                    TextButton(onClick = { vm.analyzeFrom(boardPosition) }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Rounded.CenterFocusStrong, null); Spacer(Modifier.width(6.dp)); Text("Analyze here")
                    }
                }
            }
        }
        item {
            GlassCard {
                Text("BEST MOVE", style = MaterialTheme.typography.labelMedium, color = Muted)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                    Text(result.bestMove, style = MaterialTheme.typography.displaySmall, color = Accent)
                    Column(horizontalAlignment = Alignment.End) {
                        Text(result.evaluation, style = MaterialTheme.typography.titleLarge)
                        Text("Depth ${result.depth}", style = MaterialTheme.typography.labelMedium, color = Muted)
                    }
                }
            }
        }
        item {
            GlassCard {
                Text("WHY THIS MOVE?", style = MaterialTheme.typography.labelMedium, color = Muted)
                Spacer(Modifier.height(10.dp))
                Text(result.explanation, style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(10.dp))
                result.reasons.forEach { Text("•  $it", color = Color.White.copy(.78f), fontSize = 14.sp, modifier = Modifier.padding(vertical = 2.dp)) }
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
    val transition = rememberInfiniteTransition(label = "analysis")
    val pulse by transition.animateFloat(
        initialValue = .96f, targetValue = 1.04f,
        animationSpec = infiniteRepeatable(tween(1100, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "pulse",
    )
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(260.dp).clip(RoundedCornerShape(54.dp)).background(Color(0xFFF1D7B5)), contentAlignment = Alignment.Center) {
                Image(painterResource(R.drawable.chess_hero), contentDescription = null, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize().padding(10.dp).scale(pulse))
            }
            Text("Finding the clearest line…", style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 28.dp))
            Text("${level.title} analysis", style = MaterialTheme.typography.labelMedium, color = Accent, modifier = Modifier.padding(top = 8.dp))
            Row(Modifier.padding(top = 22.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(3) { index -> Box(Modifier.size((9 + index * 2).dp).scale(if (index == 1) pulse else 1f).clip(CircleShape).background(listOf(Coral, Sunny, Accent)[index])) }
            }
            TextButton(onClick = onStop, modifier = Modifier.padding(top = 20.dp)) { Icon(Icons.Rounded.Stop, null); Spacer(Modifier.width(6.dp)); Text("Stop analysis") }
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
    val history = vm.state.history
    LazyColumn(modifier = Modifier.navigationBarsPadding(), contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
        item { ScreenHeader("Analysis history", onBack = { vm.navigate(AppScreen.HOME) }, trailing = "${history.size} LOCAL") }
        if (history.isEmpty()) item { GlassCard { Text("No analysis history yet", fontWeight = FontWeight.Bold); Text("Completed analyses appear here automatically.", color = Muted) } }
        items(history, key = { it.id }) { item ->
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(SurfaceDark)
                    .clickable { vm.openHistory(item) }.padding(15.dp), verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(Accent), contentAlignment = Alignment.Center) { Icon(Icons.Rounded.History, contentDescription = null, tint = Espresso) }
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

@Composable
private fun SettingsScreen(vm: MainViewModel) {
    val context = LocalContext.current
    val settings = vm.state.settings
    val backupPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            runCatching { context.contentResolver.openInputStream(it)?.bufferedReader()?.use { reader -> reader.readText() } }
                .getOrNull()?.let(vm::importData)
        }
    }
    LazyColumn(contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 110.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Settings", fontSize = 31.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(vertical = 12.dp)) }
        item {
            GlassCard {
                Text("APPEARANCE", color = Accent, fontSize = 10.sp, letterSpacing = 1.4.sp, fontWeight = FontWeight.Bold)
                SettingToggle("Animations", "Fast screen transitions", settings.animations) { vm.updateSettings(settings.copy(animations = it)) }
                SettingToggle("High-contrast board", "Brighter neutral squares", settings.highContrastBoard) { vm.updateSettings(settings.copy(highContrastBoard = it)) }
            }
        }
        item {
            GlassCard {
                Text("BOARD", color = Accent, fontSize = 10.sp, letterSpacing = 1.4.sp, fontWeight = FontWeight.Bold)
                SettingToggle("Coordinates", "Show files and ranks", settings.coordinates) { vm.updateSettings(settings.copy(coordinates = it)) }
                SettingToggle("Legal move hints", "Highlight destinations while exploring", settings.legalHints) { vm.updateSettings(settings.copy(legalHints = it)) }
                SettingToggle("Haptic feedback", "Touch confirmation on board", settings.haptics) { vm.updateSettings(settings.copy(haptics = it)) }
            }
        }
        item {
            GlassCard {
                Text("ANALYSIS", color = Accent, fontSize = 10.sp, letterSpacing = 1.4.sp, fontWeight = FontWeight.Bold)
                Text("Adaptive engine presets", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 10.dp))
                Text("250 ms Beginner · 800 ms Intermediate · 2.5 s Master · 6 s God mode. MultiPV and device-safe thread/hash limits are applied automatically.", color = Muted, fontSize = 12.sp, lineHeight = 17.sp)
            }
        }
        item {
            GlassCard {
                Text("ABOUT", style = MaterialTheme.typography.labelMedium, color = Accent)
                Text("ChessMind 1.0.2", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 10.dp))
                Text("Stockfish 19 on supported ARM devices · ChessMind fallback engine on other devices.", color = Muted, fontSize = 12.sp, lineHeight = 17.sp)
            }
        }
        item {
            TextButton(onClick = vm::clearHistory, modifier = Modifier.fillMaxWidth()) { Text("Clear history", color = Coral) }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/json"
                            putExtra(Intent.EXTRA_TEXT, vm.exportData())
                            putExtra(Intent.EXTRA_SUBJECT, "ChessMind backup")
                        }
                        context.startActivity(Intent.createChooser(intent, "Export ChessMind data"))
                    }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(SurfaceLight, Color.White),
                ) { Text("Export data") }
                Button(
                    onClick = { backupPicker.launch("application/json") }, modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(Accent, Ink),
                ) { Text("Import data") }
            }
            vm.state.fenError?.let { error -> Text(error, color = Coral, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp)) }
        }
    }
}

@Composable
private fun SettingToggle(title: String, subtitle: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = Muted, fontSize = 12.sp)
        }
        Switch(checked = checked, onCheckedChange = onChecked)
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
        modifier.navigationBarsPadding().padding(bottom = 14.dp).width(244.dp).height(72.dp).shadow(18.dp, RoundedCornerShape(30.dp)),
        shape = RoundedCornerShape(30.dp), color = SurfaceLight, tonalElevation = 6.dp,
    ) {
        Row(Modifier.padding(horizontal = 12.dp), horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically) {
            listOf(
                Triple(AppScreen.HOME, "Home", Icons.Rounded.Home),
                Triple(AppScreen.SCANNER, "Scan", Icons.Rounded.CenterFocusStrong),
                Triple(AppScreen.HISTORY, "History", Icons.Rounded.History),
            ).forEach { (screen, label, icon) ->
                val selected = active == screen
                FilledIconButton(
                    onClick = { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove); onNavigate(screen) }, modifier = Modifier.size(if (screen == AppScreen.SCANNER) 54.dp else 48.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = if (screen == AppScreen.SCANNER) Sunny else if (selected) Accent else Color.Transparent,
                        contentColor = Espresso.takeIf { screen == AppScreen.SCANNER || selected } ?: Color.White.copy(.82f),
                    ),
                ) { Icon(icon, contentDescription = label, modifier = Modifier.size(if (screen == AppScreen.SCANNER) 27.dp else 24.dp)) }
            }
        }
    }
}
