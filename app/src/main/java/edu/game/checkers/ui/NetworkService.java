package edu.game.checkers.ui;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PresenceTypeFilter;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterListener;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.game.checkers.utils.Callback0;
import edu.game.checkers.utils.Callback1;
import edu.game.checkers.utils.ConnectionCallback;

import static edu.game.checkers.ui.OptionsActivity.DEFAULT_HOSTNAME;
import static edu.game.checkers.ui.OptionsActivity.DEFAULT_IP;
import static edu.game.checkers.ui.OptionsActivity.DEFAULT_PORT;
import static edu.game.checkers.ui.OptionsActivity.HOSTNAME_KEY;
import static edu.game.checkers.ui.OptionsActivity.IP_ADDRESS_KEY;
import static edu.game.checkers.ui.OptionsActivity.OPTIONS_FILE;
import static edu.game.checkers.ui.OptionsActivity.PORT_KEY;


public class NetworkService extends Service {

    private final IBinder binder = new NetworkBinder();

    private XMPP xmpp;
    private ConnectionCallback connectionCallback = null;
    private Callback1<String> gameInviteCallback = null;
    private Collection<UserInfo> users = new ArrayList<>();

    private String hostname, ipAddress;
    private int port;

    public class NetworkBinder extends Binder {
        NetworkService getService() {
            // Return this instance of NetworkService so clients can call public methods
            return NetworkService.this;
        }
    }

