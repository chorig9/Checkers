package edu.game.checkers.activities;

// handles server requests
public interface ServerRequestHandler {

    void onServerRequest(Message msg);

    void onConnectionError(String error);

}
