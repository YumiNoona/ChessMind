package app.chessmind.domain.model

object ChessRules {
    fun legalMoves(position: Position): List<Move> = pseudoLegalMoves(position).filter { move ->
        val next = apply(position, move, switchTurn = false)
        !isInCheck(next, position.sideToMove)
    }

    fun apply(position: Position, move: Move, switchTurn: Boolean = true): Position {
        val moving = position[move.from] ?: return position
        val board = position.board.toMutableList()
        val target = board[move.to.index]
        board[move.from.index] = null

        if (moving.type == PieceType.PAWN && move.to == position.enPassant && target == null) {
            Square.of(move.to.file, move.from.rank)?.let { board[it.index] = null }
        }
        if (moving.type == PieceType.KING && kotlin.math.abs(move.to.file - move.from.file) == 2) {
            val kingSide = move.to.file > move.from.file
            val rookFrom = Square.of(if (kingSide) 7 else 0, move.from.rank)!!
            val rookTo = Square.of(if (kingSide) 5 else 3, move.from.rank)!!
            board[rookTo.index] = board[rookFrom.index]
            board[rookFrom.index] = null
        }
        board[move.to.index] = if (move.promotion != null) Piece(move.promotion, moving.side) else moving

        val rights = position.castling.copy(
            whiteKingSide = position.castling.whiteKingSide && moving != Piece(PieceType.KING, Side.WHITE) && move.from != Square.parse("h1") && move.to != Square.parse("h1"),
            whiteQueenSide = position.castling.whiteQueenSide && moving != Piece(PieceType.KING, Side.WHITE) && move.from != Square.parse("a1") && move.to != Square.parse("a1"),
            blackKingSide = position.castling.blackKingSide && moving != Piece(PieceType.KING, Side.BLACK) && move.from != Square.parse("h8") && move.to != Square.parse("h8"),
            blackQueenSide = position.castling.blackQueenSide && moving != Piece(PieceType.KING, Side.BLACK) && move.from != Square.parse("a8") && move.to != Square.parse("a8"),
        )
        val ep = if (moving.type == PieceType.PAWN && kotlin.math.abs(move.to.rank - move.from.rank) == 2)
            Square.of(move.from.file, (move.from.rank + move.to.rank) / 2) else null
        return position.copy(
            board = board,
            sideToMove = if (switchTurn) position.sideToMove.opposite() else position.sideToMove,
            castling = rights,
            enPassant = ep,
            halfmoveClock = if (moving.type == PieceType.PAWN || target != null) 0 else position.halfmoveClock + 1,
            fullmoveNumber = if (switchTurn && position.sideToMove == Side.BLACK) position.fullmoveNumber + 1 else position.fullmoveNumber,
        )
    }

    fun isInCheck(position: Position, side: Side): Boolean {
        val king = position.board.indexOf(Piece(PieceType.KING, side))
        if (king < 0) return true
        return isAttacked(position, Square.fromIndex(king), side.opposite())
    }

    fun notation(position: Position, move: Move): String {
        val piece = position[move.from] ?: return move.uci
        if (piece.type == PieceType.KING && kotlin.math.abs(move.to.file - move.from.file) == 2) {
            return if (move.to.file == 6) "O-O" else "O-O-O"
        }
        val capture = position[move.to] != null || (piece.type == PieceType.PAWN && move.to == position.enPassant)
        val prefix = if (piece.type == PieceType.PAWN) {
            if (capture) ('a'.code + move.from.file).toChar().toString() else ""
        } else piece.type.fen.uppercaseChar().toString()
        val promoted = move.promotion?.let { "=${it.fen.uppercaseChar()}" } ?: ""
        val next = apply(position, move)
        val check = if (isInCheck(next, next.sideToMove)) {
            if (legalMoves(next).isEmpty()) "#" else "+"
        } else ""
        return prefix + (if (capture) "x" else "") + move.to.algebraic + promoted + check
    }

    private fun pseudoLegalMoves(position: Position): List<Move> = buildList {
        position.board.forEachIndexed { index, piece ->
            if (piece == null || piece.side != position.sideToMove) return@forEachIndexed
            val from = Square.fromIndex(index)
            when (piece.type) {
                PieceType.PAWN -> addPawnMoves(position, from, piece.side)
                PieceType.KNIGHT -> addLeaps(position, from, piece.side, KNIGHT_OFFSETS)
                PieceType.BISHOP -> addSlides(position, from, piece.side, BISHOP_DIRECTIONS)
                PieceType.ROOK -> addSlides(position, from, piece.side, ROOK_DIRECTIONS)
                PieceType.QUEEN -> addSlides(position, from, piece.side, BISHOP_DIRECTIONS + ROOK_DIRECTIONS)
                PieceType.KING -> {
                    addLeaps(position, from, piece.side, KING_OFFSETS)
                    addCastles(position, from, piece.side)
                }
            }
        }
    }

