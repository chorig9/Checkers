package edu.game.checkers.ui;


public class UserInfo {

//    class Optional<T> {
//        T value;
//        boolean isSet = false;
//
//        Optional(T value){
//            set(value);
//        }
//
//        void set(T value){
//            this.value = value;
//            isSet = true;
//        }
//    }

//    Optional<String> username, status, presence;
//    Optional<Boolean> bothWaySubscription;

    String username, status, presence;
    boolean bothWaySubscription = false;

    UserInfo(String username, String status, String presence, boolean bothWaySubscription){
        this(username, status, presence);
        this.bothWaySubscription =bothWaySubscription;
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
