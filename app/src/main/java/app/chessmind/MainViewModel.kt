package app.chessmind

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.chessmind.data.HistoryEntry
import app.chessmind.data.LocalStore
import app.chessmind.data.PracticeStats
import app.chessmind.data.SavedPosition
import app.chessmind.data.UserSettings
import app.chessmind.data.LocalProfile
import app.chessmind.data.PlayerProgress
import app.chessmind.domain.engine.AnalysisLevel
import app.chessmind.domain.engine.AnalysisResult
import app.chessmind.data.engine.HybridChessEngine
import app.chessmind.domain.engine.LocalChessEngine
import app.chessmind.domain.model.Fen
import app.chessmind.domain.model.Piece
import app.chessmind.domain.model.PieceType
import app.chessmind.domain.model.Pgn
import app.chessmind.domain.model.Position
import app.chessmind.domain.model.Side
import app.chessmind.domain.model.Square
import app.chessmind.domain.model.Move
import app.chessmind.domain.model.ChessRules
import kotlin.random.Random
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.launch

enum class AppScreen { SPLASH, ONBOARDING, HOME, PROFILE, SCANNER, IMAGE_REVIEW, SETUP, LEVEL, ANALYSIS, PLAY_SELECT, GAME, GAME_REVIEW, HISTORY, SETTINGS }

enum class GameMode { AI, FRIEND }

data class GameMoveRecord(val before: Position, val after: Position, val move: Move, val notation: String, val side: Side)
data class GameReviewItem(val ply: Int, val side: Side, val move: String, val bestMove: String, val verdict: String, val position: Position)

data class PracticePuzzle(val title: String, val subtitle: String, val fen: String, val solution: String, val hint: String)

