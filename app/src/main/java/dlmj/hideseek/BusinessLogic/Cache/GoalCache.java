package dlmj.hideseek.BusinessLogic.Cache;

import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import dlmj.hideseek.Common.Model.Goal;
import dlmj.hideseek.Common.Util.GoalComparator;
import dlmj.hideseek.Common.Util.LogUtil;
import dlmj.hideseek.Common.Util.SharedPreferenceUtil;

/**
 * Created by Two on 5/17/16.
 */
public class GoalCache extends BaseCache<Goal>{
    private static final String TAG = "GoalCache";
    private static GoalCache mInstance;
    private String mUpdateTime;
    private List<Goal> mUpdateList = new LinkedList<>();
    private Goal mClosestGoal;
    private Goal mSelectedGoal;
    private SharedPreferences mSharedPreferences;
    private boolean mHasSelectMushroom = false;
    private boolean mIfNeedClearMap = false;

    public static GoalCache getInstance(){
        synchronized (UserCache.class){
            if(mInstance == null){
                mInstance = new GoalCache();
            }
        }
        return mInstance;
    }

    public GoalCache() {
        super();
        mSharedPreferences = SharedPreferenceUtil.getSharedPreferences();
    }

    public void clearData() {
        mUpdateTime = null;
        mList.clear();
        mIfNeedClearMap = true;
    }

    public void setGoals(String goalsStr, double latitude, double longitude) {
        mUpdateList.clear();
        mList = from(goalsStr);
        GoalComparator goalComparator = new GoalComparator(latitude, longitude);

        if(mList.size() > 0) {
            do{
                mClosestGoal =  Collections.min(mList, goalComparator);

                if(!mClosestGoal.getValid()) {
                    removeItem(mClosestGoal);
                }
            } while(!mClosestGoal.getValid() && mList.size() != 0);
        }
        mIfNeedClearMap = false;
    }

    public List<Goal> from(String goalsStr) {
        try {
            JSONObject jsonObject = new JSONObject(goalsStr);
            String goalListStr = jsonObject.getString("goals");
            JSONArray goalList = new JSONArray(goalListStr);
            String goalStr;
            for (int i = 0; i < goalList.length(); i++) {
                goalStr = goalList.getString(i);
                JSONObject goal = new JSONObject(goalStr);
                Goal tempGoal = new Goal(
                        goal.getLong("pk_id"),
                        goal.getDouble("latitude"),
                        goal.getDouble("longitude"),
                        goal.getInt("orientation"),
                        goal.getInt("valid") == 1,
                        Goal.GoalTypeEnum.valueOf(goal.getInt("type")),
                        goal.getInt("is_enabled") == 1);
                mUpdateList.add(tempGoal);
                if(tempGoal.getValid()) {
                    mList.add(tempGoal);
                }
            }
            mUpdateTime = jsonObject.getString("update_time");
        } catch (JSONException e) {
            LogUtil.e(TAG, e.getMessage());
            e.printStackTrace();
        }

        return mList;
    }

    public boolean getHasSelectMushroom() {
        return mHasSelectMushroom;
    }

    public void setHasSelectMushroom(boolean hasSelectMushroom) {
        this.mHasSelectMushroom = hasSelectMushroom;
    }

    public String getUpdateTime() {
        return mUpdateTime;
    }

    public List<Goal> getUpdateList() {
        return mUpdateList;
    }

    public void setSelectedGoal(Goal goal) {
        this.mSelectedGoal = goal;
    }

    public Goal getSelectedGoal() {
        if(mSelectedGoal == null && mClosestGoal != null) {
            mClosestGoal.setIsSelected(true);
            return mClosestGoal;
        }

        return mSelectedGoal;
    }

    public boolean getIfNeedClearMap() {
        return mIfNeedClearMap;
    }

    public void refreshClosestGoal(double latitude, double longitude) {
        GoalComparator goalComparator = new GoalComparator(latitude, longitude);

        if(mList.size() > 0) {
            do{
                mClosestGoal =  Collections.min(mList, goalComparator);

                if(!mClosestGoal.getValid()) {
                    removeItem(mClosestGoal);
                }
            } while(!mClosestGoal.getValid() && mList.size() != 0);
        }
    }
}
