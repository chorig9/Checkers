package edu.game.checkers.ui;

class Friend {

    String username, status, info = "";
    boolean accepted = true;

    Friend(String username, String status, String info)
    {
        this(username, status);
        this.info = info;
    }

    Friend(String username, String status)
    {
        this.username = username;
        this.status = status;
    }

}
