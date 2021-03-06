package edu.game.checkers.ui;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.os.Handler;

import org.json.JSONException;
import org.json.JSONObject;

import edu.board.checkers.R;
import edu.game.checkers.logic.GameView;
import edu.game.checkers.utils.Callback1;
import edu.game.checkers.logic.Game;
import edu.game.checkers.logic.Position;

public class NetworkGameActivity extends GameActivity {

    private Game.Player localPlayer;
    private CommunicationManager manager;

    private PostAlertDialog dialog;
    private Handler handler;
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
        options = bundle.getInt("options", -1);

        handler = new Handler();
        dialog = new PostAlertDialog(this, handler);

        if(options == -1){
            new AlertDialog(this).createExitDialog("exit", "wrong options");
        }

        boolean locallyInitialized = bundle.getBoolean("locallyInitialized");
        if(locallyInitialized){
            localPlayer = Game.Player.WHITE;
        }
        else{
            localPlayer = Game.Player.BLACK;
            boardView.rotate();
        }

        String header = getString(R.string.name_header) + otherPlayer + " " + Integer.toString(options);
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

    @Override
    protected TouchManager createTouchManager(){
        return new TouchManager(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(board.getCurrentPlayer() != localPlayer)
                    return true;

                int tileSize = v.getWidth() / 8;
                GameView gameView = (GameView) v;
                Position touchPosition = new Position(
                        (int) (event.getX() / tileSize) ,
                        (int) (event.getY() / tileSize)
                );

                Position realPosition = gameView.calculatePosition(touchPosition);

                if(realPosition == null)
                    return false;

                Position prevPosition = null;
                if(board.getSelectedPiece() != null)
                    prevPosition = board.getSelectedPiece().getPosition().copy();

                Game.ClickResult result = board.clicked(realPosition, true);

                if(result == Game.ClickResult.MOVED){
                    manager.sendMove(new MoveMessage(prevPosition, realPosition).toString());
                }

                updateTurnView();

                return true;
            }
        };
    }

    @Override
    public void undoMove(final View view)
    {
        manager.sendRequest("undo", new Callback1<String>() {
            @Override
            public void onAction(String response) {
                if(response.equals("yes")) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            NetworkGameActivity.super.undoMove(view);
                        }
                    });
                }
                else
                    dialog.createInfoDialog("Undo request response", "Player didn't agree");
            }
        });
    }

    private void endGame() {
        manager.sendInfo("exit");
    }

    private void startGame(){
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
            manager.setCallbacks(new RequestCallback(), new GameCallback());

            startGame();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };

    private class RequestCallback implements Callback1<String>{

        @Override
        public void onAction(String request) {

            switch(request){
                case "undo":
                    dialog.createQuestionDialog("Undo move", "Player wants to undo move", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(which == Dialog.BUTTON_POSITIVE) {
                                manager.sendResponse("yes");
                                NetworkGameActivity.super.undoMove(findViewById(R.id.undo_move));
                            }
                            else{
                                manager.sendResponse("no");
                            }
                        }
                    });
                    break;
                case "exit":
                    manager.end();
                    dialog.createExitDialog("Exit", "Other player has disconnected");
                    break;
            }
        }
    }

    private class GameCallback implements Callback1<String>{

        @Override
        public void onAction(String move) {
            Pair<Position, Position> moveStruct = (new MoveMessage(move)).getMove();

            if(board.getCurrentPlayer() != localPlayer){
                board.clicked(moveStruct.first, false);
                board.clicked(moveStruct.second, false);
                boardView.postInvalidate();
                updateTurnView();
            }
        }
    }

    private class MoveMessage {

        private String message;

        MoveMessage(Position from, Position to){
            try{
                JSONObject move = new JSONObject();

                JSONObject position = new JSONObject();
                position.put("x", from.x);
                position.put("y", from.y);
                move.put("from", position.toString());

                position.put("x", to.x);
                position.put("y", to.y);
                move.put("to", position.toString());

                message = move.toString();
            } catch (JSONException e){
                // TODO
            }
        }

        MoveMessage(String message){
            this.message = message;
        }

        @Override
        public String toString(){
            return message;
        }

        Pair<Position, Position> getMove(){
            try{
                JSONObject move = new JSONObject(message);
                JSONObject from = new JSONObject(move.get("from").toString());
                JSONObject to   = new JSONObject(move.get("to").toString());

                Position positionFrom = new Position(from.getInt("x"), from.getInt("y"));
                Position positionTo = new Position(to.getInt("x"), to.getInt("y"));

                return new Pair<>(positionFrom, positionTo);
            } catch (JSONException e){
                //TODO
                return null;
            }
        }
    }
}