package app.chessmind

import android.os.Bundle
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
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
        setContent { ChessMindTheme { ChessMindApp() } }
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
                transitionSpec = { fadeIn(tween(160)) togetherWith fadeOut(tween(120)) },
                label = "screen",
                modifier = Modifier.fillMaxSize().padding(padding).statusBarsPadding(),
            ) { screen ->
                when (screen) {
                    AppScreen.HOME -> HomeScreen(vm)
                    AppScreen.SETUP -> SetupScreen(vm)
                    AppScreen.LEVEL -> LevelScreen(vm)
                    AppScreen.ANALYSIS -> AnalysisScreen(vm)
                    AppScreen.PRACTICE -> ComingSoon("Practice", "Daily positions, tactics, and mistake review are next in the build.")
                    AppScreen.SAVED -> ComingSoon("Saved", "Positions you keep will live here, entirely on-device.")
                    AppScreen.SETTINGS -> SettingsScreen()
                }
            }
        }
        if (state.screen in setOf(AppScreen.HOME, AppScreen.PRACTICE, AppScreen.SAVED, AppScreen.SETTINGS)) {
            FloatingDock(state.screen, vm::navigate, Modifier.align(Alignment.BottomCenter))
        }
    }
}

@Composable
private fun HomeScreen(vm: MainViewModel) {
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
        item {
            HeroBanner { vm.navigate(AppScreen.SETUP) }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                BentoTile(
                    title = "Scan\nboard", kicker = "CAMERA", icon = "⌗",
                    colors = listOf(Color(0xFF9C6DF4), Color(0xFF7350D8)), modifier = Modifier.weight(1f).height(174.dp),
                ) { vm.navigate(AppScreen.SETUP) }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    BentoTile(
                        title = "Import image", kicker = "GALLERY", icon = "▣",
                        colors = listOf(Color(0xFFFF9774), Color(0xFFF36F68)), modifier = Modifier.fillMaxWidth().height(81.dp), compact = true,
                    ) { vm.navigate(AppScreen.SETUP) }
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
private fun SetupScreen(vm: MainViewModel) {
    val state = vm.state
    var showFen by remember { mutableStateOf(false) }
    val context = LocalContext.current
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        ScreenHeader("Set up position", onBack = { vm.navigate(AppScreen.HOME) })
        ChessBoard(
            position = state.position,
            flipped = state.flipped,
            selected = state.selected,
            onTap = vm::tapSquare,
            onLongPressFallback = vm::erase,
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
        PiecePalette(state.palettePiece, vm::setPalette)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = { showFen = true }, modifier = Modifier.weight(1f)) { Text("Load FEN") }
            TextButton(onClick = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("ChessMind FEN", Fen.encode(state.position)))
            }, modifier = Modifier.weight(1f)) { Text("Copy FEN") }
        }
        Spacer(Modifier.weight(1f))
        Button(
            onClick = { vm.navigate(AppScreen.LEVEL) },
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
private fun ChessBoard(position: Position, flipped: Boolean, selected: Square?, onTap: (Square) -> Unit, onLongPressFallback: (Square) -> Unit) {
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
                        Box(
                            Modifier.weight(1f).fillMaxHeight()
                                .background(if (isSelected) Accent else if (light) BoardLight else BoardDark)
                                .clickable { onTap(square) },
                            contentAlignment = Alignment.Center,
                        ) {
                            position[square]?.let { piece ->
                                Text(piece.symbol, fontSize = 34.sp, color = if (piece.side == Side.WHITE) Color(0xFFF6F2E8) else Color(0xFF141817))
                            }
                            if (displayFile == 0) Text(
                                "${rank + 1}", fontSize = 9.sp, color = (if (light) BoardDark else BoardLight).copy(.8f),
                                modifier = Modifier.align(Alignment.TopStart).padding(2.dp),
                            )
                            if (displayRank == 7) Text(
                                "${('a'.code + file).toChar()}", fontSize = 9.sp, color = (if (light) BoardDark else BoardLight).copy(.8f),
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
private fun LevelScreen(vm: MainViewModel) {
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
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
            }
        }
        return
    }
    LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
        item { ScreenHeader("Analysis", onBack = { vm.navigate(AppScreen.LEVEL) }, trailing = state.level.title) }
        item { ChessBoard(state.position, state.flipped, null, {}, {}) }
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
        item { Text("${result.engineName} • Offline", color = Muted, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(bottom = 18.dp)) }
    }
}

@Composable
private fun SettingsScreen() {
    LazyColumn(contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 110.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Settings", fontSize = 31.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(vertical = 12.dp)) }
        items(listOf(
            "Appearance" to "Dark theme · Calm board",
            "Analysis" to "Engine presets and limits",
            "Board" to "Coordinates · Haptics · Legal hints",
            "Storage" to "Export or clear local data",
            "Privacy" to "Everything stays on this device",
            "About" to "ChessMind 1.0.1",
        )) { (title, subtitle) ->
            GlassCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) { Text(title, fontWeight = FontWeight.SemiBold); Text(subtitle, color = Muted, fontSize = 13.sp) }
                    Text("›", color = Muted, fontSize = 24.sp)
                }
            }
        }
    }
}

@Composable
private fun ComingSoon(title: String, subtitle: String) {
    Box(Modifier.fillMaxSize().padding(start = 28.dp, end = 28.dp, bottom = 100.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(110.dp).clip(RoundedCornerShape(32.dp)).background(Brush.linearGradient(listOf(Purple, Color(0xFF684AC9)))), contentAlignment = Alignment.Center) {
                Text("♙", fontSize = 64.sp, color = Color.White)
            }
            Spacer(Modifier.height(18.dp))
            Text(title, fontSize = 30.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = Muted, textAlign = TextAlign.Center, lineHeight = 21.sp, modifier = Modifier.padding(top = 8.dp))
        }
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
                Modifier.size(48.dp).clip(CircleShape)
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
