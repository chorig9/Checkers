package edu.game.checkers.logic;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class King extends Piece{

    public King(Position position, Board.Player owner)
    {
        super(position, owner);
    }

    @Override
    public void draw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(owner == Board.Player.WHITE ? Color.WHITE : Color.BLACK);

        int tileSize = canvas.getWidth() / 8;

        float cx = (float)((position.x + 0.5) * tileSize);
        float cy = (float)((position.y + 0.5) * tileSize);
        float radius = (float)(0.9 * tileSize / 2);

        float width = (float)(0.1 * radius);
        canvas.drawCircle(cx, cy, radius, paint);
        paint.setColor(owner == Board.Player.BLACK ? Color.WHITE : Color.BLACK);
        canvas.drawCircle(cx, cy, radius / 2, paint);
        paint.setColor(owner == Board.Player.WHITE ? Color.WHITE : Color.BLACK);
        canvas.drawCircle(cx, cy, radius / 2 - width, paint);
    }

    @Override
    public boolean isMoveCapturing(Position target, int options, Piece[][] pieces) {
        if(!Board.isOptionEnabled(options, Board.flyingKing))
        {
            return pieces[target.x][target.y] == null
                && Math.abs(target.x - position.x) == 2 && Math.abs(target.y - position.y) == 2
                && checkForPieces(target, pieces) == PiecesOnWay.One;
        }
        else
        {
            return pieces[target.x][target.y] == null
                && Math.abs(target.x - position.x) == Math.abs(target.y - position.y)
                && checkForPieces(target, pieces) == PiecesOnWay.One;
        }
    }

    @Override
    public boolean isMoveCorrect(Position target, int options, Piece[][] pieces) {
        if(!Board.isOptionEnabled(options, Board.flyingKing))
        {
            return pieces[target.x][target.y] == null
                && Math.abs(target.x - position.x) == 1 && Math.abs(target.y - position.y) == 1;
        }
        else
        {
            return pieces[target.x][target.y] == null
                && Math.abs(target.x - position.x) == Math.abs(target.y - position.y)
                && checkForPieces(target, pieces) == PiecesOnWay.None;
        }
    }

    @Override
    public King copy()
    {
        return new King(new Position(position.x, position.y), owner);
    }

    @Override
    public boolean canCapture(int options, Piece[][] pieces) {
        int range;

        if(!Board.isOptionEnabled(options, Board.flyingKing))
            range = 2;
        else
            range = 7;

        for(int px = -range; px <= range; px++)
        {
            for(int k = -1; k <= 1; k += 2)
            {
                Position pos = new Position(position.x + px, position.y + k * px);
                if(!pos.equals(position) && pos.isInRange() && isMoveCapturing(pos, options, pieces))
                    return true;
            }
        }

        return false;
    }

    // Multiple - more than one (owner doesn't matter) or 1 piece of current player
    private enum PiecesOnWay {None, One, Multiple}

    private PiecesOnWay checkForPieces(Position target, Piece pieces[][])
    {
        boolean pieceOnWay = false;
        int px = (target.x > position.x) ? 1 : -1;
        int py = (target.y > position.y) ? 1 : -1;

        for(int x = position.x + px, y = position.y + py; x != target.x; x+=px, y+=py)
        {
            if(pieces[x][y] != null)
            {
                if(pieces[x][y].getOwner() == owner || pieceOnWay)
                    return PiecesOnWay.Multiple;
                else
                    pieceOnWay = true;
            }
        }

        return pieceOnWay ? PiecesOnWay.One : PiecesOnWay.None;
    }
}
