package dlmj.hideseek.Common.Model;

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

    public Goal(long pkId, double latitude, double longitude, int orientation, boolean valid,
                GoalTypeEnum type, boolean isEnabled) {
        this.mPkId = pkId;
        this.mLatitude = latitude;
        this.mLongitude = longitude;
        this.mOrientation = orientation;
        this.mValid = valid;
        this.mType = type;
        this.mIsEnabled = isEnabled;
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

    public enum GoalTypeEnum {
        mushroom(1), monster(2), bomb(3);

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

        public int toDrawable() {
            switch(this) {
                case mushroom:
                    return R.drawable.mushroom;
                case monster:
                    return R.drawable.monster;
                case bomb:
                    return R.drawable.explode;
                default:
                    return 0;
            }
        }

        public int getValue() {
            return value;
        }
    }
}
