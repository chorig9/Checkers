package edu.game.checkers.activities;

import android.graphics.Paint;
import android.support.v4.util.Pair;
import android.support.v4.util.Pools;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PresenceListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.filter.PresenceTypeFilter;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterListener;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smack.util.SystemUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class XMPPHandler {

    private final static int PORT = 5222;
    private final static String HOST = "89.40.127.125";
    private final static String SERVICE_NAME = "example.com";

    private AbstractXMPPConnection conn;
    private Chat chat;
    private Roster roster;

    public XMPPHandler(String username, String password)
            throws IOException, XMPPException, SmackException {

        XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
                .setUsernameAndPassword(username, password)
                .setHost(HOST)
                .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                .setServiceName(SERVICE_NAME)
                .setPort(PORT)
                .setDebuggerEnabled(true)
                .build();

        conn = new XMPPTCPConnection(config);

        conn.connect();
        conn.login();
        if(conn.isAuthenticated()) {
            ChatManager chatManager = ChatManager.getInstanceFor(conn);
            chatManager.addChatListener(
                    new ChatManagerListener() {
                        @Override
                        public void chatCreated(Chat chat, boolean createdLocally) {
                            XMPPHandler.this.chat = chat;
                            chat.addMessageListener(new MessageListener());
                        }
                    });

            roster = Roster.getInstanceFor(conn);
            roster.setSubscriptionMode(Roster.SubscriptionMode.manual);

            conn.addAsyncStanzaListener(
                    new StanzaListener() {
                        @Override
                        public void processPacket(Stanza packet) throws SmackException.NotConnectedException {
                            Presence subscribed = new Presence(Presence.Type.subscribed);
                            subscribed.setTo(packet.getFrom());
                            conn.sendStanza(subscribed);

                            if (!roster.isLoaded()) {
                                try {
                                    roster.reloadAndWait();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            if (roster.getEntry(packet.getFrom()) == null) {
                                Presence subscribe = new Presence(Presence.Type.subscribe);
                                subscribe.setTo(packet.getFrom());
                                conn.sendStanza(subscribe);
                            }
                        }
                    },
                    PresenceTypeFilter.SUBSCRIBE);
        }
    }

    public Collection<Friend> getRosterEntries(){
        if(!roster.isLoaded()){
            try{
                roster.reloadAndWait();
            } catch(Exception e){
                e.printStackTrace();
            }
        }

        Collection<RosterEntry> entries = roster.getEntries();
        Collection<Friend> users = new ArrayList<>();
        for(RosterEntry entry : entries){
            Presence presence = roster.getPresence(entry.getUser());
            users.add(new Friend(entry.getUser(), presence.getType().name()));
        }

        return users;
    }

    public static String parseFrom(String from){
        int index = from.indexOf(XMPPHandler.SERVICE_NAME);
        return from.substring(0, index + XMPPHandler.SERVICE_NAME.length());
    }

    public void setPresenceListener(RosterListener listener){
        roster.addRosterListener(listener);
    }

    private class MessageListener implements ChatMessageListener{
        @Override
        public void processMessage(Chat chat, org.jivesoftware.smack.packet.Message message) {
            System.out.println("Received message: "
                    + (message != null ? message.getBody() : "NULL"));
        }
    }

}
