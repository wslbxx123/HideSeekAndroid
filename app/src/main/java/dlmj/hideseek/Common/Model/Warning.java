package dlmj.hideseek.Common.Model;

/**
 * Created by Two on 11/10/2016.
 */
public class Warning {
    private Goal mGoal;
    private String mCreateTime;

    public Warning(Goal goal, String createTime) {
        this.mGoal = goal;
        this.mCreateTime = createTime;
    }

    public Goal getGoal() {
        return mGoal;
    }

    public String getCreateTime() {
        return mCreateTime;
    }
}
