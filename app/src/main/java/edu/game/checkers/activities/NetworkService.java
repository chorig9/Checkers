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
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;


public class NetworkService extends Service {

    private final IBinder binder = new NetworkBinder();

    private final static int PORT = 8189;
    private final static String HOST = "89.40.127.125";

    // in ms
    private final static int SERVER_TIMEOUT = 600;
    private final static int USER_TIMEOUT = 60000;
    private final static int CONNECTION_TIMEOUT = 6000;

    private BufferedReader in;
    private PrintWriter out;

    private class Request{
        final Message msg;
        final ServiceResponseHandler handler;
        final boolean expectResponse;

        /**
         * @param msg - message
         * @param handler - response handler
         * @param expectResponse - will player respond?
         */
        Request(Message msg, ServiceResponseHandler handler, boolean expectResponse){
            this.msg = msg;
            this.handler = handler;
            this.expectResponse = expectResponse;
        }
    }

    private volatile boolean connected = false;
    //private List<AsyncTask<Void, Void, Void>> requests = new ArrayList<>();
    private volatile List<Request> requests = new ArrayList<>();
    private Queue<Message> serverRequests = new PriorityQueue<>();

    private volatile boolean inGame = false;
    private GameController gameController;

    public void connectToServer(ServiceResponseHandler callback, ServiceRequestHandler requestCallback)
    {
        new ConnectToServerTask(callback, requestCallback).execute();
    }

    public void startMainThread(final ServiceRequestHandler callback)
    {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(connected){
                    try{
                        if(in.ready()){
                            Message msg = new Message(in.readLine());

                            switch (msg.getCode()){
                                case Message.INVITE:
                                    callback.onInvite(msg.getArguments().get(0));
                                    break;
                                default:
                                    if(inGame)
                                        gameController.onMessage(msg);
                                    break;
                            }
                        }
                        else if(!requests.isEmpty()){
                            Log.d("requests", requests.toString());
                            Request request = requests.get(0);
                            requests.remove(0);

                            // send request
                            out.println(request.msg.toString());

                            if(request.expectResponse){
                                try{
                                    // wait for response
                                    long startTime = System.currentTimeMillis();
                                    while(System.currentTimeMillis() - startTime < USER_TIMEOUT
                                            && !in.ready()){
                                    }

                                    if(in.ready())
                                        request.handler.onServerResponse(new Message(in.readLine()));
                                    else // exception if no response
                                        request.handler.onServerResponse(new Message(Message.TIMEOUT));
                                }
                                catch (IOException e){
                                    callback.onConnectionError(e.getMessage());
                                    throw e;
                                }
                            }
                        }
                    }
                    catch (IOException e){
                        callback.onConnectionError(e.getMessage());
                        connected = false;
                        return;
                    }
                }
            }
        });

        thread.start();
    }

    public void makeRequest(final Message msg, final ServiceResponseHandler callback)
    {
        //requests.add(new MakeRequestTask(callback, msg));
        requests.add(new Request(msg, callback, true));
    }

    public void sendMessage(final Message msg)
    {
        //requests.add(new SendMessageTask(msg));
        requests.add(new Request(msg, null, false));
    }

    public void startGame(GameController controller)
    {
        this.gameController = controller;
        inGame = true;
    }

    public boolean isConnected()
    {
        return connected;
    }

    @Override
    public void onDestroy() {
        connected = false;
        try {
            if(in != null)
                in.close();
        }
        catch(IOException e) {
            // TODO - log?
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

    private class MakeRequestTask extends AsyncTask<Void, Void, Void>{

        ServiceResponseHandler callback;
        Message response = null;
        Exception exception = null;
        Message msg;

        public MakeRequestTask(ServiceResponseHandler callback, Message msg){
            this.callback = callback;
            this.msg = msg;
        }

        @Override
        protected Void doInBackground(Void... msg) {
            try{
                // send request
                out.println(this.msg.toString());

                // wait for response
                long startTime = System.currentTimeMillis();
                while(System.currentTimeMillis() - startTime < SERVER_TIMEOUT
                        && !in.ready()){
                }

                if(in.ready())
                    response = new Message(in.readLine());
                else // exception if no response
                    response = new Message(Message.TIMEOUT);
            }
            catch(IOException e){
                exception = e;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(response != null)
                callback.onServerResponse(response);
            else
                callback.onConnectionError(exception.getMessage());
        }
    }

    private class SendMessageTask extends AsyncTask<Void, Void, Void>
    {
        Message msg;

        public SendMessageTask(Message msg){
            this.msg = msg;
        }

        @Override
        protected Void doInBackground(Void... params) {
            out.println(msg.toString());
            return null;
        }
    }

    private class ConnectToServerTask extends AsyncTask<Void, Void, Void> {

        ServiceResponseHandler callback;
        ServiceRequestHandler requestCallback;
        Exception exception = null;

        public ConnectToServerTask(ServiceResponseHandler callback,
                                   ServiceRequestHandler requestCallback){
            this.callback = callback;
            this.requestCallback = requestCallback;
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
                startMainThread(requestCallback);
            }
        }
    }
}
