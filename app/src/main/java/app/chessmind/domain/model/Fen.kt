package app.chessmind.domain.model

import kotlin.math.abs

object Fen {
    fun parse(input: String): Result<Position> = runCatching {
        val fields = input.trim().split(Regex("\\s+"))
        require(fields.size == 6) { "FEN needs 6 fields (board, turn, castling, en passant, clocks)." }
        val ranks = fields[0].split('/')
        require(ranks.size == 8) { "Board must have exactly 8 ranks." }

        val board = MutableList<Piece?>(64) { null }
        ranks.forEachIndexed { fenRank, encoded ->
            var file = 0
            encoded.forEach { token ->
                if (token.isDigit()) {
                    val empty = token.digitToInt()
                    require(empty in 1..8) { "Empty-square counts must be 1–8." }
                    file += empty
                } else {
                    require(file < 8) { "Rank ${8 - fenRank} has too many squares." }
                    val side = if (token.isUpperCase()) Side.WHITE else Side.BLACK
                    val type = PieceType.entries.firstOrNull { it.fen == token.lowercaseChar() }
                        ?: error("Unknown piece '$token'.")
                    val rank = 7 - fenRank
                    board[rank * 8 + file] = Piece(type, side)
                    file++
                }
            }
            require(file == 8) { "Rank ${8 - fenRank} contains $file squares instead of 8." }
        }

        require(board.count { it == Piece(PieceType.KING, Side.WHITE) } == 1) {
            "White must have exactly one king."
        }
        require(board.count { it == Piece(PieceType.KING, Side.BLACK) } == 1) {
            "Black must have exactly one king."
        }
        val whiteKing = board.indexOf(Piece(PieceType.KING, Side.WHITE))
        val blackKing = board.indexOf(Piece(PieceType.KING, Side.BLACK))
        require(abs(whiteKing % 8 - blackKing % 8) > 1 || abs(whiteKing / 8 - blackKing / 8) > 1) {
            "The kings cannot stand on adjacent squares."
        }
        Side.entries.forEach { side ->
            require(board.count { it?.side == side } <= 16) { "${side.name.lowercase().replaceFirstChar { it.uppercase() }} has more than 16 pieces." }
            require(board.count { it == Piece(PieceType.PAWN, side) } <= 8) { "${side.name.lowercase().replaceFirstChar { it.uppercase() }} has more than 8 pawns." }
        }
        require(board.indices.none { index -> board[index]?.type == PieceType.PAWN && index / 8 in setOf(0, 7) }) {
            "Pawns cannot remain on the first or eighth rank."
        }

        val turn = when (fields[1]) {
            "w" -> Side.WHITE; "b" -> Side.BLACK
            else -> error("Side to move must be 'w' or 'b'.")
        }
        val castling = parseCastling(fields[2])
        val enPassant = if (fields[3] == "-") null else
            Square.parse(fields[3]) ?: error("Invalid en-passant square.")
        val halfmove = fields[4].toIntOrNull()?.also { require(it >= 0) }
            ?: error("Invalid halfmove clock.")
        val fullmove = fields[5].toIntOrNull()?.also { require(it >= 1) }
            ?: error("Fullmove number must be at least 1.")

        Position(board, turn, castling, enPassant, halfmove, fullmove)
    }

    fun encode(position: Position): String {
        val board = (7 downTo 0).joinToString("/") { rank ->
            buildString {
                var empty = 0
                for (file in 0..7) {
                    val piece = position[Square.of(file, rank)!!]
                    if (piece == null) empty++ else {
                        if (empty > 0) append(empty)
                        append(piece.fen)
                        empty = 0
                    }
                }
                if (empty > 0) append(empty)
            }
        }
        return listOf(
            board,
            if (position.sideToMove == Side.WHITE) "w" else "b",
            position.castling.asFen(),
            position.enPassant?.algebraic ?: "-",
            position.halfmoveClock,
            position.fullmoveNumber,
        ).joinToString(" ")
    }

    private fun parseCastling(value: String): CastlingRights {
        if (value == "-") return CastlingRights()
        require(value.all { it in "KQkq" } && value.toSet().size == value.length) {
            "Castling rights may only contain KQkq."
        }
        return CastlingRights('K' in value, 'Q' in value, 'k' in value, 'q' in value)
    }
}
