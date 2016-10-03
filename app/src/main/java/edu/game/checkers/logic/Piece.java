package edu.game.checkers.logic;

import android.graphics.Canvas;

import java.util.ArrayList;

public abstract class Piece {

    Position position;
    Player owner;

    public Piece(Position position, Player owner)
    {
        this.position = position;
        this.owner = owner;
    }

    public Position moveTo(Position position, Piece pieces[][])
    {
        Position capturedPiecePosition = null;

        int minX = Math.min(this.position.x, position.x);
        int minY = Math.min(this.position.y, position.y);
        int maxX = Math.max(this.position.x, position.x);

        for(int x = minX + 1, y = minY + 1; x < maxX; x++, y++)
        {
            if(pieces[x][y] != null)
                capturedPiecePosition = new Position(x, y);
        }

        pieces[position.x][position.y] = pieces[this.position.x][this.position.y];
        pieces[this.position.x][this.position.y] = null;

        this.position.x = position.x;
        this.position.y = position.y;

        return capturedPiecePosition;
    }

    public Player getOwner()
    {
        return owner;
    }

    public abstract void draw(Canvas canvas);

    public abstract boolean isMoveValid(Position target, int options, Piece pieces[][]);

    public abstract ArrayList<Position> getValidPositions(int options, Piece pieces[][]);

    public abstract boolean canJump(int options, Piece pieces[][]);
}
