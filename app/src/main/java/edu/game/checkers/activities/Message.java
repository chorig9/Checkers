package edu.game.checkers.activities;

import android.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.game.checkers.logic.Position;

public class Message {

    //Message should contain one of following string and possibly
    //some arguments after separator (separator before each argument)

    public final static String SEPARATOR = "#";
    public final static String HI = "hi",
            PLAYER_WHITE = "1",
            PLAYER_BLACK = "2",
            ERROR = "error", // unexpected or wrong response
            NO = "no",       // client response e.g. for invite
            OK = "ok",       // same
            EXIT_SERVER = "exit_server", // player disconnects from server
            EXIT_GAME = "exit_game",     // player disconnects from game
            GAME_OVER = "game_over",// game has ended (one player won or draw)
            INVITE = "invite",           // invite player to game
            GET_PLAYERS = "get_players", // get list of all connected players
            UPDATE_PLAYERS = "up_players",
            START_GAME = "start",
            CHANGE_OPTIONS = "options",
            MOVE = "move",               // game move
            TIMEOUT = "timeout",
            PING = "ping",
            INFO = "info",
            UNDO_MOVE = "undo_move";

    public final static char REQUEST = '$',
            RESPONSE = '%',
            GAME = '^';

    private String msg = "";

    Message(String... params){
        if(params.length > 0)
        {
            msg += params[0];
            for(int i = 1; i < params.length; i++)
                msg += SEPARATOR + params[i];
        }
    }

    void addPrefix(char prefix)
    {
        msg = prefix + msg;
    }

    char getType()
    {
        if(msg == null || msg.length() == 0)
            return '0';
        return msg.charAt(0);
    }

    String getCode()
    {
        int separatorIndex = msg.indexOf(SEPARATOR);
        if(separatorIndex == -1)
            return msg.substring(1);
        else
            return msg.substring(1, separatorIndex);
    }

    List<String> getArguments()
    {
        List<String> list = Arrays.asList(msg.split(SEPARATOR));

        if(list.size() <= 1){
            List<String> arguments = new ArrayList<>();
            arguments.add("Empty");
            return arguments;
        }

        return list.subList(1, list.size());
    }

    public Pair<Position, Position> parseMove()
    {
        String mv1 = getArguments().get(0);
        String coors1[] = mv1.split(Position.SEPARATOR);

        int x1 = Integer.decode(coors1[0]), y1 = Integer.decode(coors1[1]);
        String mv2 = getArguments().get(1);
        String coors2[] = mv2.split(Position.SEPARATOR);

        int x2 = Integer.decode(coors2[0]), y2 = Integer.decode(coors2[1]);

        return new Pair<>(new Position(x1, y1), new Position(x2, y2));
    }

    @Override
    public String toString()
    {
        return msg;
    }
}

