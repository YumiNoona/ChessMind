package app.chessmind.domain.model

enum class Side { WHITE, BLACK;
    fun opposite() = if (this == WHITE) BLACK else WHITE
}

enum class PieceType(val fen: Char, val symbol: String) {
    KING('k', "♚"), QUEEN('q', "♛"), ROOK('r', "♜"),
    BISHOP('b', "♝"), KNIGHT('n', "♞"), PAWN('p', "♟")
}

data class Piece(val type: PieceType, val side: Side) {
    val fen: Char get() = if (side == Side.WHITE) type.fen.uppercaseChar() else type.fen
    val symbol: String get() = when (side) {
        Side.WHITE -> when (type) {
            PieceType.KING -> "♔"; PieceType.QUEEN -> "♕"; PieceType.ROOK -> "♖"
            PieceType.BISHOP -> "♗"; PieceType.KNIGHT -> "♘"; PieceType.PAWN -> "♙"
        }
        Side.BLACK -> type.symbol
    }
}

@JvmInline
value class Square private constructor(val index: Int) {
    val file: Int get() = index % 8
    val rank: Int get() = index / 8
    val algebraic: String get() = "${('a'.code + file).toChar()}${rank + 1}"

    companion object {
        fun of(file: Int, rank: Int): Square? =
            if (file in 0..7 && rank in 0..7) Square(rank * 8 + file) else null

        fun parse(value: String): Square? {
            if (value.length != 2) return null
            return of(value[0].lowercaseChar() - 'a', value[1] - '1')
        }

        fun fromIndex(index: Int): Square = Square(index)
    }
}

data class CastlingRights(
    val whiteKingSide: Boolean = false,
    val whiteQueenSide: Boolean = false,
    val blackKingSide: Boolean = false,
    val blackQueenSide: Boolean = false,
) {
    fun asFen(): String = buildString {
        if (whiteKingSide) append('K')
        if (whiteQueenSide) append('Q')
        if (blackKingSide) append('k')
        if (blackQueenSide) append('q')
    }.ifEmpty { "-" }
}

data class Position(
    val board: List<Piece?>,
    val sideToMove: Side = Side.WHITE,
    val castling: CastlingRights = CastlingRights(),
    val enPassant: Square? = null,
    val halfmoveClock: Int = 0,
    val fullmoveNumber: Int = 1,
) {
    init { require(board.size == 64) { "Board must contain exactly 64 squares" } }

    operator fun get(square: Square): Piece? = board[square.index]
    fun withPiece(square: Square, piece: Piece?): Position =
        copy(board = board.toMutableList().also { it[square.index] = piece })

    companion object {
        val EMPTY = Position(List(64) { null })
        val START = Fen.parse("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1").getOrThrow()
    }
}

data class Move(
    val from: Square,
    val to: Square,
    val promotion: PieceType? = null,
) {
    val uci: String get() = from.algebraic + to.algebraic + (promotion?.fen ?: "")
}
