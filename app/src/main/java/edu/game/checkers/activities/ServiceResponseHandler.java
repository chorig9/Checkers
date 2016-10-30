package edu.game.checkers.activities;

// handles service responses after request
public interface ServiceResponseHandler {

    void onConnectionError(String error);

    void onServerResponse(Message response);

}
