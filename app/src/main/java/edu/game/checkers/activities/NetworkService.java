package edu.game.checkers.activities;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.util.Pair;

import org.jivesoftware.smack.PresenceListener;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.RosterListener;

import java.io.IOException;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;


public class NetworkService extends Service {

    private final IBinder binder = new NetworkBinder();

    // in ms
    public final static int SERVER_TIMEOUT = 1000;
    public final static int USER_TIMEOUT = 10000;
    private final static int QUEUE_CAPACITY = 10;

    private volatile boolean connected = false;

    private volatile boolean inGame = false;
    private GameController gameController;
    private XMPPHandler xmppHandler;

    private Queue<Message> responseQueue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);

    public void connectToServer(ConnectionCallback callback)
    {
        new ConnectToServerTask(callback).execute("android_test", "test");
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

    public Collection<Friend> getFriendsList() {
        return xmppHandler.getRosterEntries();
    }

    @Override
    public void onDestroy() {
        connected = false;
        new EndConnectionTask().execute();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
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

        XMPPHandler handler;
        ConnectionCallback callback;
        Exception exception = null;

        public ConnectToServerTask(ConnectionCallback callback){
            this.callback = callback;
        }

        @Override
        protected Void doInBackground(String... params){
            try {
                handler = new XMPPHandler(params[0], params[1]);
                handler.setPresenceListener(new RosterListener() {

                    public void entriesAdded(Collection<String> addresses) {}
                    public void entriesUpdated(Collection<String> addresses) {}
                    public void entriesDeleted(Collection<String> addresses) {}

                    @Override
                    public void presenceChanged(Presence presence) {
                        String jid = XMPPHandler.parseFrom(presence.getFrom());
                        callback.onPresenceChanged(jid, presence.getType().name());
                    }
                });
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
                xmppHandler = handler;
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
}
