package dlmj.hideseek.DataAccess;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import dlmj.hideseek.Common.Model.Reward;
import dlmj.hideseek.Common.Params.SharedPreferenceSettings;
import dlmj.hideseek.Common.Util.SharedPreferenceUtil;

/**
 * Created by Two on 22/10/2016.
 */
public class RewardTableManager {
    private final static String TABLE_NAME = "reward";
    private SQLiteDatabase mSQLiteDatabase;
    private static RewardTableManager mInstance;
    private SharedPreferences mSharedPreferences;
    private long mRewardMinId = 0;
    private SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public static RewardTableManager getInstance(Context context){
        synchronized (RewardTableManager.class){
            if(mInstance == null){
                mInstance = new RewardTableManager(context);
            }
        }
        return mInstance;
    }

    public RewardTableManager(Context context){
        mSharedPreferences = SharedPreferenceUtil.getSharedPreferences();
        mSQLiteDatabase = DatabaseManager.getInstance(context).getDatabase();
        mSQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                "reward_id bigint, " +
                "name varchar, " +
                "image_url varchar, " +
                "record integer, " +
                "exchange_count integer, " +
                "introduction varchar, " +
                "version bigint)");
    }

    public void updateRewards(long rewardMinId, long version, List<Reward> rewardList) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        SharedPreferenceSettings rewardVersion = SharedPreferenceSettings.REWARD_VERSION;
        editor.putLong(rewardVersion.getId(), version);
        SharedPreferenceSettings minId = SharedPreferenceSettings.REWARD_MIN_ID;
        editor.putLong(minId.getId(), rewardMinId);
        editor.apply();

        synchronized (RewardTableManager.class) {
            mSQLiteDatabase.beginTransaction();
            for(Reward reward : rewardList){
                ContentValues contentValues = new ContentValues();
                contentValues.put("name", reward.getName());
                contentValues.put("image_url", reward.getImageUrl());
                contentValues.put("record", reward.getRecord());
                contentValues.put("exchange_count", reward.getExchangeCount());
                contentValues.put("introduction", reward.getIntroduction());
                contentValues.put("version", reward.getVersion());
                String[] args = { String.valueOf(reward.getPkId()) };
                int count = mSQLiteDatabase.update(TABLE_NAME, contentValues, "reward_id=?", args);

                if(count == 0) {
                    contentValues.put("reward_id", reward.getPkId());
                    mSQLiteDatabase.insert(TABLE_NAME, null, contentValues);
                }
            }
            mSQLiteDatabase.setTransactionSuccessful();
            mSQLiteDatabase.endTransaction();
        }
    }

    public String getUpdateDate() {
        SharedPreferenceSettings updateDate = SharedPreferenceSettings.REWARD_UPDATE_TIME;
        String updateDateStr = mSharedPreferences.getString(
                updateDate.getId(),
                (String) updateDate.getDefaultValue());
        return updateDateStr;
    }

    public void clearMoreData() {
        Cursor cursor = mSQLiteDatabase.rawQuery("select reward_id from " + TABLE_NAME +
                " order by reward_id desc limit 20", null);
        cursor.moveToLast();

        if(cursor.getCount() > 0) {
            Long rewardId = cursor.getLong(0);

            if(mRewardMinId < rewardId) {
                mRewardMinId = rewardId;
            }

            SharedPreferences.Editor editor = mSharedPreferences.edit();
            SharedPreferenceSettings minId = SharedPreferenceSettings.REWARD_MIN_ID;
            editor.putLong(minId.getId(), rewardId);
            editor.apply();
            mSQLiteDatabase.delete(TABLE_NAME, "reward_id<?", new String[]{rewardId + ""});
        }

        cursor.close();
    }

    public List<Reward> searchRewards() {
        String updateDateStr = getUpdateDate();

        Date curDate = new Date(System.currentTimeMillis());
        String curDateStr = mDateFormat.format(curDate);

        SharedPreferences.Editor editor = mSharedPreferences.edit();
        SharedPreferenceSettings updateDate = SharedPreferenceSettings.REWARD_UPDATE_TIME;
        editor.putString(updateDate.getId(), curDateStr);
        editor.apply();

        if(!updateDateStr.isEmpty() && !curDateStr.equals(updateDateStr)) {
            clearMoreData();
        }

        String limit = null;
        String queryStr;
        String[] selectionArgs;
        if(mRewardMinId == 0) {
            limit = 10 + "";
            selectionArgs = new String[]{};
            queryStr = null;
        } else {
            queryStr = "reward_id>=?";
            selectionArgs = new String[]{mRewardMinId + ""};
        }

        Cursor cursor;

        if(limit == null) {
            cursor = mSQLiteDatabase.query(TABLE_NAME, null, queryStr,
                    selectionArgs, null, null, "reward_id desc");
        } else {
            cursor = mSQLiteDatabase.query(TABLE_NAME, null, queryStr,
                    selectionArgs, null, null, "reward_id desc", limit);
        }

        List<Reward> rewardList = new LinkedList<>();
        while (cursor.moveToNext()) {
            rewardList.add(new Reward(
                            cursor.getLong(cursor.getColumnIndex("reward_id")),
                            cursor.getString(cursor.getColumnIndex("name")),
                            cursor.getString(cursor.getColumnIndex("image_url")),
                            cursor.getInt(cursor.getColumnIndex("record")),
                            cursor.getInt(cursor.getColumnIndex("exchange_count")),
                            cursor.getString(cursor.getColumnIndex("introduction")),
                            cursor.getLong(cursor.getColumnIndex("version")))
            );
        }

        if(rewardList.size() > 0) {
            mRewardMinId = rewardList.get(rewardList.size() - 1).getPkId();
        }

        cursor.close();
        return rewardList;
    }

    public List<Reward> getMoreRewards(int count, long version, boolean hasLoaded) {
        Cursor cursor = mSQLiteDatabase.query(TABLE_NAME, null, "version<=? and reward_id<?",
                new String[]{version + "", mRewardMinId + ""}, null, null, "reward_id desc", count + "");
        List<Reward> rewardList = new LinkedList<>();
        if(cursor.getCount() == count || hasLoaded) {
            while (cursor.moveToNext()) {
                rewardList.add(new Reward(
                                cursor.getLong(cursor.getColumnIndex("reward_id")),
                                cursor.getString(cursor.getColumnIndex("name")),
                                cursor.getString(cursor.getColumnIndex("image_url")),
                                cursor.getInt(cursor.getColumnIndex("record")),
                                cursor.getInt(cursor.getColumnIndex("exchange_count")),
                                cursor.getString(cursor.getColumnIndex("introduction")),
                                cursor.getLong(cursor.getColumnIndex("version")))
                );
            }
        }

        if(rewardList.size() > 0) {
            mRewardMinId = rewardList.get(rewardList.size() - 1).getPkId();
        }

        cursor.close();
        return rewardList;
    }

    public long getVersion() {
        SharedPreferenceSettings rewardVersion = SharedPreferenceSettings.REWARD_VERSION;
        long versionValue = mSharedPreferences.getLong(
                rewardVersion.getId(),
                (long) rewardVersion.getDefaultValue());
        return versionValue;
    }

    public long getRewardMinId() {
        SharedPreferenceSettings minId = SharedPreferenceSettings.REWARD_MIN_ID;
        long rewardMinId = mSharedPreferences.getLong(
                minId.getId(),
                (long) minId.getDefaultValue());
        return rewardMinId;
    }
}
