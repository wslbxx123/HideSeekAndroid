package dlmj.hideseek.Common.Util;

import java.util.Comparator;

import dlmj.hideseek.Common.Model.Goal;

/**
 * Created by Two on 5/18/16.
 */
public class GoalComparator implements Comparator<Goal> {
    private double mLatitude, mLongitude;

    public GoalComparator(double latitude, double longitude) {
        this.mLatitude = latitude;
        this.mLongitude = longitude;
    }

    @Override
    public int compare(Goal goal, Goal goal2) {
        double distance = Math.pow(goal.getLatitude() - mLatitude, 2) +
                Math.pow(goal.getLongitude() - mLongitude, 2);
        double distance2 = Math.pow(goal2.getLatitude() - mLatitude, 2) +
                Math.pow(goal2.getLongitude() - mLongitude, 2);

        if(distance > distance2) {
            return 1;
        } else {
            return -1;
        }
    }
}
