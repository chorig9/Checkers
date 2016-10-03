package edu.game.checkers.logic;

import android.graphics.Canvas;
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
        paint.setColor(owner.getColor());

        int tileSize = canvas.getWidth() / 8;

        float cx = (float)((position.x + 0.5) * tileSize);
        float cy = (float)((position.y + 0.5) * tileSize);
        float radius = (float)(tileSize / 2);

        canvas.drawCircle(cx, cy, radius, paint);
    }

    @Override
    public boolean isMoveValid(Position target, int options, Piece[][] pieces) {
        if(pieces[target.x][target.y] != null)
            return false;

        // is this a valid simple move
        boolean move = Math.abs(target.x - position.x) == 1 && target.y - position.y == orientation;

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

        for(int px = -1; px <= 1; px+=2)
        {
            int x = position.x + px;
            if(Position.inRange(x) && Position.inRange(forY)
                    && Position.inRange(x + px) && Position.inRange(forY + orientation)
                    && pieces[x][forY] != null && pieces[x][forY].getOwner() != owner
                    && pieces[x + px][forY + orientation] == null)
                return true;
        }

        if(Game.isOptionEnabled(options, Game.backwardCapture))
        {
            for(int px = -1; px <= 1; px+=2)
            {
                int x = position.x + px;
                if(Position.inRange(x) && Position.inRange(backY)
                        && Position.inRange(x + px) && Position.inRange(backY - orientation)
                        && pieces[x][backY] != null && pieces[x][backY].getOwner() != owner
                        && pieces[x + px][backY - orientation] == null)
                    return true;
            }
        }

        return false;
    }

    @Override
    public Position moveTo(Position position, Piece pieces[][])
    {
        Position pos = super.moveTo(position, pieces);

        if((orientation == 1 && position.y == 7) || (orientation == - 1 && position.y == 0))
            pieces[position.x][position.y] = new King(position, owner);

        return pos;
    }
}
