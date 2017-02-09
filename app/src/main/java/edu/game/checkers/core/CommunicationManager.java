package edu.game.checkers.core;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import edu.game.checkers.core.callbacks.ConnectionCallback;
import edu.game.checkers.core.callbacks.GameController;
import edu.game.checkers.core.callbacks.RequestCallback;
import edu.game.checkers.core.callbacks.ResponseCallback;

public class CommunicationManager {

    private Chat chat;
    private ConnectionCallback connectionCallback;

    private String localName;
    private static int IdCounter = 0;

    private volatile boolean inGame = false;
    private GameController gameController;
    private Map<String, ResponseCallback> responseMap = new HashMap<>();

    public CommunicationManager(String localName, Chat chat, ConnectionCallback connectionCallback){
        this.chat = chat;
        this.localName = localName;
        this.connectionCallback = connectionCallback;
    }

    public void acceptConnection(final RequestCallback requestCallback){
        chat.addMessageListener(new ChatMessageListener() {
            @Override
            public void processMessage(Chat chat, Message message) {
                try {
                    JSONObject json = new JSONObject(message.getBody());

                    String id = json.get("id").toString();
                    String body = json.get("body").toString();
                    String type = json.get("type").toString();

                    switch (type){
                        case "response":
                            ResponseCallback callback = responseMap.remove(id);
                            if(callback != null)
                                callback.onResponse(body);
                            break;
                        case "request":
                            requestCallback.onRequest(id, body);
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
            json.put("id", responseId);
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

    public void sendRequest(String message, ResponseCallback callback){
        try {
            String id = nextId();

            JSONObject json = new JSONObject();
            json.put("id", id);
            json.put("body", message);
            json.put("type", "request");

            responseMap.put(id, callback);

            chat.sendMessage(json.toString());
        } catch (SmackException.NotConnectedException e) {
            connectionCallback.onConnectionError(e.getMessage());
        } catch (JSONException e){
            //TODO
        }
    }

    public String getOtherName(){
        return chat.getParticipant();
    }

    private String nextId(){
        return localName + Integer.toString(IdCounter++);
    }

}
