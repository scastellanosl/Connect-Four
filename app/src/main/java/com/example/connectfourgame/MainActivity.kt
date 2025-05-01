package com.example.connectfourgame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.connectfourgame.ui.theme.ConnectFourGameTheme
import kotlinx.coroutines.delay

/**
 * Main activity hosting the Connect Four game using Jetpack Compose.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ConnectFourGameTheme(darkTheme = true) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ConnectFourGame()
                }
            }
        }
    }
}

/**
 * Represents the game mode selected by the user.
 */
enum class GameMode {
    TWO_PLAYERS, VS_AI
}

/**
 * Root composable that contains game state and UI.
 */
@Composable
fun ConnectFourGame() {
    val rows = 6
    val cols = 7

    var board by remember { mutableStateOf(Array(rows) { IntArray(cols) { 0 } }) }
    var playerTurn by remember { mutableStateOf(true) }
    var winner by remember { mutableStateOf(0) }
    var gameMode by remember { mutableStateOf<GameMode?>(null) }

    // Game mode selection screen
    if (gameMode == null) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Button(onClick = { gameMode = GameMode.TWO_PLAYERS }) {
                Text("Play 2 Players")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { gameMode = GameMode.VS_AI }) {
                Text("Play vs AI")
            }
        }
        return
    }

    // Main game layout
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        // Game status message
        Text(
            text = when {
                winner == 1 -> "You win!"
                winner == 2 -> if (gameMode == GameMode.VS_AI) "AI wins!" else "Player 2 wins!"
                winner == 3 -> "Draw!"
                else -> if (gameMode == GameMode.VS_AI)
                    if (playerTurn) "Your turn" else "AI's turn..."
                else if (playerTurn) "Player 1's turn" else "Player 2's turn"
            },
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )

        // Game board UI
        Board(board) { col ->
            if (winner == 0 && (gameMode == GameMode.TWO_PLAYERS || playerTurn)) {
                val row = findAvailableRow(board, col)
                if (row != -1) {
                    val currentPlayer = if (playerTurn) 1 else 2
                    board = board.copyWithMove(row, col, currentPlayer)
                    when {
                        checkWinner(board, currentPlayer) -> winner = currentPlayer
                        isBoardFull(board) -> winner = 3
                        else -> playerTurn = !playerTurn
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Restart game
        Button(onClick = {
            board = Array(rows) { IntArray(cols) { 0 } }
            winner = 0
            playerTurn = true
        }) {
            Icon(Icons.Filled.Refresh, contentDescription = "Reset")
        }

        // Return to main menu
        Button(onClick = { gameMode = null }) {
            Icon(Icons.Filled.ArrowForward, contentDescription = "Menu")
        }

        // Handle AI turn with delay
        if (gameMode == GameMode.VS_AI && !playerTurn && winner == 0) {
            LaunchedEffect(board) {
                delay(500)
                val validCols = (0 until cols).filter { findAvailableRow(board, it) != -1 }
                val col = validCols.random()
                val row = findAvailableRow(board, col)
                if (row != -1) {
                    board = board.copyWithMove(row, col, 2)
                    when {
                        checkWinner(board, 2) -> winner = 2
                        isBoardFull(board) -> winner = 3
                        else -> playerTurn = true
                    }
                }
            }
        }
    }
}

/**
 * Renders the full board grid with clickable columns.
 */
@Composable
fun Board(board: Array<IntArray>, onColumnClick: (Int) -> Unit) {
    Column {
        for (row in board) {
            Row {
                for ((colIndex, cell) in row.withIndex()) {
                    Cell(cell) { onColumnClick(colIndex) }
                }
            }
        }
    }
}

/**
 * A single cell of the game board.
 */
@Composable
fun Cell(value: Int, onClick: () -> Unit) {
    val color = when (value) {
        1 -> Color.Red
        2 -> Color.Blue
        else -> Color.LightGray
    }

    Box(
        modifier = Modifier
            .size(48.dp)
            .padding(4.dp)
            .background(color, CircleShape)
            .clickable { onClick() }
    )
}

/**
 * Finds the next available row in a column from bottom to top.
 */
fun findAvailableRow(board: Array<IntArray>, col: Int): Int {
    for (r in board.indices.reversed()) {
        if (board[r][col] == 0) return r
    }
    return -1
}

/**
 * Checks for four connected pieces horizontally, vertically, or diagonally.
 */
fun checkWinner(board: Array<IntArray>, player: Int): Boolean {
    val rows = board.size
    val cols = board[0].size

    for (r in 0 until rows) {
        for (c in 0 until cols) {
            if (c <= cols - 4 && (0..3).all { board[r][c + it] == player }) return true
            if (r <= rows - 4 && (0..3).all { board[r + it][c] == player }) return true
            if (r <= rows - 4 && c <= cols - 4 && (0..3).all { board[r + it][c + it] == player }) return true
            if (r >= 3 && c <= cols - 4 && (0..3).all { board[r - it][c + it] == player }) return true
        }
    }
    return false
}

/**
 * Returns true if the board has no empty cells.
 */
fun isBoardFull(board: Array<IntArray>): Boolean {
    return board.all { row -> row.all { it != 0 } }
}

/**
 * Creates a copy of the board with a move applied at [row], [col] by [player].
 */
fun Array<IntArray>.copyWithMove(row: Int, col: Int, player: Int): Array<IntArray> {
    return Array(size) { r ->
        IntArray(this[0].size) { c ->
            if (r == row && c == col) player else this[r][c]
        }
    }
}
