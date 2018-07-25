package e.adwaya.myapplication;

import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.text.DecimalFormat;

public class ErrorProcessor extends AppCompatActivity {

    private static DecimalFormat df2 = new DecimalFormat(".##");              //format to show results originally in "double" form with unnecessary decimal places
    public static final double distOverEst=1.8;//earlier 2.25//later earlier 2.0     // the maximum distance considered to be valid for a certain timestamp (usually 1 seconds)
    public static final double distUnderEst=0.40;//earlier 0.6                       // the minimum distance considered to be valid for a certain timestamp

    //Any data outside the boundaries set by distUnderEst and distOverEst are considered to be bad data and otherwise data is good data

    //The distUnderEst and distOverEst were chosen as they are estimated to represent the minimum and maximum probable walking speed of blind people (in meters/second)


    // For each timestamp interval (usually one second, since GPS update comes at one second intervals), if the distance measured to be travelled is outside the boundaries set by distOverEst and distUnderEst, then that data is considered to be erroneous. To account for this error, all the distances in the timestamps where the distance is within this boundary are added up and average over the number of times they occur.
    // This average is multiplied with the number of times the distance returned was outside these set boundaries, and finally added to the total distance already obtained by adding up all the good data, to get the best possible estimation of the distance covered





    public static int counterOver=0;                                                 //No. of occurrences where the distance travelled may have been overestimated
    public static int counterUnder=0;                                                // No. of occurrences where the distance travelled may have been underestimated
    public double stepMeasure=0.0;                                                   // The corrected stepMeasure ..the final desired result
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        //setting up the textviews....finalDataTV shows all the important results...notably the distance which was estimated to be covered (after the processing is accomplished), the number of steps walked, and the step measure

        TextView disDataTV=findViewById(R.id.disDataTV);
        TextView finalDataTV=findViewById(R.id.finalDataTV);
        TextView stepAnalyzer =(TextView)findViewById(R.id.stepAnalyzer);


        double totalDist=0;                       //total processed distance covered
        double freq=0;                            //frequency of good smallDistance data (distance covered in each second, obtained from the smallDis array. Timestamp intervals (usually one second) are also obtained from the timeStamps array list), which is later averaged over the times they occur to account for the cases where the data is erroneous
        double timeSpaN=1;                        //In case the time interval is not always one seconds (sometimes more than one distance has been returned to be covered within the same second and sometimes data is returned at 2 seconds intervals...Usually they account for each other so the timestamps were probably recorded due to the lack of precision of the System clock
        long prevTime=(long)MainActivity.timeStore.get(0);
        disDataTV.setText("Tme(s)\tSmallDis\tCumulDis\n");  //Shows all raw data to help in determining data processing method during experiments


        //Data processing begins

        for(int i=1;i<MainActivity.smallDis.size();i++)
        {
            double distDiff=(double)MainActivity.smallDis.get(i);
            long currentTime = (long)MainActivity.timeStore.get(i);
            double steps = (double)MainActivity.stepReads.get(i);
            if(Math.abs(currentTime-prevTime)>2)
            {
                timeSpaN=Math.abs(currentTime-prevTime);
            }
            if(distDiff>distOverEst*timeSpaN )
            {
                //Overestimation
                if(timeSpaN==1)
                counterOver++;
                else
                    counterOver+=timeSpaN;
            }
            else if(distDiff<=distUnderEst*timeSpaN && (i>=3 && i<=MainActivity.smallDis.size()-2))
            {
                //Underestimation

                if(timeSpaN==1)
                counterUnder++;
                else
                    counterUnder+=timeSpaN;
            }
            else
            {
                //Correct Data

                totalDist+=distDiff;

                freq++;

            }
            disDataTV.setText(disDataTV.getText().toString()+"  "+df2.format(MainActivity.timeStore.get(i))+" \t "+df2.format(MainActivity.smallDis.get(i))+" \t " + df2.format(MainActivity.cumulativeDis.get(i))+" \t" + df2.format(MainActivity.accuracyReads.get(i)) + "\t" + counterOver + "\t "+ counterUnder +"\t" +df2.format(steps) + " \n");
            prevTime=currentTime;
            timeSpaN=1;
        }


        double averageDistDiff=totalDist/freq;                           //Average correct data for each second interval
        double distToAdd=(counterOver+counterUnder)*averageDistDiff;     //Distance which is to account for the intervals during which the data was erroneous
        totalDist=totalDist+distToAdd;                                   // Erroneous data accounted for and added to get final estimated distance travelled
        if(MainActivity.stepCounter==0)                                  // If no steps were taken,then distance travelled is zero
        {
            totalDist=0;
        }
        stepMeasure=totalDist/MainActivity.stepCounter;                   // Desired step measure to be used as a constant in Better Navigation App

        //Final Data is shown

        finalDataTV.setText("Distance Travelled:\n " + new DecimalFormat("#.###").format(totalDist) + " meters\n"+ "  Time : \n" + MainActivity.timeDiff + " secs\n" + "No. of steps taken: \n" + MainActivity.stepCounter + "\nLength per step: \n" + stepMeasure + " meters/step" + "\nDistToAdd: " + df2.format(distToAdd)+ "\nFreq: " + freq + "\n No. of irregularities: " + (counterUnder+counterOver));

    }
}
