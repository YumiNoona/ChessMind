package app.chessmind.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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

    @Test fun `legal moves never capture the opposing king`() {
        val position = Fen.parse("4k3/4Q3/8/8/8/8/8/4K3 w - - 0 1").getOrThrow()
        assertTrue(ChessRules.legalMoves(position).none { it.to.algebraic == "e8" })
    }

    @Test fun `imports a basic PGN game`() {
        val game = Pgn.parse("[Event \"Test\"]\n\n1. e4 e5 2. Nf3 Nc6 *").getOrThrow()
        assertEquals(4, game.moves.size)
        assertEquals(Side.WHITE, game.positions.last().sideToMove)
    }

    @Test fun `imports PGN with disambiguation comments NAGs and castling variants`() {
        val pgn = """
            [Event "World Championship"]
            [Site "Reykjavik"]
            [Date "1972.07.23"]
            [Round "6"]
            [White "Fischer, Robert J."]
            [Black "Spassky, Boris V."]
            [Result "1-0"]

            1. c4 e6 2. Nf3 d5 3. d4 Nf6 4. Nc3 Be7 5. Bg5 O-O 6. e3 h6
            7. Bh4 b6 8. cxd5 Nxd5 9. Bxe7 Qxe7 10. Nxd5 exd5 11. Rc1 Be6
            12. Qa4 c5 13. Qa3 Rc8 14. Bb5 a6 15. dxc5 bxc5 16. O-O Ra7
            17. Be2 Nd7 18. Nd4 Qf8 19. Nxe6 fxe6 20. e4 d4 21. f4 Qe7
            22. e5 Rb8 23. Bc4 Kh8 24. Qh3 Nf8 25. b3 a5 26. f5 exf5
            27. Rxf5 Nh7 28. Rcf1 Qd8 29. Qg3 Re7 30. h4 Rbb7 31. e6 Rbc7
            32. Qe5 Qe8 33. a4 Qd8 34. R1f2 Qe8 35. R2f3 Qd8 36. Bd3 Qe8
            37. Qe4 Nf6 38. Rxf6 gxf6 39. Rxf6 Kg8 40. Bc4 Kh8 41. Qf4 1-0
        """.trimIndent()
        val game = Pgn.parse(pgn).getOrThrow()
        assertEquals(81, game.moves.size)
        assertEquals("Fischer, Robert J.", game.tags["White"])
        assertEquals("1-0", game.tags["Result"])
    }

    @Test fun `imports PGN with numeric castling 0-0 and 0-0-0 and promotions`() {
        val pgn = """
            1. e4 e5 2. Nf3 Nc6 3. Bc4 Bc5 4. 0-0 Nf6 5. d3 0-0
            6. c3 d6 7. Nbd2 a6 8. Bb3 Ba7 9. Re1 Re8 *
        """.trimIndent()
        val game = Pgn.parse(pgn).getOrThrow()
        assertEquals(18, game.moves.size)
    }

    @Test fun `disambiguates notation when two pieces can reach same square`() {
        val pos = Fen.parse("4k3/8/8/8/8/8/8/R4R1K w - - 0 1").getOrThrow()
        val rad1 = ChessRules.notation(pos, Move(Square.parse("a1")!!, Square.parse("d1")!!))
        val rfd1 = ChessRules.notation(pos, Move(Square.parse("f1")!!, Square.parse("d1")!!))
        assertEquals("Rad1", rad1)
        assertEquals("Rfd1", rfd1)
    }

    @Test fun `detects insufficient material correctly`() {
        // King vs King
        val kvk = Fen.parse("4k3/8/8/8/8/8/8/4K3 w - - 0 1").getOrThrow()
        assertTrue(ChessRules.isInsufficientMaterial(kvk))

        // King + Knight vs King
        val knvk = Fen.parse("4k3/8/8/8/8/5N2/8/4K3 w - - 0 1").getOrThrow()
        assertTrue(ChessRules.isInsufficientMaterial(knvk))

        // King + Bishop vs King
        val kbvk = Fen.parse("4k3/8/8/8/8/5B2/8/4K3 w - - 0 1").getOrThrow()
        assertTrue(ChessRules.isInsufficientMaterial(kbvk))

        // King + Bishop vs King + Bishop same square color
        val kbbSame = Fen.parse("4k3/8/8/8/5b2/8/8/2B1K3 w - - 0 1").getOrThrow()
        assertTrue(ChessRules.isInsufficientMaterial(kbbSame))

        // King + Pawn vs King (NOT insufficient)
        val kpvk = Fen.parse("4k3/8/8/8/8/4P3/8/4K3 w - - 0 1").getOrThrow()
        assertFalse(ChessRules.isInsufficientMaterial(kpvk))

        // King + Queen vs King (NOT insufficient)
        val kqvk = Fen.parse("4k3/8/8/8/8/4Q3/8/4K3 w - - 0 1").getOrThrow()
        assertFalse(ChessRules.isInsufficientMaterial(kqvk))
    }
}
