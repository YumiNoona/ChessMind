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
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
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

private val Ink = Color(0xFF160A34)
private val SurfaceDark = Color(0xFF251747)
private val SurfaceLight = Color(0xFF34245A)
private val Accent = Color(0xFF47DDD4)
private val Sunny = Color(0xFFFFD55F)
private val Coral = Color(0xFFFF8569)
private val Purple = Color(0xFF9466F2)
private val Muted = Color(0xFFB9AFD2)
private val BoardLight = Color(0xFFFFE7B8)
private val BoardDark = Color(0xFF9A6CDD)

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
            onBackground = Color(0xFFF4F7F1),
            onSurface = Color(0xFFF4F7F1),
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
                AppScreen.SCANNER, AppScreen.IMAGE_REVIEW, AppScreen.SETUP, AppScreen.SAVED, AppScreen.HISTORY,
                AppScreen.PRACTICE, AppScreen.SETTINGS -> AppScreen.HOME
                else -> AppScreen.HOME
            }
        )
    }
    Box(Modifier.fillMaxSize().background(Ink)) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(Color(0xFF38285F), radius = size.width * .72f, center = Offset(size.width * .13f, -size.height * .08f))
            drawCircle(Color(0xFF211243), radius = size.width * .66f, center = Offset(size.width * .98f, size.height * .38f))
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
                    AppScreen.SCANNER -> ScannerScreen(vm)
                    AppScreen.IMAGE_REVIEW -> ImageReviewScreen(vm)
                    AppScreen.SETUP -> SetupScreen(vm)
                    AppScreen.LEVEL -> LevelScreen(vm)
                    AppScreen.ANALYSIS -> AnalysisScreen(vm)
                    AppScreen.PRACTICE -> PracticeScreen(vm)
                    AppScreen.SAVED -> SavedScreen(vm)
                    AppScreen.HISTORY -> HistoryScreen(vm)
                    AppScreen.SETTINGS -> SettingsScreen(vm)
                }
            }
        }
        if (state.screen in setOf(AppScreen.HOME, AppScreen.PRACTICE, AppScreen.SAVED, AppScreen.SETTINGS)) {
            FloatingDock(state.screen, vm::navigate, Modifier.align(Alignment.BottomCenter))
        }
    }
}

