package e.adwaya.myapplication;


import android.app.Service;
import android.content.Intent;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Adwaya on 6/1/2018.
 */

public class LocationService extends Service implements GoogleApiClient.ConnectionCallbacks, com.google.android.gms.location.LocationListener, GoogleApiClient.OnConnectionFailedListener {

private static final long INTERVAL = 500*2;
private static final long FASTEST_INTERVAL=500;
LocationRequest mLocationRequest;
GoogleApiClient mGoogleApiClient;
Location mCurrentLocation, lStart, lEnd;
static double distance=0.0;
public static List timeStore = new ArrayList();
public static List smallDis = new ArrayList();
public static List cumulativeDis=new ArrayList();
public double prevDis=0;
public static double stepMeasure=0;
public static final double distanceAnomaly=18;//height of tallest man in the world
public static double distanceToSubtract=0;
public static double distanceToAdd=0;
static final double distToWalk=100.00;
static final long timeToWalkBefore=5;
public static int counter=0;
public int counterCounter=0;

private final IBinder mBinder = new LocalBinder();
@Nullable
@Override
public IBinder onBind(Intent intent)
{
createLocationRequest();
mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API).addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();
mGoogleApiClient.connect();
return mBinder;
}

private void createLocationRequest()
    {
        mLocationRequest= new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }
@Override
public void onConnected(@Nullable Bundle bundle)
{
    try
    {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,this);
    }
    catch(SecurityException e)
    {
        ;
    }
}

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        MainActivity.progressDialog.dismiss();
        mCurrentLocation = location;
        if (lStart == null) {
            lStart = lEnd = mCurrentLocation;

        } else
            lEnd = mCurrentLocation;
        updateUI();
    }

    private void updateUI() {
      //  String stepsNow = String.valueOf(MainActivity.stepCountTextView.getText());
      //  double steps = Double.parseDouble(stepsNow);
        if (MainActivity.p == 0 /*&& steps>=stepToWalkBefore*/) {
            distance += (lStart.distanceTo(lEnd));
            MainActivity.endTime = System.currentTimeMillis();
            long diff = MainActivity.endTime - MainActivity.startTime;
            diff = TimeUnit.MILLISECONDS.toSeconds(diff);
            smallDis.add(distance-prevDis);
            cumulativeDis.add(distance);
            timeStore.add(diff);
//            if(distance>=distanceAnomaly && diff==timeToWalkBefore)
//            {
//                //counter++;
//                distanceToSubtract=distance;
//                distanceToAdd=MainActivity.stepCounter;
//
//              //  System.exit(0);
//            }

            MainActivity.timeDiff=diff;
           // if (distance <= distToWalk) {
                MainActivity.distanceTextView.setText(new DecimalFormat("#.###").format(distance) + " meters" + "  Time : " + diff + " secs");

                if (MainActivity.stepCounter != 0) {
                    stepMeasure = distance / (MainActivity.stepCounter);
                    MainActivity.stepMeasureTextView.setText(stepMeasure + " meters");
              }

            //}
            lStart = lEnd;
                prevDis=distance;
//            else
//            {
////                MainActivity mm=new MainActivity();
//                MainActivity.unbindService();
//            }
        }
    }

    public boolean onUnBind(Intent intent)
    {
        stopLocationUpdates();
        if(mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();

        lStart=lEnd=null;
        distance=0;
        return super.onUnbind(intent);

    }
    private void stopLocationUpdates()
    {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        distance=0;
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public class LocalBinder extends Binder {
    public LocationService getService()
    {
        return LocationService.this;
    }
}











}
