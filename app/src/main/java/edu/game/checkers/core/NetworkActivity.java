package edu.game.checkers.core;

import android.app.Dialog;
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
import android.os.Handler;
import java.util.ArrayList;

import edu.board.checkers.R;
import edu.game.checkers.core.callbacks.ConnectionCallback;
import edu.game.checkers.core.callbacks.ConnectionCreatedCallback;
import edu.game.checkers.core.callbacks.InviteCallback;
import edu.game.checkers.core.callbacks.PresenceCallback;
import edu.game.checkers.core.callbacks.RequestCallback;
import edu.game.checkers.core.callbacks.ResponseCallback;
import edu.game.checkers.core.callbacks.SubscriptionListener;

public class NetworkActivity extends AppCompatActivity {

    private ArrayList<Friend> currentFriendsList = new ArrayList<>();
    private ArrayList<Friend> friendsList = new ArrayList<>();
    private ArrayAdapter<Friend> adapter;
    private ListView listView;

    private PostAlertDialog dialog;

    // state of activity (has list been loaded and layout changed?)
    private boolean listLoaded = false;

    NetworkService networkService;
    boolean bound = false;
    volatile boolean active = false;

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
    protected ServiceConnection connection = new ServiceConnection() {

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

    @Override
    public void onBackPressed() {
        if(listLoaded){
            EditText editText = (EditText) findViewById(R.id.search);
            final String username = editText.getText().toString();

            if(!username.isEmpty()){
                editText.setText("");
            }
            else {
                dialog.createQuestionDialog("Exit", "Do you want to exit?",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(which == Dialog.BUTTON_POSITIVE){
                                    NetworkActivity.super.onBackPressed();
                                }
                                else{
                                    dialog.dismiss();
                                }
                            }
                        });
            }
        }
        else
            super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        dialog = new PostAlertDialog(this, new Handler());
        setContentView(R.layout.activity_enter_name);
        initName();
    }

    private void initName()
    {
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_enter_name);

        SharedPreferences preferences = getSharedPreferences(OptionsActivity.OPTIONS_FILE,
                MODE_PRIVATE);

        String name = preferences.getString("name", getResources().getString(R.string.enter_name));
        if(!name.equals(getResources().getString(R.string.enter_username)))
            ((EditText) findViewById(R.id.username)).setText(name);

        String password = preferences.getString("password", getResources().getString(R.string.enter_password));
        if(!password.equals(getResources().getString(R.string.enter_password)))
            ((EditText) findViewById(R.id.password)).setText(password);
    }

    public void connect(View view) {
        if(!bound){
            dialog.createInfoDialog(getString(R.string.error), "Service not bounded");
            return;
        }

        // save username and password
        SharedPreferences preferences = getSharedPreferences(OptionsActivity.OPTIONS_FILE,
                MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        String name = ((EditText) findViewById(R.id.username)).getText().toString();
        editor.putString("name",  name);
        String password = ((EditText) findViewById(R.id.password)).getText().toString();
        editor.putString("password", password);
        editor.apply();

        networkService.connectToServer(name, password, new ConnectionCallback() {
            @Override
            public void onConnectionError(String error) {
                dialog.createInfoDialog(getString(R.string.error), error);
            }

            @Override
            public void onSuccess() {
                initList();
            }
        });

        networkService.setConnectionCreatedCallback(new ConnectionCreatedCallback() {
            @Override
            public void onConnectionCreated(CommunicationManager manager) {
                manager.acceptConnection(new ServerRequestCallback(manager));
            }
        });
    }

    private void initList() {

        listLoaded = true;

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
            public void onSubscriptionChange() {
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

                dialog.createQuestionDialog(friendsList.get(position).username, "Remove from list?",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if(which == Dialog.BUTTON_POSITIVE) {
                                            networkService.unsubscribeUser(friendsList.get(position).username);
                                            friendsList.remove(position);
                                            showUsers(findViewById(R.id.search));
                                        }
                                        else{
                                            dialog.dismiss();
                                        }
                                    }
                                });

                return true;
            }
        });

        adapter.notifyDataSetChanged();
    }

    public void invitePlayer(View view) {
        final LinearLayout element = (LinearLayout) view;
        TextView textView = (TextView) element.getChildAt(0);
        String name = textView.getText().toString();

        // disallow sending multiple invitations
        element.setEnabled(false);

        final CommunicationManager manager = networkService.startConnectionTo(name);
        manager.acceptConnection(new ServerRequestCallback(manager));

        manager.sendRequest("invite", new ResponseCallback() {
            @Override
            public void onResponse(String response) {
                element.post(new Runnable() {
                    @Override
                    public void run() {
                        element.setEnabled(true);
                    }
                });
                if(response.equals("ok")){
                    SharedPreferences optPreferences = getSharedPreferences(OptionsActivity.OPTIONS_FILE,
                            MODE_PRIVATE);
                    int options = optPreferences.getInt(OptionsActivity.OPTIONS_KEY, 0);
                    startGame(options, manager);
                }
                else if(response.equals("no")){
                    dialog.createInfoDialog("Response",
                            manager.getOtherName() + " rejected your invitation");
                }
                else{
                    // Error
                }
            }
        });
    }

    private void startGame(final int options, CommunicationManager manager){
        networkService.saveCommunicationManager(manager);
        Intent intent = new Intent(NetworkActivity.this, NetworkGameActivity.class);
        intent.putExtra("options", options);
        intent.putExtra("name", manager.getOtherName());

        startActivity(intent);
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

                    networkService.subscribeUser(username, new InviteCallback() {
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
                if(!friend.accepted){
                    name.setTextColor(Color.GRAY);
                    status.setTextColor(Color.GRAY);
                }
                else {
                    name.setTextColor(Color.BLACK);
                    status.setTextColor(Color.BLACK);
                }

                if(friend.status.equals("available"))
                    name.setBackgroundColor(Color.GREEN);
                else
                    name.setBackgroundColor(Color.RED);

                status.setText(friend.info);
            }
            return convertView;
        }
    }

    private class ServerRequestCallback implements RequestCallback {

        CommunicationManager manager;
        ServerRequestCallback(CommunicationManager manager){
            this.manager = manager;
        }

        @Override
        public void onRequest(final String requestId, String request) {

            final DialogInterface.OnClickListener invitationAccept =
                    new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(which == Dialog.BUTTON_POSITIVE) {
                        manager.sendResponse(requestId, "yes");

                        Friend otherPlayer = null;
                        for(Friend friend : friendsList){
                            if(friend.username.equals(manager.getOtherName()))
                                otherPlayer = friend;
                        }

                        if(otherPlayer == null)
                            NetworkActivity.this.dialog.createInfoDialog("Error", "User nto found");
                        else
                            startGame(Integer.valueOf(otherPlayer.info), manager);
                    }
                    else{
                        manager.sendResponse(requestId, "no");
                    }
                }
            };

            if(request.equals("invitation")){
                dialog.createQuestionDialog("Invitation",
                        "Accept invitation from: " + manager.getOtherName() + " ?", invitationAccept);
            }
        }
    }
}