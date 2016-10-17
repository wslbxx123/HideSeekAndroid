package dlmj.hideseek.DataAccess;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import dlmj.hideseek.Common.Model.Goal;
import dlmj.hideseek.Common.Model.RaceGroup;
import dlmj.hideseek.Common.Model.RecordItem;
import dlmj.hideseek.Common.Params.SharedPreferenceSettings;
import dlmj.hideseek.Common.Util.SharedPreferenceUtil;

/**
 * Created by Two on 6/3/16.
 */
public class RaceGroupTableManager {
    private SQLiteDatabase mSQLiteDatabase;
    private static RaceGroupTableManager mInstance;
    private SharedPreferences mSharedPreferences;
    private long mRecordMinId = 0;
    private SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public static RaceGroupTableManager getInstance(Context context){
        synchronized (RaceGroupTableManager.class){
            if(mInstance == null){
                mInstance = new RaceGroupTableManager(context);
            }
        }
        return mInstance;
    }

    public RaceGroupTableManager(Context context){
        mSharedPreferences = SharedPreferenceUtil.getSharedPreferences();
        mSQLiteDatabase = DatabaseManager.getInstance(context).getDatabase();
        mSQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS race_group (" +
                "record_id bigint, " +
                "photo_url varchar, " +
                "nickname varchar, " +
                "time datetime, " +
                "goal_type integer, " +
                "score integer, " +
                "score_sum integer, " +
                "version bigint, " +
                "show_type_name varchar)");
    }

    public void addRaceGroup(List<RaceGroup> raceGroupList) {
        synchronized (RaceGroupTableManager.class) {
            mSQLiteDatabase.beginTransaction();
            for(RaceGroup raceGroup : raceGroupList){
                ContentValues contentValues = new ContentValues();
                contentValues.put("record_id", raceGroup.getRecordId());
                contentValues.put("photo_url", raceGroup.getPhotoUrl());
                contentValues.put("nickname", raceGroup.getNickname());
                contentValues.put("time", raceGroup.getRecordItem().getTime());
                contentValues.put("goal_type", raceGroup.getRecordItem().getGoalType().getValue());
                contentValues.put("score", raceGroup.getRecordItem().getScore());
                contentValues.put("score_sum", raceGroup.getRecordItem().getScoreSum());
                contentValues.put("version", raceGroup.getRecordItem().getVersion());
                contentValues.put("show_type_name", raceGroup.getRecordItem().getShowTypeName());
                mSQLiteDatabase.insert("race_group", null, contentValues);
            }

            mSQLiteDatabase.setTransactionSuccessful();
            mSQLiteDatabase.endTransaction();
        }
    }

    public void updateRaceGroup(long recordMinId, long version,
                                List<RaceGroup> raceGroupList) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        SharedPreferenceSettings raceGroupVersion = SharedPreferenceSettings.RACE_GROUP_VERSION;
        editor.putLong(raceGroupVersion.getId(), version);
        SharedPreferenceSettings minId = SharedPreferenceSettings.RACE_GROUP_RECORD_MIN_ID;
        editor.putLong(minId.getId(), recordMinId);
        editor.apply();

