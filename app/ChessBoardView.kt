package com.example.botvinnikchess

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.Piece
import com.github.bhlangonijr.chesslib.Square
import com.github.bhlangonijr.chesslib.Side
import kotlin.math.min

class ChessBoardView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var squareSize: Float = 0f
    private val lightPaint = Paint().apply { color = ContextCompat.getColor(context, R.color.square_light) }
    private val darkPaint = Paint().apply { color = ContextCompat.getColor(context, R.color.square_dark) }
    private val selectedPaint = Paint().apply { color = ContextCompat.getColor(context, R.color.square_selected) }
    private val textPaint = Paint().apply {
        color = Color.BLACK
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }

    var board: Board = Board()
    var isFlipped: Boolean = false // Set to true when player plays as Black
    private var selectedSquare: Square? = null

    var onMoveListener: ((Square, Square) -> Unit)? = null

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        val size = min(width, height)
        setMeasuredDimension(size, size)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        squareSize = w / 8f
        textPaint.textSize = squareSize * 0.6f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawBoard(canvas)
        drawPieces(canvas)
    }

    private fun drawBoard(canvas: Canvas) {
        for (row in 0..7) {
            for (col in 0..7) {
                val isLight = (row + col) % 2 == 0
                val paint = if (isLight) lightPaint else darkPaint

                val left = col * squareSize
                val top = row * squareSize
                canvas.drawRect(left, top, left + squareSize, top + squareSize, paint)

                // Highlight selected square
                val currentSquare = getSquareFromRowCol(row, col)
                if (selectedSquare == currentSquare) {
                    canvas.drawRect(left, top, left + squareSize, top + squareSize, selectedPaint)
                }
            }
        }
    }

    private fun drawPieces(canvas: Canvas) {
        for (row in 0..7) {
            for (col in 0..7) {
                val sq = getSquareFromRowCol(row, col)
                val piece = board.getPiece(sq)
                if (piece != Piece.NONE) {
                    val symbol = getPieceSymbol(piece)
                    val x = col * squareSize + squareSize / 2f
                    val y = row * squareSize + squareSize / 2f - (textPaint.descent() + textPaint.ascent()) / 2f
                    canvas.drawText(symbol, x, y, textPaint)
                }
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val col = (event.x / squareSize).toInt().coerceIn(0, 7)
            val row = (event.y / squareSize).toInt().coerceIn(0, 7)
            val clickedSquare = getSquareFromRowCol(row, col)

            if (selectedSquare == null) {
                // Select piece if it belongs to current turn
                val piece = board.getPiece(clickedSquare)
                if (piece != Piece.NONE && piece.pieceSide == board.sideToMove) {
                    selectedSquare = clickedSquare
                    invalidate()
                }
            } else {
                // Attempt move
                val from = selectedSquare!!
                val to = clickedSquare
                selectedSquare = null
                onMoveListener?.invoke(from, to)
                invalidate()
            }
        }
        return true
    }

    private fun getSquareFromRowCol(row: Int, col: Int): Square {
        val actualRow = if (isFlipped) row else 7 - row
        val actualCol = if (isFlipped) 7 - col else col
        return Square.squareAt(actualRow * 8 + actualCol)
    }

    private fun getPieceSymbol(piece: Piece): String {
        return when (piece) {
            Piece.WHITE_PAWN -> "♙"
            Piece.WHITE_KNIGHT -> "♘"
            Piece.WHITE_BISHOP -> "♗"
            Piece.WHITE_ROOK -> "♖"
            Piece.WHITE_QUEEN -> "♕"
            Piece.WHITE_KING -> "♔"
            Piece.BLACK_PAWN -> "♟"
            Piece.BLACK_KNIGHT -> "♞"
            Piece.BLACK_BISHOP -> "♝"
            Piece.BLACK_ROOK -> "♜"
            Piece.BLACK_QUEEN -> "♛"
            Piece.BLACK_KING -> "♚"
            else -> ""
        }
    }
}
