package edu.game.checkers.activities;

public class Friend {

    public String username, status, info = "";
    public boolean accepted = true;

    public Friend(String username, String status, String info)
    {
        this(username, status);
        this.info = info;
    }

    public Friend(String username, String status)
    {
        this.username = username;
        this.status = status;
    }

}
