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

    public void moveTo(Position position, Piece pieces[][])
    {
        pieces[position.x][position.y] = pieces[this.position.x][this.position.y];
        pieces[this.position.x][this.position.y] = null;

        this.position.x = position.x;
        this.position.y = position.y;

        //TODO delete captured piece
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
