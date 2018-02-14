package net.obstfelder.celleye;

import com.neovisionaries.ws.client.WebSocket;

/**
 * Created by obsjoa on 17.02.2017.
 */

public class TaskResponse
{
    public boolean error;
    public String message;
    public WebSocket websocket;
    public Exception e;

    public TaskResponse(){}

    public TaskResponse(boolean error, String message, WebSocket websocket) {
        this.error = error;
        this.message = message;
        this.websocket = websocket;
    }

    public TaskResponse(boolean error, String message, WebSocket websocket, Exception e) {
        this.error = error;
        this.message = message;
        this.websocket = websocket;
        this.e = e;
    }

    @Override
    public String toString() {
        return "TaskResponse{" +
                "error=" + error +
                ", message='" + message + '\'' +
                ", websocket=" + websocket +
                ", e=" + e +
                '}';
    }
}
