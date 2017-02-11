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
    private StanzaListener stanzaListener;
    private String otherName;
    private Queue<Callback1<String>> responseQueue = new ArrayBlockingQueue<>(10);

    public CommunicationManager(String to, XMPPConnection conn, ConnectionCallback connectionCallback){
        this.conn = conn;
        this.otherName = to;
        this.connectionCallback = connectionCallback;
    }

    public void setCallbacks(final Callback1<String> requestCallback, final Callback1<String> gameCallback){
        stanzaListener = new StanzaListener() {
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
                            gameCallback.onAction(body);
                            break;
                    }
                } catch (JSONException e) {
                    //TODO
                }
            }
        };

        conn.addAsyncStanzaListener(stanzaListener, StanzaTypeFilter.MESSAGE);
    }

    // send response to a request
    public void sendResponse(String response){
        try {
            sendMessage(response, "response");
        } catch (SmackException.NotConnectedException e) {
            connectionCallback.onConnectionError(e.getMessage());
        }
    }

    // send request and excpect response
    public void sendRequest(String message, Callback1<String> callback){
        try {
            sendMessage(message, "request");
            responseQueue.add(callback);
        } catch (SmackException.NotConnectedException e) {
            connectionCallback.onConnectionError(e.getMessage());
        }
    }

    // send game move
    public void sendMove(String move){
        try {
            sendMessage(move, "game");
        } catch (SmackException.NotConnectedException e) {
            connectionCallback.onConnectionError(e.getMessage());
        }
    }

    // send request and dont excpect response
    public void sendInfo(String info){
        try {
            sendMessage(info, "request");
        } catch (SmackException.NotConnectedException e) {
            connectionCallback.onConnectionError(e.getMessage());
        }
    }

    // MUST be called when connection is being closed
    public void end(){
        conn.removeAsyncStanzaListener(stanzaListener);
    }

    private void sendMessage(String body, String type) throws SmackException.NotConnectedException {
        try {
            JSONObject json = new JSONObject();
            json.put("body", body);
            json.put("type", type);

            Message stanza = new Message();
            stanza.setTo(otherName);
            stanza.setBody(json.toString());
            conn.sendStanza(stanza);
        } catch (JSONException e){
            //TODO
        }
    }
}
