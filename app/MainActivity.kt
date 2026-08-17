package com.example.botvinnikchess

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.method.ScrollingMovementMethod
import androidx.appcompat.app.AppCompatActivity
import com.example.botvinnikchess.databinding.ActivityMainBinding
import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.Side
import com.github.bhlangonijr.chesslib.Square
import com.github.bhlangonijr.chesslib.move.Move
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val board = Board()
    private val engine = BotvinnikEngine()
    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    private var playerSide = Side.WHITE
    private var botSide = Side.BLACK

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvMoveHistory.movementMethod = ScrollingMovementMethod()
        binding.chessBoardView.board = board

        binding.chessBoardView.onMoveListener = { from, to ->
            handleUserMove(from, to)
        }

        binding.btnWhite.setOnClickListener {
            playerSide = Side.WHITE
            botSide = Side.BLACK
            binding.chessBoardView.isFlipped = false
            restartGame()
        }

        binding.btnBlack.setOnClickListener {
            playerSide = Side.BLACK
            botSide = Side.WHITE
            binding.chessBoardView.isFlipped = true
            restartGame()
        }

        binding.btnRestart.setOnClickListener {
            restartGame()
        }

        restartGame()
    }

    private fun handleUserMove(from: Square, to: Square) {
        if (board.sideToMove != playerSide || board.isMated || board.isDraw) return

        val move = Move(from, to)
        if (board.legalMoves().contains(move)) {
            board.doMove(move)
            binding.chessBoardView.invalidate()
            updateStatus()

            if (!board.isMated && !board.isDraw) {
                triggerBotMove()
            }
        }
    }

    private fun triggerBotMove() {
        binding.tvStatus.text = "Botvinnik is thinking..."
        executor.execute {
            val bestMove = engine.getBestMove(board)
            mainHandler.post {
                if (bestMove != null) {
                    board.doMove(bestMove)
                    binding.chessBoardView.invalidate()
                    updateStatus()
                }
            }
        }
    }

    private fun restartGame() {
        board.loadFromFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1")
        binding.chessBoardView.invalidate()
        updateStatus()

        if (botSide == Side.WHITE) {
            triggerBotMove()
        }
    }

    private fun updateStatus() {
        val sideText = if (board.sideToMove == Side.WHITE) "White" else "Black"
        val statusText = when {
            board.isMated -> "Game Over: ${if (board.sideToMove == Side.WHITE) "Black" else "White"} wins by Checkmate!"
            board.isDraw -> "Game Over: Draw!"
            board.isKingAttacked -> "$sideText is in Check!"
            else -> "$sideText's turn."
        }
        binding.tvStatus.text = statusText
        binding.tvMoveHistory.text = board.history.joinToString(" ") { it.toString() }
    }
}