    @Override
    public void onDestroy() {
        new EndConnectionTask().execute();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void connectToServer(String username, String password, ConnectionCallback callback)
    {
        loadConfig();
        this.connectionCallback = callback;
        new ConnectToServerTask(callback).execute(username, password);
    }

    public void subscribeUser(final String username){
        try{
            xmpp.sendSubscribeStanza(toJid(username));
        }
        catch(SmackException.NotConnectedException e){
            this.connectionCallback.onConnectionError(e.getMessage());
        }
    }

    public void unsubscribeUser(String username){
        try {
            RosterEntry entry = xmpp.roster.getEntry(toJid(username));
            xmpp.roster.removeEntry(entry);
        } catch(Exception e){
            connectionCallback.onConnectionError(e.getMessage());
        }
    }

    public CommunicationManager getCommunicationManager(String to){
        return new CommunicationManager(toJid(to), xmpp.conn, connectionCallback);
    }

    public Collection<UserInfo> getUsers() {
//        try{
//            // reload list
//            xmpp.roster.reloadAndWait();
//
//            Collection<RosterEntry> entries = xmpp.roster.getEntries();
//            Collection<UserInfo> users = new ArrayList<>();
//            for(RosterEntry entry : entries){
//                Presence presence = xmpp.roster.getPresence(entry.getUser());
//
//
//                System.out.println(presence.getMode().toString());
//
//
//                UserInfo userInfo = createUserInfo(presence);
//                userInfo.bothWaySubscription.set(xmpp.roster.isSubscribedToMyPresence(entry.getUser()));
//                users.add(userInfo);
//            }
//
//            return users;
//        } catch(Exception e){
//            connectionCallback.onConnectionError(e.getMessage());
//            return null;
//        }

        return users;
    }

    public void setStatus(String status){
        try {
            Presence presence = new Presence(Presence.Type.available, status,
                    42, Presence.Mode.available);

            xmpp.conn.sendStanza(presence);
        } catch (SmackException.NotConnectedException e) {
            connectionCallback.onConnectionError(e.getMessage());
        }
    }

    public void setPresenceListener(final Callback1<UserInfo> listener){

    }

    private UserInfo createUserInfo(Presence from){
        String username = toUsername(from.getFrom());
        String status = from.getStatus();
        String presenceName = from.getType().name();

        return new UserInfo(username, status, presenceName);
    }

    public void setUsersCollectionListener(final Callback0 listener){
        xmpp.usersCollectionChanged = listener;
    }

    public void setGameInviteCallback(Callback1<String> gameInviteCallback){
        this.gameInviteCallback = gameInviteCallback;
    }

    public void sendGameInvitation(final String user, final Callback1<Boolean> responseCallback){
        try{
            Message stanza = new Message();
            stanza.setTo(toJid(user));

            JSONObject json = new JSONObject();
            json.put("type", "request");
            json.put("body", "invite");

            stanza.setBody(json.toString());
            xmpp.conn.sendStanza(stanza);

            addGameInvitationResponseCallback(user, responseCallback);
        }
        catch (JSONException e){
            e.printStackTrace();
        }
        catch (SmackException.NotConnectedException e){
            connectionCallback.onConnectionError(e.getMessage());
        }
    }

    public void sendGameInvitationResponse(String user, String response){
        try{
            Message stanza = new Message();
            stanza.setTo(toJid(user));

            JSONObject json = new JSONObject();
            json.put("type", "response");
            json.put("body", response);

            stanza.setBody(json.toString());
            xmpp.conn.sendStanza(stanza);
        }
        catch (JSONException e){
            //TODO
        }
        catch (SmackException.NotConnectedException e){
            connectionCallback.onConnectionError(e.getMessage());
        }
    }

    private void addGameInvitationResponseCallback(
            final String user,
            final Callback1<Boolean> responseCallback)
    {
        xmpp.conn.addAsyncStanzaListener(new StanzaListener() {
            @Override
            public void processPacket(Stanza packet) throws SmackException.NotConnectedException {
                Message message = (Message) packet;

                if(toUsername(message.getFrom()).equals(user)) {
                    try {
                        JSONObject json = new JSONObject(message.getBody());

                        String type = json.get("type").toString();
                        String body = json.get("body").toString();

                        if (type.equals("response")) {
                            responseCallback.onAction(body.equals("yes"));
                            xmpp.conn.removeAsyncStanzaListener(this);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, StanzaTypeFilter.MESSAGE );
    }

    private String toUsername(String jid){
        int index = jid.indexOf(hostname);
        return jid.substring(0, index - 1);
    }

    private String toJid(String username) {
        return username + "@" + hostname;
    }

    private void loadConfig(){
        SharedPreferences preferences = getSharedPreferences(OPTIONS_FILE, MODE_PRIVATE);
        hostname = preferences.getString(HOSTNAME_KEY, DEFAULT_HOSTNAME);
        ipAddress = preferences.getString(IP_ADDRESS_KEY, DEFAULT_IP);
        port = preferences.getInt(PORT_KEY, DEFAULT_PORT);
    }

    /*
    * params - {username, password}
     */
    private class ConnectToServerTask extends AsyncTask<String, Void, Void> {

        XMPP handler;
        ConnectionCallback callback;
        Exception exception = null;

        ConnectToServerTask(ConnectionCallback callback){
            this.callback = callback;
        }

        @Override
        protected Void doInBackground(String... params){
            try {
                handler = new XMPP(params[0], params[1]);
            } catch (Exception e) {
                exception = e;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(exception != null)
                callback.onConnectionError(exception.getMessage());
            else {
                xmpp = handler;
                callback.onSuccess();
            }
        }
    }

    private class EndConnectionTask extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... params) {
            try {
                //TODO
                throw new IOException();
            }
            catch(IOException e) {
                Logger.getAnonymousLogger().log(Level.FINE, "closing socket error");
            }
            return null;
        }
    }

    private UserInfo findUserByJid(String jid){
        String username = toUsername(jid);
        return findUserByName(username);
    }

    private UserInfo findUserByName(String username){
        for(UserInfo userInfo : users){
            if(userInfo.username.equals(username))
                return userInfo;
        }
        return null;
    }

    private class XMPP {

        private AbstractXMPPConnection conn;
        private Roster roster;
        private Callback0 usersCollectionChanged = null;
        private Collection<String> subscribingUsers = new ArrayList<>();

        XMPP(final String username, String password)
                throws IOException, XMPPException, SmackException {

            XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
                    .setUsernameAndPassword(username, password)
                    .setHost(ipAddress)
                    .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                    .setServiceName(hostname)
                    .setPort(port)
                    .build();

            conn = new XMPPTCPConnection(config);

            conn.connect();
            conn.login();
            if(conn.isAuthenticated()) {
//                initSubscribeListener();
//                initUnSubscribeListener();
//                initSubscribedListener();
                initInvitationListener();

                    initRosterListener();
            }
        }

        private void initSubscribeListener(){
            roster = Roster.getInstanceFor(conn);
            roster.setSubscriptionMode(Roster.SubscriptionMode.manual);

            // Accepts all subscribe requests and also send subscribe request back
            conn.addAsyncStanzaListener(
                    new StanzaListener() {
                        @Override
                        public void processPacket(Stanza packet) throws SmackException.NotConnectedException {
                            String sender = packet.getFrom();

                            sendSubscribeResponseStanza(sender);
                            UserInfo user = findUserByJid(sender);

                            if(user == null) {
                                subscribingUsers.add(sender);
                                sendSubscribeStanza(sender);
                            }
                            else{
                                user.bothWaySubscription = true;
                                usersCollectionChanged.onAction();
                            }
                        }
                    },
                    PresenceTypeFilter.SUBSCRIBE);
        }

        private void sendSubscribeResponseStanza(String to) throws SmackException.NotConnectedException {
            Presence subscribed = new Presence(Presence.Type.subscribed);
            subscribed.setTo(to);
            conn.sendStanza(subscribed);
        }

        private void sendSubscribeStanza(String to) throws SmackException.NotConnectedException {
            Presence subscribe = new Presence(Presence.Type.subscribe);
            subscribe.setTo(to);
            conn.sendStanza(subscribe);
        }

        private void initUnSubscribeListener(){
            conn.addAsyncStanzaListener(new StanzaListener() {
                @Override
                public void processPacket(Stanza packet) throws SmackException.NotConnectedException {
                    String sender = packet.getFrom();
                    UserInfo user = findUserByJid(sender);

                    if(user != null) {
                        users.remove(findUserByJid(sender));
                        if(usersCollectionChanged != null)
                            usersCollectionChanged.onAction();

                        removeUserFromRoster(sender);
                    }
                }
            }, PresenceTypeFilter.UNSUBSCRIBE);
        }

        private void removeUserFromRoster(String jid){
            try {
                if(!roster.isLoaded()){
                    roster.reloadAndWait();
                }
                RosterEntry entry = roster.getEntry(jid);
                if(entry != null)
                    roster.removeEntry(entry);
            } catch (InterruptedException e){
                // TODO
            } catch (Exception e) {
                connectionCallback.onConnectionError(e.getMessage());
            }
        }

        private void initSubscribedListener(){
            conn.addAsyncStanzaListener(new StanzaListener() {
                @Override
                public void processPacket(Stanza packet) throws SmackException.NotConnectedException {
                    String sender = packet.getFrom();
                    Presence presence = (Presence) packet;
                    UserInfo userInfo = createUserInfo(presence);

                    if(subscribingUsers.contains(sender)) {
                        userInfo.bothWaySubscription = true;
                        subscribingUsers.remove(sender);
                    }

                    users.add(userInfo);

                    if(usersCollectionChanged != null)
                        usersCollectionChanged.onAction();
                }
            }, PresenceTypeFilter.SUBSCRIBED);
        }

        private void initInvitationListener(){
            conn.addAsyncStanzaListener(new StanzaListener() {
                @Override
                public void processPacket(Stanza packet) throws SmackException.NotConnectedException {
                    Message message = (Message) packet;

                    try{
                        JSONObject json = new JSONObject(message.getBody());

                        String type = json.get("type").toString();
                        String body = json.get("body").toString();

                        if(type.equals("request") && body.equals("invite")){
                            gameInviteCallback.onAction(toUsername(message.getFrom()));
                        }
                    }
                    catch (JSONException e){
                        //TODO
                    }
                }
            }, StanzaTypeFilter.MESSAGE);
        }

        private void initRosterListener(){
            xmpp.roster.addRosterListener(new RosterListener() {

                @Override
                public void entriesAdded(Collection<String> addresses) {
                    for(String address : addresses){
                        Presence presence = roster.getPresence(address);
                        UserInfo userInfo = createUserInfo(presence);
                        userInfo.bothWaySubscription = xmpp.roster.isSubscribedToMyPresence(address);

                        users.add(userInfo);
                    }
                }

                @Override
                public void entriesUpdated(Collection<String> addresses) {
                    System.out.println(addresses);
                }

                @Override
                public void entriesDeleted(Collection<String> addresses) {
                    for(String address : addresses){
                        users.remove(findUserByJid(address));
                    }
                }

                @Override
                public void presenceChanged(Presence presence) {
                    UserInfo presenceInfo = createUserInfo(presence);
                    UserInfo user = findUserByJid(presence.getFrom());

                    user.presence = presenceInfo.presence;
                    user.status = presenceInfo.status;

                    if(usersCollectionChanged != null)
                        usersCollectionChanged.onAction();
                }
            });
        }
    }
}