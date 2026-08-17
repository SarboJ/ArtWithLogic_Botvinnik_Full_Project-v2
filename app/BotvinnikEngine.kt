package com.example.botvinnikchess

import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.Piece
import com.github.bhlangonijr.chesslib.Side
import com.github.bhlangonijr.chesslib.move.Move
import kotlin.math.max
import kotlin.math.min

class BotvinnikEngine {

    private val pawnEvalWhite = arrayOf(
        intArrayOf(0,  0,  0,  0,  0,  0,  0,  0),
        intArrayOf(50, 50, 50, 50, 50, 50, 50, 50),
        intArrayOf(10, 10, 20, 30, 30, 20, 10, 10),
        intArrayOf(5,  5,  10, 27, 27, 10,  5,  5),
        intArrayOf(0,  0,   0, 22, 22, -5,  0,  0),
        intArrayOf(5, -5, -10,  0,  0,-10, -5,  5),
        intArrayOf(5, 10,  10,-20,-20, 10, 10,  5),
        intArrayOf(0,  0,   0,  0,  0,  0,  0,  0)
    )

    private val knightEval = arrayOf(
        intArrayOf(-50,-40,-30,-30,-30,-30,-40,-50),
        intArrayOf(-40,-20,  0,  0,  0,  0,-20,-40),
        intArrayOf(-30,  0, 10, 15, 15, 10,  0,-30),
        intArrayOf(-30,  5, 15, 20, 20, 15,  5,-30),
        intArrayOf(-30,  0, 15, 20, 20, 15,  0,-30),
        intArrayOf(-30,  5, 10, 15, 15, 10,  5,-30),
        intArrayOf(-40,-20,  0,  5,  5,  0,-20,-40),
        intArrayOf(-50,-40,-30,-30,-30,-30,-40,-50)
    )

    fun getBestMove(board: Board, depth: Int = 3): Move? {
        val moves = board.legalMoves()
        if (moves.isEmpty()) return null

        var bestMove: Move? = null
        val isMaximizing = board.sideToMove == Side.WHITE
        var bestValue = if (isMaximizing) Int.MIN_VALUE else Int.MAX_VALUE

        for (move in moves) {
            board.doMove(move)
            val boardValue = minimax(board, depth - 1, Int.MIN_VALUE, Int.MAX_VALUE, !isMaximizing)
            board.undoMove()

            if (isMaximizing) {
                if (boardValue > bestValue) {
                    bestValue = boardValue
                    bestMove = move
                }
            } else {
                if (boardValue < bestValue) {
                    bestValue = boardValue
                    bestMove = move
                }
            }
        }
        return bestMove
    }

    private fun minimax(board: Board, depth: Int, alpha: Int, beta: Int, isMaximizing: Boolean): Int {
        if (depth == 0 || board.isMated || board.isDraw) {
            return evaluateBoard(board)
        }

        var a = alpha
        var b = beta
        val moves = board.legalMoves()

        if (isMaximizing) {
            var maxEval = Int.MIN_VALUE
            for (move in moves) {
                board.doMove(move)
                val evaluation = minimax(board, depth - 1, a, b, false)
                board.undoMove()
                maxEval = max(maxEval, evaluation)
                a = max(a, evaluation)
                if (b <= a) break
            }
            return maxEval
        } else {
            var minEval = Int.MAX_VALUE
            for (move in moves) {
                board.doMove(move)
                val evaluation = minimax(board, depth - 1, a, b, true)
                board.undoMove()
                minEval = min(minEval, evaluation)
                b = min(b, evaluation)
                if (b <= a) break
            }
            return minEval
        }
    }

    private fun evaluateBoard(board: Board): Int {
        var total = 0
        for (sq in com.github.bhlangonijr.chesslib.Square.values()) {
            if (sq == com.github.bhlangonijr.chesslib.Square.NONE) continue
            val piece = board.getPiece(sq)
            if (piece != Piece.NONE) {
                val value = getPieceValue(piece, sq.ordinal)
                if (piece.pieceSide == Side.WHITE) total += value else total -= value
            }
        }
        return total
    }

    private fun getPieceValue(piece: Piece, squareIdx: Int): Int {
        val row = squareIdx / 8
        val col = squareIdx % 8
        val baseValue = when (piece.pieceType) {
            com.github.bhlangonijr.chesslib.PieceType.PAWN -> 100
            com.github.bhlangonijr.chesslib.PieceType.KNIGHT -> 320
            com.github.bhlangonijr.chesslib.PieceType.BISHOP -> 330
            com.github.bhlangonijr.chesslib.PieceType.ROOK -> 500
            com.github.bhlangonijr.chesslib.PieceType.QUEEN -> 900
            com.github.bhlangonijr.chesslib.PieceType.KING -> 20000
            else -> 0
        }

        val positional = when (piece.pieceType) {
            com.github.bhlangonijr.chesslib.PieceType.PAWN -> pawnEvalWhite[7 - row][col]
            com.github.bhlangonijr.chesslib.PieceType.KNIGHT -> knightEval[7 - row][col]
            else -> 0
        }
        return baseValue + positional
    }
}
