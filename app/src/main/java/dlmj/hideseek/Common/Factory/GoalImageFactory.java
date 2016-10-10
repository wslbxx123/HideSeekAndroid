package dlmj.hideseek.Common.Factory;

import android.content.Context;
import android.content.res.Resources;

import dlmj.hideseek.Common.Model.Goal;
import dlmj.hideseek.R;

/**
 * Created by Two on 08/10/2016.
 */
public class GoalImageFactory {
    Context mContent;

    public GoalImageFactory(Context context){
        mContent = context;
    }

    public int get(Goal.GoalTypeEnum goalType, String showTypeName){
        switch(goalType) {
            case reward:
                return R.drawable.reward_exchange;
            case mushroom:
                return R.drawable.mushroom;
            case bomb:
                return R.drawable.bomb_src;
            default:
                return mContent.getResources().getIdentifier(showTypeName,
                        "drawable", mContent.getPackageName());
        }
    }
}
