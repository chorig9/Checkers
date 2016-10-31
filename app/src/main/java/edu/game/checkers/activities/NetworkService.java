package edu.game.checkers.activities;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;


public class NetworkService extends Service {

    private final IBinder binder = new NetworkBinder();

    private final static int PORT = 8189;
    private final static String HOST = "89.40.127.125";

    // in ms
    public final static int SERVER_TIMEOUT = 1000;
    public final static int USER_TIMEOUT = 10000;

    private final static int QUEUE_CAPACITY = 10;

    private BufferedReader in;
    private PrintWriter out;
    private volatile boolean connected = false;

    private volatile boolean inGame = false;
    private GameController gameController;

    private Queue<Message> responseQueue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);

    public void connectToServer(ServerRequestHandler callback)
    {
        new ConnectToServerTask(callback).execute();
    }

    public void startMainThread(final ServerRequestHandler callback)
    {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    while(connected){
                        // blocking
                        Message msg = new Message(in.readLine());
                        Log.d("ABCD", msg.toString());

                        switch (msg.getType()){
                            case Message.REQUEST:
                                new HandleRequestTask(callback).execute(msg);
                                break;
                            case Message.RESPONSE:
                                responseQueue.add(msg);
                                break;
                            case Message.GAME:
                                if(inGame)
                                    new HandleGameTask(gameController).execute(msg);
                                break;
                        }
                    }
                }
                catch (IOException e){
                    callback.onConnectionError(e.getMessage());
                    connected = false;
                }
            }
        });

        thread.start();
    }

    public void makeRequest(final Message msg, int timeout, final ServerResponseHandler callback)
    {
        msg.addPrefix(Message.REQUEST);
        new MakeRequestTask(callback, timeout).execute(msg);
    }

    public void sendResponse(final Message msg)
    {
        msg.addPrefix(Message.RESPONSE);
        new SendMessageTask().execute(msg);
    }

    public void sendGameMessage(final Message msg)
    {
        msg.addPrefix(Message.GAME);
        new SendMessageTask().execute(msg);
    }

    public void startGame(GameController controller)
    {
        this.gameController = controller;
        inGame = true;
    }

    @Override
    public void onDestroy() {
        connected = false;
        try {
            if(in != null)
                in.close();
        }
        catch(IOException e) {
            Logger.getAnonymousLogger().log(Level.FINE, "onDestroy error");
        }
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
            out.println(msg[0].toString());

            // wait for response
            long startTime = System.currentTimeMillis();
            while(System.currentTimeMillis() - startTime < timeout
                    && responseQueue.isEmpty()){
            }

            if(!responseQueue.isEmpty())
                response = responseQueue.poll();
            else
                response = new Message(Message.TIMEOUT, "timeout");

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            callback.onServerResponse(response);
        }
    }

    private class SendMessageTask extends AsyncTask<Message, Void, Void>
    {
        @Override
        protected Void doInBackground(Message... msg) {
            out.println(msg[0].toString());
            return null;
        }
    }

    private class ConnectToServerTask extends AsyncTask<Void, Void, Void> {

        ServerRequestHandler callback;
        Exception exception = null;

        public ConnectToServerTask(ServerRequestHandler callback){
            this.callback = callback;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try{
                Socket socket = new Socket(HOST, PORT);

                InputStream inStream = socket.getInputStream();
                OutputStream outStream = socket.getOutputStream();

                in = new BufferedReader(new InputStreamReader(inStream));
                out = new PrintWriter(outStream, true);
            }
            catch(IOException e){
                exception = e;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(exception != null)
                callback.onConnectionError(exception.getMessage());
            else {
                connected = true;
                startMainThread(callback);
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

    // to avoid changing view from wrong thread exception (maybe not most elegant, but works)
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
}
