package app.chessmind.domain.engine

import app.chessmind.domain.model.Move
import app.chessmind.domain.model.ChessRules
import app.chessmind.domain.model.Position

enum class AnalysisLevel(val title: String, val subtitle: String, val timeMs: Long, val multiPv: Int) {
    BEGINNER("Beginner", "Simple, safe, easy to understand", 250, 1),
    INTERMEDIATE("Intermediate", "Practical club-level analysis", 800, 2),
    MASTER("Master", "Deeper plans and candidate moves", 2_500, 3),
    GOD("God mode", "Strongest practical engine answer", 6_000, 4),
}

data class CandidateLine(
    val move: String,
    val evaluation: String,
    val label: String,
    val variation: List<String>,
)

data class AnalysisResult(
    val bestMove: String,
    val evaluation: String,
    val depth: Int,
    val explanation: String,
    val reasons: List<String>,
    val principalVariation: List<String>,
    val candidates: List<CandidateLine> = emptyList(),
    val engineName: String,
)

interface ChessEngine {
    suspend fun analyze(position: Position, level: AnalysisLevel): AnalysisResult
    fun stop()
}

/**
 * Deterministic local preview engine used until the Stockfish binary is packaged.
 * It keeps UI/domain work testable and is intentionally labelled as preview analysis.
 */
class PreviewEngine : ChessEngine {
    override suspend fun analyze(position: Position, level: AnalysisLevel): AnalysisResult {
        val legalMoves = ChessRules.legalMoves(position)
        val preferred = listOf("g1f3", "g8f6", "b1c3", "b8c6", "d2d4", "d7d5", "e2e4", "e7e5")
        val best = preferred.firstNotNullOfOrNull { uci -> legalMoves.firstOrNull { it.uci == uci } }
            ?: legalMoves.firstOrNull()
        val notation = best?.let { ChessRules.notation(position, it) } ?: "—"
        val candidateMoves = legalMoves.take(level.multiPv.coerceAtLeast(1))
        val white = position.sideToMove.name.lowercase().replaceFirstChar { it.uppercase() }
        return AnalysisResult(
            bestMove = notation,
            evaluation = if (best == null) "Game over" else "+0.20",
            depth = when (level) {
                AnalysisLevel.BEGINNER -> 6; AnalysisLevel.INTERMEDIATE -> 10
                AnalysisLevel.MASTER -> 14; AnalysisLevel.GOD -> 18
            },
            explanation = if (best == null) "$white has no legal move in this position." else
                "$white can play $notation. This local preview verifies legality; full strength and evaluation arrive with the packaged engine.",
            reasons = if (best == null) listOf("Checkmate, stalemate, or an invalid edited position") else
                listOf("Legal in the current position", "Keeps the analysis fully offline", "Ready for deeper Stockfish evaluation"),
            principalVariation = listOf(notation).filter { it != "—" },
            candidates = candidateMoves.mapIndexed { index, move ->
                CandidateLine(ChessRules.notation(position, move), "—", if (index == 0) "Preview" else "Legal", listOf(move.uci))
            },
            engineName = "Preview engine",
        )
    }

    override fun stop() = Unit
}
