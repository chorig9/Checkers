package edu.game.checkers.logic;

import android.util.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;

import edu.game.checkers.activities.GameActivity;

public class NetworkManager{

    public final static int PORT = 8189;
    public final static String HOST = "89.40.127.125";

    // in ms
    public final static int SERVER_TIMEOUT = 600;
    public final static int USER_TIMEOUT = 6000;
    public final static int MAX_TIMEOUT = 600000;

    private final Socket socket;
    private final BufferedReader in;
    private final PrintWriter out;

    public NetworkManager() throws IOException
    {
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

    // send request in format "command#parameter" and receive "command#response" - 'symmetric'
    public List<String> sendRequest(String msg, int timeout) throws IOException
    {
        send(msg);
        String response = receive(timeout);
        List<String> list = Message.toList(response);

        if(list.get(0).equals(Message.toList(msg).get(0)))
            return list.subList(1, list.size() - 1);
        else
            throw new IOException("wrong response");
    }

    public void sendMove(Position piece, Position target) throws IOException
    {
        send(Message.MOVE + Message.SEPARATOR + piece.toString()
                + Message.SEPARATOR + target.toString()); // TODO toString in Position
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
            throw new IOException("timeout");
    }
}
