package dlmj.hideseek.DataAccess;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.LinkedList;
import java.util.List;

import dlmj.hideseek.Common.Model.Goal;
import dlmj.hideseek.Common.Model.Record;
import dlmj.hideseek.Common.Params.SharedPreferenceSettings;
import dlmj.hideseek.Common.Util.SharedPreferenceUtil;

/**
 * Created by Two on 7/8/16.
 */
public class RecordTableManager {
    private static String TAG = "RecordTableManager";
    private SQLiteDatabase mSQLiteDatabase;
    private static RecordTableManager mInstance;
    private SharedPreferences mSharedPreferences;
    private long mRecordMinId = 0;

    public static RecordTableManager getInstance(Context context){
        synchronized (RaceGroupTableManager.class){
            if(mInstance == null){
                mInstance = new RecordTableManager(context);
            }
        }
        return mInstance;
    }

    public RecordTableManager(Context context){
        mSharedPreferences = SharedPreferenceUtil.getSharedPreferences();
        mSQLiteDatabase = DatabaseManager.getInstance(context).getDatabase();
        mSQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS record (" +
                "record_id bigint, " +
                "date_str varchar, " +
                "time datetime, " +
                "goal_type integer, " +
                "score integer, " +
                "score_sum integer, " +
                "version bigint)");
    }

    public void updateRecord(int scoreSum, long recordMinId, long version,
                                List<Record> recordList) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        SharedPreferenceSettings recordScoreSum = SharedPreferenceSettings.SCORE_SUM;
        editor.putInt(recordScoreSum.getId(), scoreSum);
        SharedPreferenceSettings recordGroupVersion = SharedPreferenceSettings.RECORD_VERSION;
        editor.putLong(recordGroupVersion.getId(), version);
        SharedPreferenceSettings minId = SharedPreferenceSettings.RECORD_MIN_ID;
        editor.putLong(minId.getId(), recordMinId);
        editor.apply();

        synchronized (RecordTableManager.class) {
            mSQLiteDatabase.beginTransaction();
            for(Record record : recordList){
                ContentValues contentValues = new ContentValues();
                contentValues.put("date_str", record.getDate());
                contentValues.put("time", record.getTime());
                contentValues.put("goal_type", record.getGoalType().getValue());
                contentValues.put("score", record.getScore());
                contentValues.put("score_sum", record.getScoreSum());
                contentValues.put("version", record.getVersion());
                String[] args = { String.valueOf(record.getRecordId()) };
                int count = mSQLiteDatabase.update("record", contentValues, "record_id=?", args);

                if(count == 0) {
                    contentValues.put("record_id", record.getRecordId());
                    mSQLiteDatabase.insert("record", null, contentValues);
                }
            }

            mSQLiteDatabase.setTransactionSuccessful();
            mSQLiteDatabase.endTransaction();
        }
    }

    public List<Record> searchRecords() {
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
            cursor = mSQLiteDatabase.query("record", null, queryStr,
                    selectionArgs, null, null, "record_id desc");
        } else {
            cursor = mSQLiteDatabase.query("record", null, queryStr,
                    selectionArgs, null, null, "record_id desc", limit);
        }

        return getRecordList(cursor);
    }

    private List<Record> getRecordList(Cursor cursor) {
        List<Record> recordList = new LinkedList<>();

        while (cursor.moveToNext()) {
            recordList.add(new Record(
                    cursor.getLong(cursor.getColumnIndex("record_id")),
                    cursor.getString(cursor.getColumnIndex("time")),
                    Goal.GoalTypeEnum.valueOf(cursor.getInt(cursor.getColumnIndex("goal_type"))),
                    cursor.getInt(cursor.getColumnIndex("score")),
                    cursor.getInt(cursor.getColumnIndex("score_sum")),
                    cursor.getLong(cursor.getColumnIndex("version")),
                    cursor.getString(cursor.getColumnIndex("date_str"))
            ));
        }

        if(recordList.size() > 0) {
            mRecordMinId = recordList.get(recordList.size() - 1).getRecordId();
        }

        cursor.close();
        return recordList;
    }

    public void clear() {
        synchronized (RecordTableManager.class) {
            mSQLiteDatabase.beginTransaction();
            mSQLiteDatabase.execSQL("delete from record; " +
                    "update sqlite_sequence SET seq = 0 where name ='record'");
            mSQLiteDatabase.setTransactionSuccessful();
            mSQLiteDatabase.endTransaction();
        }

        SharedPreferenceSettings minId = SharedPreferenceSettings.RECORD_MIN_ID;
        SharedPreferenceSettings recordVersion = SharedPreferenceSettings.RECORD_VERSION;
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.remove(minId.getId());
        editor.remove(recordVersion.getId());
        editor.apply();
    }

    public int getScoreSum() {
        SharedPreferenceSettings scoreSumVersion = SharedPreferenceSettings.SCORE_SUM;
        int scoreSumValue = mSharedPreferences.getInt(
                scoreSumVersion.getId(),
                (int) scoreSumVersion.getDefaultValue());
        return scoreSumValue;
    }

    public long getVersion() {
        SharedPreferenceSettings recordVersion = SharedPreferenceSettings.RECORD_VERSION;
        long versionValue = mSharedPreferences.getLong(
                recordVersion.getId(),
                (long) recordVersion.getDefaultValue());
        return versionValue;
    }

    public long getRecordMinId() {
        SharedPreferenceSettings minId = SharedPreferenceSettings.RECORD_MIN_ID;
        long recordMinId = mSharedPreferences.getLong(
                minId.getId(),
                (long) minId.getDefaultValue());
        return recordMinId;
    }

    public List<Record> getMoreRecords(int count, long version, boolean afterLoaded) {
        Cursor cursor = mSQLiteDatabase.query("record", null, "version<=? and record_id<?",
                new String[]{version + "", mRecordMinId + ""}, null, null, "record_id desc", count + "");
        List<Record> recordList = new LinkedList<>();
        if(cursor.getCount() == count || afterLoaded) {
            recordList = getRecordList(cursor);
        }

        if(recordList.size() > 0) {
            mRecordMinId = recordList.get(recordList.size() - 1).getRecordId();
        }

        cursor.close();
        return recordList;
    }
}
