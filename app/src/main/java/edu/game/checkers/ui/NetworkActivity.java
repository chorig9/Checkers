package edu.game.checkers.ui;

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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.os.Handler;

import java.util.ArrayList;
import java.util.Collection;

import edu.board.checkers.R;
import edu.game.checkers.utils.Callback0;
import edu.game.checkers.utils.Callback1;
import edu.game.checkers.utils.ConnectionCallback;

import static edu.game.checkers.ui.OptionsActivity.OPTIONS_KEY;

public class NetworkActivity extends AppCompatActivity {

    private UserCollection userCollection;
    private ArrayList<UserInfo> currentlyDisplayedUsers = new ArrayList<>();
    private UserCollectionAdapter adapter;
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
                super.onBackPressed();
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
        String password = ((EditText) findViewById(R.id.password)).getText().toString();

        editor.putString("name",  name);
        editor.putString("password", password);
        editor.apply();


        networkService.connectToServer(name, password, new ConnectionCallback() {
            @Override
            public void onConnectionError(String error) {
                dialog.createInfoDialog(getString(R.string.error), error);
            }

            @Override
            public void onSuccess() {
                onConnectionSuccess();
            }
        });

        addGameInviteCallback();
    }

    private void addGameInviteCallback(){
        networkService.setGameInviteCallback(new Callback1<String>() {

            @Override
            public void onAction(final String user) {

                dialog.createQuestionDialog("Invitation", "Accept invitation from: " + user + " ?",
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(which == Dialog.BUTTON_POSITIVE) {
                                    acceptedGameInvite(user);
                                } else{
                                    rejectedGameInvite(user);
                                }
                            }
                        });
            }
        });
    }


    private void acceptedGameInvite(String user){
        UserInfo otherPlayer = findUserInfoByName(user);

        if(otherPlayer == null) {
            NetworkActivity.this.dialog.createInfoDialog("Error", "Invitation from unknown user");
        }
        else {
            networkService.sendGameInvitationResponse(user, "yes");

            // take other's player options, other player is initializing connection
            int options = Integer.decode(otherPlayer.status);
            startGame(options, user, false);
        }
    }

    private void rejectedGameInvite(String user){
        // TODO
    }

    private void onConnectionSuccess(){
        SharedPreferences preferences = getSharedPreferences(OptionsActivity.OPTIONS_FILE,
                MODE_PRIVATE);
        int options = preferences.getInt(OPTIONS_KEY, -1);

        networkService.setStatus(Integer.toString(options));
        userCollection = networkService.getUserCollection();
        userCollection.setListener(new Callback0() {
            @Override
            public void onAction() {
                postShowUsers();
            }
        });
        initList();
    }

    private UserInfo findUserInfoByName(String username){
        UserInfo userInfo = null;
        for(UserInfo u : userCollection){
            if(u.username.equals(username))
                userInfo = u;
        }

        return userInfo;
    }

    private Collection<UserInfo> getUsersMatchingUsername(String username){
        Collection<UserInfo> users = new ArrayList<>();

        for(UserInfo u : userCollection){
            if(u.username.startsWith(username))
                users.add(u);
        }

        return users;
    }

    private void initList() {
        listLoaded = true;
        setContentView(R.layout.activity_network_list);

        listView = (ListView) findViewById(R.id.players_list);
        adapter = new UserCollectionAdapter(this, R.layout.player_list_element, currentlyDisplayedUsers);
        listView.setAdapter(adapter);
        postShowUsers();

        addTextChangeListener();
        addRemoveItemListener();
    }

    private void addTextChangeListener(){
        final EditText text = (EditText) findViewById(R.id.search);
        text.addTextChangedListener(new TextWatcher() {

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                postShowUsers();
            }
        });
    }

    private void addRemoveItemListener(){
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, final View view,
                                           final int position, long id) {

                final String username = currentlyDisplayedUsers.get(position).username;

                dialog.createQuestionDialog(username, "Remove from list?",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(which == Dialog.BUTTON_POSITIVE) {
                                    networkService.unsubscribeUser(username);
                                    postShowUsers();
                                }
                                else{
                                    dialog.dismiss();
                                }
                            }
                        });

                return true;
            }
        });
    }

    private void postShowUsers(){
        listView.post(new Runnable() {
            @Override
            public void run() {
                showUsers(findViewById(R.id.search));
            }
        });
    }

    public void invitePlayer(View view) {
        final RelativeLayout element = (RelativeLayout) view;
        TextView nameView = (TextView) element.getChildAt(0);
        final String name = nameView.getText().toString();

        if(element.getTag() == null || element.getTag().equals("enabled")){
            element.setTag("disabled");
            networkService.sendGameInvitation(name, new Callback1<Boolean>() {
                @Override
                public void onAction(Boolean accepted) {

                    listView.post(new Runnable() {
                        @Override
                        public void run() {
                            element.setTag("enabled");
                        }
                    });

                    SharedPreferences preferences = getSharedPreferences(OptionsActivity.OPTIONS_FILE,
                            MODE_PRIVATE);
                    int options = preferences.getInt("options", -1);
                    if(accepted){
                        // this host is initializing connection
                        startGame(options, name, true);
                    }
                }
            });
        }
    }

    private void startGame(int options, String username, boolean locallyInitialized){
        Intent intent = new Intent(NetworkActivity.this, NetworkGameActivity.class);
        intent.putExtra("options", options);
        intent.putExtra("name", username);
        intent.putExtra("locallyInitialized", locallyInitialized);

        startActivity(intent);
    }

    public void showUsers(final View view){
        LinearLayout inviteLayout = (LinearLayout) findViewById(R.id.invite_space);
        inviteLayout.removeAllViews();

        final String searchedUsername = ((EditText) findViewById(R.id.search)).getText().toString();

        currentlyDisplayedUsers.clear();
        currentlyDisplayedUsers.addAll(getUsersMatchingUsername(searchedUsername));

        if(currentlyDisplayedUsers.size() != 0){
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
                    networkService.subscribeUser(searchedUsername);
                }
            });

            inviteLayout.addView(button);
        }
    }

    private class UserCollectionAdapter extends ArrayAdapter<UserInfo> {

        UserCollectionAdapter(Context context, int resource, ArrayList<UserInfo> list) {
            super(context, resource, list);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            // Get the data item for this position
            UserInfo user = getItem(position);

            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(R.layout.player_list_element, parent, false);
            }
            TextView name = (TextView) convertView.findViewById(R.id.friend_name);
            TextView status = (TextView) convertView.findViewById(R.id.friend_status);
            TextView invited = (TextView) convertView.findViewById(R.id.friend_invitation);

            if(user != null){

                name.setText(user.username);
                status.setText(user.status);

                setAvailability(convertView, user.presence);
                setInviteStatus(invited, user.invitedToGame);
                setBothWaySubscriptionStatus(name, user.bothWaySubscription);
            }

            return convertView;
        }

        private void setBothWaySubscriptionStatus(TextView view, boolean bothWaySubscription){
            if(!bothWaySubscription){
                view.setTextColor(Color.GRAY);
            }
            else {
                view.setTextColor(Color.BLACK);
            }
        }

        private void setInviteStatus(TextView view, boolean invited){
            if(invited){
                view.setText("Invited");
            }
            else{
                view.setText("");
            }
        }

        private void setAvailability(View view, String presence){
            if(presence.equals("available")){
                view.setTag("enabled");
                view.setBackgroundColor(Color.GREEN);
            }
            else{
                view.setTag("disabled");
                view.setBackgroundColor(Color.RED);
            }
        }
    }


}