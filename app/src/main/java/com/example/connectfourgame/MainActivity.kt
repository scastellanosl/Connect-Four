package com.example.connectfourgame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.connectfourgame.ui.theme.ConnectFourGameTheme
import kotlinx.coroutines.delay
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ConnectFourGameTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ConnectFourGame()
                }
            }
        }
    }
}

@Composable
fun ConnectFourGame() {
    val rows = 6
    val cols = 7

    var board by remember { mutableStateOf(Array(rows) { IntArray(cols) { 0 } }) }
    var playerTurn by remember { mutableStateOf(true) }
    var winner by remember { mutableStateOf(0) }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = when {
                winner == 1 -> "¡Ganaste!"
                winner == 2 -> "¡La máquina ganó!"
                winner == 3 -> "¡Empate!"
                else -> if (playerTurn) "Tu turno" else "Turno de la máquina..."
            },
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )

        Board(board, onColumnClick = { col ->
            if (playerTurn && winner == 0) {
                val row = findAvailableRow(board, col)
                if (row != -1) {
                    board = board.copyWithMove(row, col, 1)
                    if (checkWinner(board, 1)) {
                        winner = 1
                    } else if (isBoardFull(board)) {
                        winner = 3
                    } else {
                        playerTurn = false
                    }
                }
            }
        })

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            board = Array(rows) { IntArray(cols) { 0 } }
            winner = 0
            playerTurn = true
        }) {
            Text("Reiniciar")
        }

        if (!playerTurn && winner == 0) {
            LaunchedEffect(Unit) {
                delay(500)
                val validCols = (0 until cols).filter { findAvailableRow(board, it) != -1 }
                val col = validCols.random()
                val row = findAvailableRow(board, col)
                if (row != -1) {
                    board = board.copyWithMove(row, col, 2)
                    if (checkWinner(board, 2)) {
                        winner = 2
                    } else if (isBoardFull(board)) {
                        winner = 3
                    } else {
                        playerTurn = true
                    }
                }
            }
        }
    }
}

@Composable
fun Board(board: Array<IntArray>, onColumnClick: (Int) -> Unit) {
    Column {
        for (row in board.indices) {
            Row {
                for (col in board[row].indices) {
                    Cell(board[row][col], onClick = { onColumnClick(col) })
                }
            }
        }
    }
}

@Composable
fun Cell(value: Int, onClick: () -> Unit) {
    val color = when (value) {
        1 -> Color.Red
        2 -> Color.Yellow
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

// Helper functions
fun findAvailableRow(board: Array<IntArray>, col: Int): Int {
    for (r in board.size - 1 downTo 0) {
        if (board[r][col] == 0) return r
    }
    return -1
}

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

fun isBoardFull(board: Array<IntArray>): Boolean {
    return board.all { row -> row.all { it != 0 } }
}

fun Array<IntArray>.copyWithMove(row: Int, col: Int, player: Int): Array<IntArray> {
    return Array(this.size) { r -> IntArray(this[0].size) { c -> if (r == row && c == col) player else this[r][c] } }
}