    private fun MutableList<Move>.addPawnMoves(position: Position, from: Square, side: Side) {
        val direction = if (side == Side.WHITE) 1 else -1
        val startRank = if (side == Side.WHITE) 1 else 6
        val promotionRank = if (side == Side.WHITE) 7 else 0
        val one = Square.of(from.file, from.rank + direction)
        if (one != null && position[one] == null) {
            addPromotions(from, one, promotionRank)
            val two = Square.of(from.file, from.rank + direction * 2)
            if (from.rank == startRank && two != null && position[two] == null) add(Move(from, two))
        }
        for (df in listOf(-1, 1)) {
            val to = Square.of(from.file + df, from.rank + direction) ?: continue
            val target = position[to]
            if ((target?.side == side.opposite() && target.type != PieceType.KING) || to == position.enPassant) addPromotions(from, to, promotionRank)
        }
    }

    private fun MutableList<Move>.addPromotions(from: Square, to: Square, promotionRank: Int) {
        if (to.rank == promotionRank) listOf(PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT).forEach { add(Move(from, to, it)) }
        else add(Move(from, to))
    }

    private fun MutableList<Move>.addLeaps(position: Position, from: Square, side: Side, offsets: List<Pair<Int, Int>>) {
        offsets.forEach { (df, dr) ->
            val to = Square.of(from.file + df, from.rank + dr) ?: return@forEach
            val target = position[to]
            if (target == null || (target.side != side && target.type != PieceType.KING)) add(Move(from, to))
        }
    }

    private fun MutableList<Move>.addSlides(position: Position, from: Square, side: Side, directions: List<Pair<Int, Int>>) {
        directions.forEach { (df, dr) ->
            var distance = 1
            while (true) {
                val to = Square.of(from.file + df * distance, from.rank + dr * distance) ?: break
                val target = position[to]
                if (target == null) add(Move(from, to)) else {
                    if (target.side != side && target.type != PieceType.KING) add(Move(from, to))
                    break
                }
                distance++
            }
        }
    }

    private fun MutableList<Move>.addCastles(position: Position, from: Square, side: Side) {
        val rank = if (side == Side.WHITE) 0 else 7
        if (from != Square.of(4, rank) || isInCheck(position, side)) return
        val rights = if (side == Side.WHITE) position.castling.whiteKingSide to position.castling.whiteQueenSide
            else position.castling.blackKingSide to position.castling.blackQueenSide
        if (rights.first && listOf(5, 6).all { position[Square.of(it, rank)!!] == null } &&
            position[Square.of(7, rank)!!] == Piece(PieceType.ROOK, side) &&
            listOf(5, 6).none { isAttacked(position, Square.of(it, rank)!!, side.opposite()) }) add(Move(from, Square.of(6, rank)!!))
        if (rights.second && listOf(1, 2, 3).all { position[Square.of(it, rank)!!] == null } &&
            position[Square.of(0, rank)!!] == Piece(PieceType.ROOK, side) &&
            listOf(3, 2).none { isAttacked(position, Square.of(it, rank)!!, side.opposite()) }) add(Move(from, Square.of(2, rank)!!))
    }

    private fun isAttacked(position: Position, target: Square, by: Side): Boolean {
        val pawnSourceRank = target.rank - if (by == Side.WHITE) 1 else -1
        if (listOf(-1, 1).any { df -> Square.of(target.file + df, pawnSourceRank)?.let { position[it] == Piece(PieceType.PAWN, by) } == true }) return true
        if (KNIGHT_OFFSETS.any { (df, dr) -> Square.of(target.file + df, target.rank + dr)?.let { position[it] == Piece(PieceType.KNIGHT, by) } == true }) return true
        if (KING_OFFSETS.any { (df, dr) -> Square.of(target.file + df, target.rank + dr)?.let { position[it] == Piece(PieceType.KING, by) } == true }) return true
        return rayAttacked(position, target, by, BISHOP_DIRECTIONS, setOf(PieceType.BISHOP, PieceType.QUEEN)) ||
            rayAttacked(position, target, by, ROOK_DIRECTIONS, setOf(PieceType.ROOK, PieceType.QUEEN))
    }

    private fun rayAttacked(position: Position, target: Square, by: Side, directions: List<Pair<Int, Int>>, types: Set<PieceType>): Boolean =
        directions.any { (df, dr) ->
            var distance = 1
            while (true) {
                val square = Square.of(target.file + df * distance, target.rank + dr * distance) ?: break
                val piece = position[square]
                if (piece != null) return@any piece.side == by && piece.type in types
                distance++
            }
            false
        }

    private val KNIGHT_OFFSETS = listOf(-2 to -1, -2 to 1, -1 to -2, -1 to 2, 1 to -2, 1 to 2, 2 to -1, 2 to 1)
    private val KING_OFFSETS = listOf(-1 to -1, -1 to 0, -1 to 1, 0 to -1, 0 to 1, 1 to -1, 1 to 0, 1 to 1)
    private val BISHOP_DIRECTIONS = listOf(-1 to -1, -1 to 1, 1 to -1, 1 to 1)
    private val ROOK_DIRECTIONS = listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1)
}
