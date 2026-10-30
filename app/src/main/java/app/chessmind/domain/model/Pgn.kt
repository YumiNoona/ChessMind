package app.chessmind.domain.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class PgnGame(val tags: Map<String, String>, val moves: List<Move>, val positions: List<Position>, val result: String?)

object Pgn {
    fun parse(input: String): Result<PgnGame> = runCatching {
        val tags = TAG.findAll(input).associate { it.groupValues[1] to it.groupValues[2] }
        var position = tags["FEN"]?.let { Fen.parse(it).getOrThrow() } ?: Position.START
        val positions = mutableListOf(position)
        val moves = mutableListOf<Move>()
        var body = input.lineSequence().filterNot { it.trimStart().startsWith("[") }.joinToString(" ")
            .replace(COMMENT, " ")

        // Repeatedly strip variations to handle nested parentheses cleanly
        while (VARIATION.containsMatchIn(body)) {
            body = body.replace(VARIATION, " ")
        }

        var result: String? = null
        val tokens = body.split(Regex("\\s+")).filter { it.isNotBlank() }
        for (raw in tokens) {
            if (raw in RESULTS) { result = raw; break }
            if (raw.startsWith("$")) continue // Skip numeric annotation glyphs
            if (raw.all { it.isDigit() || it == '.' }) continue

            val token = raw.replace(Regex("^\\d+\\.+"), "").trim()
            if (token.isBlank()) continue
            if (token in RESULTS) { result = token; break }

            val move = findMove(position, token)
                ?: error("Could not apply PGN move '$token' (token '$raw') after ${moves.size} moves.")
            moves += move
            position = ChessRules.apply(position, move)
            positions += position
        }
        require(moves.isNotEmpty()) { "No chess moves were found in the PGN." }
        PgnGame(tags, moves, positions, result)
    }

    fun export(start: Position, moves: List<Move>, event: String = "ChessMind game", result: String = "*"): String {
        var position = start
        val header = buildString {
            append("[Event \"$event\"]\n")
            append("[Site \"ChessMind\"]\n")
            append("[Date \"${SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).format(Date())}\"]\n")
            if (start != Position.START) {
                append("[SetUp \"1\"]\n")
                append("[FEN \"${Fen.encode(start)}\"]\n")
            }
            append("[Result \"$result\"]\n\n")
        }
        val body = buildString {
            var moveNum = start.fullmoveNumber
            var isWhite = start.sideToMove == Side.WHITE
            if (!isWhite && moves.isNotEmpty()) {
                append("$moveNum... ")
            }
            moves.forEach { move ->
                if (isWhite) {
                    append("$moveNum. ")
                }
                append(ChessRules.notation(position, move)).append(' ')
                position = ChessRules.apply(position, move)
                if (!isWhite) {
                    moveNum++
                }
                isWhite = !isWhite
            }
            append(result)
        }
        return header + body.trim()
    }

    private fun findMove(position: Position, rawToken: String): Move? {
        val legal = ChessRules.legalMoves(position)
        if (legal.isEmpty()) return null

        // 1. Direct UCI match (e.g. "e2e4", "e7e8q")
        legal.firstOrNull { it.uci.equals(rawToken, ignoreCase = true) }?.let { return it }

        // Clean token: strip check '+', checkmate '#', annotations '!', '?'
        val token = rawToken.replace(Regex("[+#?!]+$"), "")

        // 2. Exact SAN match with notation
        val wanted = normalize(token)
        legal.firstOrNull { normalize(ChessRules.notation(position, it)) == wanted }?.let { return it }

        // 3. Castling variants (O-O, 0-0, O-O-O, 0-0-0)
        val isKingSideCastle = token == "O-O" || token == "0-0" || token.equals("o-o", ignoreCase = true)
        val isQueenSideCastle = token == "O-O-O" || token == "0-0-0" || token.equals("o-o-o", ignoreCase = true)
        if (isKingSideCastle) {
            return legal.firstOrNull {
                val p = position[it.from]
                p?.type == PieceType.KING && it.to.file - it.from.file == 2
            }
        }
        if (isQueenSideCastle) {
            return legal.firstOrNull {
                val p = position[it.from]
                p?.type == PieceType.KING && it.from.file - it.to.file == 2
            }
        }

        // 4. Flexible SAN resolution
        val promoMatch = Regex("[=(]?([QRBNqrbn])[)]?$").find(token)
        val promoType = promoMatch?.let { m ->
            PieceType.entries.firstOrNull { it.fen == m.groupValues[1].first().lowercaseChar() }
        }
        val baseToken = if (promoMatch != null) token.substring(0, promoMatch.range.first) else token

        val firstChar = baseToken.first()
        val isPieceMove = firstChar in "BNRQK"
        val pieceType = if (isPieceMove) {
            PieceType.entries.firstOrNull { it.fen == firstChar.lowercaseChar() } ?: PieceType.PAWN
        } else PieceType.PAWN

        val movePart = if (isPieceMove) baseToken.substring(1) else baseToken
        val noCapture = movePart.replace("x", "").replace(":", "")

        if (noCapture.length >= 2) {
            val destSquare = Square.parse(noCapture.takeLast(2))
            if (destSquare != null) {
                val disambig = noCapture.dropLast(2)
                val disambigFile = disambig.firstOrNull { it in 'a'..'h' }?.let { it - 'a' }
                val disambigRank = disambig.firstOrNull { it in '1'..'8' }?.let { it - '1' }

                val candidates = legal.filter { m ->
                    val p = position[m.from]
                    p?.type == pieceType &&
                    m.to == destSquare &&
                    (promoType == null || m.promotion == promoType) &&
                    (disambigFile == null || m.from.file == disambigFile) &&
                    (disambigRank == null || m.from.rank == disambigRank)
                }
                if (candidates.isNotEmpty()) return candidates.first()
            }
        }

        // Short pawn capture notation like "ed"
        if (!isPieceMove && noCapture.length == 2 && noCapture[0] in 'a'..'h' && noCapture[1] in 'a'..'h') {
            val fromFile = noCapture[0] - 'a'
            val toFile = noCapture[1] - 'a'
            return legal.firstOrNull { m ->
                position[m.from]?.type == PieceType.PAWN &&
                m.from.file == fromFile &&
                m.to.file == toFile
            }
        }

        return null
    }

    private fun normalize(value: String): String =
        value.replace(Regex("[+#?!]+$"), "")
            .replace("0-0-0", "O-O-O")
            .replace("0-0", "O-O")

    private val TAG = Regex("(?m)^\\s*\\[([A-Za-z0-9_]+)\\s+\"([^\"]*)\"\\]\\s*$")
    private val COMMENT = Regex("\\{[^}]*}|;[^\\r\\n]*")
    private val VARIATION = Regex("\\([^()]*\\)")
    private val RESULTS = setOf("1-0", "0-1", "1/2-1/2", "*")
}
