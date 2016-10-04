package edu.game.checkers.logic;

import android.util.Pair;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

public class PlayerLocal extends Player{

    private volatile Position sourcePosition, targetPosition;
    private CountDownLatch done;

    public PlayerLocal(int color, Game game)
    {
        super(color, game);
    }

    public Pair<Position, Position> makeMove()
    {
        done = new CountDownLatch(1);
        game.getTouchManager().setUser(this);

        try
        {
            done.await();
            game.getTouchManager().setUser(null);

            Pair<Position, Position> move = new Pair<>(sourcePosition, targetPosition);
            sourcePosition = targetPosition = null;
            return move;
        }
        catch(InterruptedException e)
        {
            //TODO
        }

        game.getTouchManager().setUser(null);
        return null;
    }

    // returns valid positions for clicked piece and set sourcePosition ot targetPosition
    public ArrayList<Position> clicked(Position position)
    {
        boolean optimalCapture = game.isOptionEnabled(Game.optimalCapture);
        int x = position.x, y = position.y;

        int options = game.getOptions();
        Piece[][] pieces = game.getPieces();
        Piece piece = pieces[x][y];

        // if no piece can jump or this piece can jump and jump is optimal
        if(piece != null && piece.getOwner() == this
                && (!canAnyPieceJump() || (piece.canJump(options, pieces)
                    && (!optimalCapture || getMaxNumberOfCaptures()
                    == piece.optimalMoveCaptures(options, pieces)))))
        {
            sourcePosition = position;
            return game.getPieces()[x][y].getValidPositions(game.getOptions(), game.getPieces());
        }
        else
        {
            if(sourcePosition != null
                    && game.getPieces()[sourcePosition.x][sourcePosition.y].isMoveValid(position,
                    game.getOptions(), game.getPieces()))
            {
                targetPosition = position;
                done.countDown();
            }
            return new ArrayList<>();
        }
    }
}
