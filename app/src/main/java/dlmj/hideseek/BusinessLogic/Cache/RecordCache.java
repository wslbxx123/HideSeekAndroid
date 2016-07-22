package dlmj.hideseek.BusinessLogic.Cache;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import dlmj.hideseek.Common.Model.Goal;
import dlmj.hideseek.Common.Model.Record;
import dlmj.hideseek.Common.Util.LogUtil;
import dlmj.hideseek.DataAccess.RecordTableManager;

/**
 * Created by Two on 5/2/16.
 */
public class RecordCache extends BaseCache<Record>{
    private static String TAG = "RecordCache";
    private static RecordCache mInstance;
    private SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private SimpleDateFormat mTimeFormat = new SimpleDateFormat("HH:mm");
    private int mScoreSum = 0;
    private RecordTableManager mRecordTableManager;
    private long mVersion = 0;

    public static RecordCache getInstance(Context context){
        synchronized (UserCache.class){
            if(mInstance == null){
                mInstance = new RecordCache(context);
            }
        }
        return mInstance;
    }

    public RecordCache(Context context) {
        super();
        mRecordTableManager = RecordTableManager.getInstance(context);
    }

    public int getScoreSum() {
        if(mScoreSum > 0) {
            return mScoreSum;
        }

        return mRecordTableManager.getScoreSum();
    }

    public List<Record> getList() {
        if(super.getList().size() > 0) {
            return mList;
        }

        mList = mRecordTableManager.searchRecords();
        return mList;
    }

    public void setRecords(String recordsStr) {
        saveRecords(recordsStr);

        mList = mRecordTableManager.searchRecords();
        mVersion = mRecordTableManager.getVersion();
    }

    public void saveRecords(String recordsStr) {
        List<Record> list = new LinkedList<>();
        try {
            JSONObject jsonObject = new JSONObject(recordsStr);
            long version = jsonObject.has("version") ? jsonObject.getLong("version") :
                    mRecordTableManager.getVersion();
            long recordMinId = jsonObject.getLong("record_min_id");
            String recordListStr = jsonObject.getString("scores");
            mScoreSum = jsonObject.has("score_sum") ? jsonObject.getInt("score_sum") : mScoreSum;
            JSONArray recordList = new JSONArray(recordListStr);
            String recordStr;
            for (int i = 0; i < recordList.length(); i++) {
                recordStr = recordList.getString(i);
                JSONObject record = new JSONObject(recordStr);
                Date date = mDateFormat.parse(record.getString("time"));

                list.add(new Record(record.getLong("pk_id"),
                        mTimeFormat.format(date),
                        Goal.GoalTypeEnum.valueOf(record.getInt("goal_type")),
                        record.getInt("score"),
                        record.getInt("score_sum"),
                        record.getLong("version"),
                        DateFormat.getDateInstance(DateFormat.DEFAULT).format(date)));
            }

            mRecordTableManager.updateRecord(mScoreSum, recordMinId, version, list);
        } catch (JSONException e) {
            LogUtil.e(TAG, e.getMessage());
            e.printStackTrace();
        } catch(ParseException e) {
            LogUtil.e(TAG, e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean getMoreRecords(int count, boolean hasLoaded) {
        List<Record> recordList = mRecordTableManager.getMoreRecords(count, mVersion, hasLoaded);

        mList.addAll(recordList);
        return recordList.size() > 0;
    }

    public void addRecords(String recordStr) {
        saveRecords(recordStr);

        getMoreRecords(10, true);
    }
}
