package edu.game.checkers.ui;

import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import edu.game.checkers.utils.Callback0;

class UserCollection implements Iterable<UserInfo>{

    private Roster roster;
    private Callback0 listener;
    private Set<String> invitations = new HashSet<>();

    UserCollection(Roster roster){
        this.roster = roster;
    }

    @Override
    public Iterator<UserInfo> iterator() {
        return new Iterator<UserInfo>() {

            private Iterator<RosterEntry> rosterIterator = roster.getEntries().iterator();

            @Override
            public boolean hasNext() {
                return rosterIterator.hasNext();
            }

            @Override
            public UserInfo next() {
                RosterEntry entry = rosterIterator.next();
                Presence presence = roster.getPresence(entry.getUser());

                String username = entry.getName();
                String status = presence.getStatus();
                String presenceName = presence.getType().name();
                boolean bothWaySubscriptions = roster.isSubscribedToMyPresence(entry.getUser());
                boolean invitedToGame = invitations.contains(username);

                return new UserInfo(username, status, presenceName, bothWaySubscriptions, invitedToGame);
            }

            @Override
            public void remove() {
                rosterIterator.remove();
            }
        };
    }

    void setInvited(String user){
        invitations.add(user);
    }

    void setNotInvited(String user){
        invitations.remove(user);
    }

    void invalidate(){
        if(listener != null)
            listener.onAction();
    }

    void setListener(Callback0 listener){
        this.listener = listener;
    }
}
