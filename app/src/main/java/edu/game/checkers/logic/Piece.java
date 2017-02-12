package edu.game.checkers.logic;

import android.graphics.Canvas;
import java.util.ArrayList;

public abstract class Piece{

    protected Position position;
    protected Game.Player owner;
    protected Board board;

    public Piece(Position position, Game.Player owner, Board board) {
        this.position = position;
        this.owner = owner;
        this.board = board;
    }

    public void moveTo(Position position) {
        // if this move is capturing this variable holds position of captured piece
        // otherwise it is null
        Position capturedPiecePosition = getCapturedPiecePosition(position);
        Piece pieces[][] = board.pieces;

        pieces[position.x][position.y] = pieces[this.position.x][this.position.y];
        pieces[this.position.x][this.position.y] = null;

        this.position.x = position.x;
        this.position.y = position.y;

        if(capturedPiecePosition != null)
            pieces[capturedPiecePosition.x][capturedPiecePosition.y] = null;
    }

    public Game.Player getOwner()
    {
        return owner;
    }

    public boolean isMoveValid(Position target) {
        return isMoveCapturingAndOptimal(target)
                || (!canCapture() && isMoveCorrectAndDoNotCapture(target));
    }

    // for every capturing move from this piece's position getMaxNumberOfPiecesCapturedByThisMove() is called
    // the above function moves piece to destination(on copy of original pieces table) and
    // calls this function again (for new position) - mutual recursion
    public int getNumberOfPiecesCapturedByOptimalMove() {
        int maxNumberOfPieces = 0;

        for(int x = -7; x <= 7; x++) {
            for(int k = -1; k <= 1; k+=2) {
                Position newPosition = new Position(position.x + x, position.y + k * x);
                if(!newPosition.equals(position) && newPosition.isInRange() && isMoveCorrectAndCapturing(newPosition)) {
                    int numberOfPieces = getMaxNumberOfPiecesCapturedByThisMove(newPosition);

                    if(numberOfPieces > maxNumberOfPieces)
                        maxNumberOfPieces = numberOfPieces;
                }
            }
        }
        return maxNumberOfPieces;
    }

    // should only be called when move is capturing
    // therefore returns at least 1
    public int getMaxNumberOfPiecesCapturedByThisMove(Position target) {
        int numberOfCaptures = 1;

        Board copyBoard = board.copy();
        Piece correspondingPiece = copyBoard.pieces[position.x][position.y];

        correspondingPiece.moveTo(target);
        numberOfCaptures += correspondingPiece.getNumberOfPiecesCapturedByOptimalMove();

        return numberOfCaptures;
    }

    public ArrayList<Position> getValidPositions() {
        ArrayList<Position> validPositions = new ArrayList<>();

        for(int x = -7; x <= 7; x++) {
            for(int k = -1; k <= 1; k+= 2) {
                Position moveTo = new Position(position.x + x, position.y + k * x);
                if(!moveTo.equals(position) && moveTo.isInRange() && isMoveValid(moveTo))
                    validPositions.add(moveTo);
            }
        }

        return validPositions;
    }

    public Position getPosition()
    {
        return position;
    }

    public abstract void draw(Canvas canvas);

    public abstract boolean isMoveCorrectAndCapturing(Position target);

    public abstract boolean isMoveCorrectAndDoNotCapture(Position target);

    public abstract boolean canCapture();

    public abstract Piece copy();

    private boolean isMoveCapturingAndOptimal(Position target){
        boolean optimalCapture = Game.isOptionEnabled(board.options, Game.optimalCapture);

        return isMoveCorrectAndCapturing(target) && (getNumberOfPiecesCapturedByOptimalMove()
                == getMaxNumberOfPiecesCapturedByThisMove(target) || !optimalCapture);
    }

    private Position getCapturedPiecePosition(Position position) {
        Position capturedPiecePosition = null;

        int px = (position.x > this.position.x) ? 1 : -1;
        int py = (position.y > this.position.y) ? 1 : -1;

        for(int x = this.position.x + px, y = this.position.y + py; x != position.x; x+=px, y+=py)
        {
            if(board.pieces[x][y] != null)
                capturedPiecePosition = new Position(x, y);
        }

        return capturedPiecePosition;
    }

    boolean isTargetPositionEmpty(Position target){
        return board.pieces[target.x][target.y] == null;
    }

}
