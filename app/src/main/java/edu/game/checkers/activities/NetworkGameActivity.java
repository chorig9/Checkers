package edu.game.checkers.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.io.IOException;
import java.util.ArrayList;

import edu.board.checkers.R;
import edu.game.checkers.logic.Board;
import edu.game.checkers.logic.Message;
import edu.game.checkers.logic.NetworkManager;
import edu.game.checkers.logic.Position;

public class NetworkGameActivity extends GameActivity{

    private Board.Player localPlayer;
    private NetworkManager manager;

    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
    }

    private class ConnectThread implements Runnable{

        @Override
        public void run() {
            Bundle bundle = getIntent().getExtras();
            int options = bundle.getInt("options");

            try {
                manager = new NetworkManager();

                boolean accepted = false;
                while(!accepted)
                {
                    String name = ((EditText) findViewById(R.id.username)).getText().toString();
                    manager.send(Message.HI + Message.SEPARATOR + name
                            + Message.SEPARATOR + Integer.toString(options));

                    String response = manager.receive(NetworkManager.SERVER_TIMEOUT);

                    if(response.equals(Message.OK))
                        accepted = true;
                    else
                        onServerResponse(response);
                }
            } catch (IOException e) {
                onConnectionError();
            }
        }

//        private class GameThread implements Runnable {
//
//            @Override
//            public void run() {
//                try {
//                    String start_response = manager.receive(NetworkManager.SERVER_TIMEOUT);
//                    if(start_response.equals(Message.PLAYER_WHITE))
//                        localPlayer = Board.Player.WHITE;
//                    else
//                        localPlayer = Board.Player.BLACK;
//
//                    initGame();
//
//                    while(true)
//                    {
//                        String msg = manager.receive(NetworkManager.USER_TIMEOUT);
//
//                        switch (msg)
//                        {
//                            case Message.MOVE:
//                                Pair<Position, Position> move = Message.parseMove(msg);
//                                remoteClick(move.first);
//                                remoteClick(move.second);
//                                break;
//                            case Message.EXIT_GAME:
//                                manager.closeConnection();
//                                return;
//                        }
//                    }
//                } catch (IOException e) {
//                    onConnectionError();
//                }
//            }
//        }
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
                try {
                    manager.sendMove(board.getSelectedPiece().getPosition(), position);
                    board.moveSelectedPiece(position);
                    boardView.setHints(null);
                    boardView.postInvalidate();
                } catch (IOException e) {
                    onConnectionError();
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
        try {
            manager.send(Message.MOVE_UNDONE);
        } catch (IOException e) {
            onConnectionError();
        }
    }

    private void onServerResponse(final String response)
    {
        AlertDialog alertDialog = new AlertDialog.Builder(NetworkGameActivity.this).create();
        alertDialog.setTitle("Response");
        alertDialog.setMessage(response);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    private void onConnectionError()
    {
        AlertDialog alertDialog = new AlertDialog.Builder(NetworkGameActivity.this).create();
        alertDialog.setTitle("Error");
        alertDialog.setMessage("Connection error occurred");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                });
        alertDialog.show();
    }

    public class EnterNameActivity extends AppCompatActivity {

        private final static String NAME = "name";

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setContentView(R.layout.activity_enter_name);

            SharedPreferences preferences = getSharedPreferences(OptionsActivity.OPTIONS_FILE,
                    MODE_PRIVATE);

            String name = preferences.getString(NAME, getResources().getString(R.string.enter_name));
            ((EditText)findViewById(R.id.username)).setText(name);
        }

        public void connect(View view) {
            // save username
            SharedPreferences preferences = getSharedPreferences(OptionsActivity.OPTIONS_FILE,
                    MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(NAME, ((EditText)findViewById(R.id.username)).getText().toString());
            editor.apply();

            // TODO - AsyncTask (doInBackground - connect to server, onPostExecute - display players)
            //Thread thread = new Thread(new ConnectThread());
            //thread.start();
        }

        //TODO - better name
        public void showPlayers()
        {
            Intent listIntent = new Intent(this, PlayersListActivity.class);
            startActivity(listIntent);
        }
    }

    public class PlayersListActivity extends AppCompatActivity {

        private ArrayList<String> list = new ArrayList<>();
        private ArrayAdapter<String> adapter;
        private ListView listView;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setContentView(R.layout.activity_network_list);

            listView = (ListView) findViewById(R.id.players_list);
            adapter = new ArrayAdapter<>(NetworkGameActivity.this,
            R.layout.player_list_element, list);

            listView.setAdapter(adapter);

    //        list.addAll(manager.sendRequest(Message.GET_PLAYERS,
    //        NetworkManager.SERVER_TIMEOUT));
    //
    //        playersList.post(new Runnable() {
    //            @Override
    //            public void run() {
    //                adapter.notifyDataSetChanged();
    //            }
    //        });
        }

    }
}