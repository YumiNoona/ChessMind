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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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

private val Ink = Color(0xFF0B0D0E)
private val SurfaceDark = Color(0xFF151819)
private val SurfaceLight = Color(0xFF1E2223)
private val Accent = Color(0xFFB8F35A)
private val Muted = Color(0xFF98A09E)
private val BoardLight = Color(0xFFC9CFBF)
private val BoardDark = Color(0xFF59645A)

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
    Box(
        Modifier.fillMaxSize().background(
            Brush.radialGradient(listOf(Color(0xFF243027), Ink), center = Offset(850f, -100f), radius = 1150f)
        )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                if (state.screen in setOf(AppScreen.HOME, AppScreen.PRACTICE, AppScreen.SAVED, AppScreen.SETTINGS)) {
                    BottomNav(state.screen, vm::navigate)
                }
            },
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
    }
}

@Composable
private fun HomeScreen(vm: MainViewModel) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 22.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("CHESSMIND", fontSize = 13.sp, letterSpacing = 2.4.sp, color = Accent, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("See the move.\nUnderstand why.", fontSize = 34.sp, lineHeight = 38.sp, fontWeight = FontWeight.SemiBold)
                }
                StatusDot()
            }
        }
        item { PrivacyCard() }
        item {
            PrimaryAction("⌗", "Scan board", "Point your camera at any chessboard") { }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SmallAction("▣", "Import image", Modifier.weight(1f)) { }
                SmallAction("♙", "Set up position", Modifier.weight(1f)) { vm.navigate(AppScreen.SETUP) }
            }
        }
        item {
            Text("QUICK START", fontSize = 12.sp, letterSpacing = 1.6.sp, color = Muted, fontWeight = FontWeight.Bold)
        }
        item {
            GlassCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(52.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xFF242A27)), contentAlignment = Alignment.Center) {
                        Text("♞", fontSize = 30.sp, color = Accent)
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Starting position", fontWeight = FontWeight.SemiBold)
                        Text("Try the offline analysis flow", color = Muted, fontSize = 13.sp)
                    }
                    Text("→", color = Accent, fontSize = 24.sp, modifier = Modifier.clickable { vm.navigate(AppScreen.LEVEL) }.padding(8.dp))
                }
            }
        }
    }
}

@Composable
private fun StatusDot() {
    Row(Modifier.clip(RoundedCornerShape(50)).background(Color.White.copy(.06f)).padding(horizontal = 10.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(7.dp).clip(CircleShape).background(Accent))
        Spacer(Modifier.width(6.dp))
        Text("OFFLINE", fontSize = 10.sp, letterSpacing = 1.sp, color = Muted)
    }
}

@Composable
private fun PrivacyCard() {
    GlassCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("⌾", color = Accent, fontSize = 23.sp)
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Private by design", fontWeight = FontWeight.SemiBold)
                Text("Photos and positions stay on this device.", color = Muted, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun PrimaryAction(icon: String, title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Accent),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth().height(122.dp),
    ) {
        Row(Modifier.fillMaxSize().padding(22.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(icon, color = Ink, fontSize = 42.sp)
            Spacer(Modifier.width(20.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = Ink, fontSize = 23.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, color = Ink.copy(.68f), fontSize = 13.sp)
            }
            Text("→", color = Ink, fontSize = 28.sp)
        }
    }
}

@Composable
private fun SmallAction(icon: String, title: String, modifier: Modifier, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = modifier.height(112.dp), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(SurfaceLight.copy(.82f))) {
        Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Text(icon, color = Accent, fontSize = 27.sp)
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        }
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
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = Ink),
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
    Box(Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(12.dp)).border(1.dp, Color.White.copy(.12f), RoundedCornerShape(12.dp))) {
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
    Card(onClick = onClick, shape = RoundedCornerShape(19.dp), colors = CardDefaults.cardColors(SurfaceLight.copy(.8f)), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(38.dp).clip(CircleShape).background(if (level == AnalysisLevel.GOD) Accent else Color.White.copy(.08f)), contentAlignment = Alignment.Center) {
                Text(when (level) { AnalysisLevel.BEGINNER -> "1"; AnalysisLevel.INTERMEDIATE -> "2"; AnalysisLevel.MASTER -> "3"; AnalysisLevel.GOD -> "∞" }, color = if (level == AnalysisLevel.GOD) Ink else Accent, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(level.title, fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
                Text(level.subtitle, color = Muted, fontSize = 13.sp)
            }
            Text("›", fontSize = 25.sp, color = Muted)
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
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Settings", fontSize = 31.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(vertical = 12.dp)) }
        items(listOf(
            "Appearance" to "Dark theme · Calm board",
            "Analysis" to "Engine presets and limits",
            "Board" to "Coordinates · Haptics · Legal hints",
            "Storage" to "Export or clear local data",
            "Privacy" to "Everything stays on this device",
            "About" to "ChessMind 1.0.0",
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
    Box(Modifier.fillMaxSize().padding(28.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("♙", fontSize = 56.sp, color = Accent)
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
        Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
            .background(SurfaceDark.copy(.82f))
            .border(1.dp, Color.White.copy(.07f), RoundedCornerShape(20.dp))
            .padding(17.dp),
        content = content,
    )
}

@Composable
private fun BottomNav(active: AppScreen, onNavigate: (AppScreen) -> Unit) {
    Row(
        Modifier.fillMaxWidth().background(Ink.copy(.96f)).navigationBarsPadding().height(66.dp).padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        listOf(
            AppScreen.HOME to ("⌂" to "Home"),
            AppScreen.PRACTICE to ("♟" to "Practice"),
            AppScreen.SAVED to ("◇" to "Saved"),
            AppScreen.SETTINGS to ("⚙" to "Settings"),
        ).forEach { (screen, item) ->
            Column(
                Modifier.clip(RoundedCornerShape(12.dp)).clickable { onNavigate(screen) }.padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(item.first, color = if (active == screen) Accent else Muted, fontSize = 19.sp)
                Text(item.second, color = if (active == screen) Accent else Muted, fontSize = 10.sp)
            }
        }
    }
}
