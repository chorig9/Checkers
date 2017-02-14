package edu.game.checkers.ui;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PresenceListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PresenceTypeFilter;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntries;
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
    private UserCollection userCollection;

    private String hostname, ipAddress;
    private int port;

    public class NetworkBinder extends Binder {
        NetworkService getService() {
            // Return this instance of NetworkService so clients can call public methods
            return NetworkService.this;
        }
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

    public void subscribeUser(String username){
        xmpp.createRosterEntry(toJid(username));
    }

    public void unsubscribeUser(String username){
        xmpp.removeRosterEntry(toJid(username));
    }

    public CommunicationManager getCommunicationManager(String to){
        return new CommunicationManager(toJid(to), xmpp.conn, connectionCallback);
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

    public void setGameInviteCallback(Callback1<String> gameInviteCallback){
        this.gameInviteCallback = gameInviteCallback;
    }

    public void sendGameInvitation(String user, final Callback1<Boolean> responseCallback){
        try{
            Message stanza = new Message();
            stanza.setTo(toJid(user));

            JSONObject json = new JSONObject();
            json.put("type", "request");
            json.put("body", "invite");

            stanza.setBody(json.toString());
            xmpp.conn.sendStanza(stanza);

            userCollection.setInvited(user);
            userCollection.invalidate();

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

    public UserCollection getUserCollection(){
        return userCollection;
    }

    private void addGameInvitationResponseCallback(
            final String user,
            final Callback1<Boolean> responseCallback)
    {
        xmpp.conn.addAsyncStanzaListener(new StanzaListener() {
            @Override
            public void processPacket(Stanza packet) throws SmackException.NotConnectedException {
                Message message = (Message) packet;

                userCollection.setNotInvited(user);
                userCollection.invalidate();

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
            if(exception != null) {
                callback.onConnectionError(exception.getMessage());
                exception.printStackTrace();
            }
            else {
                xmpp = handler;
                callback.onSuccess();
            }
        }
    }

    private class XMPP {

        private AbstractXMPPConnection conn;
        private Roster roster;

        XMPP(final String username, String password)
                throws IOException, XMPPException, SmackException {

            XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
                    .setUsernameAndPassword(username, password)
                    .setHost(ipAddress)
                    .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                    .setServiceName(hostname)
                    .setPort(port)
                    .setDebuggerEnabled(true)
                    .build();

            conn = new XMPPTCPConnection(config);

            conn.connect();
            conn.login();
            if(conn.isAuthenticated()) {
                roster = Roster.getInstanceFor(conn);
                roster.setSubscriptionMode(Roster.SubscriptionMode.manual);

                if(!roster.isLoaded())
                    try {
                        roster.reloadAndWait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                for(RosterEntry entry : roster.getEntries()){
                    entry.setName(toUsername(entry.getUser()));
                }

                userCollection = new UserCollection(roster);

                initSubscribeListener();
                initUnSubscribeListener();
                initInvitationListener();
                initRosterListener();
            }
        }

        private void initSubscribeListener(){
            // Accepts all subscribe requests and also send subscribe request back
            conn.addAsyncStanzaListener(
                    new StanzaListener() {
                        @Override
                        public void processPacket(Stanza packet) throws SmackException.NotConnectedException {
                            String sender = packet.getFrom();

                            sendSubscribedStanza(sender);

                            RosterEntry entry = roster.getEntry(sender);
                            if(entry == null){
                                createRosterEntry(sender);
                            }
                        }
                    },
                    PresenceTypeFilter.SUBSCRIBE);
        }

        private void createRosterEntry(String jid){
            try{

                roster.createEntry(jid, toUsername(jid), null);

            } catch (SmackException.NotLoggedInException
                    | SmackException.NoResponseException
                    | XMPPException.XMPPErrorException
                    | SmackException.NotConnectedException e) {
                connectionCallback.onConnectionError(e.getMessage());
                e.printStackTrace();
            }
        }

        private void sendSubscribedStanza(String to) throws SmackException.NotConnectedException {
            Presence subscribed = new Presence(Presence.Type.subscribed);
            subscribed.setTo(to);
            conn.sendStanza(subscribed);
        }

        private void initUnSubscribeListener(){
            conn.addAsyncStanzaListener(new StanzaListener() {
                @Override
                public void processPacket(Stanza packet) throws SmackException.NotConnectedException {
                    String sender = packet.getFrom();
                    removeRosterEntry(sender);
                }
            }, PresenceTypeFilter.UNSUBSCRIBE);
        }

        private void removeRosterEntry(String jid){
            try {

                RosterEntry entry = roster.getEntry(jid);
                if(entry != null)
                    roster.removeEntry(entry);

            } catch (SmackException.NotConnectedException
                    | SmackException.NotLoggedInException
                    | SmackException.NoResponseException
                    | XMPPException.XMPPErrorException e) {
                connectionCallback.onConnectionError(e.getMessage());
                e.printStackTrace();
            }
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
            roster.addRosterListener(new RosterListener() {

                @Override
                public void entriesAdded(Collection<String> addresses) {
                    userCollection.invalidate();
                }

                @Override
                public void entriesUpdated(Collection<String> addresses) {
                    userCollection.invalidate();
                }

                @Override
                public void entriesDeleted(Collection<String> addresses) {
                    userCollection.invalidate();
                }

                @Override
                public void presenceChanged(Presence presence) {
                    userCollection.invalidate();
                }
            });
        }
    }
}