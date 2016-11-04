package edu.game.checkers.activities;

// handles server requests and errors
public interface ServerRequestHandler {

    void onServerRequest(Message msg);

    void onConnectionError(String error);

}
