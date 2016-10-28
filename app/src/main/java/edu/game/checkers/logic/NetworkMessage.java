package edu.game.checkers.logic;

import android.util.ArraySet;
import android.util.Pair;

import java.util.Arrays;
import java.util.List;

import edu.game.checkers.logic.Position;

public class NetworkMessage {

    //NetworkMessage should contain one of following string and possibly
    //some arguments after separator (separator before each argument)

    public final static String SEPARATOR = "#";
    public final static String HI = "hi",
            PLAYER_WHITE = "1",
            PLAYER_ = "2",
            ERROR = "error", // unexpected or wrong response
            NO = "no",       // client response e.g. for invite
            OK = "ok",       // same
            EXIT_SERVER = "exit_server", // player disconnects from server
            EXIT_GAME = "exit_game",     // player disconnects from board
            GAME_OVER = "game_over",// board has ended (one player won or draw)
            INVITE = "invite",           // invite player to board
            GET_PLAYERS = "get_players", // get list of all connected players
            START_GAME = "start",
            NEXT_TURN = "next",
            CHANGE_OPTIONS = "options",
            MOVE = "move",               // board move, format : TODO
            MOVE_UNDONE = "move_undone",
            TIMEOUT = "timeout",
            PING = "ping",
            PONG = "pong";


    public final static String toString(String[] message)
    {
        String msg = "";

        for(String s : message)
            msg += SEPARATOR + s;

        return msg;
    }

    public final static List<String> toList(String message)
    {
        return Arrays.asList(message.split(SEPARATOR));
    }

    public final static Pair<Position, Position> parseMove(String msg)
    {
        //TODO
        return new Pair<>(null, null);
    }

}

