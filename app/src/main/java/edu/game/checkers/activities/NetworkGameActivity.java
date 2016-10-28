package edu.game.checkers.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;

import java.io.IOException;

import edu.game.checkers.logic.Board;
import edu.game.checkers.logic.Message;
import edu.game.checkers.logic.Position;

public class NetworkGameActivity extends GameActivity{

    private Board.Player localPlayer;
    private NetworkService networkService;
    private boolean bound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public void localClick(Position position) {
        if(board.getCurrentPlayer() == localPlayer) {
            if (board.canBeSelected(position)) {
                board.selectPiece(position);
                boardView.setHints(board.getSelectedPiece().
                        getValidPositions(board.getOptions(), board.getPieces()));
                boardView.postInvalidate();
            } else if (board.canSelectedPieceBeMoved(position)) {
                //TODO - is bounded?
                if(!bound){
                    // stop game
                    // ERROR
                }
                networkService.sendMove(board.getSelectedPiece().getPosition(), position);
                board.moveSelectedPiece(position);
                boardView.setHints(null);
                boardView.postInvalidate();
            }
        }
    }

    @Override
    public void remoteClick(Position position){
        if(board.getCurrentPlayer() != localPlayer)
            super.remoteClick(position);
    }

    @Override
    public void undoMove(View view)
    {
        super.undoMove(view);
            //TODO - is bounded?
        networkService.send(Message.MOVE_UNDONE);
    }

}