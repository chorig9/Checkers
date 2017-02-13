package edu.game.checkers.utils;

public interface ConnectionCallback {

    void onConnectionError(String error);
    void onSuccess();

}
