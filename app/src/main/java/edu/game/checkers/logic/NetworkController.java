package edu.game.checkers.logic;

import java.io.IOException;

public class NetworkController extends GameController{

    private NetworkManager networkManager;

    public NetworkController(Board board, BoardView boardView, Board.Player localPlayer) {
        super(board, boardView, localPlayer);

    }

    @Override
    protected void onEvent(Position position) {
        boardView.postInvalidate();
        try {
            networkManager.send("TODO");
        } catch (IOException e) {
            // TODO
        }
    }

}
