package edu.game.checkers.core;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import edu.board.checkers.R;
import edu.game.checkers.core.callbacks.Callback1;
import edu.game.checkers.logic.Board;
import edu.game.checkers.logic.Position;

public class NetworkGameActivity extends GameActivity {

    private Board.Player localPlayer;
    private Handler handler = new Handler();
    private CommunicationManager manager;

    NetworkService networkService;
    boolean bound = false;
    volatile boolean active = false;

    private String otherPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initGame();

        Bundle bundle = getIntent().getExtras();

        TextView title = (TextView) findViewById(R.id.name_header);
        otherPlayer = bundle.getString("name");
        options = bundle.getInt("options", 0);

        String header = getString(R.string.name_header) + otherPlayer;
        title.setText(header);
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
        active = false;
        // Unbind from the service
        if (bound)
            unbindService(connection);
        bound = false;
    }

    @Override
    public void onBackPressed() {
        new AlertDialog(this).createQuestionDialog("Exit", "Do you want to exit?",
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which == Dialog.BUTTON_POSITIVE){
                    endGame();
                    NetworkGameActivity.super.onBackPressed();
                }
            }
        });
    }

    private void endGame() {
        //networkService.sendRequest(new Message(Message.EXIT_GAME));
    }

    private void startGame(){
        //            networkService.startGame(new GameController() {
//                @Override
//                public void onMessage(Message message) {
//                    switch (message.getCode()){
//                        case Message.START_GAME:
//                            String player = message.getArguments().get(0);
//                            if(player.equals(Message.PLAYER_BLACK)) {
//                                localPlayer = Board.Player.BLACK;
//                                boardView.rotate();
//                            }
//                            else
//                                localPlayer = Board.Player.WHITE;
//                            break;
//                        case Message.MOVE:
//                            Pair<Position, Position> move = message.parseMove();
//
//                            if(board.getCurrentPlayer() != localPlayer) {
//                                board.clicked(move.first, false);
//                                board.clicked(move.second, false);
//                            }
//                            break;
//                        case Message.GAME_OVER:
//                            if(active)
//                                handler.post(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        new AlertDialog(NetworkGameActivity.this).
//                                                createExitDialog("End", "Other player has disconnected");
//                                    }
//                                });
//                            break;
//                        case Message.UNDO_MOVE:
//                            board.undoMove();
//                            boardView.setPieces(board.getPieces());
//                            boardView.postInvalidate();
//                            break;
//                        default:
//                            break;
//                    }
//                }
//            });
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

            manager = networkService.getCommunicationManager(otherPlayer);

            manager.setRequestCallback(new Callback1<String>() {
                @Override
                public void onAction(String param) {
                    // TODO
                }
            });

            startGame();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };

    @Override
    protected TouchManager createTouchManager(){
        return new TouchManager(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Position position = calculatePosition(v, event);

                if(position == null)
                    return false;

                Position prevPosition = null;
                if(board.getSelectedPiece() != null)
                    prevPosition = board.getSelectedPiece().getPosition();

                Board.ClickResult result = board.clicked(position, true);

                if(result == Board.ClickResult.MOVED){
//                    networkService.sendGameMessage(new Message(Message.MOVE,
//                            prevPosition.toString(), position.toString()));
                }

                turnView.setColor(board.getCurrentPlayer() == Board.Player.WHITE ?
                        Color.WHITE : Color.BLACK);

                return true;
            }
        };
    }

    @Override
    public void undoMove(View view)
    {
        // super.undoMove(view);
    }
}