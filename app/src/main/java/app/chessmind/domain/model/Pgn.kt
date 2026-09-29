package app.chessmind.domain.model

data class PgnGame(val tags: Map<String, String>, val moves: List<Move>, val positions: List<Position>, val result: String?)

object Pgn {
    fun parse(input: String): Result<PgnGame> = runCatching {
        val tags = TAG.findAll(input).associate { it.groupValues[1] to it.groupValues[2] }
        var position = tags["FEN"]?.let { Fen.parse(it).getOrThrow() } ?: Position.START
        val positions = mutableListOf(position)
        val moves = mutableListOf<Move>()
        val body = input.lineSequence().filterNot { it.trimStart().startsWith("[") }.joinToString(" ")
            .replace(COMMENT, " ").replace(VARIATION, " ")
        var result: String? = null
        val tokens = body.split(Regex("\\s+")).filter { it.isNotBlank() }
        for (raw in tokens) {
            val token = raw.replace(Regex("^\\d+\\.(\\.\\.)?"), "").trim()
            if (token.isBlank() || token.matches(Regex("\\d+\\.+"))) continue
            if (token in RESULTS) { result = token; break }
            val wanted = normalize(token)
            val legal = ChessRules.legalMoves(position)
            val move = legal.firstOrNull { normalize(ChessRules.notation(position, it)) == wanted }
                ?: legal.firstOrNull { it.uci.equals(token, ignoreCase = true) }
                ?: error("Could not apply PGN move '${token}' after ${moves.size} moves.")
            moves += move
            position = ChessRules.apply(position, move)
            positions += position
        }
        require(moves.isNotEmpty()) { "No chess moves were found in the PGN." }
        PgnGame(tags, moves, positions, result)
    }

    fun export(start: Position, moves: List<Move>, event: String = "ChessMind analysis"): String {
        var position = start
        val body = buildString {
            moves.forEachIndexed { index, move ->
                if (index % 2 == 0) append("${index / 2 + 1}. ")
                append(ChessRules.notation(position, move)).append(' ')
                position = ChessRules.apply(position, move)
            }
            append('*')
        }
        return "[Event \"$event\"]\n[Result \"*\"]\n\n${body.trim()}"
    }

    private fun normalize(value: String) = value.replace(Regex("[+#?!]+$"), "").replace("0-0", "O-O")
    private val TAG = Regex("(?m)^\\s*\\[([A-Za-z0-9_]+)\\s+\"([^\"]*)\"\\]\\s*$")
    private val COMMENT = Regex("\\{[^}]*}|;[^\\r\\n]*")
    private val VARIATION = Regex("\\([^()]*\\)")
    private val RESULTS = setOf("1-0", "0-1", "1/2-1/2", "*")
}
