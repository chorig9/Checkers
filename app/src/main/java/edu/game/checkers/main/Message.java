package edu.game.checkers.main;

public class Message {

    //Message should contain one of following string and possibly
    //some arguments after separator (separator before each argument)

    public final static String SEPARATOR = "#";
    public final static String HI = "hi",
            ERROR = "error", // unexpected or wrong response
            NO = "no",       // client response e.g. for invite
            OK = "ok",       // same
            EXIT_SERVER = "exit_server", // player disconnects from server
            EXIT_GAME = "exit_game",     // player disconnects from game
            GAME_OVER = "game_over",// game has ended (one player won or draw)
            INVITE = "invite",           // invite player to game
            GET_PLAYERS = "get_players", // get list of all connetced players
            START_GAME = "start",
            NEXT_TURN = "next",
            CHANGE_OPTIONS = "options",
            MOVE = "move",               // game move
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

    public final static String[] toArray(String message)
    {
        return message.split(SEPARATOR);
    }

}

