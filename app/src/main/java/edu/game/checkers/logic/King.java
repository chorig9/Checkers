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
        paint.setColor(owner.getColor());

        int tileSize = canvas.getWidth() / 8;

        float cx = (float)((position.x + 0.5) * tileSize);
        float cy = (float)((position.y + 0.5) * tileSize);
        float radius = (float)(tileSize / 2);

        float width = (float)(0.1 * radius);
        canvas.drawCircle(cx, cy, radius, paint);
        if(owner.getColor() == Color.WHITE)
            paint.setColor(Color.BLACK);
        else
            paint.setColor(Color.WHITE);
        canvas.drawCircle(cx, cy, radius / 2, paint);
        paint.setColor(owner.getColor());
        canvas.drawCircle(cx, cy, radius / 2 - width, paint);
    }

    @Override
    public boolean isMoveValid(Position target, int options, Piece[][] pieces) {
        if(pieces[target.x][target.y] != null)
            return false;

        // obligatory jump
        boolean obligatoryCapture = Game.isOptionEnabled(options, Game.obligatoryCapture);

        boolean jump, move;
        if(!Game.isOptionEnabled(options, Game.flyingKing))
        {
            // is this a valid simple move
            move = Math.abs(target.x - position.x) == 1 && Math.abs(target.y - position.y) == 1;

            // is this a valid jump
            jump = Math.abs(target.x - position.x) == 2 && Math.abs(target.y - position.y) == 2
                    && pieces[(target.x + position.x)/2][(target.y + position.y)/2] != null
                    && pieces[(target.x + position.x)/2][(target.y+position.y)/2].getOwner()!=owner;
        }
        else
        {
            int piecesInWay = 0;
            int px = (target.x > position.x) ? 1 : -1;
            int py = (target.y > position.y) ? 1 : -1;

            for(int x = position.x + px, y = position.y + py; x != target.x; x+=px, y+=py)
            {
                if(pieces[x][y] != null && pieces[x][y].getOwner() != owner)
                    piecesInWay++;
                else if(pieces[x][y] != null && pieces[x][y].getOwner() == owner)
                    return false;
            }

            move = Math.abs(target.x - position.x) == Math.abs(target.y - position.y);

            Piece phantomPiece = new King(new Position(target.x, target.y), owner);


            jump = Math.abs(target.x - position.x) == Math.abs(target.y - position.y)
                    && piecesInWay == 1;
        }

        return ((move && !obligatoryCapture) || (move && !canJump(options, pieces))) || jump;
    }

    @Override
    public ArrayList<Position> getValidPositions(int options, Piece[][] pieces) {
        ArrayList<Position> positions = new ArrayList<>();

        for(int px = -7; px <= 7; px++)
        {
            for(int k = -1; k <= 1; k += 2)
            {
                int x = position.x + px;
                int y = position.y + k * px;
                if(x != position.x && y != position.y && Position.inRange(x) && Position.inRange(y)
                        && isMoveValid(new Position(x, y), options, pieces))
                    positions.add(new Position(x, y));
            }
        }

        return positions;
    }

    @Override
    public boolean canJump(int options, Piece[][] pieces) {
        int range;

        if(!Game.isOptionEnabled(options, Game.flyingKing))
            range = 1;
        else
            range = 7;

        for(int px = -range; px <= range; px++)
        {
            for(int k = -1; k <= 1; k += 2)
            {
                int x = position.x + px;
                int y = position.y + k * px;
                if(Position.inRange(x) && Position.inRange(y)
                        && Position.inRange(x + px) && Position.inRange(y + k * px)
                        && Position.inRange(x - px) && Position.inRange(y - k * px)
                        && pieces[x][y] != null && pieces[x][y].getOwner() != owner
                        && pieces[x + px][y + k * px] == null
                        && (pieces[x - px][y - k * px] == null
                        || (x - px == position.x && y - k * px == position.y)))
                    return true;

            }
        }

        return false;
    }
}
