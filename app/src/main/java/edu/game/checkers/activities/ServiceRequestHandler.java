package edu.game.checkers.activities;

// handles server requests
public interface ServiceRequestHandler {

    void onInvite(String name);

    void onConnectionError(String error);

}
