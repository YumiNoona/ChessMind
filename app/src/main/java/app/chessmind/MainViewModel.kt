package app.chessmind

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import app.chessmind.domain.engine.AnalysisLevel
import app.chessmind.domain.engine.AnalysisResult
import app.chessmind.domain.engine.PreviewEngine
import app.chessmind.domain.model.Fen
import app.chessmind.domain.model.Piece
import app.chessmind.domain.model.PieceType
import app.chessmind.domain.model.Position
import app.chessmind.domain.model.Side
import app.chessmind.domain.model.Square

enum class AppScreen { HOME, SETUP, LEVEL, ANALYSIS, PRACTICE, SAVED, SETTINGS }

data class AppUiState(
    val screen: AppScreen = AppScreen.HOME,
    val position: Position = Position.START,
    val flipped: Boolean = false,
    val selected: Square? = null,
    val palettePiece: Piece? = null,
    val level: AnalysisLevel = AnalysisLevel.INTERMEDIATE,
    val result: AnalysisResult? = null,
    val fenError: String? = null,
)

class MainViewModel : ViewModel() {
    var state by mutableStateOf(AppUiState())
        private set

    private val engine = PreviewEngine()

    fun navigate(screen: AppScreen) { state = state.copy(screen = screen, selected = null) }
    fun chooseLevel(level: AnalysisLevel) {
        state = state.copy(screen = AppScreen.ANALYSIS, level = level, result = null, selected = null)
    }
    fun flip() { state = state.copy(flipped = !state.flipped) }
    fun clear() { state = state.copy(position = Position.EMPTY, selected = null) }
    fun reset() { state = state.copy(position = Position.START, selected = null) }
    fun setPalette(piece: Piece?) { state = state.copy(palettePiece = piece, selected = null) }
    fun toggleTurn() { state = state.copy(position = state.position.copy(sideToMove = state.position.sideToMove.opposite())) }

    fun tapSquare(square: Square) {
        val palette = state.palettePiece
        if (palette != null) {
            state = state.copy(position = state.position.withPiece(square, palette))
            return
        }
        val selected = state.selected
        if (selected == null) {
            if (state.position[square] != null) state = state.copy(selected = square)
        } else if (selected == square) {
            state = state.copy(selected = null)
        } else {
            val moving = state.position[selected]
            state = state.copy(
                position = state.position.withPiece(selected, null).withPiece(square, moving),
                selected = null,
            )
        }
    }

    fun erase(square: Square) {
        state = state.copy(position = state.position.withPiece(square, null), selected = null)
    }

    fun loadFen(value: String): Boolean = Fen.parse(value).fold(
        onSuccess = {
            state = state.copy(position = it, fenError = null, selected = null)
            true
        },
        onFailure = {
            state = state.copy(fenError = it.message ?: "Invalid FEN")
            false
        },
    )

    fun dismissFenError() { state = state.copy(fenError = null) }

    suspend fun analyze(level: AnalysisLevel) {
        state = state.copy(level = level, result = null, screen = AppScreen.ANALYSIS)
        val result = engine.analyze(state.position, level)
        state = state.copy(result = result)
    }

    fun piece(side: Side, type: PieceType) = Piece(type, side)
}
