package edu.game.checkers.activities;

import edu.game.checkers.logic.Position;

public interface GameController {

    public void remoteClick(Position position);

    public void localClick(Position position);

}
