package dlmj.hideseek.Common.Model;

/**
 * Created by Two on 5/1/16.
 */
public class RecordItem {
    private long mRecordId;
    private String mTime;
    private Goal.GoalTypeEnum mGoalType;
    private int mScore;
    private int mScoreSum;
    private long mVersion;

    public RecordItem(long recordId, String time, Goal.GoalTypeEnum goalType, int score,
                      int scoreSum, long version) {
        mRecordId = recordId;
        mTime = time;
        mGoalType = goalType;
        mScore = score;
        mScoreSum = scoreSum;
        mVersion = version;
    }

    public long getRecordId() {
        return mRecordId;
    }

    public String getTime() {
        return mTime;
    }

    public void setTime(String time) {
        mTime = time;
    }

    public Goal.GoalTypeEnum getGoalType() {
        return mGoalType;
    }

    public int getScore() {
        return mScore;
    }

    public int getScoreSum() {
        return mScoreSum;
    }

    public long getVersion() {
        return mVersion;
    }
}
