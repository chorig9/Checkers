package edu.game.checkers.ui;

class Friend {

    String username, status;
    int options;
    boolean accepted = true;
    boolean invitedToGame = false;

    Friend(String username, String status, int options)
    {
        this(username, status);
        this.options = options;
    }

    Friend(String username, String status)
    {
        this.username = username;
        this.status = status;
    }

}
