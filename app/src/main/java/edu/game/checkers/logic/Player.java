package edu.game.checkers.logic;

import android.util.Pair;

public abstract class Player {

    public enum Color { WHITE, BLACK }

    Color color;
    Game game;

    public Player(Color color, Game game)
    {
        this.color = color;
        this.game = game;
    }

    public Piece[][] getPieces()
    {
        return game.getPieces();
    }

    public int getOptions()
    {
        return game.getOptions();
    }

    public boolean canAnyPieceJump()
    {
        if((game.getOptions() & (1 << Game.obligatoryCapture)) != 0)
            return false;

        for(int x = 0; x <8; x++)
        {
            for(int y = 0; y < 8; y++)
            {
                if(game.getPieces()[x][y].canJump(game.getPieces()))
                    return true;
            }
        }

        return false;
    }

    public abstract Pair<Position, Position> makeMove();

}
