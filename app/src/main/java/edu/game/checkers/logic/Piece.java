package edu.game.checkers.logic;

import android.graphics.Canvas;
import java.util.ArrayList;

public abstract class Piece{

    Position position;
    Board.Player owner;

    public Piece(Position position, Board.Player owner)
    {
        this.position = position;
        this.owner = owner;
    }

    public void moveTo(Position position, Piece pieces[][])
    {
        // if this move is capturing this variable holds position of captured piece
        // isMoveCapturing function wasn't used because it depends on board options
        // which are unknown in this method
        // otherwise it is null
        Position capturedPiecePosition = getCapturedPiecePosition(position, pieces);

        pieces[position.x][position.y] = pieces[this.position.x][this.position.y];
        pieces[this.position.x][this.position.y] = null;

        this.position.x = position.x;
        this.position.y = position.y;

        if(capturedPiecePosition != null)
            pieces[capturedPiecePosition.x][capturedPiecePosition.y] = null;
    }

    public Board.Player getOwner()
    {
        return owner;
    }

    public boolean isMoveValid(Position target, int options, Piece pieces[][])
    {
        boolean optimalCapture = Board.isOptionEnabled(options, Board.optimalCapture);

        // move is good and: (is capturing and optimal)
        // or capturing is not obligatory or piece cannot jump
        return (isMoveCapturing(target, options, pieces)
                && (!optimalCapture || optimalMoveCaptures(options, pieces)
                    == thisMoveCaptures(target, options, pieces)))
                || (!canCapture(options, pieces)
                && isMoveCorrect(target, options, pieces));
    }

    // returns how many pieces can this piece capture at most
    // for every capturing move from this piece's position thisMoveCaptures() is called
    // the above function moves piece to destination(on copy of original pieces table) and
    // calls this function again (for new position) - mutual recursion
    public int optimalMoveCaptures(int options, Piece[][] pieces) {
        int max = 0;

        for(int x = -7; x <= 7; x++)
        {
            for(int k = -1; k <= 1; k+=2)
            {
                Position pos = new Position(position.x + x, position.y + k * x);
                if(!pos.equals(position) && pos.isInRange() && isMoveCapturing(pos, options, pieces))
                {
                    int n = thisMoveCaptures(pos, options, pieces);

                    if(n > max)
                        max = n;
                }
            }
        }
        return max;
    }

    // returns how many pieces can by captured by performing this move
    // should only be called when move is capturing
    // therefore returns at least 1
    public int thisMoveCaptures(Position target, int options, Piece pieces[][])
    {
        // n = 1 : "recursion base"
        int n = 1;
        Piece[][] copyPieces = new Piece[8][8];

        for(int i = 0; i < 8; i++)
        {
            for(int j = 0; j < 8; j++)
            {
                if(pieces[i][j] == null)
                    copyPieces[i][j] = null;
                else
                    copyPieces[i][j] = pieces[i][j].copy();
            }
        }

        Piece thisPiece = copyPieces[position.x][position.y];
        thisPiece.moveTo(target, copyPieces);

        n += thisPiece.optimalMoveCaptures(options, copyPieces);

        return n;
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

    private Position getCapturedPiecePosition(Position position, Piece[][] pieces)
    {
        Position capturedPiecePosition = null;

        int px = (position.x > this.position.x) ? 1 : -1;
        int py = (position.y > this.position.y) ? 1 : -1;

        for(int x = this.position.x + px, y = this.position.y + py; x != position.x; x+=px, y+=py)
        {
            if(pieces[x][y] != null)
                capturedPiecePosition = new Position(x, y);
        }

        return capturedPiecePosition;
    }

    public abstract void draw(Canvas canvas);

    // checks if move is correct and capturing
    public abstract boolean isMoveCapturing(Position target, int options, Piece pieces[][]);

    // checks if move is correct and not captures anything
    public abstract boolean isMoveCorrect(Position target, int options, Piece pieces[][]);

    // checks if piece can capture
    public abstract boolean canCapture(int options, Piece pieces[][]);

    public abstract Piece copy();
}
