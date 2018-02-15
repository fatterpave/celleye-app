package net.obstfelder.celleye;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

/**
 * Created by paven on 14.02.2018.
 */
public class CellEyeRestartBroadcastReceiver extends WakefulBroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.i(CellEyeRestartBroadcastReceiver.class.getSimpleName(), "Service Stops! Restart.");
        //context.startService(new Intent(context, CellEyeService.class));;

        Intent service = new Intent(context, CellEyeService.class);
        startWakefulService(context, service);
    }
}
