package edu.game.checkers.logic;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.ArrayList;

public class Men extends  Piece{

    private int orientation;

    public Men(Position position, Player owner)
    {
        super(position, owner);

        if(position.y > 4)
            orientation = -1;
        else
            orientation = 1;
    }

    @Override
    public void draw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(Color.RED);

        int tileSize = canvas.getWidth() / 8;

        canvas.drawRect(position.x * tileSize,position.y * tileSize,
                (position.x + 1) * tileSize,(position.y + 1) * tileSize,paint);
    }

    @Override
    public boolean isMoveValid(Position target, int options, Piece[][] pieces) {
        // simple move by one
        boolean move = Math.abs(target.x - position.x) == 1 && target.y - position.y == orientation
                && pieces[target.x][target.y] == null;

        // is backward capture allowed
        boolean backwardJumpAllowed = ((1 << Game.backwardCapture) & options) != 0;

        // is this a valid jump
        boolean jump = Math.abs(target.x - position.x) == 2
                && pieces[(target.x + position.x)/2][(target.y + position.y)/2] == null;

        boolean forwardJump = target.y - position.y == 2 * orientation;
        boolean backwardJump = Math.abs(target.y - position.y) == 2;

        return move || (jump && forwardJump) || (jump && backwardJumpAllowed && backwardJump);
    }

    @Override
    public ArrayList<Position> getValidPositions(int options, Piece[][] pieces) {
        ArrayList<Position> positions = new ArrayList<>();

        for(int x = position.x - 3; x <= position.x + 3; x+=2)
        {
            for(int y = position.y - 3; y <= position.y + 3; y+=2)
            {
                if(x >= 0 && x < 8 && y >= 0 && y < 8
                        && isMoveValid(new Position(x, y), options, pieces))
                    positions.add(new Position(x, y));
            }
        }

        return positions;
    }

    @Override
    public boolean canJump(int options, Piece[][] pieces) {
        int forY = position.y + orientation;
        int backY = position.y - orientation;

        for(int x = position.x - 1; x <= position.x + 1; x+=2)
        {
            if(x >= 0 && x < 8 && forY >= 0 && forY < 8
                    && pieces[x][forY] != null && pieces[x][forY].getOwner() != owner)
                return true;
        }

        if(((1 << Game.backwardCapture) & options) != 0)
        {
            for(int x = position.x - 1; x <= position.x + 1; x+=2)
            {
                if(x >= 0 && x < 8 && backY >= 0 && backY < 8
                        && pieces[x][backY] != null && pieces[x][backY].getOwner() != owner)
                    return true;
            }
        }

        return false;
    }
}
