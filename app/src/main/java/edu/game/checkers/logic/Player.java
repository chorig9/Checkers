package edu.game.checkers.logic;

public abstract class Player{

    final Game game;
    final int color;
    boolean myTurn;

    public Player(int color, Game game)
    {
        this.game = game;
        this.color = color;
        this.myTurn = false;
    }

    public void turnOn()
    {
        myTurn = true;
    }

    public void turnOff()
    {
        myTurn = false;
    }

    public boolean canAnyPieceJump()
    {
        for(int x = 0; x <8; x++)
        {
            for(int y = 0; y < 8; y++)
            {
                if(game.getPieces()[x][y] != null && game.getPieces()[x][y].getOwner() == this
                    && game.getPieces()[x][y].canCapture(game.getOptions(), game.getPieces()))
                    return true;
            }
        }

        return false;
    }

    public int getMaxNumberOfCaptures()
    {
        int max = 0;
        for(int x = 0; x <8; x++)
        {
            for(int y = 0; y < 8; y++)
            {
                Piece piece = game.getPieces()[x][y];
                if(piece != null && piece.getOwner() == this
                    && piece.canCapture(game.getOptions(), game.getPieces()))
                {
                    int n = piece.optimalMoveCaptures(game.getOptions(), game.getPieces());

                    max = (n > max) ? n : max;
                }
            }
        }
        return max;
    }

}
