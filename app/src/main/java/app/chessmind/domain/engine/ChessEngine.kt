package app.chessmind.domain.engine

import app.chessmind.domain.model.Move
import app.chessmind.domain.model.ChessRules
import app.chessmind.domain.model.PieceType
import app.chessmind.domain.model.Position
import app.chessmind.domain.model.Side
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.abs

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

class LocalChessEngine : ChessEngine {
    private val stopped = AtomicBoolean(false)
    private var deadline = Long.MAX_VALUE

    override suspend fun analyze(position: Position, level: AnalysisLevel): AnalysisResult = withContext(Dispatchers.Default) {
        stopped.set(false)
        deadline = System.nanoTime() + level.timeMs * 1_000_000
        val targetDepth = when (level) {
            AnalysisLevel.BEGINNER -> 2
            AnalysisLevel.INTERMEDIATE -> 3
            AnalysisLevel.MASTER -> 4
            AnalysisLevel.GOD -> 5
        }
        val legal = orderedMoves(position)
        if (legal.isEmpty()) return@withContext gameOver(position, targetDepth)

        var completedDepth = 1
        var ranked = legal.map { it to SearchNode(0, listOf(it)) }
        for (depth in 1..targetDepth) {
            if (expired()) break
            val iteration = mutableListOf<Pair<Move, SearchNode>>()
            for (move in legal) {
                if (expired()) break
                val child = ChessRules.apply(position, move)
                val response = negamax(child, depth - 1, -MATE_SCORE, MATE_SCORE, 1)
                iteration += move to SearchNode(-response.score, listOf(move) + response.line)
            }
            if (iteration.size == legal.size) {
                ranked = iteration.sortedByDescending { it.second.score }
                completedDepth = depth
            }
        }

        val visible = ranked.take(level.multiPv.coerceAtLeast(1))
        val bestMove = visible.first().first
        val best = visible.first().second
        val san = notationLine(position, best.line)
        val reasons = explain(position, bestMove)
        val sideName = if (position.sideToMove == Side.WHITE) "White" else "Black"
        AnalysisResult(
            bestMove = ChessRules.notation(position, bestMove),
            evaluation = formatScore(best.score, position.sideToMove),
            depth = completedDepth,
            explanation = "$sideName should play ${ChessRules.notation(position, bestMove)}. ${reasons.joinToString(" ")}",
            reasons = reasons,
            principalVariation = san,
            candidates = visible.mapIndexed { index, (move, node) ->
                CandidateLine(
                    move = ChessRules.notation(position, move),
                    evaluation = formatScore(node.score, position.sideToMove),
                    label = when (index) { 0 -> "Best"; 1 -> "Strong"; else -> "Playable" },
                    variation = notationLine(position, node.line),
                )
            },
            engineName = "ChessMind local engine",
        )
    }

    override fun stop() { stopped.set(true) }

    private fun negamax(position: Position, depth: Int, initialAlpha: Int, beta: Int, ply: Int): SearchNode {
        if (depth == 0 || expired()) return SearchNode(evaluate(position), emptyList())
        val moves = orderedMoves(position)
        if (moves.isEmpty()) return SearchNode(if (ChessRules.isInCheck(position, position.sideToMove)) -MATE_SCORE + ply else 0, emptyList())
        var alpha = initialAlpha
        var best = SearchNode(-MATE_SCORE, emptyList())
        for (move in moves) {
            if (expired()) break
            val response = negamax(ChessRules.apply(position, move), depth - 1, -beta, -alpha, ply + 1)
            val score = -response.score
            if (score > best.score) best = SearchNode(score, listOf(move) + response.line)
            alpha = maxOf(alpha, score)
            if (alpha >= beta) break
        }
        return if (best.line.isEmpty()) SearchNode(evaluate(position), emptyList()) else best
    }

    private fun orderedMoves(position: Position): List<Move> = ChessRules.legalMoves(position).sortedByDescending { move ->
        val captured = position[move.to]?.type?.let(::pieceValue) ?: 0
        captured * 10 + if (move.promotion != null) 900 else 0
    }

    private fun evaluate(position: Position): Int {
        var white = 0
        position.board.forEachIndexed { index, piece ->
            if (piece != null) {
                var value = pieceValue(piece.type)
                val file = index % 8
                val rank = index / 8
                if (piece.type != PieceType.KING && file in 2..5 && rank in 2..5) value += 12
                white += if (piece.side == Side.WHITE) value else -value
            }
        }
        return if (position.sideToMove == Side.WHITE) white else -white
    }

    private fun pieceValue(type: PieceType): Int = when (type) {
        PieceType.PAWN -> 100; PieceType.KNIGHT -> 320; PieceType.BISHOP -> 330
        PieceType.ROOK -> 500; PieceType.QUEEN -> 900; PieceType.KING -> 20_000
    }

    private fun notationLine(start: Position, moves: List<Move>): List<String> {
        var position = start
        return moves.map { move -> ChessRules.notation(position, move).also { position = ChessRules.apply(position, move) } }
    }

    private fun explain(position: Position, move: Move): List<String> {
        val moving = position[move.from] ?: return listOf("It is legal and keeps the position playable.")
        val reasons = mutableListOf<String>()
        val captured = position[move.to]
        if (captured != null) reasons += "It wins or exchanges the ${captured.type.name.lowercase()}."
        if (move.promotion != null) reasons += "The pawn promotes to a ${move.promotion.name.lowercase()}."
        if (moving.type == PieceType.KING && abs(move.to.file - move.from.file) == 2) reasons += "Castling improves king safety and activates the rook."
        val next = ChessRules.apply(position, move)
        if (ChessRules.isInCheck(next, next.sideToMove)) reasons += "It gives check and forces an immediate response."
        if (move.to.file in 2..5 && move.to.rank in 2..5) reasons += "It increases control of the center."
        if (moving.type in setOf(PieceType.KNIGHT, PieceType.BISHOP) && move.from.rank in setOf(0, 7)) reasons += "It develops a minor piece toward active squares."
        if (reasons.isEmpty()) reasons += "It improves the position without allowing an immediate tactical loss."
        return reasons
    }

    private fun formatScore(score: Int, rootSide: Side): String {
        if (abs(score) > MATE_SCORE - 500) return if (score > 0) "Mate" else "Mated"
        val whiteScore = if (rootSide == Side.WHITE) score else -score
        return "%+.2f".format(whiteScore / 100.0)
    }

    private fun gameOver(position: Position, depth: Int): AnalysisResult {
        val check = ChessRules.isInCheck(position, position.sideToMove)
        return AnalysisResult("—", if (check) "Checkmate" else "Draw", depth,
            if (check) "The side to move is checkmated." else "The position is stalemate.",
            listOf(if (check) "No legal move can answer the check" else "No legal move is available"), emptyList(), engineName = "ChessMind local engine")
    }

    private fun expired() = stopped.get() || System.nanoTime() >= deadline
    private data class SearchNode(val score: Int, val line: List<Move>)
    private companion object { const val MATE_SCORE = 100_000 }
}
