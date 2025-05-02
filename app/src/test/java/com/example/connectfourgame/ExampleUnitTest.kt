package com.example.connectfourgame

import org.junit.Assert.*
import org.junit.Test

class GameLogicTest {

    @Test
    fun testHorizontalWin() {
        val board = Array(6) { IntArray(7) { 0 } }
        for (i in 0..3) board[0][i] = 1
        assertTrue(checkWinner(board, 1))
    }

    @Test
    fun testVerticalWin() {
        val board = Array(6) { IntArray(7) { 0 } }
        for (i in 0..3) board[i][0] = 2
        assertTrue(checkWinner(board, 2))
    }

    @Test
    fun testNoWin() {
        val board = Array(6) { IntArray(7) { 0 } }
        board[0][0] = 1
        board[0][1] = 1
        board[0][2] = 1
        board[0][3] = 2
        assertFalse(checkWinner(board, 1))
    }

    @Test
    fun testBoardFull() {
        val board = Array(6) { IntArray(7) { 1 } }
        assertTrue(isBoardFull(board))
    }

    @Test
    fun testFindAvailableRow() {
        val board = Array(6) { IntArray(7) { 0 } }
        board[5][0] = 1
        assertEquals(4, findAvailableRow(board, 0))
    }

    @Test
    fun testCopyWithMove() {
        val board = Array(6) { IntArray(7) { 0 } }
        val newBoard = board.copyWithMove(5, 3, 2)
        assertEquals(2, newBoard[5][3])
        assertEquals(0, board[5][3]) //
    }
}