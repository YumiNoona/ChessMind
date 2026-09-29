package app.chessmind.data.engine

import android.content.Context
import app.chessmind.domain.engine.AnalysisLevel
import app.chessmind.domain.engine.AnalysisResult
import app.chessmind.domain.engine.CandidateLine
import app.chessmind.domain.engine.ChessEngine
import app.chessmind.domain.engine.LocalChessEngine
import app.chessmind.domain.model.ChessRules
import app.chessmind.domain.model.Fen
import app.chessmind.domain.model.Move
import app.chessmind.domain.model.PieceType
import app.chessmind.domain.model.Position
import app.chessmind.domain.model.Side
import app.chessmind.domain.model.Square
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.TreeMap

class HybridChessEngine(context: Context) : ChessEngine {
    private val stockfish = StockfishProcessEngine(context.applicationContext)
    private val fallback = LocalChessEngine()

    override suspend fun analyze(position: Position, level: AnalysisLevel): AnalysisResult = try {
        stockfish.analyze(position, level)
    } catch (error: Throwable) {
        if (error is CancellationException) throw error
        fallback.analyze(position, level)
    }

    override fun stop() { stockfish.stop(); fallback.stop() }
}

private class StockfishProcessEngine(private val context: Context) : ChessEngine {
    private val mutex = Mutex()
    private var process: Process? = null
    private var input: BufferedWriter? = null
    private var output: BufferedReader? = null

    override suspend fun analyze(position: Position, level: AnalysisLevel): AnalysisResult = withContext(Dispatchers.IO) {
        mutex.withLock {
            ensureStarted()
            command("ucinewgame")
            command("setoption name Threads value ${threads(level)}")
            command("setoption name Hash value ${hash(level)}")
            command("setoption name MultiPV value ${level.multiPv}")
            command("isready")
            readUntil("readyok")
            command("position fen ${Fen.encode(position)}")
            command("go movetime ${level.timeMs}")

            val lines = TreeMap<Int, EngineLine>()
            var bestUci: String? = null
            while (true) {
                val line = output?.readLine() ?: error("Stockfish stopped unexpectedly")
                if (line.startsWith("bestmove ")) { bestUci = line.split(' ').getOrNull(1); break }
                parseInfo(line)?.let { lines[it.multiPv] = it }
            }
            val bestMove = parseMove(bestUci ?: error("Stockfish returned no move"))
            val ordered = lines.values.toList().ifEmpty { listOf(EngineLine(1, 0, null, 0, listOf(bestMove.uci))) }
            val main = ordered.first()
            val mainMoves = main.pv.mapNotNull(::parseMoveOrNull)
            val reasons = explain(position, bestMove)
            val side = if (position.sideToMove == Side.WHITE) "White" else "Black"
            AnalysisResult(
                bestMove = ChessRules.notation(position, bestMove),
                evaluation = displayScore(main, position.sideToMove),
                depth = main.depth,
                explanation = "$side should play ${ChessRules.notation(position, bestMove)}. ${reasons.joinToString(" ")}",
                reasons = reasons,
                principalVariation = notationLine(position, mainMoves),
                principalVariationUci = main.pv,
                candidates = ordered.take(level.multiPv).mapIndexed { index, engineLine ->
                    val pv = engineLine.pv.mapNotNull(::parseMoveOrNull)
                    val move = pv.firstOrNull()
                    CandidateLine(
                        move = move?.let { ChessRules.notation(position, it) } ?: engineLine.pv.firstOrNull().orEmpty(),
                        evaluation = displayScore(engineLine, position.sideToMove),
                        label = if (index == 0) "Best" else if (index == 1) "Strong" else "Candidate",
                        variation = notationLine(position, pv),
                    )
                },
                engineName = "Stockfish 19 · Offline",
            )
        }
    }

    override fun stop() { runCatching { command("stop") } }

