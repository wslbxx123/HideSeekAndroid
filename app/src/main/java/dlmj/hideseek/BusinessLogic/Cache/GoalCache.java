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
    private long mVersion;
    private List<Goal> mUpdateList = new LinkedList<>();
    private Goal mClosestGoal;
    private Goal mSelectedGoal;
    private SharedPreferences mSharedPreferences;
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

    public void setGoals(String goalsStr, double latitude, double longitude) {
        mUpdateList.clear();
        saveGoals(goalsStr);

        refreshClosestGoal(latitude, longitude);
        mIfNeedClearMap = false;
    }

    public void saveGoals(String goalsStr) {
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
                        goal.getString("show_type_name"),
                        goal.getLong("create_by"),
                        goal.getString("introduction"),
                        goal.getInt("score"),
                        goal.getInt("union_type"));
                mUpdateList.add(tempGoal);
                if(tempGoal.getValid()) {
                    mList.add(tempGoal);
                }
            }
            mVersion = jsonObject.getLong("version");
        } catch (JSONException e) {
            LogUtil.e(TAG, e.getMessage());
            e.printStackTrace();
        }
    }

    public void setIfNeedClearMap(boolean ifNeedClearMap) {
        mIfNeedClearMap = ifNeedClearMap;
    }

    public long getVersion() {
        return mVersion;
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

    public void reset() {
        if(mSelectedGoal != null) {
            mSelectedGoal.setIsSelected(false);
            mSelectedGoal = null;
        }
        mClosestGoal = null;
        mList.clear();
        mVersion = 0;
    }
}
