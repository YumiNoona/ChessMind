package app.chessmind

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import app.chessmind.data.HistoryEntry
import app.chessmind.data.LocalStore
import app.chessmind.data.PracticeStats
import app.chessmind.data.SavedPosition
import app.chessmind.data.UserSettings
import app.chessmind.data.LocalProfile
import app.chessmind.domain.engine.AnalysisLevel
import app.chessmind.domain.engine.AnalysisResult
import app.chessmind.data.engine.HybridChessEngine
import app.chessmind.domain.model.Fen
import app.chessmind.domain.model.Piece
import app.chessmind.domain.model.PieceType
import app.chessmind.domain.model.Pgn
import app.chessmind.domain.model.Position
import app.chessmind.domain.model.Side
import app.chessmind.domain.model.Square

enum class AppScreen { ONBOARDING, HOME, PROFILE, SCANNER, IMAGE_REVIEW, SETUP, LEVEL, ANALYSIS, HISTORY, SETTINGS }

data class PracticePuzzle(val title: String, val subtitle: String, val fen: String, val solution: String, val hint: String)

data class AppUiState(
    val screen: AppScreen = AppScreen.HOME,
    val position: Position = Position.START,
    val flipped: Boolean = false,
    val selected: Square? = null,
    val palettePiece: Piece? = null,
    val level: AnalysisLevel = AnalysisLevel.INTERMEDIATE,
    val result: AnalysisResult? = null,
    val fenError: String? = null,
    val importedImage: String? = null,
    val savedPositions: List<SavedPosition> = emptyList(),
    val history: List<HistoryEntry> = emptyList(),
    val practiceStats: PracticeStats = PracticeStats(),
    val puzzleIndex: Int = 0,
    val practicePosition: Position = Position.START,
    val practiceSelected: Square? = null,
    val practiceResult: String? = null,
    val settings: UserSettings = UserSettings(),
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val profile: LocalProfile = LocalProfile(),
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val store = LocalStore(application)
    private val puzzles = listOf(
        PracticePuzzle("Mate in one", "Finish the exposed king", "7k/5Q2/6K1/8/8/8/8/8 w - - 0 1", "f7g7", "The queen can deliver mate while protected by the king."),
        PracticePuzzle("Win the queen", "Find the knight fork", "4k3/8/8/3q4/8/2N5/8/4K3 w - - 0 1", "c3d5", "Look for a square that attacks both king and queen."),
        PracticePuzzle("Promote", "Create a new queen", "7k/P7/8/8/8/8/8/7K w - - 0 1", "a7a8q", "Push the pawn to the last rank."),
    )
    var state by mutableStateOf(
        AppUiState(
            screen = if (store.isOnboarded()) AppScreen.HOME else AppScreen.ONBOARDING,
            savedPositions = store.saved(), history = store.history(), practiceStats = store.stats(),
            settings = store.settings(),
            profile = store.profile(),
            practicePosition = Fen.parse(puzzles.first().fen).getOrThrow(),
        )
    )
        private set

    private val engine = HybridChessEngine(application)
    private val undoPositions = ArrayDeque<Position>()
    private val redoPositions = ArrayDeque<Position>()

    fun navigate(screen: AppScreen) { state = state.copy(screen = screen, selected = null) }
    fun completeOnboarding() { store.completeOnboarding(); state = state.copy(screen = AppScreen.HOME) }
    fun reviewImage(uri: String) { state = state.copy(importedImage = uri, screen = AppScreen.IMAGE_REVIEW) }
    fun startImageCorrection() {
        commitPosition(Position.EMPTY)
        state = state.copy(screen = AppScreen.SETUP, fenError = "Add both kings and correct the detected pieces before analysis.")
    }
    fun chooseLevel(level: AnalysisLevel) {
        state = state.copy(screen = AppScreen.ANALYSIS, level = level, result = null, selected = null)
    }
    fun flip() { state = state.copy(flipped = !state.flipped) }
    fun clear() = commitPosition(Position.EMPTY)
    fun reset() = commitPosition(Position.START)
    fun setPalette(piece: Piece?) { state = state.copy(palettePiece = piece, selected = null) }
    fun toggleTurn() = commitPosition(state.position.copy(sideToMove = state.position.sideToMove.opposite()))
    fun toggleCastling(symbol: Char) {
        val old = state.position.castling
        val next = when (symbol) {
            'K' -> old.copy(whiteKingSide = !old.whiteKingSide)
            'Q' -> old.copy(whiteQueenSide = !old.whiteQueenSide)
            'k' -> old.copy(blackKingSide = !old.blackKingSide)
            'q' -> old.copy(blackQueenSide = !old.blackQueenSide)
            else -> old
        }
        commitPosition(state.position.copy(castling = next))
    }

    fun setEnPassant(value: String): Boolean {
        val square = if (value.trim() == "-") null else Square.parse(value.trim())
        if (value.trim() != "-" && square == null) { state = state.copy(fenError = "En-passant must be a square such as e3, or '-'."); return false }
        commitPosition(state.position.copy(enPassant = square))
        state = state.copy(fenError = null)
        return true
    }

    fun proceedToLevel(): Boolean = Fen.parse(Fen.encode(state.position)).fold(
        onSuccess = { state = state.copy(screen = AppScreen.LEVEL, fenError = null, selected = null); true },
        onFailure = { state = state.copy(fenError = it.message ?: "The position is not valid."); false },
    )

    fun tapSquare(square: Square) {
        val palette = state.palettePiece
        if (palette != null) {
            commitPosition(state.position.withPiece(square, palette))
            return
        }
        val selected = state.selected
        if (selected == null) {
            if (state.position[square] != null) state = state.copy(selected = square)
        } else if (selected == square) {
            state = state.copy(selected = null)
        } else {
            val moving = state.position[selected]
            commitPosition(state.position.withPiece(selected, null).withPiece(square, moving))
        }
    }

    fun erase(square: Square) {
        commitPosition(state.position.withPiece(square, null))
    }

    fun loadFen(value: String): Boolean = Fen.parse(value).fold(
        onSuccess = {
            commitPosition(it)
            state = state.copy(fenError = null)
            true
        },
        onFailure = {
            state = state.copy(fenError = it.message ?: "Invalid FEN")
            false
        },
    )

    fun loadPgn(value: String): Boolean = Pgn.parse(value).fold(
        onSuccess = {
            commitPosition(it.positions.last())
            state = state.copy(fenError = null)
            true
        },
        onFailure = {
            state = state.copy(fenError = it.message ?: "Invalid PGN")
            false
        },
    )

    fun dismissFenError() { state = state.copy(fenError = null) }

    fun undo() {
        if (undoPositions.isEmpty()) return
        redoPositions.addLast(state.position)
        state = state.copy(position = undoPositions.removeLast(), selected = null, canUndo = undoPositions.isNotEmpty(), canRedo = true)
    }

    fun redo() {
        if (redoPositions.isEmpty()) return
        undoPositions.addLast(state.position)
        state = state.copy(position = redoPositions.removeLast(), selected = null, canUndo = true, canRedo = redoPositions.isNotEmpty())
    }

    fun updateSettings(value: UserSettings) {
        store.saveSettings(value)
        state = state.copy(settings = value)
    }

    fun updateProfile(name: String, imageUri: String?) {
        val profile = LocalProfile(name.trim().ifBlank { "Chess player" }, imageUri)
        store.saveProfile(profile)
        state = state.copy(profile = profile)
    }

    fun analyzeFrom(position: Position) {
        commitPosition(position)
        state = state.copy(screen = AppScreen.LEVEL, result = null)
    }

    suspend fun analyze(level: AnalysisLevel) {
        state = state.copy(level = level, result = null, screen = AppScreen.ANALYSIS)
        val result = engine.analyze(state.position, level)
        store.addHistory(HistoryEntry(fen = Fen.encode(state.position), bestMove = result.bestMove, level = level.title))
        state = state.copy(result = result, history = store.history())
    }

    fun saveCurrent() {
        val result = state.result ?: return
        store.save(SavedPosition(name = "${result.bestMove} idea", fen = Fen.encode(state.position), bestMove = result.bestMove, evaluation = result.evaluation))
        state = state.copy(savedPositions = store.saved())
    }

    fun openSaved(item: SavedPosition) {
        Fen.parse(item.fen).onSuccess { state = state.copy(position = it, screen = AppScreen.LEVEL, selected = null) }
    }
    fun openHistory(item: HistoryEntry) {
        Fen.parse(item.fen).onSuccess { state = state.copy(position = it, screen = AppScreen.LEVEL, selected = null) }
    }

    fun deleteSaved(id: String) { store.deleteSaved(id); state = state.copy(savedPositions = store.saved()) }
    fun clearHistory() { store.clearHistory(); state = state.copy(history = emptyList()) }
    fun clearSaved() { store.clearSaved(); state = state.copy(savedPositions = emptyList()) }
    fun exportData(): String = store.exportJson()
    fun importData(value: String): Boolean = runCatching { store.importJson(value) }.fold(
        onSuccess = {
            state = state.copy(
                savedPositions = store.saved(), history = store.history(), practiceStats = store.stats(),
                settings = store.settings(), profile = store.profile(), fenError = null,
            )
            true
        },
        onFailure = { state = state.copy(fenError = it.message ?: "Could not import backup"); false },
    )

    fun tapPractice(square: Square) {
        if (state.practiceResult != null) return
        val selected = state.practiceSelected
        if (selected == null) {
            if (state.practicePosition[square]?.side == state.practicePosition.sideToMove) state = state.copy(practiceSelected = square)
            return
        }
        if (selected == square) { state = state.copy(practiceSelected = null); return }
        val promotion = if (state.practicePosition[selected]?.type == PieceType.PAWN && square.rank in listOf(0, 7)) PieceType.QUEEN else null
        val move = app.chessmind.domain.model.Move(selected, square, promotion)
        val legal = app.chessmind.domain.model.ChessRules.legalMoves(state.practicePosition)
        if (move !in legal) { state = state.copy(practiceSelected = null, practiceResult = "That move is not legal."); return }
        val correct = move.uci == puzzles[state.puzzleIndex].solution
        val stats = store.recordPractice(correct)
        state = state.copy(
            practicePosition = app.chessmind.domain.model.ChessRules.apply(state.practicePosition, move),
            practiceSelected = null,
            practiceResult = if (correct) "Excellent — you found the best move." else "Good try. Best move: ${puzzles[state.puzzleIndex].solution}",
            practiceStats = stats,
        )
    }

    fun nextPuzzle() {
        val next = (state.puzzleIndex + 1) % puzzles.size
        state = state.copy(puzzleIndex = next, practicePosition = Fen.parse(puzzles[next].fen).getOrThrow(), practiceSelected = null, practiceResult = null)
    }

    fun currentPuzzle(): PracticePuzzle = puzzles[state.puzzleIndex]
    fun stopAnalysis() { engine.stop(); state = state.copy(screen = AppScreen.LEVEL, result = null) }

    fun piece(side: Side, type: PieceType) = Piece(type, side)

    private fun commitPosition(position: Position) {
        if (position == state.position) return
        undoPositions.addLast(state.position)
        while (undoPositions.size > 60) undoPositions.removeFirst()
        redoPositions.clear()
        state = state.copy(position = position, selected = null, canUndo = true, canRedo = false)
    }
}
