package edu.game.checkers.core;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import edu.game.checkers.core.callbacks.Callback1;
import edu.game.checkers.core.callbacks.ConnectionCallback;

public class CommunicationManager {

    private Chat chat;
    private ConnectionCallback connectionCallback;

    private String localName;
    private static int IdCounter = 0;

    private volatile boolean inGame = false;
    private Queue<Callback1<String>> responseQueue = new ArrayBlockingQueue<>(10);

    public CommunicationManager(String localName, Chat chat, ConnectionCallback connectionCallback){
        this.chat = chat;
        this.localName = localName;
        this.connectionCallback = connectionCallback;
    }

    public void acceptConnection(final Callback1<String> requestCallback){
        chat.addMessageListener(new ChatMessageListener() {
            @Override
            public void processMessage(Chat chat, Message message) {
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
        });
    }

    public void sendResponse(String responseId, String message){
        try {
            JSONObject json = new JSONObject();
            json.put("body", message);
            json.put("type", "response");

            chat.sendMessage(json.toString());
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
            json.put("id", id);
            json.put("body", message);
            json.put("type", "request");

            responseQueue.add(callback);

            chat.sendMessage(json.toString());
        } catch (SmackException.NotConnectedException e) {
            connectionCallback.onConnectionError(e.getMessage());
        } catch (JSONException e){
            //TODO
        }
    }

    public String getOtherName(){
        String jid = chat.getParticipant();
        return jid.substring(0, jid.indexOf("@"));
    }

    private String nextId(){
        IdCounter = (IdCounter + 1) % Integer.MAX_VALUE;
        return localName + Integer.toString(IdCounter);
    }

}
