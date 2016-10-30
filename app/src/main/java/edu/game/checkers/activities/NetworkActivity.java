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

    Handler handler = new Handler();

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

            networkService.connectToServer(new BasicServiceResponseHandler(),
                    new ServiceRequestHandler() {
                        @Override
                        public void onInvite(final String name) {
                            new AlertDialog(NetworkActivity.this).createQuestionDialog(name
                                            + "invited you, accept?",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            networkService.sendMessage(new Message(Message.OK));
                                            startGame(name);
                                            dialog.dismiss();
                                        }
                                    },
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            networkService.sendMessage(new Message(Message.NO));
                                            dialog.dismiss();
                                        }
                                    });
                        }

                        @Override
                        public void onConnectionError(String error) {
                            new AlertDialog(NetworkActivity.this).createErrorDialog(error);
                        }
                    });
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

        Bundle bundle = getIntent().getExtras();
        int options = bundle.getInt("options");

        networkService.makeRequest(new Message(Message.HI, name, Integer.toString(options)),
                new BasicServiceResponseHandler() {
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
                new BasicServiceResponseHandler() {
            @Override
            public void onServerResponse(Message response) {
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
                new BasicServiceResponseHandler() {
            @Override
            public void onServerResponse(Message response) {
                if(response.getCode().equals(Message.OK)){
                    startGame(name);
                }
                else if(response.getCode().equals(Message.NO)){
                    new AlertDialog(NetworkActivity.this).createInfoDialog("Player didn't accept your invite");
                }
                else{
                    new AlertDialog(NetworkActivity.this).createInfoDialog(response.getArguments().get(0));
                }
            }
        });
    }

    private void startGame(String name){
        Intent intent = new Intent(this, NetworkGameActivity.class);
        intent.putExtra("NAME", name);
        startActivity(intent);
    }

    private class BasicServiceResponseHandler implements ServiceResponseHandler{

        @Override
        public void onConnectionError(String error) {
            new AlertDialog(NetworkActivity.this).createErrorDialog(error);
        }

        @Override
        public void onServerResponse(Message response) {
            // Empty for cases when server response is not needed (initializing connection)
        }
    }
}