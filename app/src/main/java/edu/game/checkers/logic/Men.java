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
        boolean backwardJumpAllowed = Game.isOptionEnabled(options, Game.backwardCapture);
        // obligatory jump
        boolean obligatoryCapture = Game.isOptionEnabled(options, Game.obligatoryCapture);

        // is this a valid jump
        boolean jump = Math.abs(target.x - position.x) == 2
                && pieces[(target.x + position.x)/2][(target.y + position.y)/2] != null
                && pieces[(target.x + position.x)/2][(target.y + position.y)/2].getOwner() != owner;

        boolean forwardJump = target.y - position.y == 2 * orientation;
        boolean backwardJump = Math.abs(target.y - position.y) == 2;

        return ((move && !obligatoryCapture) || (move && !canJump(options, pieces)))
                || (jump && forwardJump) || (jump && backwardJumpAllowed && backwardJump);
    }

    @Override
    public ArrayList<Position> getValidPositions(int options, Piece[][] pieces) {
        ArrayList<Position> positions = new ArrayList<>();

        for(int x = position.x - 2; x <= position.x + 2; x++)
        {
            for(int y = position.y - 2; y <= position.y + 2; y++)
            {
                if(x != position.x && y != position.y && Position.inRange(x) && Position.inRange(y)
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

        for(int x = -1; x <= 1; x+=2)
        {
            int px = position.x + x;
            if(Position.inRange(px) && Position.inRange(forY)
                    && Position.inRange(px + x) && Position.inRange(forY + orientation)
                    && pieces[px][forY] != null && pieces[px][forY].getOwner() != owner
                    && pieces[px + x][forY + orientation] == null)
                return true;
        }

        if(Game.isOptionEnabled(options, Game.backwardCapture))
        {
            for(int x = -1; x <= 1; x+=2)
            {
                int px = position.x + x;
                if(Position.inRange(px) && Position.inRange(backY)
                        && Position.inRange(px + x) && Position.inRange(backY - orientation)
                        && pieces[px][backY] != null && pieces[px][backY].getOwner() != owner
                        && pieces[px + x][backY - orientation] == null)
                    return true;
            }
        }

        return false;
    }

    @Override
    public Position moveTo(Position position, Piece pieces[][])
    {
        Position pos = super.moveTo(position, pieces);

        if(position.y == 7 * orientation)
            pieces[position.x][position.y] = new King(position, owner);

        return pos;
    }
}
