package edu.game.checkers.core.callbacks;

import edu.game.checkers.core.CommunicationManager;

public interface ConnectionCreatedCallback {

    void onConnectionCreated(CommunicationManager manager);

}