    private fun ensureStarted() {
        if (process?.isAlive == true) return
        val binary = File(context.applicationInfo.nativeLibraryDir, "libstockfish.so")
        require(binary.exists()) { "Stockfish is unavailable for this device ABI" }
        process = ProcessBuilder(binary.absolutePath).redirectErrorStream(true).start()
        input = BufferedWriter(OutputStreamWriter(process!!.outputStream))
        output = BufferedReader(InputStreamReader(process!!.inputStream))
        command("uci")
        readUntil("uciok")
    }

    private fun command(value: String) { input?.apply { write(value); newLine(); flush() } ?: error("Engine is not running") }
    private fun readUntil(marker: String) { while (output?.readLine()?.contains(marker) != true) Unit }

    private fun parseInfo(value: String): EngineLine? {
        if (!value.startsWith("info ") || " pv " !in value || " depth " !in value) return null
        val tokens = value.split(' ')
        fun after(name: String) = tokens.indexOf(name).takeIf { it >= 0 }?.let { tokens.getOrNull(it + 1) }
        val depth = after("depth")?.toIntOrNull() ?: return null
        val multiPv = after("multipv")?.toIntOrNull() ?: 1
        val cpIndex = tokens.indexOf("cp")
        val mateIndex = tokens.indexOf("mate")
        val cp = if (cpIndex >= 0) tokens.getOrNull(cpIndex + 1)?.toIntOrNull() else null
        val mate = if (mateIndex >= 0) tokens.getOrNull(mateIndex + 1)?.toIntOrNull() else null
        val pvIndex = tokens.indexOf("pv")
        if (pvIndex < 0) return null
        return EngineLine(multiPv, cp, mate, depth, tokens.drop(pvIndex + 1))
    }

    private fun parseMove(value: String): Move = parseMoveOrNull(value) ?: error("Invalid engine move '$value'")
    private fun parseMoveOrNull(value: String): Move? {
        if (value.length !in 4..5) return null
        val from = Square.parse(value.substring(0, 2)) ?: return null
        val to = Square.parse(value.substring(2, 4)) ?: return null
        val promotion = value.getOrNull(4)?.let { symbol -> PieceType.entries.firstOrNull { it.fen == symbol.lowercaseChar() } }
        return Move(from, to, promotion)
    }

    private fun notationLine(start: Position, moves: List<Move>): List<String> {
        var position = start
        return moves.take(12).mapNotNull { move ->
            if (move !in ChessRules.legalMoves(position)) return@mapNotNull null
            ChessRules.notation(position, move).also { position = ChessRules.apply(position, move) }
        }
    }

    private fun displayScore(line: EngineLine, side: Side): String {
        line.mate?.let { return if (it > 0) "M$it" else "M${-it} against" }
        val white = (line.cp ?: 0) * if (side == Side.WHITE) 1 else -1
        return "%+.2f".format(white / 100.0)
    }

    private fun explain(position: Position, move: Move): List<String> {
        val reasons = mutableListOf<String>()
        position[move.to]?.let { reasons += "It captures the ${it.type.name.lowercase()}." }
        val next = ChessRules.apply(position, move)
        if (ChessRules.isInCheck(next, next.sideToMove)) reasons += "It gives check and restricts the reply."
        if (move.to.file in 2..5 && move.to.rank in 2..5) reasons += "It strengthens central control."
        if (move.promotion != null) reasons += "It promotes the pawn."
        if (reasons.isEmpty()) reasons += "It is the engine's most accurate continuation at the completed depth."
        return reasons
    }

    private fun threads(level: AnalysisLevel) = when (level) { AnalysisLevel.GOD -> 4; AnalysisLevel.MASTER -> 3; else -> 2 }
    private fun hash(level: AnalysisLevel) = when (level) { AnalysisLevel.GOD -> 128; AnalysisLevel.MASTER -> 64; else -> 32 }
    private data class EngineLine(val multiPv: Int, val cp: Int?, val mate: Int?, val depth: Int, val pv: List<String>)
}
