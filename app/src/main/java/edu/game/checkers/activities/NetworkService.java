package edu.game.checkers.activities;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

import edu.game.checkers.logic.Message;
import edu.game.checkers.logic.Position;


public class NetworkService extends Service {

    private final IBinder binder = new NetworkBinder();

    private final static int PORT = 8189;
    private final static String HOST = "89.40.127.125";

    // in ms
    private final static int SERVER_TIMEOUT = 600;
    private final static int USER_TIMEOUT = 6000;
    private final static int MAX_TIMEOUT = 600000;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    private boolean connected = false;

    public void makeRequest(final String msg, final ServiceResponseHandler callback)
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    // send request
                    send(msg);

                    // wait for response
                    long startTime = System.currentTimeMillis();
                    while(System.currentTimeMillis() - startTime < SERVER_TIMEOUT
                            && !in.ready()){
                    }

                    if(in.ready())
                        callback.onServerResponse(in.readLine());
                    else // exception if no response
                        callback.onConnectionError("Timeout");
                }
                catch(IOException e){
                    callback.onConnectionError(e.getMessage());
                }
            }
        });
    }

    public void sendMove(final Position position, final Position target)
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                send(Message.MOVE + Message.SEPARATOR + position.toString()
                        + Message.SEPARATOR + target.toString());
            }
        });
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
    public void onCreate() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    socket = new Socket(HOST, PORT);

                    InputStream inStream = socket.getInputStream();
                    OutputStream outStream = socket.getOutputStream();

                    in = new BufferedReader(new InputStreamReader(inStream));
                    out = new PrintWriter(outStream, true);

                    connected = true;
                }
                catch(IOException e){
                    connected = false;
                }
            }
        });
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
}
