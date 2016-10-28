package edu.game.checkers.activities;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

import edu.game.checkers.logic.NetworkMessage;
import edu.game.checkers.logic.Position;


public class NetworkService extends Service {

    private final IBinder binder = new NetworkBinder();

    private final static int PORT = 8189;
    private final static String HOST = "89.40.127.125";

    // in ms
    private final static int SERVER_TIMEOUT = 600;
    private final static int USER_TIMEOUT = 6000;
    private final static int MAX_TIMEOUT = 600000;

    private BufferedReader in;
    private PrintWriter out;

    private boolean connected = false;

    public void connectToServer(ServiceResponseHandler callback)
    {
        new ConnectToServerTask(callback).execute();
    }

    public void makeRequest(final String msg, final ServiceResponseHandler callback)
    {
        new MakeRequestTask(callback).execute(msg);
    }

    public void sendMove(final Position position, final Position target)
    {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                send(NetworkMessage.MOVE + NetworkMessage.SEPARATOR + position.toString()
                        + NetworkMessage.SEPARATOR + target.toString());
            }
        });

        thread.start();
    }

    public void startGame(ServiceResponseHandler callback)
    {

    }

    public void send(String msg)
    {
        out.println(msg);
    }

    public boolean isConnected()
    {
        return connected;
    }

    @Override
    public void onDestroy() {
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

    private class MakeRequestTask extends AsyncTask<String, Void, Void>{

        ServiceResponseHandler callback;
        String response = null;
        Exception exception = null;

        public MakeRequestTask(ServiceResponseHandler callback){
            this.callback = callback;
        }

        @Override
        protected Void doInBackground(String... msg) {
            try{
                // send request
                send(msg[0]);

                // wait for response
                long startTime = System.currentTimeMillis();
                while(System.currentTimeMillis() - startTime < SERVER_TIMEOUT
                        && !in.ready()){
                }

                if(in.ready())
                    response = in.readLine();
                else // exception if no response
                    throw new IOException("Timeout");
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

    private class ConnectToServerTask extends AsyncTask<Void, Void, Void> {

        ServiceResponseHandler callback;
        Exception exception = null;

        public ConnectToServerTask(ServiceResponseHandler callback){
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
            else
                connected = true;
        }
    }
}
