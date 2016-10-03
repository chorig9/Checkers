package edu.game.checkers.logic;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.ArrayList;

public class King extends Piece{

    public King(Position position, Player owner)
    {
        super(position, owner);
    }

    @Override
    public void draw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(Color.BLUE);

        int tileSize = canvas.getWidth() / 8;

        canvas.drawRect(position.x * tileSize,position.y * tileSize,
                (position.x + 1) * tileSize,(position.y + 1) * tileSize,paint);
    }

    @Override
    public boolean isMoveValid(Position target, int options, Piece[][] pieces) {
        return false;
    }

    @Override
    public ArrayList<Position> getValidPositions(int options, Piece[][] pieces) {
        return null;
    }

    @Override
    public boolean canJump(int options, Piece[][] pieces) {
        return false;
    }
}