data class AppUiState(
    val screen: AppScreen = AppScreen.SPLASH,
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
    val progress: PlayerProgress = PlayerProgress(),
    val gameMode: GameMode = GameMode.AI,
    val gamePosition: Position = Position.START,
    val gameSelected: Square? = null,
    val gameMoves: List<GameMoveRecord> = emptyList(),
    val gameResult: String? = null,
    val aiRating: Int = 425,
    val aiThinking: Boolean = false,
    val gameReview: List<GameReviewItem> = emptyList(),
    val reviewLoading: Boolean = false,
    val reviewProgress: Int = 0,
    val reviewTotal: Int = 0,
    val gameRatingBefore: Int = 500,
    val lastRatingChange: Int = 0,
    val selectedTimeMinutes: Int = 5,
    val whiteTimeSeconds: Int = 300,
    val blackTimeSeconds: Int = 300,
    val clockStarted: Boolean = false,
    val aiDrawOffer: Boolean = false,
    val aiHasOfferedDraw: Boolean = false,
    val drawStatus: String? = null,
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
            screen = AppScreen.SPLASH,
            savedPositions = store.saved(), history = store.history(), practiceStats = store.stats(),
            settings = store.settings(),
            profile = store.profile(),
            progress = store.progress(),
            selectedTimeMinutes = store.settings().defaultTimeMinutes,
            practicePosition = Fen.parse(puzzles.first().fen).getOrThrow(),
        )
    )
        private set

    private val engine = HybridChessEngine(application)
    private val emergencyEngine = LocalChessEngine()
    private val launchDestination = if (store.isOnboarded()) AppScreen.HOME else AppScreen.ONBOARDING
    private val undoPositions = ArrayDeque<Position>()
    private val redoPositions = ArrayDeque<Position>()

    fun navigate(screen: AppScreen) { state = state.copy(screen = screen, selected = null) }
    fun finishSplash() { if (state.screen == AppScreen.SPLASH) state = state.copy(screen = launchDestination) }
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

    fun dragSetupPiece(from: Square, to: Square) {
        val moving = state.position[from] ?: return
        commitPosition(state.position.withPiece(from, null).withPiece(to, moving))
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
        val selectedClock = if (value.defaultTimeMinutes != state.settings.defaultTimeMinutes) {
            value.defaultTimeMinutes
        } else {
            state.selectedTimeMinutes
        }
        state = state.copy(settings = value, selectedTimeMinutes = selectedClock)
    }

    fun updateProfile(name: String, imageUri: String?) {
        val profile = LocalProfile(name.trim().ifBlank { "Chess player" }, imageUri)
        store.saveProfile(profile)
        state = state.copy(profile = profile)
    }

    fun setMatchMinutes(minutes: Int) { state = state.copy(selectedTimeMinutes = minutes) }

    fun startGame(mode: GameMode) {
        val playerRating = state.progress.rating
        val leagueOpponent = when {
            playerRating < 600 -> playerRating - Random.nextInt(170, 251)
            playerRating < 800 -> playerRating - Random.nextInt(110, 181)
            playerRating < 1_000 -> playerRating - Random.nextInt(80, 141)
            playerRating < 1_300 -> playerRating - Random.nextInt(60, 111)
            playerRating < 1_600 -> playerRating - Random.nextInt(45, 91)
            else -> playerRating - Random.nextInt(25, 71)
        }
        val opponent = (leagueOpponent + when (state.settings.aiStrength) {
            -1 -> -100
            1 -> 90
            else -> 0
        }).coerceAtLeast(150)
        val clockSeconds = state.selectedTimeMinutes * 60
        state = state.copy(
            screen = AppScreen.GAME, gameMode = mode, gamePosition = Position.START,
            gameSelected = null, gameMoves = emptyList(), gameResult = null,
            aiRating = opponent, aiThinking = false, gameReview = emptyList(), reviewLoading = false,
            reviewProgress = 0, reviewTotal = 0, gameRatingBefore = state.progress.rating, lastRatingChange = 0,
            whiteTimeSeconds = clockSeconds, blackTimeSeconds = clockSeconds,
            clockStarted = false, aiDrawOffer = false, aiHasOfferedDraw = false, drawStatus = null,
            flipped = false,
        )
    }

    fun tickGameClock() {
        if (state.gameResult != null || state.selectedTimeMinutes == 0 || !state.clockStarted) return
        if (state.gamePosition.sideToMove == Side.WHITE) {
            val next = (state.whiteTimeSeconds - 1).coerceAtLeast(0)
            state = state.copy(whiteTimeSeconds = next)
            if (next == 0) finishGame("Black wins on time")
        } else {
            val next = (state.blackTimeSeconds - 1).coerceAtLeast(0)
            state = state.copy(blackTimeSeconds = next)
            if (next == 0) finishGame("White wins on time")
        }
    }

    fun tapGame(square: Square) {
        if (state.gameResult != null || state.aiThinking) return
        if (state.gameMode == GameMode.AI && state.gamePosition.sideToMove == Side.BLACK) return
        val selected = state.gameSelected
        if (selected == null) {
            if (state.gamePosition[square]?.side == state.gamePosition.sideToMove) state = state.copy(gameSelected = square)
            return
        }
        if (selected == square) { state = state.copy(gameSelected = null); return }
        if (!attemptGameMove(selected, square)) state = state.copy(gameSelected = null)
    }

    fun dragGame(from: Square, to: Square) {
        if (state.gameResult != null || state.aiThinking) return
        if (state.gameMode == GameMode.AI && state.gamePosition.sideToMove == Side.BLACK) return
        attemptGameMove(from, to)
    }

    private fun attemptGameMove(from: Square, to: Square): Boolean {
        val move = ChessRules.legalMoves(state.gamePosition).firstOrNull {
            it.from == from && it.to == to && (it.promotion == null || it.promotion == PieceType.QUEEN)
        } ?: return false
        applyGameMove(move)
        if (state.gameMode == GameMode.AI && state.gameResult == null) state = state.copy(aiThinking = true)
        return true
    }

    suspend fun playAiMove() {
        if (!state.aiThinking || state.gameResult != null || state.gamePosition.sideToMove != Side.BLACK) return
        val position = state.gamePosition
        val level = when {
            state.aiRating < 650 -> AnalysisLevel.BEGINNER
            state.aiRating < 1000 -> AnalysisLevel.INTERMEDIATE
            state.aiRating < 1500 -> AnalysisLevel.MASTER
            else -> AnalysisLevel.GOD
        }
        val result = withTimeoutOrNull(3_500) { engine.analyze(position, level) }
        if (result == null) engine.stop()
        val legal = ChessRules.legalMoves(position)
        val best = result?.principalVariationUci?.firstOrNull()?.let(::parseUciMove)?.takeIf { it in legal }
        val leagueAccuracy = when {
            state.aiRating < 400 -> .12
            state.aiRating < 600 -> .22
            state.aiRating < 800 -> .34
            state.aiRating < 1_000 -> .46
            state.aiRating < 1_300 -> .60
            state.aiRating < 1_600 -> .72
            state.aiRating < 2_000 -> .84
            else -> .92
        }
        val accuracy = (leagueAccuracy + when (state.settings.aiStrength) {
            -1 -> -.10
            1 -> .08
            else -> 0.0
        }).coerceIn(.08, .96)
        val move = when {
            state.settings.aiVariedOpenings && state.gameMoves.size <= 1 -> humanLikeMove(position, legal, best)
            best != null && Random.nextDouble() < accuracy -> best
            state.aiRating < 600 -> legal.randomOrNull()
            else -> humanLikeMove(position, legal, best)
        }
        if (move != null) applyGameMove(move)
        state = state.copy(aiThinking = false)
    }

    fun resignGame() {
        if (state.gameResult != null) return
        finishGame(if (state.gameMode == GameMode.AI) "AI wins by resignation" else "${state.gamePosition.sideToMove.opposite().name.lowercase().replaceFirstChar { it.uppercase() }} wins by resignation")
    }

    fun offerDraw() {
        if (state.gameResult != null || state.gameMoves.isEmpty() || state.aiThinking) return
        if (state.gameMode == GameMode.FRIEND) {
            finishGame("Draw by agreement")
            return
        }
        if (drawIsReasonable(state.gamePosition, state.gameMoves.size)) {
            finishGame("Draw by agreement")
        } else {
            state = state.copy(drawStatus = "The AI declined—the position still has plenty of play.")
        }
    }

    fun respondToAiDraw(accept: Boolean) {
        if (!state.aiDrawOffer || state.gameResult != null) return
        if (accept) finishGame("Draw by agreement")
        else state = state.copy(aiDrawOffer = false, drawStatus = "You declined the AI's draw offer.")
    }

    fun buildGameReview() {
        if (state.gameMoves.isEmpty()) return
        viewModelScope.launch {
            val moves = state.gameMoves
            val inspected = if (moves.size <= 24) moves.indices.toSet() else {
                List(24) { step -> (step * (moves.lastIndex.toDouble() / 23.0)).toInt() }.toSet()
            }
            state = state.copy(screen = AppScreen.GAME_REVIEW, reviewLoading = true, gameReview = emptyList(), reviewProgress = 0, reviewTotal = moves.size)
            val reviews = mutableListOf<GameReviewItem>()
            moves.forEachIndexed { index, record ->
                val answer = if (index in inspected) withTimeoutOrNull(700) {
                    engine.analyze(record.before, AnalysisLevel.BEGINNER)
                } else null
                if (answer == null && index in inspected) engine.stop()
                val bestMove = answer?.bestMove ?: record.notation
                val verdict = when {
                    answer == null -> "Quick review"
                    normalizeMove(bestMove) == normalizeMove(record.notation) -> "Best move"
                    else -> "Review this"
                }
                reviews += GameReviewItem(index + 1, record.side, record.notation, bestMove, verdict, record.after)
                state = state.copy(gameReview = reviews.toList(), reviewProgress = index + 1)
            }
            state = state.copy(reviewLoading = false)
        }
    }

    private fun applyGameMove(move: Move) {
        val before = state.gamePosition
        val notation = ChessRules.notation(before, move)
        val after = ChessRules.apply(before, move)
        state = state.copy(
            gamePosition = after, gameSelected = null,
            gameMoves = state.gameMoves + GameMoveRecord(before, after, move, notation, before.sideToMove),
            clockStarted = true, drawStatus = null,
            flipped = if (state.gameMode == GameMode.FRIEND && state.settings.autoFlipFriend) !state.flipped else state.flipped,
        )
        val legal = ChessRules.legalMoves(after)
        if (legal.isEmpty()) {
            val result = if (ChessRules.isInCheck(after, after.sideToMove)) {
                "${before.sideToMove.name.lowercase().replaceFirstChar { it.uppercase() }} wins by checkmate"
            } else "Draw by stalemate"
            finishGame(result)
        } else if (
            state.gameMode == GameMode.AI && before.sideToMove == Side.BLACK && !state.aiHasOfferedDraw &&
            drawIsReasonable(after, state.gameMoves.size)
        ) {
            state = state.copy(aiDrawOffer = true, aiHasOfferedDraw = true)
        }
    }

    private fun drawIsReasonable(position: Position, plies: Int): Boolean {
        val values = mapOf(PieceType.PAWN to 1, PieceType.KNIGHT to 3, PieceType.BISHOP to 3, PieceType.ROOK to 5, PieceType.QUEEN to 9, PieceType.KING to 0)
        val balance = kotlin.math.abs(position.board.sumOf { piece ->
            if (piece == null) 0 else (values[piece.type] ?: 0) * if (piece.side == Side.WHITE) 1 else -1
        })
        val (minimumPlies, maximumImbalance) = when (state.settings.aiDrawPolicy) {
            0 -> 44 to 0
            2 -> 16 to 2
            else -> 28 to 1
        }
        return plies >= minimumPlies && balance <= maximumImbalance
    }

    private fun finishGame(result: String) {
        var progress = state.progress
        var ratingChange = 0
        if (state.gameMode == GameMode.AI) {
            val score = when {
                result.startsWith("White wins") -> 1.0
                result.startsWith("Draw") -> .5
                else -> 0.0
            }
            val expected = 1.0 / (1.0 + Math.pow(10.0, (state.aiRating - progress.rating) / 400.0))
            val change = (32 * (score - expected)).toInt()
            ratingChange = change
            progress = progress.copy(
                rating = (progress.rating + change).coerceAtLeast(100), games = progress.games + 1,
                wins = progress.wins + if (score == 1.0) 1 else 0,
                draws = progress.draws + if (score == .5) 1 else 0,
            )
            store.saveProgress(progress)
        }
        state = state.copy(gameResult = result, aiThinking = false, aiDrawOffer = false, progress = progress, lastRatingChange = ratingChange)
    }

    private fun humanLikeMove(position: Position, legal: List<Move>, best: Move?): Move? {
        if (legal.isEmpty()) return null
        val values = mapOf(PieceType.PAWN to 1, PieceType.KNIGHT to 3, PieceType.BISHOP to 3, PieceType.ROOK to 5, PieceType.QUEEN to 9, PieceType.KING to 0)
        val ranked = legal.filterNot { it == best }.sortedByDescending { move ->
            val capture = position[move.to]?.let { values[it.type] ?: 0 } ?: 0
            val center = if (move.to.file in 2..5 && move.to.rank in 2..5) 1 else 0
            capture * 10 + center + Random.nextInt(0, 8)
        }
        val pool = when { state.aiRating < 600 -> 8; state.aiRating < 1000 -> 5; else -> 3 }
        return ranked.take(pool).randomOrNull() ?: best ?: legal.random()
    }

    private fun parseUciMove(uci: String): Move? {
        if (uci.length !in 4..5) return null
        val from = Square.parse(uci.substring(0, 2)) ?: return null
        val to = Square.parse(uci.substring(2, 4)) ?: return null
        val promotion = uci.getOrNull(4)?.let { symbol -> PieceType.entries.firstOrNull { it.fen == symbol.lowercaseChar() } }
        return Move(from, to, promotion)
    }

    private fun normalizeMove(value: String) = value.replace("+", "").replace("#", "").trim()

    fun analyzeFrom(position: Position) {
        commitPosition(position)
        state = state.copy(screen = AppScreen.LEVEL, result = null)
    }

    suspend fun analyze(level: AnalysisLevel) {
        state = state.copy(level = level, result = null, screen = AppScreen.ANALYSIS)
        val position = state.position
        val result = withTimeoutOrNull(level.timeMs + 2_500) { engine.analyze(position, level) } ?: run {
            engine.stop()
            withTimeoutOrNull(2_000) { emergencyEngine.analyze(position, AnalysisLevel.BEGINNER) }
        } ?: run {
            state = state.copy(screen = AppScreen.LEVEL)
            return
        }
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

    fun dragPractice(from: Square, to: Square) {
        if (state.practicePosition[from]?.side != state.practicePosition.sideToMove) return
        state = state.copy(practiceSelected = from)
        tapPractice(to)
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
