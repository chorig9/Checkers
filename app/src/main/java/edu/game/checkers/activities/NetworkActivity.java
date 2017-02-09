package edu.game.checkers.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import edu.board.checkers.R;

public class NetworkActivity extends AppCompatActivity {

    private final static String NAME = "name";
    private final static String PASSWORD = "password";

    private NetworkService networkService;
    private boolean bound = false;

    private ArrayList<Friend> currentFriendsList = new ArrayList<>();
    private ArrayList<Friend> friendsList = new ArrayList<>();
    private ArrayAdapter<Friend> adapter;
    private ListView listView;

    private volatile boolean active = false;

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

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to NetworkService, cast the IBinder and get NetworkService instance
            NetworkService.NetworkBinder binder = (NetworkService.NetworkBinder) service;
            networkService = binder.getService();
            bound = true;
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
        if(!name.equals(getResources().getString(R.string.enter_username)))
            ((EditText) findViewById(R.id.username)).setText(name);

        String password = preferences.getString(PASSWORD, getResources().getString(R.string.enter_password));
        if(!password.equals(getResources().getString(R.string.enter_password)))
            ((EditText) findViewById(R.id.password)).setText(password);
    }

    public void connect(View view) {
        // save username and password
        SharedPreferences preferences = getSharedPreferences(OptionsActivity.OPTIONS_FILE,
                MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        String name = ((EditText) findViewById(R.id.username)).getText().toString();
        editor.putString(NAME,  name);
        String password = ((EditText) findViewById(R.id.password)).getText().toString();
        editor.putString(PASSWORD, password);
        editor.apply();

        SharedPreferences optPreferences = getSharedPreferences(OptionsActivity.OPTIONS_FILE,
                MODE_PRIVATE);
        int options = optPreferences.getInt(OptionsActivity.OPTIONS_KEY, 0);

        networkService.connectToServer(name, password, new ConnectionCallback() {
            @Override
            public void onConnectionError(String error) {
                new AlertDialog(NetworkActivity.this).createInfoDialog("Error", error);
            }

            @Override
            public void onSuccess() {
                initList();
            }
        });
    }

    private void initList() {

        networkService.setPresenceListener(new PresenceCallback() {
            @Override
            public void onPresenceChanged(String username, String presence){
                Friend friend = null;
                for(Friend element : friendsList){
                    if(element.username.equals(username))
                        friend = element;
                }

                if(friend != null){
                    friend.status = presence;

                    if(currentFriendsList.contains(friend)) {
                        listView.post(new Runnable() {
                            @Override
                            public void run() {
                                adapter.notifyDataSetChanged();
                            }
                        });
                    }
                }
            }
        });

        networkService.setSubscriptionListener(new SubscriptionListener() {
            @Override
            public void onSubscribtionChange() {
                friendsList.clear();
                friendsList.addAll(networkService.getFriendsList());

                // post is used because showUsers will be called from outside NetworkActivity
                listView.post(new Runnable() {
                    @Override
                    public void run() {
                        showUsers(findViewById(R.id.search));
                    }
                });

            }
        });

        setContentView(R.layout.activity_network_list);

        listView = (ListView) findViewById(R.id.players_list);
        adapter = new FriendListAdapter(this, R.layout.player_list_element, currentFriendsList);

        listView.setAdapter(adapter);

        friendsList.addAll(networkService.getFriendsList());
        currentFriendsList.addAll(friendsList);

        final EditText text = (EditText) findViewById(R.id.search);
        text.addTextChangedListener(new TextWatcher() {

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                showUsers(findViewById(R.id.search));
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, final View view,
                                           final int position, long id) {

                new AlertDialog(NetworkActivity.this).
                        createQuestionDialog(friendsList.get(position).username, "Remove from list?",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        networkService.unsubscribeUser(friendsList.get(position).username);
                                        friendsList.remove(position);
                                        showUsers(findViewById(R.id.search));
                                    }
                                });

                return true;
            }
        });

        adapter.notifyDataSetChanged();
    }

    public void invitePlayer(View view) {
//        TextView textView = (TextView) view;
//        final String[] player = textView.getText().toString().split(Message.EXTRA_SEPARATOR);
//        final String name = player[0];
//        final int options = Integer.decode(player[1]);
//
//        final SharedPreferences optPreferences = getSharedPreferences(OptionsActivity.OPTIONS_FILE,
//                MODE_PRIVATE);
//        int myOptions = optPreferences.getInt(OptionsActivity.OPTIONS_KEY, 0);
//
//        if(options != myOptions){
//            new AlertDialog(this)
//                    .createQuestionDialog("If you continue your options will be temporarily changed, proceed?",
//                            new DialogInterface.OnClickListener() { // yes
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    initializeGame(name, options);
//                                }
//                            }, new DialogInterface.OnClickListener() { // no
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    // nothing
//                                }
//                            });
//        }
//        else{
//            initializeGame(name, options);
//        }
    }

    private void initializeGame(final String name, final int options) {
//        networkService.sendRequest(new Message(Message.INVITE, name),
//                NetworkService.USER_TIMEOUT, new ServerResponseHandler() {
//                    @Override
//                    public void onServerResponse(final Message response) {
//                        if(response.getCode().equals(Message.OK)){
//                            startGame(name, options);
//                        }
//                        else if(response.getCode().equals(Message.NO)){
//                            new AlertDialog(NetworkActivity.this).
//                                    createInfoDialog("Player rejected your invite");
//                        }
//                        else{
//                            new AlertDialog(NetworkActivity.this).
//                                    createInfoDialog(response.getArguments().get(0));
//                        }
//                    }});
    }

    private void startGame(final String name, final int options){
//        networkService.sendRequest(new Message(Message.START_GAME), NetworkService.USER_TIMEOUT,
//                new ServerResponseHandler(){
//            @Override
//            public void onServerResponse(Message response) {
//                Intent intent = new Intent(NetworkActivity.this, NetworkGameActivity.class);
//                intent.putExtra("NAME", name);
//                intent.putExtra("options", options);
//                startActivity(intent);
//            }
//        });
    }

    public void showUsers(final View view){
        LinearLayout inviteLayout = (LinearLayout) findViewById(R.id.invite_space);
        inviteLayout.removeAllViews();
        final String username = ((EditText) findViewById(R.id.search)).getText().toString();

        currentFriendsList.clear();
        for(Friend friend : friendsList){
            if(friend.username.contains(username))
                currentFriendsList.add(friend);
        }

        if(currentFriendsList.size() != 0){
            adapter.notifyDataSetChanged();
        }
        else { // no user is displayed - show invite button
            Button button = new Button(this);
            String invite = "Click to invite";
            button.setGravity(Gravity.CENTER);
            button.setText(invite);
            button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 23);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Friend friend = new Friend(username, "unknown");
                    friend.accepted = false;
                    friendsList.add(friend);

                    networkService.inviteUser(username, new InviteCallback() {
                        @Override
                        public void onInvitedResponse(boolean accepted) {
                            if(accepted)
                                friend.accepted = true;
                            else
                                friendsList.remove(friend);
                            showUsers(view);
                        }
                    });
                    showUsers(view);
                }
            });

            inviteLayout.addView(button);
        }
    }

    private class FriendListAdapter extends ArrayAdapter<Friend> {

        public FriendListAdapter(Context context, int resource, ArrayList<Friend> list) {
            super(context, resource, list);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            // Get the data item for this position
            Friend friend = getItem(position);

            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(R.layout.player_list_element, parent, false);
            }
            TextView name = (TextView) convertView.findViewById(R.id.friend_name);
            TextView status = (TextView) convertView.findViewById(R.id.friend_status);

            if(friend != null){
                name.setText(friend.username);
                status.setText(friend.status);
                if(!friend.accepted){
                    name.setTextColor(Color.GRAY);
                    status.setTextColor(Color.GRAY);
                }
                else {
                    name.setTextColor(Color.BLACK);
                    status.setTextColor(Color.BLACK);
                }
            }

            return convertView;
        }

    }

