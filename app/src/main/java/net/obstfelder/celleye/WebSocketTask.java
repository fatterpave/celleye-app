package net.obstfelder.celleye;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import net.obstfelder.celleye.model.CellResponseData;

import java.util.ArrayList;


/**
 * Created by obsjoa on 27.02.2017.
 */

public class WebSocketTask extends AsyncTask<String, CellResponseData, TaskResponse>
{
    private boolean showErrorDialog = false;
    private String wsUrl;
    private ProgressDialog disconnectDialog;
    private Context CONTEXT;
    private String USER;
    private static final String TAG = "110 TELEMARK";
    private WebSocketHandler webSocketHandler;
    private AlertDialog newAlarmPopup;

    public WebSocketTask(Context context, String user)
    {
        super();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Log.i(TAG,">>Websocket URL is: "+sharedPreferences.getString("WEBSOCKET_URL","wss://tmsweb.sk-asp.net:9443/ws"));
        wsUrl = sharedPreferences.getString("WEBSOCKET_URL","wss://tmsweb.sk-asp.net:9443/ws");

        CONTEXT = context;
        USER = user;
        disconnectDialog = new ProgressDialog(CONTEXT);
        disconnectDialog.setTitle("Feil");
        disconnectDialog.setMessage("Mistet kontakt med server. Prøver å gjenopprette kontakt...");

        BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                Log.i(TAG,"Received acknowledged signal");
                String nimId = intent.getStringExtra("nimId");

            }
        };
        LocalBroadcastManager.getInstance(CONTEXT).registerReceiver(mMessageReceiver,new IntentFilter("EVENT_ACKNOWLEDGED"));
    }


    public Context getContext()
    {
        return CONTEXT;
    }

    public WebSocketHandler getWebSocketHandler()
    {
        return webSocketHandler;
    }



    public void doProgress(CellResponseData... values)
    {
        publishProgress(values);
    }

    protected TaskResponse doInBackground(final String ...user)
    {
        webSocketHandler = new WebSocketHandler(this,wsUrl);
        return webSocketHandler.connect();
    }

    @Override
    protected void onPostExecute(TaskResponse response)
    {
        super.onPostExecute(response);
    }


    @Override
    protected void onProgressUpdate(CellResponseData... values)
    {
        if(values==null)
        {
            if(showErrorDialog)
            {
                if(!((Activity)getContext()).isFinishing())
                {
                    Log.i(TAG,"Showing websocket connect error message");
                    disconnectDialog.show();
                }
            }
            else
            {
                disconnectDialog.dismiss();
                Log.i(TAG,"Dismissing websocket connect error message");
            }
        }
        else
        {
            //Oppdater listene
            /*if (currentAlarmPayloadType == AlarmPayloadType.ACCEPTED)
            {
                populateAlarmList(values[0], true);
                sendAlarmDetailUpdate(values[0],null);
            }*/

        }
    }

    /*private void sendAlarmDetailUpdate(List<Alarm> assignedList,List<Alarm> acceptedList)
    {
        Intent intent = new Intent("UPDATED_ALARM");
        ArrayList<String> nimList = new ArrayList<>();
        if(assignedList!=null) for(Alarm assigned : assignedList) nimList.add(assigned.getNimId());
        if(acceptedList!=null) for(Alarm accepted : acceptedList) nimList.add(accepted.getNimId());
        intent.putStringArrayListExtra("nimList",nimList);
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
    }

    private void generateAlert(boolean withPopup)
    {
        Intent intent = new Intent(CONTEXT.getApplicationContext(),AlarmNotificationService.class);
        intent.putExtra("newalarm",newAlarm);
        CONTEXT.startService(intent);
        if(withPopup) showAlarmAlert();
        newAlarm = null;
    }

    private void showAlarmAlert()
    {
        LayoutInflater inflater = LayoutInflater.from(CONTEXT);
        View alertView = inflater.inflate(R.layout.alarm_alert_layout,null);
        ((TextView)alertView.findViewById(R.id.alertName)).setText(newAlarm.getPersonName());
        ((TextView)alertView.findViewById(R.id.alertAddress)).setText(newAlarm.getPersonAddress());
        AlertDialog.Builder builder = new AlertDialog.Builder(CONTEXT);
        builder
                .setView(alertView)
                .setTitle("timedout".equals(newAlarm.getHandlingState())?"ALARM UTGÅTT":newAlarm.getMessage())
                .setIcon(getAlarmIcon(newAlarm.getSeverity()))
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        removeNotification();
                    }
                });
        newAlarmPopup = builder.create();
        newAlarmPopup.show();
    }

    public void removeNotification()
    {
        Intent intent = new Intent(CONTEXT.getApplicationContext(),AlarmNotificationService.class);
        intent.putExtra("newalarm",newAlarm);
        CONTEXT.startService(intent);
    }


    private int getAlarmIcon(int severity)
    {
        switch(severity)
        {
            case 5: return R.drawable.ic_brightness_1_red_24dp;
            case 4: return R.drawable.ic_brightness_1_orange_24dp;
            case 3: return R.drawable.ic_brightness_1_yellow_24dp;
            case 2: return R.drawable.ic_brightness_1_blue_24dp;
            case 1: return R.drawable.ic_brightness_1_lightblue_24dp;
            default: return R.drawable.ic_brightness_1_green_24dp;
        }
    }*/
}

