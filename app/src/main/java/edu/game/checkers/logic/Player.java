package edu.game.checkers.logic;

import android.util.Pair;

public abstract class Player {

    Game game;
    int color;

    public Player(int color, Game game)
    {
        this.color = color;
        this.game = game;
    }

    public int getColor()
    {
        return color;
    }

    public boolean canAnyPieceJump()
    {
        if(!game.isOptionEnabled(Game.obligatoryCapture))
            return false;

        for(int x = 0; x <8; x++)
        {
            for(int y = 0; y < 8; y++)
            {
                if(game.getPieces()[x][y] != null && game.getPieces()[x][y].getOwner() == this
                && game.getPieces()[x][y].canJump(game.getOptions(), game.getPieces()))
                    return true;
            }
        }

        return false;
    }

    public abstract Pair<Position, Position> makeMove();

}
