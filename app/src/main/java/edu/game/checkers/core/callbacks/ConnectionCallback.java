package edu.game.checkers.core.callbacks;

public interface ConnectionCallback {

    void onConnectionError(String error);
    void onSuccess();

}
