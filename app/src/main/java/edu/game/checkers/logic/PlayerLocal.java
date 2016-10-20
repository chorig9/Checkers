package edu.game.checkers.logic;

public class PlayerLocal extends Player{

    public PlayerLocal(int color, Game game)
    {
        super(color, game);
    }

    @Override
    public void turnOn()
    {
        super.turnOn();
        game.touchManager.setUser(this);
    }

    public void clicked(Position position)
    {
        if(!myTurn)
            return;

        if(game.canBeSelected(position))
        {
            game.selectPiece(position);
            game.showHints();
        }
        else
            game.moveSelectedPiece(position);
    }
}
