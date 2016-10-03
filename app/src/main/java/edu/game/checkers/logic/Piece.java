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

        int px = (position.x > this.position.x) ? 1 : -1;
        int py = (position.y > this.position.y) ? 1 : -1;

        for(int x = this.position.x + px, y = this.position.y + py; x != position.x; x+=px, y+=py)
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

    public boolean isMoveValid(Position target, int options, Piece pieces[][])
    {
        boolean obligatoryCapture = Game.isOptionEnabled(options, Game.obligatoryCapture);

        // move is good and: is capturing or capturing is not obligatory or piece cannot jump
        return isMoveCapturing(target, options, pieces)
                || ((!obligatoryCapture || !canJump(options, pieces))
                && isMoveCorrect(target, options, pieces));
    }

    // checks if move is correct and capturing
    public abstract boolean isMoveCapturing(Position target, int options, Piece pieces[][]);

    // checks if move is correct but not captures anything
    public abstract boolean isMoveCorrect(Position target, int options, Piece pieces[][]);

    public abstract ArrayList<Position> getValidPositions(int options, Piece pieces[][]);

    public abstract boolean canJump(int options, Piece pieces[][]);
}
