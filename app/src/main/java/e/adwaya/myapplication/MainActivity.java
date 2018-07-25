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

             private static final long INTERVAL = 500*2;           //stores GPS refresh rate
             Location mCurrentLocation, lStart, lEnd;              //stores starting location, end location, current location for each GPS call
             public static int count=0;
             static double distance=0.0;                           //stores distance for each GPS update
             public static List timeStore = new ArrayList();       //stores time difference between each update
             public static List smallDis = new ArrayList();        // stores distance difference between each update
             public static List cumulativeDis=new ArrayList();     // stores cumulative distance at the end of each time update
             public static List accuracyReads=new ArrayList();     // stores the accuracy at each time update
             public static List stepReads=new ArrayList();         // stores the no. of steps between each time update
             public double prevDis=0;                              // stores the total distance covered by the end of the previous tmestamp, in order to calculate small distances covered between each timestamp
             public static double stepMeasure=0;                   // with each consecutive read, it stores the current measure of each step the user took
             public static int count2=0;                           // To maker sure the instruction to start walking (once GPS accuracy have been achieved) is said only once
             public static float accuracy=30;                      // stores the current accuracy of the location being reported
             public static TextToSpeech textToSpeech;              // text to speech object to give instructions to the user to find step length
             private SensorManager mSensorManager;                 // sensor manager to initiate the proximity sensor
             private Sensor mProximity;                            // the proximity sensor which is used to return the proximity of the screen to any object
             static int p=0;                                        // Value which denotes whether the user has completed a testing or not and if it is time to activate the ErrorProcessing class to get data once user has walked the desired distance (recommended at least walk for 10 seconds to get good results)
             public float distanceProx=5;                           // Stores the distance of minimum proximity allowed and if the proximity sensor returns any value less than 5 m, then the screen will be made untouchable so no other external factors come into play while the app is running
             public static long timeDiff=0;                         // stores the time difference between each update
             public static double stepCounter=0;                    // stores the number of steps walked by the user
             static long startTime, endTime;                      // stores the start and end time of the user
             LocationManager locationManager;                     // Location Manager to start location updates
             LocationListener locationListener;                   // location listener to listen for each new location

             static Button startButton;                            // Button to represent the START button which the user can click
    static TextView distanceTextView;                              // Text view to represent the distance covered by the user while running the app
    static TextView stepCountTextView;                             // Text view to represent the number of steps walked by the user
    static TextView stepMeasureTextView;                           // Text view to represent the step length of the user as the user walks
    SensorManager sensorManager;                                   // Sensor Manager to initiate the step counter sensor
    boolean running = false;                                       // boolean storing whether the app is running a test or not
    Criteria criteria;                                             // Criteria object to refine the gps call for most accurate location, as accurate as possible
    public static double counterSteps=0;                           // Store the initial number of steps since last reboot at the time the app is first opened. To get the correct step reading..
    public double prevSteps=0;                                     // Count the step count since the last timestamp so as to store how many steps were walked between each timestamp


             //un-registering all sensors onDestroy to prevent memory leak

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

        // declaring text to speech object
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
                        textToSpeech.setPitch(5.0f);
                        textToSpeech.setSpeechRate(1.0f);
                         speak();
                    }
                }
            }
        });

        //setting up all sensor managers, buttons, text-views, and criteria object

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

        //defining functionality of the start button and the process it should initiate once it is clicked by the user (Note: once the user presses the Start button, the text on it changes to "STOP" and the user can press the same button to stop the step-measuring process and proceed to obtain data)

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(startButton.getText().toString().equalsIgnoreCase("Start")) {
                    p = 0;
                    startButton.setText("Stop");

                    startButton.setEnabled(false);

                    //function which beings the testing process

                        locationDetails();
                }
                else
                {
                    //if here, the user has pressed stop and the ErrorProcessor class is ready to run to obtain data

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
                    if(timeStore.size()!=0) {

                        //starts the ErrorProcessor class once the user is done with the test

                        Intent i2 = new Intent(getApplicationContext(), ErrorProcessor.class);
                        startActivity(i2);
                    }

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

   //declaring the different text instructions the user should get under different situations

    public static void speak()
    {
        String text="Press the large button on the middle to start.";
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


             //Method started once the user is ready to start the test

             public void locationDetails(){
                    //ensures that interval has been set
                    // Until a location update is received whose accuracy is less than or equal to 4m, the user is told to wait
                     locationListener = new LocationListener() {
                         @Override
                         public void onLocationChanged(Location location) {
                             accuracy=location.getAccuracy();

                             //If accuracy of the received location is less than 4m, then the user is instructed to start walking


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


                                 //stores distance, steps, and time covered with each location update
                                     if (MainActivity.p == 0 ) {
                                         distance += (lStart.distanceTo(lEnd));
                                         MainActivity.endTime = System.currentTimeMillis();
                                         long diff = MainActivity.endTime - MainActivity.startTime;
                                         diff = TimeUnit.MILLISECONDS.toSeconds(diff);
                                         smallDis.add(distance-prevDis);
                                         cumulativeDis.add(distance);
                                         timeStore.add(diff);
                                         double noOfSteps=MainActivity.stepCounter;
                                         stepReads.add(noOfSteps-prevSteps);
                                         accuracyReads.add(accuracy);
                                         MainActivity.timeDiff=diff;

                                         MainActivity.distanceTextView.setText(new DecimalFormat("#.###").format(distance) + " meters" + "  Time : " + diff + " secs");

                                         if (MainActivity.stepCounter != 0) {
                                             stepMeasure = distance / (MainActivity.stepCounter);
                                             MainActivity.stepMeasureTextView.setText(stepMeasure + " meters");
                                         }


                                         lStart = lEnd;
                                         prevDis=distance;
                                         prevSteps=noOfSteps;
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


                     //Code to handle the GPS updates at an interval of 1 second


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

        // handles the proximity sensor, making sure the screen cannot be accidentally touched when the phone is kept in a pocket
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


        }

        //handles the step counting process
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER){
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
