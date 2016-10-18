package edu.game.checkers.logic;

import android.util.Pair;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class PlayerNetwork extends Player{

    // TODO - read from config file?
    public final static int PORT = 8189;
    public final static String HOST = "89.40.127.125";

    private final Socket socket;
    private final BufferedReader in;
    private final PrintWriter out;

    public PlayerNetwork(int color, Game game, Socket socket, BufferedReader in, PrintWriter out)
    {
        super(color, game);

        this.socket = socket;
        this.in = in;
        this.out = out;
    }

    @Override
    public Pair<Position, Position> makeMove() {
        return null;
    }


}
