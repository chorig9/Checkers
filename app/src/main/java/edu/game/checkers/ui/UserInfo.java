package edu.game.checkers.ui;


public class UserInfo {

    String username, status, presence;
    boolean bothWaySubscription = false;
    boolean invitedToGame = false;

    UserInfo(String username, String status, String presence, boolean bothWaySubscription, boolean invitedToGame){
        this(username, status, presence, bothWaySubscription);
        this.invitedToGame = invitedToGame;
    }

    UserInfo(String username, String status, String presence, boolean bothWaySubscription){
        this(username, status, presence);
        this.bothWaySubscription = bothWaySubscription;
    }

    UserInfo(String username, String status, String presence) {
        this(username, status);
        this.presence = presence;
    }

    UserInfo(String username, String status) {
        this(username);
        this.status = status;
    }

    UserInfo(String username){
        this.username = username;
    }
}
