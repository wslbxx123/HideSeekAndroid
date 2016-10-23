package dlmj.hideseek.BusinessLogic.Cache;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

import dlmj.hideseek.Common.Model.Reward;
import dlmj.hideseek.Common.Util.LogUtil;
import dlmj.hideseek.DataAccess.RewardTableManager;

/**
 * Created by Two on 22/10/2016.
 */
public class RewardCache extends BaseCache<Reward> {
    private static final String TAG = "RewardCache";
    private static RewardCache mInstance;
    private RewardTableManager mRewardTableManager;
    private long mVersion = 0;

    public static RewardCache getInstance(Context context){
        synchronized (RewardCache.class){
            if(mInstance == null){
                mInstance = new RewardCache(context);
            }
        }
        return mInstance;
    }

    public RewardCache(Context context) {
        super();
        mRewardTableManager = RewardTableManager.getInstance(context);
    }

    public List<Reward> getList() {
        if(super.getList().size() > 0) {
            return mList;
        }

        mList = mRewardTableManager.searchRewards();
        return mList;
    }

    public boolean getMoreRewards(int count, boolean hasLoaded) {
        List<Reward> rewardList = mRewardTableManager.getMoreRewards(count, mVersion, hasLoaded);

        mList.addAll(rewardList);
        return rewardList.size() > 0;
    }

    public void setRewards(String rewardStr) {
        saveRewards(rewardStr);

        mList = mRewardTableManager.searchRewards();
        mVersion = mRewardTableManager.getVersion();
    }

    public void saveRewards(String rewardStr) {
        List<Reward> list = new LinkedList<>();
        try {
            JSONObject result = new JSONObject(rewardStr);
            String reward = result.getString("reward");

            long version = result.has("version") ? result.getLong("version") :
                    mRewardTableManager.getVersion();
            long rewardMinId = result.getLong("reward_min_id");
            JSONArray rewardList = new JSONArray(reward);
            String rewardInfoStr;
            for (int i = 0; i < rewardList.length(); i++) {
                rewardInfoStr = rewardList.getString(i);
                JSONObject rewardInfo = new JSONObject(rewardInfoStr);

                list.add(new Reward(
                                rewardInfo.getLong("pk_id"),
                                rewardInfo.getString("reward_name"),
                                rewardInfo.getString("reward_image_url"),
                                rewardInfo.getInt("record"),
                                rewardInfo.getInt("exchange_count"),
                                rewardInfo.getString("introduction"),
                                rewardInfo.getLong("version"))
                );
            }

            mRewardTableManager.updateRewards(rewardMinId, version, list);
        } catch (JSONException e) {
            LogUtil.e(TAG, e.getMessage());
            e.printStackTrace();
        }
    }

    public void addRewards(String rewardStr) {
        saveRewards(rewardStr);

        getMoreRewards(10, true);
    }
}