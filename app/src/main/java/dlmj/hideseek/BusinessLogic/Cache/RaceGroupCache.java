package dlmj.hideseek.BusinessLogic.Cache;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

import dlmj.hideseek.Common.Model.Goal;
import dlmj.hideseek.Common.Model.RaceGroup;
import dlmj.hideseek.Common.Model.RecordItem;

import dlmj.hideseek.Common.Util.LogUtil;
import dlmj.hideseek.DataAccess.RaceGroupTableManager;

/**
 * Created by Two on 6/2/16.
 */
public class RaceGroupCache extends BaseCache<RaceGroup>{
    private final static String TAG = "RaceGroupCache";
    private static RaceGroupCache mInstance;
    private RaceGroupTableManager mRaceGroupTableManager;
    private long mVersion = 0;

    public static RaceGroupCache getInstance(Context context){
        synchronized (RaceGroupCache.class){
            if(mInstance == null){
                mInstance = new RaceGroupCache(context);
            }
        }
        return mInstance;
    }

    public RaceGroupCache(Context context) {
        super();
        mRaceGroupTableManager = RaceGroupTableManager.getInstance(context);
    }

    public List<RaceGroup> getList() {
        if(super.getList().size() > 0) {
            return mList;
        }

        mList = mRaceGroupTableManager.searchRaceGroup();
        return mList;
    }

    public boolean getMoreRaceGroup(int count, boolean hasLoaded) {
        List<RaceGroup> raceGroupList = mRaceGroupTableManager.getMoreRaceGroup(count, mVersion, hasLoaded);

        mList.addAll(raceGroupList);
        return raceGroupList.size() > 0;
    }

    public void setRaceGroup(String raceGroupStr) {
        saveRaceGroup(raceGroupStr);

        mList = mRaceGroupTableManager.searchRaceGroup();
        mVersion = mRaceGroupTableManager.getVersion();
    }

    public void saveRaceGroup(String raceGroupStr) {
        List<RaceGroup> list = new LinkedList<>();
        try {
            JSONObject result = new JSONObject(raceGroupStr);
            String raceGroup = result.getString("race_group");

            long version = result.has("version") ? result.getLong("version") :
                    mRaceGroupTableManager.getVersion();
            long recordMinId = result.getLong("record_min_id");
            JSONArray recordList = new JSONArray(raceGroup);
            String recordStr;
            for (int i = 0; i < recordList.length(); i++) {
                recordStr = recordList.getString(i);
                JSONObject record = new JSONObject(recordStr);

                list.add(new RaceGroup(
                        record.getLong("pk_id"),
                        record.getString("nickname"),
                        record.getString("photo_url"),
                        new RecordItem(record.getLong("pk_id"),
                                record.getString("time"),
                                Goal.GoalTypeEnum.valueOf(record.getInt("goal_type")),
                                record.getInt("score"),
                                record.getInt("score_sum"),
                                record.getLong("version"),
                                record.getString("show_type_name"))));
            }

            mRaceGroupTableManager.updateRaceGroup(recordMinId, version, list);
        } catch (JSONException e) {
            LogUtil.e(TAG, e.getMessage());
            e.printStackTrace();
        }
    }

    public void addRaceGroup(String raceGroupStr) {
        saveRaceGroup(raceGroupStr);

        getMoreRaceGroup(10, true);
    }
}
