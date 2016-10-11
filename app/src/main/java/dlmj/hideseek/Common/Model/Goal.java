package dlmj.hideseek.Common.Model;

import android.content.Context;

import dlmj.hideseek.R;

/**
 * Created by Two on 5/17/16.
 */
public class Goal{
    private long mPkId;
    private double mLatitude;
    private double mLongitude;
    private int mOrientation;
    private boolean mIsSelected = false;
    private boolean mValid;
    private GoalTypeEnum mType;
    private boolean mIsEnabled;
    private String mShowTypeName;
    private long mCreateBy;
    private String mIntroduction;
    private int mScore;
    private int mUnionType;

    public Goal(long pkId, double latitude, double longitude, int orientation, boolean valid,
                GoalTypeEnum type, String showTypeName, long createBy, String introduction,
                int score, int unionType) {
        this.mPkId = pkId;
        this.mLatitude = latitude;
        this.mLongitude = longitude;
        this.mOrientation = orientation;
        this.mValid = valid;
        this.mType = type;
        this.mShowTypeName = showTypeName;
        this.mCreateBy = createBy;
        this.mIntroduction = introduction;
        this.mScore = score;
        this.mUnionType = unionType;
    }

    public long getPkId() {
        return mPkId;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public int getOrientation() {
        return mOrientation;
    }

    public void setIsSelected(boolean isSelected) {
        mIsSelected = isSelected;
    }

    public boolean getIsSelected() {
        return mIsSelected;
    }

    public boolean getValid() {
        return mValid;
    }

    public void setValid(boolean valid) {
        this.mValid = valid;
    }

    public GoalTypeEnum getType() {
        return mType;
    }

    public boolean isEnabled() {
        return mIsEnabled;
    }

    public String getShowTypeName() {
        return mShowTypeName;
    }

    public int getScore() {
        return mScore;
    }

    public String getGoalName(Context context) {
        switch(this.mType) {
            case mushroom:
                return context.getString(R.string.mushroom);
            case bomb:
                return context.getString(R.string.bomb);
            case monster:
                int identifier = context.getResources().getIdentifier(mShowTypeName, "string",
                        context.getPackageName());
                return context.getString(identifier);
            default:
                return "";
        }
    }

    public enum GoalTypeEnum {
        reward(0), mushroom(1), monster(2), bomb(3);

        private int value = 0;

        private GoalTypeEnum(int value) {    //    必须是private的，否则编译错误
            this.value = value;
        }

        public static GoalTypeEnum valueOf(int value) {
            switch (value) {
                case 1:
                    return mushroom;
                case 2:
                    return monster;
                case 3:
                    return bomb;
                default:
                    return null;
            }
        }

        public int getValue() {
            return value;
        }
    }
}
