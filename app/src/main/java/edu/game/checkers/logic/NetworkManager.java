package edu.game.checkers.logic;

import android.util.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class NetworkManager implements Runnable{

    public final static int PORT = 8189;
    public final static String HOST = "89.40.127.125";

    // in ms
    public final static int SERVER_TIMEOUT = 200;
    public final static int USER_TIMEOUT = 6000; // TODO - move to board or elsewhere
    public final static int MAX_TIMEOUT = 600000;

    private final Socket socket;
    private final BufferedReader in;
    private final PrintWriter out;

    private final GameController gameController;

    public NetworkManager(GameController gameController) throws IOException
    {
        this.gameController = gameController;
        socket = new Socket(HOST, PORT);

        InputStream inStream = socket.getInputStream();
        OutputStream outStream = socket.getOutputStream();

        in = new BufferedReader(new InputStreamReader(inStream));
        out = new PrintWriter(outStream, true);
    }

    public void closeConnection() throws IOException
    {
        in.close();
        out.close();
        socket.close();
    }

    public void send(String msg) throws IOException
    {
        out.println(msg);
    }

    public String receive(int timeout) throws IOException
    {
        long startTime = System.currentTimeMillis();
        while(System.currentTimeMillis() - startTime < timeout
                && !in.ready()){
        }

        if(in.ready())
            return in.readLine();
        else
            throw new IOException();
    }

    @Override
    public void run() {
        while(true){
            try {
                String msg = receive(USER_TIMEOUT);

                switch (msg)
                {
                    case Message.MOVE:
                        Pair<Position, Position> move = Message.parseMove(msg);
                        gameController.clicked(move.first);
                        gameController.clicked(move.second);
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
