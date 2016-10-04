package edu.game.checkers.logic;

import android.graphics.Canvas;
import android.graphics.Paint;

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
    public boolean isMoveCapturing(Position target, int options, Piece[][] pieces) {
        boolean backwardJumpAllowed = Game.isOptionEnabled(options, Game.backwardCapture);

        return pieces[target.x][target.y] == null && Math.abs(target.x - position.x) == 2
                && pieces[(target.x + position.x)/2][(target.y + position.y)/2] != null
                && pieces[(target.x + position.x)/2][(target.y + position.y)/2].getOwner() != owner
                && ((backwardJumpAllowed && Math.abs(target.y - position.y) == 2)
                || target.y - position.y == 2 * orientation);
    }

    @Override
    public boolean isMoveCorrect(Position target, int options, Piece[][] pieces) {
        return pieces[target.x][target.y] == null
                && Math.abs(target.x - position.x) == 1 && target.y - position.y == orientation;
    }

    @Override
    public boolean canJump(int options, Piece[][] pieces) {
        int forY = position.y + 2 * orientation;
        int backY = position.y - 2 * orientation;

        for(int px = -2; px <= 2; px+=4)
        {
            Position pos = new Position(position.x + px, forY);
            if(pos.isInRange() && isMoveCapturing(pos, options, pieces))
                return true;
        }

        if(Game.isOptionEnabled(options, Game.backwardCapture))
        {
            for(int px = -2; px <= 2; px+=4)
            {
                Position pos = new Position(position.x + px, backY);
                if(pos.isInRange() && isMoveCapturing(pos, options, pieces))
                    return true;
            }
        }

        return false;
    }

    // additionally converts men to king
    @Override
    public Position moveTo(Position position, Piece pieces[][])
    {
        Position pos = super.moveTo(position, pieces);

        if((orientation == 1 && position.y == 7) || (orientation == - 1 && position.y == 0))
            pieces[position.x][position.y] = new King(position, owner);

        return pos;
    }

    @Override
    public Men copy()
    {
        return new Men(new Position(position.x, position.y), owner);
    }
}