//    //responsible for handling server requests and connection errors
//    private class ServerRequestHandlerAdapter implements ServerRequestHandler {
//
//        @Override
//        public void onServerRequest(final Message msg) {
//            if(active) {
//                switch (msg.getCode()) {
//                    case Message.INVITE:
//                        new AlertDialog(NetworkActivity.this).createQuestionDialog(
//                                msg.getArguments().get(0) + " invited you, accept?",
//                                new DialogInterface.OnClickListener() { // 'ok' button
//                                    @Override
//                                    public void onClick(DialogInterface dialog, int which) {
//                                        networkService.sendResponse(new Message(Message.OK));
//                                        SharedPreferences optPreferences = getSharedPreferences(OptionsActivity.OPTIONS_FILE,
//                                                MODE_PRIVATE);
//                                        int options = optPreferences.getInt(OptionsActivity.OPTIONS_KEY, 0);
//                                        startGame(msg.getArguments().get(0), options);
//                                        dialog.dismiss();
//                                    }
//                                },
//                                new DialogInterface.OnClickListener() { // 'no' button
//                                    @Override
//                                    public void onClick(DialogInterface dialog, int which) {
//                                        networkService.sendResponse(new Message(Message.NO));
//                                        dialog.dismiss();
//                                    }
//                                });
//                        break;
//                    case Message.UPDATE_PLAYERS:
//                        if (msg.getArguments().get(0).equals("-"))   // delete player from list
//                            list.remove(msg.getArguments().get(1));
//                        else
//                            list.add(msg.getArguments().get(1));    // add player to list
//                        adapter.notifyDataSetChanged();
//                        break;
//                }
//            }
//        }
//
//        @Override
//        public void onConnectionError(String error) {
//            if(active)
//                new AlertDialog(NetworkActivity.this).createExitDialog("Error", error);
//        }
//    }

}