        synchronized (RaceGroupTableManager.class) {
            mSQLiteDatabase.beginTransaction();
            for(RaceGroup raceGroup : raceGroupList){
                ContentValues contentValues = new ContentValues();
                contentValues.put("photo_url", raceGroup.getPhotoUrl());
                contentValues.put("nickname", raceGroup.getNickname());
                contentValues.put("time", raceGroup.getRecordItem().getTime());
                contentValues.put("goal_type", raceGroup.getRecordItem().getGoalType().getValue());
                contentValues.put("score", raceGroup.getRecordItem().getScore());
                contentValues.put("score_sum", raceGroup.getRecordItem().getScoreSum());
                contentValues.put("version", raceGroup.getRecordItem().getVersion());
                contentValues.put("show_type_name", raceGroup.getRecordItem().getShowTypeName());
                String[] args = { String.valueOf(raceGroup.getRecordId()) };
                int count = mSQLiteDatabase.update("race_group", contentValues, "record_id=?", args);

                if(count == 0) {
                    contentValues.put("record_id", raceGroup.getRecordId());
                    mSQLiteDatabase.insert("race_group", null, contentValues);
                }
            }
            mSQLiteDatabase.setTransactionSuccessful();
            mSQLiteDatabase.endTransaction();
        }
    }

    public void clearMoreData() {
        Cursor cursor = mSQLiteDatabase.rawQuery("select record_id from race_group " +
                "order by record_id desc limit 20", null);
        cursor.moveToLast();

        if(cursor.getCount() > 0) {
            Long recordId = cursor.getLong(0);

            if(mRecordMinId < recordId) {
                mRecordMinId = recordId;
            }

            SharedPreferences.Editor editor = mSharedPreferences.edit();
            SharedPreferenceSettings minId = SharedPreferenceSettings.RACE_GROUP_RECORD_MIN_ID;
            editor.putLong(minId.getId(), recordId);
            editor.apply();
            mSQLiteDatabase.delete("race_group", "record_id<?", new String[]{recordId + ""});
        }

        cursor.close();
    }

    public List<RaceGroup> searchRaceGroup() {
        String updateDateStr = getUpdateDate();

        Date curDate = new Date(System.currentTimeMillis());
        String curDateStr = mDateFormat.format(curDate);

        SharedPreferences.Editor editor = mSharedPreferences.edit();
        SharedPreferenceSettings updateDate = SharedPreferenceSettings.RACE_GROUP_UPDATE_TIME;
        editor.putString(updateDate.getId(), curDateStr);
        editor.apply();

        if(!updateDateStr.isEmpty() && !curDateStr.equals(updateDateStr)) {
            clearMoreData();
        }

        String limit = null;
        String queryStr;
        String[] selectionArgs;
        if(mRecordMinId == 0) {
            limit = 10 + "";
            selectionArgs = new String[]{};
            queryStr = null;
        } else {
            queryStr = "record_id>=?";
            selectionArgs = new String[]{mRecordMinId + ""};
        }

        Cursor cursor;

        if(limit == null) {
            cursor = mSQLiteDatabase.query("race_group", null, queryStr,
                    selectionArgs, null, null, "record_id desc");
        } else {
            cursor = mSQLiteDatabase.query("race_group", null, queryStr,
                    selectionArgs, null, null, "record_id desc", limit);
        }

        List<RaceGroup> raceGroupList = new LinkedList<>();
        while (cursor.moveToNext()) {
            raceGroupList.add(new RaceGroup(cursor.getLong(cursor.getColumnIndex("record_id")),
                    cursor.getString(cursor.getColumnIndex("nickname")),
                    cursor.getString(cursor.getColumnIndex("photo_url")),
                    new RecordItem(cursor.getLong(cursor.getColumnIndex("record_id")),
                            cursor.getString(cursor.getColumnIndex("time")),
                            Goal.GoalTypeEnum.valueOf(cursor.getInt(cursor.getColumnIndex("goal_type"))),
                            cursor.getInt(cursor.getColumnIndex("score")),
                            cursor.getInt(cursor.getColumnIndex("score_sum")),
                            cursor.getLong(cursor.getColumnIndex("version")),
                            cursor.getString(cursor.getColumnIndex("show_type_name")))
                    ));
        }

        if(raceGroupList.size() > 0) {
            mRecordMinId = raceGroupList.get(raceGroupList.size() - 1).getRecordId();
        }

        cursor.close();
        return raceGroupList;
    }

    public void clear() {
        synchronized (RaceGroupTableManager.class) {
            mSQLiteDatabase.beginTransaction();
            mSQLiteDatabase.execSQL("delete from race_group; " +
                    "update sqlite_sequence SET seq = 0 where name ='race_group'");
            mSQLiteDatabase.setTransactionSuccessful();
            mSQLiteDatabase.endTransaction();
        }

        SharedPreferenceSettings minId = SharedPreferenceSettings.RACE_GROUP_RECORD_MIN_ID;
        SharedPreferenceSettings raceGroupVersion = SharedPreferenceSettings.RACE_GROUP_VERSION;
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.remove(minId.getId());
        editor.remove(raceGroupVersion.getId());
        editor.apply();
    }

    public long getVersion() {
        SharedPreferenceSettings raceGroupVersion = SharedPreferenceSettings.RACE_GROUP_VERSION;
        long versionValue = mSharedPreferences.getLong(
                raceGroupVersion.getId(),
                (long) raceGroupVersion.getDefaultValue());
        return versionValue;
    }

    public long getRecordMinId() {
        SharedPreferenceSettings minId = SharedPreferenceSettings.RACE_GROUP_RECORD_MIN_ID;
        long recordMinId = mSharedPreferences.getLong(
                minId.getId(),
                (long) minId.getDefaultValue());
        return recordMinId;
    }

    public String getUpdateDate() {
        SharedPreferenceSettings updateDate = SharedPreferenceSettings.RACE_GROUP_UPDATE_TIME;
        String updateDateStr = mSharedPreferences.getString(
                updateDate.getId(),
                (String) updateDate.getDefaultValue());
        return updateDateStr;
    }

    public List<RaceGroup> getMoreRaceGroup(int count, long version, boolean hasLoaded) {
        Cursor cursor = mSQLiteDatabase.query("race_group", null, "version<=? and record_id<?",
                new String[]{version + "", mRecordMinId + ""}, null, null, "record_id desc", count + "");
        List<RaceGroup> raceGroupList = new LinkedList<>();
        if(cursor.getCount() == count || hasLoaded) {
            while (cursor.moveToNext()) {
                raceGroupList.add(new RaceGroup(cursor.getLong(cursor.getColumnIndex("record_id")),
                        cursor.getString(cursor.getColumnIndex("nickname")),
                        cursor.getString(cursor.getColumnIndex("photo_url")),
                        new RecordItem(cursor.getLong(cursor.getColumnIndex("record_id")),
                                cursor.getString(cursor.getColumnIndex("time")),
                                Goal.GoalTypeEnum.valueOf(cursor.getInt(cursor.getColumnIndex("goal_type"))),
                                cursor.getInt(cursor.getColumnIndex("score")),
                                cursor.getInt(cursor.getColumnIndex("score_sum")),
                                cursor.getLong(cursor.getColumnIndex("version")),
                                cursor.getString(cursor.getColumnIndex("show_type_name")))
                ));
            }
        }

        if(raceGroupList.size() > 0) {
            mRecordMinId = raceGroupList.get(raceGroupList.size() - 1).getRecordId();
        }

        cursor.close();
        return raceGroupList;
    }
}
