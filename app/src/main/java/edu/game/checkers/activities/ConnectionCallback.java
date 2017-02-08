package edu.game.checkers.activities;

public interface ConnectionCallback {

    void onConnectionError(String error);
    void onSuccess();

}
