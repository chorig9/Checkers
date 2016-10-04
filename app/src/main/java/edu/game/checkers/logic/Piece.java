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

    public boolean isMoveValid(Position target, int options, Piece pieces[][])
    {
        boolean obligatoryCapture = Game.isOptionEnabled(options, Game.obligatoryCapture);
        boolean optimalCapture = Game.isOptionEnabled(options, Game.optimalCapture);

        // move is good and: (is capturing and optimal)
        // or capturing is not obligatory or piece cannot jump
        return (isMoveCapturing(target, options, pieces)
                && (!optimalCapture || getOptimalCapture(options, pieces).equals(target)))
                || ((!obligatoryCapture || !canJump(options, pieces))
                && isMoveCorrect(target, options, pieces));
    }
    // returns move which is optimal(should only be called when piece can jump)
    public Position getOptimalCapture(int options, Piece[][] pieces) {
        int max = 0;
        Position optimal = null;

        for(int x = -7; x <= 7; x++)
        {
            for(int k = -1; k <= 1; k+=2)
            {
                Position pos = new Position(position.x + x, position.y + k * x);
                if(!pos.equals(position) && pos.isInRange() && isMoveCapturing(pos, options, pieces))
                {
                    int n = getNumberOfCaptures(pos, options, pieces);

                    if(n > max)
                    {
                        max = n;
                        optimal = pos;
                    }
                }
            }
        }
        return optimal;
    }

    public ArrayList<Position> getValidPositions(int options, Piece pieces[][])
    {
        ArrayList<Position> positions = new ArrayList<>();

        for(int x = -7; x <= 7; x++)
        {
            for(int k = -1; k <= 1; k+= 2)
            {
                Position pos = new Position(position.x + x, position.y + k * x);
                if(!pos.equals(position) && pos.isInRange() && isMoveValid(pos, options, pieces))
                    positions.add(pos);
            }
        }

        return positions;
    }


    public abstract void draw(Canvas canvas);

    // checks if move is correct and capturing
    public abstract boolean isMoveCapturing(Position target, int options, Piece pieces[][]);

    // checks if move is correct but not captures anything
    public abstract boolean isMoveCorrect(Position target, int options, Piece pieces[][]);

    // returns how many pieces can by captured by performing this move
    public abstract int getNumberOfCaptures(Position target, int options, Piece pieces[][]);

    public abstract boolean canJump(int options, Piece pieces[][]);
}
