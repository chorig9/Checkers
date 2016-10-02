package edu.game.checkers.logic;

import android.util.Pair;
import java.util.concurrent.CountDownLatch;

public class PlayerLocal extends Player{

    private volatile Position sourcePosition, targetPosition;
    private CountDownLatch done;

    public PlayerLocal(Color color, Game game)
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
            return new Pair<>(sourcePosition, targetPosition);
        }
        catch(InterruptedException e)
        {
            //TODO
        }

        game.getTouchManager().setUser(null);
        return null;
    }

    public void setSource(Position position)
    {
        sourcePosition = position;
    }

    public void setTarget(Position position)
    {
        if(sourcePosition != null && getPieces()[sourcePosition.x][sourcePosition.y].isMoveValid(position, game.getOptions(), getPieces()))
        {
            targetPosition = position;
            done.countDown();
        }
    }

}
