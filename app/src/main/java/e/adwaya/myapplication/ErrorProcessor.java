package e.adwaya.myapplication;

import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.text.DecimalFormat;

public class ErrorProcessor extends AppCompatActivity {

    private static DecimalFormat df2 = new DecimalFormat(".##");
    public static final double distOverEst=1.8;//earlier 2.25//later earlier 2.0
    public static final double distUnderEst=0.40;//earlier 0.6
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
        double freq=0;
        double timeSpaN=1;
        long prevTime=(long)MainActivity.timeStore.get(0);
        disDataTV.setText("Tme(s)\tSmallDis\tCumulDis\n");
        for(int i=1;i<MainActivity.smallDis.size();i++)
        {
            double distDiff=(double)MainActivity.smallDis.get(i);
            long currentTime = (long)MainActivity.timeStore.get(i);
            if(Math.abs(currentTime-prevTime)>2)
            {
                timeSpaN=Math.abs(currentTime-prevTime);
            }
            if(distDiff>distOverEst*timeSpaN )
            {
                if(timeSpaN==1)
                counterOver++;
                else
                    counterOver+=timeSpaN;
            }
            else if(distDiff<=distUnderEst*timeSpaN && (i>=3 && i<=MainActivity.smallDis.size()-2))
            {
                if(timeSpaN==1)
                counterUnder++;
                else
                    counterUnder+=timeSpaN;
            }
            else
            {
                totalDist+=distDiff;

                freq++;

            }
            disDataTV.setText(disDataTV.getText().toString()+"  "+df2.format(MainActivity.timeStore.get(i))+"   \t "+df2.format(MainActivity.smallDis.get(i))+"   \t " + df2.format(MainActivity.cumulativeDis.get(i))+"   \t" + df2.format(MainActivity.accuracyReads.get(i)) + " \t" + counterOver + "\t "+ counterUnder +" \n");
            prevTime=currentTime;
            timeSpaN=1;
        }


        double averageDistDiff=totalDist/freq;
        double distToAdd=(counterOver+counterUnder)*averageDistDiff;
        totalDist=totalDist+distToAdd;
        if(MainActivity.stepCounter==0)
        {
            totalDist=0;
        }
//        double distanceNow = MainActivity.distance;
//
//        distanceNow-=MainActivity.distanceToSubtract;
//        MainActivity.distanceToAdd*=MainActivity.stepMeasure;
//        distanceNow+=MainActivity.distanceToAdd;
        //distanceTextView.setText(distanceNow+"");
        stepMeasure=totalDist/MainActivity.stepCounter;
        finalDataTV.setText("Distance Travelled:\n " + new DecimalFormat("#.###").format(totalDist) + " meters\n"+ "  Time : \n" + MainActivity.timeDiff + " secs\n" + "No. of steps taken: \n" + MainActivity.stepCounter + "\nLength per step: \n" + stepMeasure + " meters/step" + "\nDistToAdd: " + df2.format(distToAdd)+ "\nFreq: " + freq + "\n No. of irregularities: " + (counterUnder+counterOver));

    }
}
