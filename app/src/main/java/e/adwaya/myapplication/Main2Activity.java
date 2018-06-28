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
    public static double nSecs=0;
    public static long prevTimeStamp=0;
    public static double distMin=Double.MAX_VALUE;
    public static double distDiff;
    public static long timestamp;
    public static long timestamp2;
    public static double distDiff2;
    public static double timeKeeper;
    public static double distToUse;
    public static double distUnderConsd;
    public static double distFromideal;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        TextView disDataTV=findViewById(R.id.disDataTV);
        TextView finalDataTV=findViewById(R.id.finalDataTV);
        disDataTV.setText("Tme(s)\tSmallDis\tCumulDis\n");
        for(int i=0;i<LocationService.smallDis.size();i++)
        {
                 distDiff = (double) LocationService.smallDis.get(i);
                 timestamp = (long) LocationService.timeStore.get(i);
                 if(timestamp==prevTimeStamp && i!=0)
                 {
                     continue;
                 }

                 if(prevTimeStamp!=0 || i!=0) {
                nSecs = (double) (timestamp - prevTimeStamp);
                distUnderConsd = distDiff / nSecs;
                 distFromideal = Math.abs(distUnderConsd - ((distOverEst + distUnderEst) / 2));
                if (distFromideal <= distMin) {
                    distMin = distFromideal;
                    timeKeeper = timestamp;
                    distToUse = distUnderConsd;
                }
            }
                prevTimeStamp=timestamp;


        }
        prevTimeStamp=0;
        double totalDist=0;
        double totalCorrected=0;
        double totalRemains=0;
        for(int i=0;i<LocationService.smallDis.size();i++) {
            disDataTV.setText(disDataTV.getText().toString()+"  "+df2.format(LocationService.timeStore.get(i))+"   \t "+df2.format(LocationService.smallDis.get(i))+"   \t " + df2.format(LocationService.cumulativeDis.get(i))+"   \n");
            distDiff2 = (double) LocationService.smallDis.get(i);
             timestamp2 = (long) LocationService.timeStore.get(i);
            if (prevTimeStamp != 0 || i!=0) {
                nSecs = (int) (timestamp2 - prevTimeStamp);

                if (((distDiff2/nSecs) <= distUnderEst) || ((distDiff2/nSecs) >  distOverEst))

                {
                  totalCorrected+=(nSecs*distToUse);
                  totalDist+=(nSecs*distToUse);

                }
                else
                {
                    totalDist+=distDiff2;
                    totalRemains+=distDiff2;
                }
                prevTimeStamp=timestamp2;
            }
            else
            {
                prevTimeStamp=timestamp2;
            }

        }
        double finalDistAdd=(MainActivity.timeDiff-prevTimeStamp)*distToUse;
        totalDist+=finalDistAdd;
        if(distToUse<distUnderEst || distToUse>=distOverEst)
        {
            totalDist=(double)LocationService.cumulativeDis.get(LocationService.cumulativeDis.size()-1);
            //Is it better for the system to exit? ask....high chances of erroneous data
        }
        //If no steps are taken/detected, distance travelled must be zero
        if(MainActivity.stepCounter==0)
        {
            totalDist=0;
        }
        stepMeasure=totalDist/MainActivity.stepCounter;
        finalDataTV.setText("Distance Travelled:\n " + new DecimalFormat("#.###").format(totalDist) + " meters\n"+ "  Time : \n" + MainActivity.timeDiff + " secs\n" + "No. of steps taken: \n" + MainActivity.stepCounter + "\nLength per step: \n" + stepMeasure + " meters/step"+ "\nDistanceUsed: " + new DecimalFormat("#.###").format(distToUse) + "\nFinalDistToAdd: " + new DecimalFormat("#.###").format(finalDistAdd) + "\nTotalCorrected: " + new DecimalFormat("#.###").format(totalCorrected) + "\nTotal Not Corrected: " + new DecimalFormat("#.###").format(totalRemains));
//        double totalDist=0;
//        double time=0;
//        disDataTV.setText("Tme(s)\tSmallDis\tCumulDis\n");
//        for(int i=0;i<LocationService.smallDis.size();i++)
//        {
//            double distDiff=(double)LocationService.smallDis.get(i);
//            if(i==0)
//            {
//                long timestamp=(long)LocationService.timeStore.get(i);
//                if(timestamp==0)
//                {
//                    counterUnder--;
//                }
//            }
//            if(distDiff>distOverEst)
//            {
//                counterOver++;
//            }
//            else if(distDiff<=distUnderEst)
//            {
//                counterUnder++;
//            }
//            else
//            {
//                totalDist+=distDiff;
//
//                    time++;
//
//            }
//            disDataTV.setText(disDataTV.getText().toString()+"  "+df2.format(LocationService.timeStore.get(i))+"   \t "+df2.format(LocationService.smallDis.get(i))+"   \t " + df2.format(LocationService.cumulativeDis.get(i))+"   \n");
//        }
//
//        double averageDistDiff=totalDist/time;
//        double distToAdd=(counterOver+counterUnder)*averageDistDiff;
//        totalDist=totalDist+distToAdd;
//        stepMeasure=totalDist/MainActivity.stepCounter;
//        finalDataTV.setText("Distance Travelled:\n " + new DecimalFormat("#.###").format(totalDist) + " meters\n"+ "  Time : \n" + MainActivity.timeDiff + " secs\n" + "No. of steps taken: \n" + MainActivity.stepCounter + "\nLength per step: \n" + stepMeasure + " meters/step");

    }
}