@Composable
private fun OnboardingScreen(vm: MainViewModel) {
    var page by remember { mutableStateOf(0) }
    val content = listOf(
        Triple("SCAN", "Point. Capture. Solve.", "Use your camera or gallery. Your board images remain on this device."),
        Triple("ANALYZE", "Find a stronger move.", "Choose a learning level and get legal candidates, lines, and grounded explanations."),
        Triple("PRACTICE", "Turn answers into skill.", "Solve offline positions, build a streak, and revisit the ideas you save."),
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
                    .clip(RoundedCornerShape(54.dp)).background(Brush.linearGradient(listOf(listOf(Coral, Purple, Accent)[page], listOf(Sunny, Color(0xFF6344BD), Color(0xFF1EAAA8))[page]))),
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
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { vm.reviewImage(it.toString()) }
    }
    LazyColumn(
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 116.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(44.dp).clip(CircleShape).background(Sunny)
                            .border(3.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) { Text("♞", color = Ink, fontSize = 25.sp) }
                    Spacer(Modifier.width(11.dp))
                    Column {
                        Text("WELCOME BACK", fontSize = 9.sp, letterSpacing = 1.5.sp, color = Accent, fontWeight = FontWeight.Bold)
                        Text("ChessMind", fontSize = 19.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
                Box(
                    Modifier.size(42.dp).clip(RoundedCornerShape(14.dp)).background(Color.White.copy(.08f))
                        .border(1.dp, Color.White.copy(.10f), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center,
                ) { Text("⌘", color = Accent, fontSize = 20.sp) }
            }
        }
        if (vm.state.history.isNotEmpty()) item {
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color.White.copy(.06f))
                    .clickable { vm.navigate(AppScreen.HISTORY) }.padding(14.dp), verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("↺", color = Sunny, fontSize = 25.sp)
                Spacer(Modifier.width(11.dp))
                Column(Modifier.weight(1f)) {
                    Text("Recent analysis", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("${vm.state.history.first().bestMove} · ${vm.state.history.first().level}", color = Muted, fontSize = 11.sp)
                }
                Text("›", color = Accent, fontSize = 24.sp)
            }
        }
        item {
            HeroBanner { vm.navigate(AppScreen.SCANNER) }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                BentoTile(
                    title = "Scan\nboard", kicker = "CAMERA", icon = "⌗",
                    colors = listOf(Color(0xFF9C6DF4), Color(0xFF7350D8)), modifier = Modifier.weight(1f).height(174.dp),
                ) { vm.navigate(AppScreen.SCANNER) }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    BentoTile(
                        title = "Import image", kicker = "GALLERY", icon = "▣",
                        colors = listOf(Color(0xFFFF9774), Color(0xFFF36F68)), modifier = Modifier.fillMaxWidth().height(81.dp), compact = true,
                    ) { imagePicker.launch("image/*") }
                    BentoTile(
                        title = "Set position", kicker = "MANUAL", icon = "♙",
                        colors = listOf(Color(0xFF58DDD6), Color(0xFF26BBB9)), modifier = Modifier.fillMaxWidth().height(81.dp), compact = true,
                    ) { vm.navigate(AppScreen.SETUP) }
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MiniFeature("♟", "Daily\npuzzle", Sunny, Ink, Modifier.weight(1f)) { vm.navigate(AppScreen.PRACTICE) }
                MiniFeature("◎", "Saved\npositions", Color(0xFF6B56A7), Color.White, Modifier.weight(1f)) { vm.navigate(AppScreen.SAVED) }
            }
        }
        item {
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color.White.copy(.07f))
                    .border(1.dp, Color.White.copy(.08f), RoundedCornerShape(18.dp)).clickable { vm.navigate(AppScreen.LEVEL) }.padding(15.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(42.dp).clip(RoundedCornerShape(13.dp)).background(Accent.copy(.18f)), contentAlignment = Alignment.Center) {
                    Text("◉", color = Accent, fontSize = 21.sp)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Everything stays offline", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text("Private, fast, no account", color = Muted, fontSize = 11.sp)
                }
                Text("›", color = Accent, fontSize = 25.sp)
            }
        }
    }
}

@Composable
private fun HeroBanner(onClick: () -> Unit) {
    Box(
        Modifier.fillMaxWidth().height(180.dp).shadow(18.dp, RoundedCornerShape(28.dp), spotColor = Color.Black.copy(.35f))
            .clip(RoundedCornerShape(28.dp)).background(Brush.linearGradient(listOf(Color(0xFFFFD965), Color(0xFFFFAE5E))))
            .clickable(onClick = onClick),
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(Color.White.copy(.13f), radius = size.width * .45f, center = Offset(size.width * .92f, size.height * .10f))
            drawCircle(Color(0xFFFF8E5D).copy(.28f), radius = size.width * .33f, center = Offset(size.width * .68f, size.height * 1.1f))
        }
        Column(Modifier.padding(20.dp).fillMaxHeight(), verticalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("FIND THE BEST MOVE", color = Ink.copy(.58f), fontSize = 10.sp, letterSpacing = 1.2.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(5.dp))
                Text("Turn any board\ninto an answer.", color = Ink, fontSize = 24.sp, lineHeight = 26.sp, fontWeight = FontWeight.ExtraBold)
            }
            Box(Modifier.clip(RoundedCornerShape(12.dp)).background(Ink).padding(horizontal = 14.dp, vertical = 9.dp)) {
                Text("START SOLVING  →", color = Color.White, fontSize = 10.sp, letterSpacing = .7.sp, fontWeight = FontWeight.Bold)
            }
        }
        Text("♞", fontSize = 114.sp, color = Ink.copy(.88f), modifier = Modifier.align(Alignment.BottomEnd).padding(end = 18.dp, bottom = 2.dp))
        Text("♙", fontSize = 66.sp, color = Color.White.copy(.97f), modifier = Modifier.align(Alignment.TopEnd).padding(end = 86.dp, top = 8.dp))
    }
}

