package net.obstfelder.celleye;

import android.app.IntentService;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.BatteryManager;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.neovisionaries.ws.client.*;
import net.obstfelder.celleye.model.CellResponseData;
import net.obstfelder.celleye.model.Payload;
import net.obstfelder.celleye.model.WebSocketRequest;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by paven on 18.08.2017.
 */
public class CellEyeService extends Service
{
    private static final String TAG = "CELLEYE";
    private String USER;
    private boolean reconnect;
    private final String EVENT_MESSAGE = "EVENT_MESSAGE";
    private final String EVENT_CONNECTION = "EVENT_CONNECTION";
    private final String COMMAND_SUBSCRIBE = "SUBSCRIBE";
    private WebSocket ws;
    private int battery;
    private boolean LOG_OUT = false;
    private static final String COMMAND_RESPONSE_DATA = "RESPONSE_DATA";
    private FusedLocationProviderClient mFusedLocationClient;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        USER = intent.getStringExtra("user");
        this.registerReceiver(this.mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        new Thread(new Runnable(){
            @Override
            public void run() {
                mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
                initWebSocket();
            }
        }).start();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(this.mBatInfoReceiver);
        super.onDestroy();
        Log.i("EXIT", "ondestroy!");
        Intent broadcastIntent = new Intent("RestartCellEye");
        sendBroadcast(broadcastIntent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void initWebSocket()
    {
        try {
            broadcastMessage("Connecting...", true, true);
            ws = new WebSocketFactory()
                    .setConnectionTimeout(0)
                    //.createSocket("ws://192.168.20.14:9000/ws")
                    .createSocket("ws://46.250.220.119:9000/ws")
                    .setPongInterval(60000)
                    .setPingInterval(3000)
                    .addListener(new WebSocketAdapter()
                                 {
                                     @Override
                                     public void onError(WebSocket websocket, WebSocketException cause) throws Exception
                                     {
                                         super.onError(websocket, cause);
                                         Log.e(TAG, "Error ", cause);
                                         broadcastMessage("Websocket error", true, false);
                                     }

                                     @Override
                                     public void onPongFrame(WebSocket websocket, WebSocketFrame frame) throws Exception
                                     {
                                         //Log.d(TAG,"pong "+DateTime.now().toString("DD.MM.YYYY HH:mm:ss"));
                                         super.onPongFrame(websocket, frame);
                                     }

                                     @Override
                                     public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception
                                     {
                                         Log.d(TAG, "Connection established with CellEye Server");
                                         broadcastConnectionMessage(true);
                                         HashMap<String, String> params = new HashMap<>();
                                         params.put("username", USER);
                                         WebSocketRequest request = new WebSocketRequest();
                                         request.setCommand(COMMAND_SUBSCRIBE);
                                         request.setParams(params);
                                         ObjectMapper mapper = new ObjectMapper();
                                         String json = mapper.writeValueAsString(request);
                                         websocket.sendText(json);
                                     }

                                     @Override
                                     public void onConnectError(WebSocket websocket, WebSocketException exception) throws Exception
                                     {
                                         super.onConnectError(websocket, exception);
                                         Log.w(TAG, "Error connecting...still trying");
                                         broadcastMessage("Websocket connection error", true, true);
                                     }

                                     @Override
                                     public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception
                                     {
                                         broadcastConnectionMessage(false);
                                         super.onDisconnected(websocket, serverCloseFrame, clientCloseFrame, closedByServer);
                                         Log.w(TAG, new Date() + " Connection closed. ClosedByServer: " + closedByServer);
                                         if (!LOG_OUT)
                                         {
                                             broadcastMessage("Websocket disconnected, trying to reconnect...", true, true);
                                             reconnect = true;
                                             while (reconnect)
                                             {
                                                 Log.i(TAG, "Trying to reconnect...");
                                                 try
                                                 {
                                                     websocket = websocket.recreate(0).connect();
                                                     Log.i(TAG, "Websocket is reconnected :)");
                                                     reconnect = false;
                                                     broadcastMessage("Websocket reconnected", false, false);
                                                 }
                                                 catch (WebSocketException e)
                                                 {
                                                     Log.i(TAG, "Still unable to reconnect websocket...");
                                                 }
                                                 catch (IOException e)
                                                 {
                                                     e.printStackTrace();
                                                 }
                                                 Thread.sleep(1000);
                                             }
                                         }
                                     }

                                     @SuppressWarnings("unchecked")
                                     @Override
                                     public void onTextMessage(WebSocket websocket, String text) throws Exception
                                     {
                                         Log.i(TAG, "Text received from server: " + text);
                                         requestLocation(websocket,text,false);
                                     }

                                     @Override
                                     public void onMessageError(WebSocket websocket, WebSocketException cause, List<WebSocketFrame> frames) throws Exception
                                     {
                                         super.onMessageError(websocket, cause, frames);
                                         broadcastMessage("Websocket message error", true, false);
                                     }
                                 }
                    )
                    .connect();

        }
        catch (Exception e)
        {
            Log.d("TELEMARK", "Error connecting", e);
            broadcastMessage("No response from server", true, false);
        }
    }

    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context ctxt, Intent intent) {
            battery = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
        }
    };

    private void requestLocation(final WebSocket websocket,String websocketRequest,boolean show)
    {
        final CellResponseData data = new CellResponseData();

        try
        {
            ObjectMapper mapper = new ObjectMapper();
            Payload serverRequest = mapper.readValue(websocketRequest, new TypeReference<Payload>() {});
            if(serverRequest.forUser!=null)
            {
                data.params.put("forUser",serverRequest.forUser);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            data.command = COMMAND_RESPONSE_DATA;
            data.params.put("username",USER);
            data.params.put("network",Util.getNetworkType(this.getBaseContext()));
            data.params.put("battery",String.valueOf(battery));
                mFusedLocationClient.flushLocations();
                mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {

                            Log.d(TAG, "Current location is: " + location);
                            data.params.put("dateTime", String.valueOf(location.getTime()));
                            data.params.put("latitude", String.valueOf(location.getLatitude()));
                            data.params.put("longitude", String.valueOf(location.getLongitude()));
                            data.params.put("status", "OK");

                            ObjectMapper mapper = new ObjectMapper();
                            String json = null;
                            try {
                                json = mapper.writeValueAsString(data);
                            } catch (JsonProcessingException e) {
                                e.printStackTrace();
                            }
                            websocket.sendText(json);

                        } else {
                            Log.e(TAG, "Location is NULL!");
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int i = 0;
                        Log.i(TAG, "Failed.", e);
                    }
                });

            /*if (show) {
                String currentText = "";
                if (currentLocation != null) txtInfo.setText(currentText +
                        "Location is: " + currentLocation.getLatitude() + "," + currentLocation.getLongitude() +
                        "\nNetwork type: " + Util.getNetworkType(this.getBaseContext()) +
                        "\nAltitude: " + currentLocation.getAltitude() +
                        "\nBattery: " + battery + "%"
                );
            }*/
        }
        else
        {
            Log.d(TAG,"MISSING PERMISSION");
        }


    }

    private void broadcastConnectionMessage(boolean connected)
    {
        Intent i = new Intent(EVENT_CONNECTION);
        i.putExtra("connected",connected);
        sendBroadcast(i);
    }

    private void broadcastMessage(String message,boolean error,boolean persist)
    {
        Intent i = new Intent(EVENT_MESSAGE);
        i.putExtra("message",message);
        i.putExtra("error",error);
        i.putExtra("persist",persist);
        sendBroadcast(i);
    }


}
