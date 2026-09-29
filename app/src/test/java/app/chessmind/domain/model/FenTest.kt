package app.chessmind.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FenTest {
    @Test fun `starting position round trips`() {
        val fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
        assertEquals(fen, Fen.encode(Fen.parse(fen).getOrThrow()))
    }

    @Test fun `rejects position without kings`() {
        val result = Fen.parse("8/8/8/8/8/8/8/8 w - - 0 1")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message.orEmpty().contains("White"))
    }

    @Test fun `parses side and en passant`() {
        val position = Fen.parse("4k3/8/8/3pP3/8/8/8/4K3 b - e6 0 17").getOrThrow()
        assertEquals(Side.BLACK, position.sideToMove)
        assertEquals("e6", position.enPassant?.algebraic)
        assertEquals(17, position.fullmoveNumber)
    }

    @Test fun `starting position has twenty legal moves`() {
        assertEquals(20, ChessRules.legalMoves(Position.START).size)
    }

    @Test fun `cannot move a pinned rook away from king`() {
        val position = Fen.parse("4r1k1/8/8/8/8/8/4R3/4K3 w - - 0 1").getOrThrow()
        val moves = ChessRules.legalMoves(position)
        assertTrue(moves.none { it.from.algebraic == "e2" && it.to.file != 4 })
    }
}