@Composable
private fun BentoTile(
    title: String,
    kicker: String,
    icon: String,
    colors: List<Color>,
    modifier: Modifier,
    compact: Boolean = false,
    onClick: () -> Unit,
) {
    Box(
        modifier.shadow(12.dp, RoundedCornerShape(23.dp), spotColor = Color.Black.copy(.28f))
            .clip(RoundedCornerShape(23.dp)).background(Brush.linearGradient(colors)).clickable(onClick = onClick).padding(15.dp),
    ) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Text(kicker, color = Color.White.copy(.72f), fontSize = 8.sp, letterSpacing = 1.2.sp, fontWeight = FontWeight.Bold)
            Text(title, color = Color.White, fontSize = if (compact) 14.sp else 21.sp, lineHeight = if (compact) 15.sp else 22.sp, fontWeight = FontWeight.ExtraBold)
        }
        Text(icon, color = Color.White.copy(.30f), fontSize = if (compact) 43.sp else 82.sp, modifier = Modifier.align(Alignment.CenterEnd))
    }
}

@Composable
private fun MiniFeature(icon: String, title: String, color: Color, contentColor: Color, modifier: Modifier, onClick: () -> Unit) {
    Row(
        modifier.height(76.dp).clip(RoundedCornerShape(21.dp)).background(color).clickable(onClick = onClick).padding(horizontal = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(icon, color = contentColor, fontSize = 30.sp)
        Spacer(Modifier.width(10.dp))
        Text(title, color = contentColor, fontSize = 13.sp, lineHeight = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ScannerScreen(vm: MainViewModel) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var granted by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted = it }
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
                Spacer(Modifier.height(24.dp))
            }
        } else {
            Column(Modifier.align(Alignment.Center).padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("⌗", fontSize = 58.sp, color = Accent)
                Text("Camera access is needed", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("ChessMind only uses the camera while this scanner is open.", color = Muted, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 12.dp))
                Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }, colors = ButtonDefaults.buttonColors(Sunny, Ink)) { Text("Allow camera") }
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
        Text("Automatic piece recognition is not bundled yet. Continue to an empty board and enter the detected position manually; the photo never leaves this device.", color = Muted, fontSize = 12.sp, lineHeight = 17.sp, modifier = Modifier.padding(bottom = 12.dp))
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
                TextButton(onClick = vm::flip) { Text("Flip", color = Color.White) }
                TextButton(onClick = vm::clear) { Text("Clear", color = Color.White) }
                TextButton(onClick = vm::reset) { Text("Reset", color = Color.White) }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            TextButton(onClick = vm::undo, enabled = state.canUndo) { Text("↶ Undo") }
            TextButton(onClick = vm::redo, enabled = state.canRedo) { Text("Redo ↷") }
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
    val darkColor = if (highContrastBoard) Color(0xFF694EA8) else BoardDark
    Box(Modifier.fillMaxWidth().aspectRatio(1f).shadow(16.dp, RoundedCornerShape(22.dp), spotColor = Color.Black.copy(.35f)).clip(RoundedCornerShape(22.dp)).border(4.dp, Color.White.copy(.12f), RoundedCornerShape(22.dp))) {
        Column(Modifier.fillMaxSize()) {
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
                                .background(if (isSelected) Accent else if (isLastMove) Sunny.copy(.72f) else if (light) lightColor else darkColor)
                                .combinedClickable(
                                    onClick = { if (haptics) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove); onTap(square) },
                                    onLongClick = { if (haptics) haptic.performHapticFeedback(HapticFeedbackType.LongPress); onLongPressFallback(square) },
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            position[square]?.let { piece ->
                                Text(piece.symbol, fontSize = 34.sp, color = if (piece.side == Side.WHITE) Color(0xFFF6F2E8) else Color(0xFF141817))
                            }
                            if (isHint) Box(Modifier.size(if (position[square] == null) 10.dp else 34.dp).clip(CircleShape).background(Accent.copy(if (position[square] == null) .72f else .24f)).then(if (position[square] == null) Modifier else Modifier.border(3.dp, Accent.copy(.8f), CircleShape)))
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
        Text("How should ChessMind think?", fontSize = 28.sp, lineHeight = 32.sp, fontWeight = FontWeight.SemiBold)
        Text("You can change this for every position.", color = Muted, modifier = Modifier.padding(top = 7.dp, bottom = 22.dp))
        AnalysisLevel.entries.forEach { level ->
            LevelCard(level) { vm.chooseLevel(level) }
            Spacer(Modifier.height(10.dp))
        }
        Text("All analysis runs locally. Stronger modes use more battery and may take longer.", color = Muted, fontSize = 12.sp, lineHeight = 17.sp, modifier = Modifier.padding(12.dp))
    }
}

@Composable
private fun LevelCard(level: AnalysisLevel, onClick: () -> Unit) {
    val cardColor = when (level) {
        AnalysisLevel.BEGINNER -> Color(0xFF2CC8C4)
        AnalysisLevel.INTERMEDIATE -> Color(0xFF7957D9)
        AnalysisLevel.MASTER -> Color(0xFFE46D72)
        AnalysisLevel.GOD -> Sunny
    }
    Card(onClick = onClick, shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(cardColor), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(42.dp).clip(CircleShape).background(if (level == AnalysisLevel.GOD) Ink else Color.White.copy(.18f)), contentAlignment = Alignment.Center) {
                Text(when (level) { AnalysisLevel.BEGINNER -> "1"; AnalysisLevel.INTERMEDIATE -> "2"; AnalysisLevel.MASTER -> "3"; AnalysisLevel.GOD -> "∞" }, color = if (level == AnalysisLevel.GOD) Sunny else Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(level.title, color = if (level == AnalysisLevel.GOD) Ink else Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text(level.subtitle, color = if (level == AnalysisLevel.GOD) Ink.copy(.62f) else Color.White.copy(.72f), fontSize = 13.sp)
            }
            Text("›", fontSize = 25.sp, color = if (level == AnalysisLevel.GOD) Ink else Color.White)
        }
    }
}

@Composable
private fun AnalysisScreen(vm: MainViewModel) {
    val state = vm.state
    val result = state.result
    if (result == null) {
        LaunchedEffect(state.level) { vm.analyze(state.level) }
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("♞", color = Accent, fontSize = 54.sp)
                Text("Analyzing locally…", fontWeight = FontWeight.SemiBold)
                TextButton(onClick = vm::stopAnalysis) { Text("Stop", color = Coral) }
            }
        }
        return
    }
    var demonstrate by remember(result) { mutableStateOf(false) }
    val bestMove = remember(state.position, result.bestMove) {
        app.chessmind.domain.model.ChessRules.legalMoves(state.position)
            .firstOrNull { app.chessmind.domain.model.ChessRules.notation(state.position, it) == result.bestMove }
    }
    val boardPosition = if (demonstrate && bestMove != null) app.chessmind.domain.model.ChessRules.apply(state.position, bestMove) else state.position
    LazyColumn(modifier = Modifier.navigationBarsPadding(), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
        item { ScreenHeader("Analysis", onBack = { vm.navigate(AppScreen.LEVEL) }, trailing = state.level.title) }
        item {
            ChessBoard(
                boardPosition, state.flipped, if (demonstrate) bestMove?.to else null, {}, {},
                showCoordinates = state.settings.coordinates,
                lastMove = if (demonstrate && bestMove != null) bestMove.from to bestMove.to else null,
                highContrastBoard = state.settings.highContrastBoard,
            )
        }
        item {
            GlassCard {
                Text("BEST MOVE", fontSize = 11.sp, letterSpacing = 1.6.sp, color = Muted, fontWeight = FontWeight.Bold)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                    Text(result.bestMove, fontSize = 42.sp, fontWeight = FontWeight.Bold, color = Accent)
                    Column(horizontalAlignment = Alignment.End) {
                        Text(result.evaluation, fontSize = 21.sp, fontWeight = FontWeight.SemiBold)
                        Text("Depth ${result.depth}", fontSize = 11.sp, color = Muted)
                    }
                }
            }
        }
        item {
            GlassCard {
                Text("WHY THIS MOVE?", fontSize = 11.sp, letterSpacing = 1.5.sp, color = Muted, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                Text(result.explanation, fontSize = 16.sp, lineHeight = 23.sp)
                Spacer(Modifier.height(10.dp))
                result.reasons.forEach { Text("•  $it", color = Color.White.copy(.78f), fontSize = 14.sp, modifier = Modifier.padding(vertical = 2.dp)) }
            }
        }
        item {
            GlassCard {
                Text("BEST LINE", fontSize = 11.sp, letterSpacing = 1.5.sp, color = Muted, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(9.dp))
                Text(result.principalVariation.joinToString("  "), fontSize = 17.sp, fontWeight = FontWeight.Medium)
            }
        }
        if (result.candidates.size > 1) item {
            GlassCard {
                Text("CANDIDATES", fontSize = 11.sp, letterSpacing = 1.5.sp, color = Muted, fontWeight = FontWeight.Bold)
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
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = { demonstrate = !demonstrate }, modifier = Modifier.weight(1f).height(54.dp),
                    shape = RoundedCornerShape(18.dp), colors = ButtonDefaults.buttonColors(Purple, Color.White),
                    enabled = bestMove != null,
                ) { Text(if (demonstrate) "Reset board" else "Show move", fontWeight = FontWeight.Bold) }
                Button(
                    onClick = vm::saveCurrent, modifier = Modifier.weight(1f).height(54.dp),
                    shape = RoundedCornerShape(18.dp), colors = ButtonDefaults.buttonColors(Sunny, Ink),
                ) { Text("Save", fontWeight = FontWeight.Bold) }
            }
        }
        item { Text("${result.engineName} • Offline", color = Muted, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(bottom = 18.dp)) }
    }
}

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
                Box(Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(Accent.copy(.16f)), contentAlignment = Alignment.Center) { Text("♞", color = Accent, fontSize = 28.sp) }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(item.bestMove, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(item.level, color = Muted, fontSize = 12.sp)
                    Text(item.fen.take(34) + "…", color = Muted.copy(.72f), fontSize = 9.sp, maxLines = 1)
                }
                Text("›", color = Accent, fontSize = 24.sp)
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
                SettingToggle("Legal move hints", "Highlight choices in practice", settings.legalHints) { vm.updateSettings(settings.copy(legalHints = it)) }
                SettingToggle("Haptic feedback", "Touch confirmation on board", settings.haptics) { vm.updateSettings(settings.copy(haptics = it)) }
            }
        }
        item {
            GlassCard {
                Text("ANALYSIS", color = Accent, fontSize = 10.sp, letterSpacing = 1.4.sp, fontWeight = FontWeight.Bold)
                Text("Adaptive offline presets", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 10.dp))
                Text("250 ms Beginner · 800 ms Intermediate · 2.5 s Master · 6 s God mode. MultiPV and device-safe thread/hash limits are applied automatically.", color = Muted, fontSize = 12.sp, lineHeight = 17.sp)
            }
        }
        item {
            GlassCard {
                Text("PRIVACY & ABOUT", color = Accent, fontSize = 10.sp, letterSpacing = 1.4.sp, fontWeight = FontWeight.Bold)
                Text("ChessMind 1.0.2", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 10.dp))
                Text("Stockfish 19 when available · ChessMind fallback engine otherwise. No account, analytics, server, or photo uploads.", color = Muted, fontSize = 12.sp, lineHeight = 17.sp)
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TextButton(onClick = vm::clearHistory, modifier = Modifier.weight(1f)) { Text("Clear history", color = Coral) }
                TextButton(onClick = vm::clearSaved, modifier = Modifier.weight(1f)) { Text("Clear saved", color = Coral) }
            }
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
                    }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(Purple, Color.White),
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
        Text("‹", fontSize = 36.sp, modifier = Modifier.clickable(onClick = onBack).padding(end = 14.dp), color = Color.White)
        Text(title, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        if (trailing != null) Text(trailing, color = Accent, fontSize = 12.sp)
    }
}

