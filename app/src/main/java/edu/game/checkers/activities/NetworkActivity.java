package edu.game.checkers.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import edu.board.checkers.R;

public class NetworkActivity extends AppCompatActivity {

    private final static String NAME = "name";

    private NetworkService networkService;
    private boolean bound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_enter_name);

        initName();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to NetworkService
        Intent intent = new Intent(this, NetworkService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (bound)
        unbindService(connection);
        bound = false;
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

            networkService.connectToServer(new ServerRequestHandlerAdapter());
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };

    private void initName()
    {
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_enter_name);

        SharedPreferences preferences = getSharedPreferences(OptionsActivity.OPTIONS_FILE,
                MODE_PRIVATE);

        String name = preferences.getString(NAME, getResources().getString(R.string.enter_name));
        ((EditText) findViewById(R.id.username)).setText(name);
    }

    public void sendName(View view) {
        // save username
        SharedPreferences preferences = getSharedPreferences(OptionsActivity.OPTIONS_FILE,
                MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        String name = ((EditText) findViewById(R.id.username)).getText().toString();
        editor.putString(NAME,  name);
        editor.apply();

        SharedPreferences optPreferences = getSharedPreferences(OptionsActivity.OPTIONS_FILE,
                MODE_PRIVATE);

        int options = optPreferences.getInt("options", 0);

        networkService.makeRequest(new Message(Message.HI, name, Integer.toString(options)),
                NetworkService.SERVER_TIMEOUT , new ServerResponseHandler() {
            @Override
            public void onServerResponse(final Message response) {
                if(response.getCode().equals(Message.OK))
                    displayPlayersList();
                else
                    new AlertDialog(NetworkActivity.this).createInfoDialog(response.getArguments().get(0));
            }
        });
    }

    private void displayPlayersList() {

        setContentView(R.layout.activity_network_list);

        final ArrayList<String> list = new ArrayList<>();
        final ArrayAdapter<String> adapter;
        final ListView listView;

        listView = (ListView) findViewById(R.id.players_list);
        adapter = new ArrayAdapter<>(this, R.layout.player_list_element, list);

        listView.setAdapter(adapter);

        networkService.makeRequest(new Message(Message.GET_PLAYERS),
                NetworkService.SERVER_TIMEOUT, new ServerResponseHandler() {
                    @Override
                    public void onServerResponse(final Message response) {
                        if(response.getCode().equals(Message.GET_PLAYERS)){
                            list.clear();
                            list.addAll(response.getArguments());
                            adapter.notifyDataSetChanged();
                        }
                    }
                });
    }

    public void invitePlayer(View view) {
        TextView textView = (TextView) view;
        final String name = textView.getText().toString();

        networkService.makeRequest(new Message(Message.INVITE, name),
                NetworkService.USER_TIMEOUT, new ServerResponseHandler() {
            @Override
            public void onServerResponse(final Message response) {
                if(response.getCode().equals(Message.OK)){
                    startGame(name);
                }
                else if(response.getCode().equals(Message.NO)){
                    new AlertDialog(NetworkActivity.this).
                            createInfoDialog("Player didn't accept your invite");
                }
                else{
                    new AlertDialog(NetworkActivity.this).
                            createInfoDialog(response.getArguments().get(0));
                }
            }});
    }

    private void startGame(final String name){
        networkService.makeRequest(new Message(Message.START_GAME), NetworkService.USER_TIMEOUT,
                new ServerResponseHandler(){
            @Override
            public void onServerResponse(Message response) {
                Intent intent = new Intent(NetworkActivity.this, NetworkGameActivity.class);
                intent.putExtra("NAME", name);
                startActivity(intent);
            }
        });
    }

    private class ServerRequestHandlerAdapter implements ServerRequestHandler {

        @Override
        public void onServerRequest(final Message msg) {
            switch (msg.getCode()){
                case Message.INVITE:
                    new AlertDialog(NetworkActivity.this).createQuestionDialog(
                            msg.getArguments().get(0) + " invited you, accept?",
                            new DialogInterface.OnClickListener() { // 'ok' button
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    networkService.sendResponse(new Message(Message.OK));
                                    startGame(msg.getArguments().get(0));
                                    dialog.dismiss();
                                }
                            },
                            new DialogInterface.OnClickListener() { // 'no' button
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    networkService.sendResponse(new Message(Message.NO));
                                    dialog.dismiss();
                                }
                            });
                    break;
            }
        }

        @Override
        public void onConnectionError(String error) {
            new AlertDialog(NetworkActivity.this).createErrorDialog(error);
        }
    }

}