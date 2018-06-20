package e.adwaya.myapplication;

import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.text.DecimalFormat;

public class Main2Activity extends AppCompatActivity {

    private static DecimalFormat df2 = new DecimalFormat(".##");
    public static final double distOverEst=2.0;//earlier 2.25
    public static final double distUnderEst=0.60;
    public static int counterOver=0;
    public static int counterUnder=0;
    public double stepMeasure=0.0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        TextView disDataTV=findViewById(R.id.disDataTV);
        TextView finalDataTV=findViewById(R.id.finalDataTV);
        double totalDist=0;
        double time=0;
        disDataTV.setText("Tme(s)\tSmallDis\tCumulDis\n");
        for(int i=0;i<LocationService.smallDis.size();i++)
        {
            double distDiff=(double)LocationService.smallDis.get(i);
            if(i==0)
            {
                long timestamp=(long)LocationService.timeStore.get(i);
                if(timestamp==0)
                {
                    counterUnder--;
                }
            }
            if(distDiff>distOverEst)
            {
                counterOver++;
            }
            else if(distDiff<=distUnderEst)
            {
                counterUnder++;
            }
            else
            {
                totalDist+=distDiff;

                    time++;

            }
            disDataTV.setText(disDataTV.getText().toString()+"  "+df2.format(LocationService.timeStore.get(i))+"   \t "+df2.format(LocationService.smallDis.get(i))+"   \t " + df2.format(LocationService.cumulativeDis.get(i))+"   \n");
        }

        double averageDistDiff=totalDist/time;
        double distToAdd=(counterOver+counterUnder)*averageDistDiff;
        totalDist=totalDist+distToAdd;
//        double distanceNow = LocationService.distance;
//
//        distanceNow-=LocationService.distanceToSubtract;
//        LocationService.distanceToAdd*=LocationService.stepMeasure;
//        distanceNow+=LocationService.distanceToAdd;
        //distanceTextView.setText(distanceNow+"");
        stepMeasure=totalDist/MainActivity.stepCounter;
        finalDataTV.setText("Distance Travelled:\n " + new DecimalFormat("#.###").format(totalDist) + " meters\n"+ "  Time : \n" + MainActivity.timeDiff + " secs\n" + "No. of steps taken: \n" + MainActivity.stepCounter + "\nLength per step: \n" + stepMeasure + " meters/step");

    }
}
