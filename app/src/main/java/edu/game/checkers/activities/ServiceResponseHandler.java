package edu.game.checkers.activities;

public interface ServiceResponseHandler {

    void onConnectionError(String error);

    void onServerResponse(String response);

}
