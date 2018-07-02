package e.adwaya.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.location.LocationListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static android.hardware.SensorManager.*;

public class MainActivity extends AppCompatActivity implements SensorEventListener
         {
             //Global variables

             private static final long INTERVAL = 500*2; //stores GPS refresh rate
             Location mCurrentLocation, lStart, lEnd;  //stores starting location, end location, current location for each GPS call
             public static int count=0;
             static double distance=0.0;   //stores distance for each GPS update
             public static List timeStore = new ArrayList();  //stores time difference between each update
             public static List smallDis = new ArrayList();
             public static List cumulativeDis=new ArrayList();
             public static List accuracyReads=new ArrayList();
             public static List stepReads=new ArrayList();
             public double prevDis=0;
             public static double stepMeasure=0;
             public static int count2=0;
             public static float accuracy=30;
             public static TextToSpeech textToSpeech;
             private SensorManager mSensorManager;
             private Sensor mProximity;
    static int p=0;
    public float distanceProx=5;
    public static long timeDiff=0;
    public static double stepCounter=0;
    static long startTime, endTime;
             LocationManager locationManager;
             LocationListener locationListener;

             static Button startButton;
    static TextView distanceTextView;
    static TextView stepCountTextView;
    static TextView stepMeasureTextView;
    SensorManager sensorManager;
    boolean running = false;
    Criteria criteria;
    public static double counterSteps=0;

    protected void onDestroy()
    {

        sensorManager.unregisterListener(this);
        mSensorManager.unregisterListener(this);
        super.onDestroy();

    }





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//does not allow phone to go to sleep...
       // stopButton.setVisibility(View.INVISIBLE);


        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status==TextToSpeech.SUCCESS)
                {
                    int result=textToSpeech.setLanguage(Locale.CANADA);
                    if(result== TextToSpeech.LANG_MISSING_DATA || result== TextToSpeech.LANG_NOT_SUPPORTED)
                    {
                        Toast.makeText(MainActivity.this,"This language is not supported", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        textToSpeech.setPitch(0.7f);
                        textToSpeech.setSpeechRate(1.0f);
                         speak();
                    }
                }
            }
        });
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        startButton = (Button) findViewById(R.id.startButton);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        distanceTextView=(TextView)findViewById(R.id.distanceTextView);
        stepCountTextView=(TextView)findViewById(R.id.stepCountTextView);
        stepMeasureTextView=(TextView)findViewById(R.id.stepMeasureTextView);
        criteria=new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_HIGH);
        criteria.setAltitudeRequired(false);
        criteria.setSpeedRequired(false);
        criteria.setCostAllowed(true);
        criteria.setBearingRequired(false);
        criteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
        criteria.setVerticalAccuracy(Criteria.ACCURACY_HIGH);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(startButton.getText().toString().equalsIgnoreCase("Start")) {
                    p = 0;
                    startButton.setText("Stop");

                    startButton.setEnabled(false);

                    locationDetails();
                }
                else
                {
                    if(locationListener!=null)
                        locationManager.removeUpdates(locationListener);

                    MainActivity.p=1;
                    MainActivity.counterSteps=0;
                    lStart=lEnd=null;
                    distance=0;
                    MainActivity.endTime = System.currentTimeMillis();
                    long diff = MainActivity.endTime - MainActivity.startTime;
                    diff = TimeUnit.MILLISECONDS.toSeconds(diff);
                    MainActivity.timeDiff=diff;
                    if(textToSpeech!=null)
                    {
                        textToSpeech.stop();
                        textToSpeech.shutdown();
                    }

                    Intent i2=new Intent(getApplicationContext(),ErrorProcessor.class);
                    startActivity(i2);


                }
            }
            });




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
    public static void speak()
    {
        String text=" Press the large button on the middle.";
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP)
            textToSpeech.speak(text,TextToSpeech.QUEUE_FLUSH,null,null);
        else
            textToSpeech.speak(text,TextToSpeech.QUEUE_FLUSH,null);

    }
    public static void speakOn()
             {
                 String text="DON'T START WALKING UNTIL INSTRUCTED. ";
                 if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP)
                     textToSpeech.speak(text,TextToSpeech.QUEUE_FLUSH,null,null);
                 else
                     textToSpeech.speak(text,TextToSpeech.QUEUE_FLUSH,null);

             }
    public static void AlternateSpeak()
             {
                 String text="Start Walking now. Press Stop when you want ";
                 if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP)
                     textToSpeech.speak(text,TextToSpeech.QUEUE_FLUSH,null,null);
                 else
                     textToSpeech.speak(text,TextToSpeech.QUEUE_FLUSH,null);

             }
             public static void AlternateSpeak2()
             {
                 String text="Start Walking now. Press Stop when you want ";
                 if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP)
                     textToSpeech.speak(text,TextToSpeech.QUEUE_ADD,null,null);
                 else
                     textToSpeech.speak(text,TextToSpeech.QUEUE_ADD,null);

             }

             public void locationDetails(){
                    //ensures that interval has been set

                     locationListener = new LocationListener() {
                         @Override
                         public void onLocationChanged(Location location) {
                             accuracy=location.getAccuracy();

                             if(accuracy<=4) {

                                 if(count==0) {
                                     if(count2==0)
                                         MainActivity.AlternateSpeak();
                                     else
                                         MainActivity.AlternateSpeak2();
                                     count2++;
                                     MainActivity.startButton.setEnabled(true);
                                    // MainActivity.startButton.setEnabled(true);
                                     MainActivity.startTime = System.currentTimeMillis();
                                     count++;
                                 }
                                 mCurrentLocation = location;
                                 if (lStart == null) {
                                     lStart = lEnd = mCurrentLocation;

                                 } else
                                     lEnd = mCurrentLocation;
                                 //accuracyReads.add(accuracy);//David's idea....

                                     if (MainActivity.p == 0 ) {
                                         distance += (lStart.distanceTo(lEnd));
                                         MainActivity.endTime = System.currentTimeMillis();
                                         long diff = MainActivity.endTime - MainActivity.startTime;
                                         diff = TimeUnit.MILLISECONDS.toSeconds(diff);
                                         smallDis.add(distance-prevDis);
                                         cumulativeDis.add(distance);
                                         timeStore.add(diff);
                                         stepReads.add(MainActivity.stepCounter);
                                         accuracyReads.add(accuracy);
                                         MainActivity.timeDiff=diff;

                                         MainActivity.distanceTextView.setText(new DecimalFormat("#.###").format(distance) + " meters" + "  Time : " + diff + " secs");

                                         if (MainActivity.stepCounter != 0) {
                                             stepMeasure = distance / (MainActivity.stepCounter);
                                             MainActivity.stepMeasureTextView.setText(stepMeasure + " meters");
                                         }


                                         lStart = lEnd;
                                         prevDis=distance;

                                     }

                             }
                             else
                             {
                                 if(count2==0) {
                                     count2++;

                                     MainActivity.speakOn();


                                 }
                                 //MainActivity.distanceTextView.setText(accuracy+"");
                                 if(count==0) {
                                     //MainActivity.speak();
                                     MainActivity.startButton.setEnabled(false);
                                 }
                             }

                         }

                         @Override
                         public void onStatusChanged(String provider, int status, Bundle extras) {
                             //not used right now
                         }

                         @Override
                         public void onProviderEnabled(String provider) {
                             //not used right now
                         }

                         @Override
                         public void onProviderDisabled(String provider) {
                            // TV1.setText("GPS permissions have been denied.\nNeed GPS permissions for app to function.");
                         }
                     };

                     if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                         Toast.makeText(this, "GPS not available", Toast.LENGTH_LONG);
                     }

                     //if at least Marshmallow, need to ask user's permission to get GPS data
                     if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                         //if permission is not yet granted, ask for it
                         if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                             requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
                             if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                 //if permission still not granted, tell user app will not work without it
                                 Toast.makeText(this, "Need GPS permissions for app to function", Toast.LENGTH_LONG);
                             }
                             //once permission is granted, set up location listener
                             //updating every UPDATE_INTERVAL milliseconds, regardless of distance change
                             else
                                 locationManager.requestLocationUpdates(locationManager.getBestProvider(criteria,true), INTERVAL, 0,locationListener);
                         } else
                             locationManager.requestLocationUpdates(locationManager.getBestProvider(criteria,true), INTERVAL, 0, locationListener);
                     } else {
                         assert locationManager != null;
                         locationManager.requestLocationUpdates(locationManager.getBestProvider(criteria,true), INTERVAL, 0, locationListener);
                     }


             }


    @Override
    protected void onPause()
    {
        super.onPause();

        running=false;

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
             distanceProx = event.values[0];
            if(distanceProx>4.0)
            {
                startButton.setEnabled(true);

            }
            else
            {
                startButton.setEnabled(false);
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
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
