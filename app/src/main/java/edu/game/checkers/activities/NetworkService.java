package edu.game.checkers.activities;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.PresenceListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.filter.OrFilter;
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
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;


public class NetworkService extends Service {

    private final IBinder binder = new NetworkBinder();

    private final static int PORT = 5222;
    private final static String HOST = "89.40.127.125";
    private final static String SERVICE_NAME = "example.com";

    // in ms
    public final static int USER_TIMEOUT = 10000;
    private final static int QUEUE_CAPACITY = 10;

    private volatile boolean connected = false;

    private volatile boolean inGame = false;
    private GameController gameController;
    private XMPP xmpp;
    private ConnectionCallback callback;

    private Queue<Message> responseQueue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);

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
        this.callback = callback;
        new ConnectToServerTask(callback).execute(username, password);
    }

    public void startMainThread(final ConnectionCallback callback)
    {
//        Thread thread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try{
//                    while(connected){
//                        // blocking
//                        String str = in.readLine();
//
//                        if(str == null){
//                            new EndConnectionTask().execute();
//                            return;
//                        }
//
//                        Message msg = new Message(str);
//
//                        switch (msg.getType()){
//                            case Message.REQUEST:
//                                new HandleRequestTask(callback).execute(msg);
//                                break;
//                            case Message.RESPONSE:
//                                responseQueue.add(msg);
//                                break;
//                            case Message.GAME:
//                                if(inGame)
//                                    new HandleGameTask(gameController).execute(msg);
//                                break;
//                        }
//                    }
//                }
//                catch (IOException e){
//                    new HandleErrorTask(callback).execute(e.getMessage());
//                }
//            }
//        });
//
//        thread.start();
    }



    public void sendRequest(final Message msg, int timeout, final ServerResponseHandler callback) {
        msg.addPrefix(Message.REQUEST);
        new MakeRequestTask(callback, timeout).execute(msg);
    }

    public void sendResponse(final Message msg) {
        msg.addPrefix(Message.RESPONSE);
        new SendMessageTask().execute(msg);
    }

    public void sendGameMessage(final Message msg) {
        msg.addPrefix(Message.GAME);
        new SendMessageTask().execute(msg);
    }

    public void startGame(GameController controller) {
        this.gameController = controller;
        inGame = true;
    }

    public void inviteUser(final String username, final InviteCallback callback){
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
            this.callback.onConnectionError(e.getMessage());
        }
    }

    public void unsubscribeUser(String username){
        try {
            RosterEntry entry = xmpp.roster.getEntry(username);
            xmpp.roster.removeEntry(entry);
        } catch(Exception e){
            callback.onConnectionError(e.getMessage());
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
            callback.onConnectionError(e.getMessage());
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

    private class MakeRequestTask extends AsyncTask<Message, Void, Void>{

        ServerResponseHandler callback;
        Message response = null;
        int timeout;

        public MakeRequestTask(ServerResponseHandler callback, int timeout){
            this.callback = callback;
            this.timeout = timeout;
        }

        @Override
        protected Void doInBackground(Message... msg) {
            // send request
//            out.println(msg[0].toString());
//            out.flush();
//
//            if(callback != null) {
//                // wait for response
//                long startTime = System.currentTimeMillis();
//                while(System.currentTimeMillis() - startTime < timeout
//                        && responseQueue.isEmpty()){
//                }
//
//                if(!responseQueue.isEmpty())
//                    response = responseQueue.poll();
//                else
//                    response = new Message(Message.TIMEOUT, "timeout");
//            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(callback != null)
                callback.onServerResponse(response);
        }
    }

    private class SendMessageTask extends AsyncTask<Message, Void, Void>
    {
        @Override
        protected Void doInBackground(Message... msg) {
            //TODO
            return null;
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
                startMainThread(callback);

                callback.onSuccess();
            }
        }
    }

    private class HandleGameTask extends AsyncTask<Message, Void, Void>
    {
        GameController callback;

        public HandleGameTask(GameController callback){
            this.callback = callback;
        }

        @Override
        protected Void doInBackground(Message... params) {
            callback.onMessage(params[0]);
            return null;
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

    private class HandleRequestTask extends AsyncTask<Message, Void, Void>{

        ServerRequestHandler callback;
        Message msg;

        public HandleRequestTask(ServerRequestHandler callback){
            this.callback = callback;
        }

        @Override
        protected Void doInBackground(Message... params) {
            msg = params[0];
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            callback.onServerRequest(msg);
        }
    }

    private class HandleErrorTask extends AsyncTask<String, Void, Void>{

        ServerRequestHandler callback;
        String msg;

        public HandleErrorTask(ServerRequestHandler callback){
            this.callback = callback;
        }

        @Override
        protected Void doInBackground(String... params) {
            msg = params[0];
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            callback.onConnectionError(msg);
        }
    }

    private class XMPP {

        private AbstractXMPPConnection conn;
        private Chat chat;
        private Roster roster;
        private SubscriptionListener subscriptionListener = null;

        public XMPP(String username, String password)
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
                ChatManager chatManager = ChatManager.getInstanceFor(conn);
                chatManager.addChatListener(
                        new ChatManagerListener() {
                            @Override
                            public void chatCreated(Chat chat, boolean createdLocally) {
                                XMPP.this.chat = chat;
                                chat.addMessageListener(new MessageListener());
                            }
                        });

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
                                        callback.onConnectionError(e.getMessage());
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
                                callback.onConnectionError(e.getMessage());
                                return;
                            }
                        }
                        RosterEntry entry = roster.getEntry(packet.getFrom());
                        if(entry != null) {
                            try {
                                roster.removeEntry(entry);
                                if(subscriptionListener != null)
                                    subscriptionListener.onSubscribtionChange();
                            } catch (Exception e) {
                                callback.onConnectionError(e.getMessage());
                            }
                        }
                    }
                }, PresenceTypeFilter.UNSUBSCRIBE);

                conn.addAsyncStanzaListener(new StanzaListener() {
                    @Override
                    public void processPacket(Stanza packet) throws SmackException.NotConnectedException {
                        if(subscriptionListener != null)
                            subscriptionListener.onSubscribtionChange();
                    }
                }, PresenceTypeFilter.SUBSCRIBED);
            }
        }

        private class MessageListener implements ChatMessageListener {
            @Override
            public void processMessage(Chat chat, org.jivesoftware.smack.packet.Message message) {
                System.out.println("Received message: "
                        + (message != null ? message.getBody() : "NULL"));
            }
        }

    }
}