@Composable
private fun GlassCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp))
            .background(SurfaceDark.copy(.90f))
            .border(1.dp, Color.White.copy(.09f), RoundedCornerShape(22.dp))
            .padding(17.dp),
        content = content,
    )
}

@Composable
private fun FloatingDock(active: AppScreen, onNavigate: (AppScreen) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier
            .navigationBarsPadding()
            .padding(bottom = 14.dp)
            .width(232.dp)
            .height(66.dp)
            .shadow(24.dp, RoundedCornerShape(28.dp), ambientColor = Color.Black.copy(.42f), spotColor = Color.Black.copy(.52f))
            .clip(RoundedCornerShape(28.dp))
            .background(Color(0xFF5D5079).copy(.78f))
            .border(1.dp, Color.White.copy(.16f), RoundedCornerShape(28.dp))
            .padding(horizontal = 13.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        listOf(
            AppScreen.HOME,
            AppScreen.PRACTICE,
            AppScreen.SETTINGS,
        ).forEach { screen ->
            val selected = active == screen
            Box(
                Modifier.size(48.dp).semantics {
                    contentDescription = when (screen) {
                        AppScreen.HOME -> "Home"
                        AppScreen.PRACTICE -> "Practice"
                        AppScreen.SETTINGS -> "Settings"
                        else -> "Navigation"
                    }
                }.clip(CircleShape)
                    .background(if (selected) Accent else Color.Transparent)
                    .clickable { onNavigate(screen) },
                contentAlignment = Alignment.Center,
            ) {
                DockGlyph(screen, if (selected) Ink else Color.White.copy(.82f))
            }
        }
    }
}

