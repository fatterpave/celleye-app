package net.obstfelder.celleye;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neovisionaries.ws.client.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by obsjoa on 27.02.2017.
 */

public class WebSocketHandler
{
    private boolean showErrorDialog = false;
    //private String wsUrl;
    private ProgressDialog disconnectDialog;
    private WebSocketTask task;
    private String wsUrl;
    private TaskResponse taskResponse;
    private static final String TAG = "CELLEYE";
    private boolean LOGGINGOUT = false;
    private boolean reconnect;
    private WebSocket ws = null;

    public WebSocketHandler(WebSocketTask task, String wsUrl)
    {
        this.task = task;
        this.wsUrl = wsUrl;
    }

    public void setLogout()
    {
        LOGGINGOUT = true;
    }

    public void disconnect()
    {
        taskResponse.websocket.disconnect();
    }


    public TaskResponse connect()
    {
        TaskResponse response = null;

        BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                Log.i(TAG,"Received logout signal");
                LOGGINGOUT = true;
                if(taskResponse.websocket.isOpen()) taskResponse.websocket.disconnect();
            }
        };
        LocalBroadcastManager.getInstance(task.getContext()).registerReceiver(mMessageReceiver,new IntentFilter("EVENT_LOGOUT"));

        try
        {
            ws = new WebSocketFactory()
                    .setConnectionTimeout(0)
                    .createSocket(wsUrl)
                    .setPongInterval(60000)
                    .setPingInterval(3000)
                    .addListener(new WebSocketAdapter()
                         {
                             @Override
                             public void onError(WebSocket websocket, WebSocketException cause) throws Exception {
                                 super.onError(websocket, cause);
                                 Log.e(TAG,"Error ",cause);
                                 //task.setShowErrorDialog(true);
                                 //task.doProgress(null);
                             }

                             @Override
                             public void onPongFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
                                 //Log.d(TAG,"pong "+DateTime.now().toString("DD.MM.YYYY HH:mm:ss"));
                                 super.onPongFrame(websocket, frame);
                             }

                             @Override
                             public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
                                 /*reconnect = false;
                                 task.setShowErrorDialog(false);
                                 task.doProgress(null);
                                 Log.d(TAG,"Connection established with Responsecenter App Server");
                                 HashMap<String,String> params = new HashMap<>();
                                 params.put("username",task.getUser().username);
                                 params.put("orgId",task.getUser().lastLoggedInOrgId);
                                 WebSocketRequest request = new WebSocketRequest("SUBSCRIBE","",params);
                                 ObjectMapper mapper = new ObjectMapper();
                                 String json = mapper.writeValueAsString(request);
                                 taskResponse = new TaskResponse(false,"Connected to websocket",websocket);
                                 websocket.sendText(json);*/
                             }

                             @Override
                             public void onConnectError(WebSocket websocket, WebSocketException exception) throws Exception {
                                 super.onConnectError(websocket, exception);
                                 taskResponse = new TaskResponse(true,"Error connecting",websocket,exception);
                                 Log.w(TAG,"Error connecting...still trying");
                                 //task.setShowErrorDialog(true);
                                 //task.doProgress(null);
                             }

                             @Override
                             public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
                                 /*super.onDisconnected(websocket, serverCloseFrame, clientCloseFrame, closedByServer);
                                 Log.w(TAG, DateTime.now()+" Connection closed. CloseByServer: "+closedByServer);
                                 if(!LOGGINGOUT)
                                 {
                                     task.setShowErrorDialog(true);
                                     task.doProgress(null);
                                     reconnect = true;
                                     while(reconnect)
                                     {
                                         Log.i(TAG,"Trying to reconnect...");
                                         try
                                         {
                                             websocket = websocket.recreate(0).connect();
                                             Log.i(TAG,"Websocket is reconnected :)");
                                             reconnect = false;
                                             task.setShowErrorDialog(false);
                                             task.doProgress(null);
                                         }
                                         catch (WebSocketException e)
                                         {
                                             Log.i(TAG,"Still unable to reconnect websocket...");
                                         }
                                         catch (IOException e)
                                         {
                                             e.printStackTrace();
                                         }
                                         Thread.sleep(1000);
                                     }
                                 }*/
                             }

                             @SuppressWarnings("unchecked")
                             @Override
                             public void onTextMessage(WebSocket websocket, String text) throws Exception
                             {
                                 Log.i(TAG,"Text received from server: "+text);
                                 String json = text;
                                 ObjectMapper mapper = new ObjectMapper();

                                 try
                                 {
                                     //AlarmPayload response = mapper.readValue(json, new TypeReference<AlarmPayload>(){});
                                     //task.setCurrentAlarmPayloadType(response.type);
                                     //task.setAcceptedAlarm(null);

                                 }
                                 catch(Exception e)
                                 {
                                     e.printStackTrace();
                                 }
                             }

                             @Override
                             public void onMessageError(WebSocket websocket, WebSocketException cause, List<WebSocketFrame> frames) throws Exception
                             {
                                 super.onMessageError(websocket, cause, frames);
                             }
                         }
                    )
                    .connect();

            return taskResponse;
        }
        catch(Exception e)
        {
            Log.d("TELEMARK","Error connecting",e);
            taskResponse = new TaskResponse(true,"Error connecting",ws,e);
            return taskResponse;
        }
    }
}
