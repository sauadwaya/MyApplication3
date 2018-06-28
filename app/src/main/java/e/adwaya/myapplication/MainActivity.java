package e.adwaya.myapplication;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.TriggerEvent;
import android.hardware.TriggerEventListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

import static android.hardware.SensorManager.*;

public class MainActivity extends AppCompatActivity implements SensorEventListener
         {
             private SensorManager mSensorManager;
             private Sensor mProximity;
             static boolean status;
    static int p=0;
    public static long timeDiff=0;
    public static double stepCounter=0;
    static long startTime, endTime;
    static ProgressDialog progressDialog;
    LocationManager locationManager;
    static Button startButton, stopButton;
    static TextView distanceTextView;
    static TextView stepCountTextView;
    static TextView stepMeasureTextView;
    SensorManager sensorManager;
    boolean running = false;
    LocationService myService;
    static int check=0;
    public static double counterSteps=0;
    final String TAG = "MainActivity";
    private ServiceConnection sc=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
          LocationService.LocalBinder binder = (LocationService.LocalBinder) service;
          myService=binder.getService();
          status=true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        status=false;
        }
    };
    protected void onDestroy()
    {
        if(status==true)
            unbindService();
        super.onDestroy();

    }
    public void unbindService()
    {
        if(status==false)
            return;
        Intent i=new Intent(getApplicationContext(),LocationService.class);
        unbindService(sc);
        status=false;
    }
    public void onBackPressed()
    {
        if(status==false)
            super.onBackPressed();
        else
            moveTaskToBack(true);
    }

    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults)
    {
        switch(requestCode)
        {
            case 1000:
            {
                if(grantResults.length >0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
                    Toast.makeText(this,"GRANTED",Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(this,"DENIED",Toast.LENGTH_SHORT).show();

            }
            return;

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//does not allow phone to go to sleep...
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            },1000);
        }
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        startButton = (Button) findViewById(R.id.startButton);
        stopButton = (Button) findViewById(R.id.stopButton);
        distanceTextView=(TextView)findViewById(R.id.distanceTextView);
        stepCountTextView=(TextView)findViewById(R.id.stepCountTextView);
        stepMeasureTextView=(TextView)findViewById(R.id.stepMeasureTextView);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                p=0;
                checkGPS();
                onResume();
                locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                    return;
                if (status == false)
                    bindService();
                progressDialog = new ProgressDialog(MainActivity.this);
                progressDialog.setIndeterminate(true);
                progressDialog.setCancelable(false);
                progressDialog.setMessage("Getting location...");
                progressDialog.show();

                //startButton.setVisibility(View.GONE);
            }
            });


         stopButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 MainActivity.p=1;
                 MainActivity.counterSteps=0;
                 LocationService.counter=0;
                 MainActivity.endTime = System.currentTimeMillis();
                 long diff = MainActivity.endTime - MainActivity.startTime;
                 diff = TimeUnit.MILLISECONDS.toSeconds(diff);
                 MainActivity.timeDiff=diff;

                 Intent i2=new Intent(getApplicationContext(),Main2Activity.class);
                 startActivity(i2);

             }
         });


    }
    private void checkGPS() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            showGPSDisabledAlert();
    }

    private void showGPSDisabledAlert()
    {
        AlertDialog.Builder alertDialogBuilder =new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Enable GPS to use application").setCancelable(false).setPositiveButton("Enable GPS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });
        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialogInterface, int i)
            {
                dialogInterface.cancel();
            }
        });

        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }
    private void bindService()
    {
        if(status==true)
           return;
        Intent i = new Intent(getApplicationContext(), LocationService.class);
        bindService(i,sc,BIND_AUTO_CREATE);
        status=true;
        startTime=System.currentTimeMillis();

    }
    @Override
    protected void onResume()
    {
        super.onResume();

            running = true;
        mSensorManager.registerListener(this, mProximity, SensorManager.SENSOR_DELAY_NORMAL);
            Sensor countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            if (countSensor != null) {
                sensorManager.registerListener(this, countSensor, SENSOR_DELAY_UI);
            } else {
                Toast.makeText(this, "Step Sensor Disabled", Toast.LENGTH_SHORT).show();
            }

    }
    @Override
    protected void onPause()
    {
        super.onPause();

        running=false;
        sensorManager.unregisterListener(this);
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            float distance = event.values[0];
            if(distance>4.0)
            {
                startButton.setEnabled(true);
                stopButton.setEnabled(true);
            }
            else
            {
                startButton.setEnabled(false);
                stopButton.setEnabled(false);
            }
            //

        } if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER){
            if (counterSteps < 1) {
                counterSteps = (double) (event.values[0]);
            }

        if (running && p == 0) {
            stepCounter = (double) (event.values[0]) - counterSteps;
            stepCountTextView.setText(stepCounter + "");

        }
    }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
