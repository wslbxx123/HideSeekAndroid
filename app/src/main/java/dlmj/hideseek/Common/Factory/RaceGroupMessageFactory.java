package dlmj.hideseek.Common.Factory;

import android.content.Context;

import dlmj.hideseek.Common.Model.Goal;
import dlmj.hideseek.R;

/**
 * Created by Two on 08/10/2016.
 */
public class RaceGroupMessageFactory {
    private Context mContext;

    public RaceGroupMessageFactory(Context context) {
        this.mContext = context;
    }

    public String get(Goal.GoalTypeEnum goalType, String showTypeName) {
        switch(goalType) {
            case mushroom:
                return mContext.getString(R.string.message_get_mushroom);
            case monster:
                return getMonsterMessage(showTypeName);
            case bomb:
                return mContext.getString(R.string.message_get_bomb);
            default:
                return "";
        }
    }

    public String getMonsterMessage(String showTypeName) {
        switch(showTypeName) {
            case "egg":
                return mContext.getString(R.string.message_get_egg);
            case "cow":
                return mContext.getString(R.string.message_get_cow);
            case "bird":
                return mContext.getString(R.string.message_get_bird);
            case "giraffe":
                return mContext.getString(R.string.message_get_giraffe);
            case "dragon":
                return mContext.getString(R.string.message_get_dragon);
            default:
                return "";
        }
    }
}
