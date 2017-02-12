package edu.game.checkers.logic;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.view.MotionEvent;
import android.view.View;

import java.util.List;

public class GameView extends View {

    private List<Position> hints;
    private Piece[][] pieces;
    private boolean rotation = false;
    private Paint paint;

    public GameView(Context context) {
        super(context);
        paint = new Paint();
    }

    public void rotate(){
        rotation = !rotation;
    }

    public boolean getBoardRotation(){
        return rotation;
    }

    public void setPieces(Piece[][] pieces) {
        this.pieces = pieces;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(rotation)
            canvas.rotate(180, canvas.getWidth() / 2, canvas.getHeight() / 2);

        drawBoard(canvas);

        for (Piece[] pieceRow : pieces)
            for (Piece piece : pieceRow)
                if (piece != null)
                    piece.draw(canvas);

        drawHints(canvas);
    }

    public void setHints(List<Position> hints)
    {
        this.hints = hints;
    }

    // Method to draw an empty chess board
    public void drawBoard(Canvas canvas)
    {
        int tileSize = canvas.getWidth() / 8;

        for(int x = 0; x < 8; x++){
            for(int y = 0; y < 8; y++){

                if((x + y) % 2 != 0)
                    paint.setColor(Color.parseColor("#0064A2"));
                else
                    paint.setColor(Color.parseColor("#D8EBFF"));

                canvas.drawRect(x * tileSize, y * tileSize,
                        (x+1) * tileSize, (y+1) * tileSize, paint);

            }
        }
    }

    // Method to show available moves for a piece
    public void drawHints(Canvas canvas)
    {
        if(hints == null)
            return;

        int tileSize = canvas.getWidth() / 8;

        paint.setColor(Color.GREEN);

        for(Position hint : hints){
            canvas.drawRect(hint.x * tileSize, hint.y * tileSize,
                    (hint.x+1) * tileSize, (hint.y+1) * tileSize, paint);
        }
    }

    @Override
    public void onMeasure(int widthSpec, int heightSpec)
    {
        super.onMeasure(widthSpec, heightSpec);
        setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth()); // square
    }

}