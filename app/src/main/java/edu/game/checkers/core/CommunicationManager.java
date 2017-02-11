package edu.game.checkers.core;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Stanza;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import edu.game.checkers.core.callbacks.Callback1;
import edu.game.checkers.core.callbacks.ConnectionCallback;

public class CommunicationManager {

    private XMPPConnection conn;
    private ConnectionCallback connectionCallback;

    private String localName;
    private String otherName;
    private static int IdCounter = 0;

    private volatile boolean inGame = false;
    private Queue<Callback1<String>> responseQueue = new ArrayBlockingQueue<>(10);

    public CommunicationManager(String localName, String to,
                                XMPPConnection conn, ConnectionCallback connectionCallback){
        this.conn = conn;
        this.localName = localName;
        this.otherName = to;
        this.connectionCallback = connectionCallback;
    }

    public void setRequestCallback(final Callback1<String> requestCallback){
        conn.addAsyncStanzaListener(new StanzaListener() {
            @Override
            public void processPacket(Stanza packet) throws SmackException.NotConnectedException {
                Message message = (Message) packet;
                try {
                    JSONObject json = new JSONObject(message.getBody());

                    String body = json.get("body").toString();
                    String type = json.get("type").toString();

                    switch (type){
                        case "response":
                            Callback1<String> callback = responseQueue.remove();
                            if(callback != null)
                                callback.onAction(body);
                            break;
                        case "request":
                            requestCallback.onAction(body);
                            break;
                        case "game":
                            //TODO
                            break;
                    }
                } catch (JSONException e) {
                    //TODO
                }
            }
        }, StanzaTypeFilter.MESSAGE);
    }

    public void sendResponse(String message){
        try {
            JSONObject json = new JSONObject();
            json.put("body", message);
            json.put("type", "response");

            Message stanza = new Message();
            stanza.setTo(otherName);
            stanza.setBody(json.toString());
            conn.sendStanza(stanza);
        } catch (JSONException e){
            //TODO
        }
        catch (SmackException.NotConnectedException e){
            connectionCallback.onConnectionError(e.getMessage());
        }
    }

    public void sendRequest(String message, Callback1<String> callback){
        try {
            String id = nextId();

            JSONObject json = new JSONObject();
            json.put("body", message);
            json.put("type", "request");

            responseQueue.add(callback);

            Message stanza = new Message();
            stanza.setTo(otherName);
            stanza.setBody(json.toString());
            conn.sendStanza(stanza);
        } catch (SmackException.NotConnectedException e) {
            connectionCallback.onConnectionError(e.getMessage());
        } catch (JSONException e){
            //TODO
        }
    }

    public String getOtherName(){
        return otherName.substring(0, otherName.indexOf("@"));
    }

    private String nextId(){
        IdCounter = (IdCounter + 1) % Integer.MAX_VALUE;
        return localName + Integer.toString(IdCounter);
    }

}
