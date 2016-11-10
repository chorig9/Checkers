package edu.game.checkers.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.util.Pair;
import android.os.Handler;


import edu.game.checkers.logic.Board;
import edu.game.checkers.logic.Position;

public class NetworkGameActivity extends GameActivity{

    private Board.Player localPlayer;
    private NetworkService networkService;
    private boolean bound = false;

    private volatile  boolean active = false;

    private Handler handler = new Handler();

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        endGame();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initGame();


        Bundle bundle = getIntent().getExtras();
        String otherName = bundle.getString("NAME");
    }

    @Override
    protected void onStart() {
        super.onStart();
        active = true;
        // Bind to NetworkService
        Intent intent = new Intent(this, NetworkService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        endGame();
        active = false;
        // Unbind from the service
        if (bound)
            unbindService(connection);
        bound = false;
    }

    private void endGame() {
        networkService.sendRequest(new Message(Message.EXIT_GAME));
    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to NetworkService, cast the IBinder and get NetworkService instance
            NetworkService.NetworkBinder binder = (NetworkService.NetworkBinder) service;
            networkService = binder.getService();
            bound = true;

            networkService.startGame(new GameController() {
                @Override
                public void onMessage(Message message) {
                    switch (message.getCode()){
                        case Message.START_GAME:
                            String player = message.getArguments().get(0);
                            if(player.equals(Message.PLAYER_BLACK))
                                localPlayer = Board.Player.BLACK;
                            else
                                localPlayer = Board.Player.WHITE;
                            break;
                        case Message.MOVE:
                            Pair<Position, Position> move = message.parseMove();
                            remoteClick(move.first);
                            remoteClick(move.second);
                            break;
                        case Message.GAME_OVER:
                            if(active)
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        new AlertDialog(NetworkGameActivity.this).
                                                createErrorDialog("Other player has disconnected");
                                    }
                                });
                            break;
                        case Message.UNDO_MOVE:
                            board.undoMove();
                            boardView.setPieces(board.getPieces());
                            boardView.postInvalidate();
                            break;
                        default:
                            break;
                    }
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };

    @Override
    public void localClick(Position position) {
        if(board.getCurrentPlayer() == localPlayer) {
            if (board.canBeSelected(position)) {
                board.selectPiece(position);
                boardView.setHints(board.getSelectedPiece().
                        getValidPositions(board.getOptions(), board.getPieces()));
                boardView.postInvalidate();
            } else if (board.canSelectedPieceBeMoved(position)) {
                if(!bound){
                    new AlertDialog(this).createErrorDialog("Connection error occurred");
                }
                else{
                    networkService.sendGameMessage(new Message(Message.MOVE,
                            board.getSelectedPiece().getPosition().toString(), position.toString()));
                    board.moveSelectedPiece(position);
                    boardView.setHints(null);
                    boardView.postInvalidate();
                }
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
        if(board.getCurrentPlayer() != localPlayer){
            boardView.setHints(null);
            boardView.postInvalidate();
        }
        networkService.sendGameMessage(new Message(Message.UNDO_MOVE));
    }
}