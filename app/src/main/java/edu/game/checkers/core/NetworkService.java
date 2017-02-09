package edu.game.checkers.core;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.filter.PresenceTypeFilter;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterListener;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.game.checkers.core.callbacks.ConnectionCallback;
import edu.game.checkers.core.callbacks.ConnectionCreatedCallback;
import edu.game.checkers.core.callbacks.InviteCallback;
import edu.game.checkers.core.callbacks.PresenceCallback;
import edu.game.checkers.core.callbacks.SubscriptionListener;


public class NetworkService extends Service {

    private final IBinder binder = new NetworkBinder();
    private volatile boolean connected = false;

    private final static int PORT = 5222;
    private final static String HOST = "89.40.127.125";
    private final static String SERVICE_NAME = "example.com";

    private String username;

    private XMPP xmpp;
    private ConnectionCallback connectionCallback;
    private ConnectionCreatedCallback connectionCreatedCallback;

    @Override
    public void onDestroy() {
        connected = false;
        new EndConnectionTask().execute();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void connectToServer(String username, String password, ConnectionCallback callback)
    {
        this.connectionCallback = callback;
        this.username = username;
        new ConnectToServerTask(callback).execute(username, password);
    }

    public CommunicationManager startConnectionTo(String user){
        ChatManager chatManager = ChatManager.getInstanceFor(xmpp.conn);
        Chat chat = chatManager.createChat(user);

        return new CommunicationManager(this.username, chat, connectionCallback);
    }

    public void subscribeUser(final String username, final InviteCallback callback){
        try{
            Presence subscribe = new Presence(Presence.Type.subscribe);
            subscribe.setTo(username);
            xmpp.conn.sendStanza(subscribe);

            xmpp.conn.addAsyncStanzaListener(new StanzaListener() {
                @Override
                public void processPacket(Stanza packet) throws SmackException.NotConnectedException {
                    if(parseFrom(packet.getFrom()).equals(username))
                        callback.onInvitedResponse(true);
                }
            }, PresenceTypeFilter.SUBSCRIBED);
            xmpp.conn.addAsyncStanzaListener(new StanzaListener() {
                @Override
                public void processPacket(Stanza packet) throws SmackException.NotConnectedException {
                    if(parseFrom(packet.getFrom()).equals(username))
                        callback.onInvitedResponse(false);
                }
            }, PresenceTypeFilter.UNSUBSCRIBED);
        }
        catch(SmackException.NotConnectedException e){
            this.connectionCallback.onConnectionError(e.getMessage());
        }
    }

    public void unsubscribeUser(String username){
        try {
            RosterEntry entry = xmpp.roster.getEntry(username);
            xmpp.roster.removeEntry(entry);
        } catch(Exception e){
            connectionCallback.onConnectionError(e.getMessage());
        }
    }

    public Collection<Friend> getFriendsList() {
        try{
            // reload list
            xmpp.roster.reloadAndWait();

            Collection<RosterEntry> entries = xmpp.roster.getEntries();
            Collection<Friend> users = new ArrayList<>();
            for(RosterEntry entry : entries){
                Presence presence = xmpp.roster.getPresence(entry.getUser());
                Friend friend = new Friend(entry.getUser(), presence.getType().name());
                friend.accepted = xmpp.roster.isSubscribedToMyPresence(entry.getUser());

                users.add(friend);
            }

            return users;
        } catch(Exception e){
            connectionCallback.onConnectionError(e.getMessage());
            return null;
        }
    }

    public void setPresenceListener(final PresenceCallback listener){
        xmpp.roster.addRosterListener(new RosterListener() {

            public void entriesAdded(Collection<String> addresses) {}
            public void entriesUpdated(Collection<String> addresses) {}
            public void entriesDeleted(Collection<String> addresses) {}

            @Override
            public void presenceChanged(Presence presence) {
                String jid = parseFrom(presence.getFrom());
                listener.onPresenceChanged(jid, presence.getType().name());
            }
        });
    }

    public void setSubscriptionListener(final SubscriptionListener listener){
        xmpp.subscriptionListener = listener;
    }

    public void setConnectionCreatedCallback(ConnectionCreatedCallback callback){
        connectionCreatedCallback = callback;
    }

    private String parseFrom(String from){
        int index = from.indexOf(SERVICE_NAME);
        return from.substring(0, index + SERVICE_NAME.length());
    }

    public class NetworkBinder extends Binder {
        NetworkService getService() {
            // Return this instance of NetworkService so clients can call public methods
            return NetworkService.this;
        }
    }

    /*
    * params - {username, password}
     */
    private class ConnectToServerTask extends AsyncTask<String, Void, Void> {

        XMPP handler;
        ConnectionCallback callback;
        Exception exception = null;

        public ConnectToServerTask(ConnectionCallback callback){
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
                connected = true;

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

    private class XMPP {

        private AbstractXMPPConnection conn;
        private Roster roster;
        private SubscriptionListener subscriptionListener = null;

        public XMPP(final String username, String password)
                throws IOException, XMPPException, SmackException {

            XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
                    .setUsernameAndPassword(username, password)
                    .setHost(HOST)
                    .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                    .setServiceName(SERVICE_NAME)
                    .setPort(PORT)
                    .setDebuggerEnabled(true)
                    .build();

            conn = new XMPPTCPConnection(config);

            conn.connect();
            conn.login();
            if(conn.isAuthenticated()) {
                roster = Roster.getInstanceFor(conn);
                roster.setSubscriptionMode(Roster.SubscriptionMode.manual);

                // Accepts all subscribe requests and also send subscribe request back
                conn.addAsyncStanzaListener(
                        new StanzaListener() {
                            @Override
                            public void processPacket(Stanza packet) throws SmackException.NotConnectedException {
                                Presence subscribed = new Presence(Presence.Type.subscribed);
                                subscribed.setTo(packet.getFrom());
                                conn.sendStanza(subscribed);

                                if (!roster.isLoaded()) {
                                    try {
                                        roster.reloadAndWait();
                                    } catch (Exception e) {
                                        connectionCallback.onConnectionError(e.getMessage());
                                    }
                                }

                                if (roster.getEntry(packet.getFrom()) == null) {
                                    Presence subscribe = new Presence(Presence.Type.subscribe);
                                    subscribe.setTo(packet.getFrom());
                                    conn.sendStanza(subscribe);
                                }
                            }
                        },
                        PresenceTypeFilter.SUBSCRIBE);

                conn.addAsyncStanzaListener(new StanzaListener() {
                    @Override
                    public void processPacket(Stanza packet) throws SmackException.NotConnectedException {
                        if (!roster.isLoaded()) {
                            try {
                                roster.reloadAndWait();
                            } catch (Exception e) {
                                connectionCallback.onConnectionError(e.getMessage());
                                return;
                            }
                        }
                        RosterEntry entry = roster.getEntry(packet.getFrom());
                        if(entry != null) {
                            try {
                                roster.removeEntry(entry);
                                if(subscriptionListener != null)
                                    subscriptionListener.onSubscriptionChange();
                            } catch (Exception e) {
                                connectionCallback.onConnectionError(e.getMessage());
                            }
                        }
                    }
                }, PresenceTypeFilter.UNSUBSCRIBE);

                conn.addAsyncStanzaListener(new StanzaListener() {
                    @Override
                    public void processPacket(Stanza packet) throws SmackException.NotConnectedException {
                        if(subscriptionListener != null)
                            subscriptionListener.onSubscriptionChange();
                    }
                }, PresenceTypeFilter.SUBSCRIBED);

                ChatManager chatManager = ChatManager.getInstanceFor(conn);
                chatManager.addChatListener(
                        new ChatManagerListener() {
                            @Override
                            public void chatCreated(Chat chat, boolean createdLocally) {
                                if(!createdLocally) {
                                    connectionCreatedCallback.
                                            onConnectionCreated(new CommunicationManager(username,
                                                    chat, connectionCallback));
                                }
                            }
                        });
            }
        }
    }
}