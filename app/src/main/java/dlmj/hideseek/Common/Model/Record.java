package dlmj.hideseek.Common.Model;

import java.util.List;

/**
 * Created by Two on 4/30/16.
 */
public class Record {
    private long mRecordId;
    private String mTime;
    private Goal.GoalTypeEnum mGoalType;
    private int mScore;
    private int mScoreSum;
    private long mVersion;
    private String mDate;

    public Record(long recordId, String time, Goal.GoalTypeEnum goalType, int score,
                  int scoreSum, long version, String date) {
        mRecordId = recordId;
        mTime = time;
        mGoalType = goalType;
        mScore = score;
        mScoreSum = scoreSum;
        mVersion = version;
        mDate = date;
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

    public String getDate() {
        return mDate;
    }

    public void setDate(String date) {
        mDate = date;
    }
}
