package net.obstfelder.celleye;

import android.*;
import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.BatteryManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import net.obstfelder.celleye.model.CellResponseData;
import net.obstfelder.celleye.model.Payload;

import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity implements Thread.UncaughtExceptionHandler
{
    private Thread.UncaughtExceptionHandler defaultUEH;
    private final String TAG = "CELLEYE";
    private final String EVENT_MESSAGE = "EVENT_MESSAGE";
    private final String EVENT_CONNECTION = "EVENT_CONNECTION";
    private final String EVENT_SEND_UPDATE = "SEND_UPDATE";
    private boolean isPaused;
    private EditText txtUserName;
    private Button btnConnect;
    private EditText txtInfo;
    private String USER;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        txtUserName = (EditText)findViewById(R.id.txtUser);
        btnConnect = (Button) findViewById(R.id.btnConnect);
        txtInfo = (EditText)findViewById(R.id.txtInfo);
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String lastUser = sharedPreferences.getString("lastuser","Name");
        txtUserName.setText(lastUser);

        if(!lastUser.equals("Name"))
        {
            USER = lastUser;
            Intent intent = new Intent(getApplicationContext(),CellEyeService.class);
            intent.putExtra("user",USER);
            startService(intent);
            moveTaskToBack(true);
        }

        BroadcastReceiver cellEyeServiceReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals(EVENT_MESSAGE))
                {
                    String message = intent.getStringExtra("message");
                    boolean error = intent.getBooleanExtra("error",false);
                    boolean persist = intent.getBooleanExtra("persist",false);

                    if(error)
                    {
                        if(persist)
                        {
                            progressDialog = new ProgressDialog(MainActivity.this);
                            progressDialog.setTitle("Interesting message");
                            progressDialog.setMessage(message);
                            progressDialog.show();
                        }
                        else
                        {
                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle("Error")
                                    .setMessage(message)
                                    .create()
                                    .show();
                        }
                    }
                    else
                    {
                        if(progressDialog!=null && progressDialog.isShowing()) progressDialog.hide();
                        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_LONG).show();
                    }
                }
                else if(intent.getAction().equals(EVENT_CONNECTION))
                {
                    boolean connected = intent.getBooleanExtra("connected",false);
                    if(connected)
                    {
                        btnConnect.setText("CONNECTED");
                        btnConnect.setBackgroundColor(Color.GREEN);
                        if(progressDialog!=null && progressDialog.isShowing()) progressDialog.hide();
                    }
                    else
                    {
                        btnConnect.setText("CONNECT");
                        btnConnect.setBackgroundColor(Color.RED);
                    }
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(EVENT_MESSAGE);
        filter.addAction(EVENT_CONNECTION);
        this.registerReceiver(cellEyeServiceReceiver, filter);

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Util.hideSoftKeyboard(MainActivity.this);
                String user = txtUserName.getText().toString();
                if(TextUtils.isEmpty(user))
                {
                    new AlertDialog.Builder(getBaseContext())
                            .setTitle("Error")
                            .setMessage("Enter a username first")
                            .create().show();
                    txtUserName.requestFocus();
                }
                else
                {
                    USER = user.toUpperCase();
                    txtInfo.setText("");
                    txtInfo.setText("Connecting with user "+user+"...\n");
                    SharedPreferences.Editor edit = sharedPreferences.edit();
                    edit.putString("lastuser",user);
                    edit.commit();
                    Intent intent = new Intent(getApplicationContext(),CellEyeService.class);
                    intent.putExtra("user",USER);
                    startService(intent);
                    //moveTaskToBack(true);
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        isPaused = true;
    }

    @Override
    protected void onResume() {
        super.onPause();
        isPaused = false;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        Log.e(TAG,"ERROR>>>>>>>>>>>>>>>>>>",throwable);
    }
}
