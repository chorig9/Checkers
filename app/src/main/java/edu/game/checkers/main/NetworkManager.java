package edu.game.checkers.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class NetworkManager {

    public final static int PORT = 8189;
    public final static String HOST = "89.40.127.125";

    // in ms
    public final static int SERVER_TIMEOUT = 200;
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

}