@Composable
private fun DockGlyph(screen: AppScreen, tint: Color) {
    Canvas(Modifier.size(23.dp)) {
        val stroke = 1.8.dp.toPx()
        when (screen) {
            AppScreen.HOME -> {
                val roof = Path().apply {
                    moveTo(size.width * .12f, size.height * .48f)
                    lineTo(size.width * .50f, size.height * .16f)
                    lineTo(size.width * .88f, size.height * .48f)
                }
                drawPath(roof, tint, style = Stroke(stroke, cap = StrokeCap.Round))
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(size.width * .22f, size.height * .43f),
                    size = Size(size.width * .56f, size.height * .42f),
                    cornerRadius = CornerRadius(size.width * .08f),
                    style = Stroke(stroke),
                )
            }
            AppScreen.PRACTICE -> {
                drawCircle(tint, radius = size.width * .16f, center = Offset(size.width * .5f, size.height * .25f), style = Stroke(stroke))
                drawLine(tint, Offset(size.width * .40f, size.height * .42f), Offset(size.width * .34f, size.height * .65f), stroke, StrokeCap.Round)
                drawLine(tint, Offset(size.width * .60f, size.height * .42f), Offset(size.width * .66f, size.height * .65f), stroke, StrokeCap.Round)
                drawLine(tint, Offset(size.width * .34f, size.height * .65f), Offset(size.width * .66f, size.height * .65f), stroke, StrokeCap.Round)
                drawLine(tint, Offset(size.width * .24f, size.height * .82f), Offset(size.width * .76f, size.height * .82f), stroke, StrokeCap.Round)
            }
            AppScreen.SETTINGS -> {
                listOf(.27f to .68f, .50f to .35f, .73f to .61f).forEach { (y, knob) ->
                    drawLine(tint, Offset(size.width * .14f, size.height * y), Offset(size.width * .86f, size.height * y), stroke, StrokeCap.Round)
                    drawCircle(tint, radius = size.width * .09f, center = Offset(size.width * knob, size.height * y))
                }
            }
            else -> Unit
        }
    }
}
