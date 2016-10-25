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

import dlmj.hideseek.Common.Model.ExchangeOrder;
import dlmj.hideseek.Common.Params.SharedPreferenceSettings;
import dlmj.hideseek.Common.Util.SharedPreferenceUtil;

/**
 * Created by Two on 22/10/2016.
 */
public class ExchangeOrderTableManager {
    private final static String TABLE_NAME = "exchange_order";
    private SQLiteDatabase mSQLiteDatabase;
    private static ExchangeOrderTableManager mInstance;
    private SharedPreferences mSharedPreferences;
    private long mOrderMinId = 0;
    private SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public static ExchangeOrderTableManager getInstance(Context context){
        synchronized (ExchangeOrderTableManager.class){
            if(mInstance == null){
                mInstance = new ExchangeOrderTableManager(context);
            }
        }
        return mInstance;
    }

    public ExchangeOrderTableManager(Context context){
        mSharedPreferences = SharedPreferenceUtil.getSharedPreferences();
        mSQLiteDatabase = DatabaseManager.getInstance(context).getDatabase();
        mSQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                "order_id bigint, " +
                "status integer, " +
                "create_time varchar, " +
                "update_time varchar, " +
                "count integer, " +
                "reward_id bigint, " +
                "reward_name varchar, " +
                "image_url varchar, " +
                "record integer, " +
                "exchange_count integer, " +
                "introduction varchar, " +
                "version bigint)");
    }

    public void updateOrders(long orderMinId, long version, List<ExchangeOrder> orderList) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        SharedPreferenceSettings exchangeOrderVersion = SharedPreferenceSettings.EXCHANGE_ORDER_VERSION;
        editor.putLong(exchangeOrderVersion.getId(), version);
        SharedPreferenceSettings minId = SharedPreferenceSettings.EXCHANGE_ORDER_MIN_ID;
        editor.putLong(minId.getId(), orderMinId);
        editor.apply();

        synchronized (ExchangeOrderTableManager.class) {
            mSQLiteDatabase.beginTransaction();
            for(ExchangeOrder exchangeOrder : orderList){
                ContentValues contentValues = new ContentValues();
                contentValues.put("status", exchangeOrder.getStatus());
                contentValues.put("create_time", exchangeOrder.getCreateTime());
                contentValues.put("update_time", exchangeOrder.getUpdateTime());
                contentValues.put("count", exchangeOrder.getCount());
                contentValues.put("reward_id", exchangeOrder.getRewardId());
                contentValues.put("reward_name", exchangeOrder.getRewardName());
                contentValues.put("image_url", exchangeOrder.getImageUrl());
                contentValues.put("record", exchangeOrder.getRecord());
                contentValues.put("exchange_count", exchangeOrder.getExchangeCount());
                contentValues.put("introduction", exchangeOrder.getIntroduction());
                contentValues.put("version", exchangeOrder.getVersion());
                String[] args = { String.valueOf(exchangeOrder.getOrderId()) };
                int count = mSQLiteDatabase.update(TABLE_NAME, contentValues, "order_id=?", args);

                if(count == 0) {
                    contentValues.put("order_id", exchangeOrder.getOrderId());
                    mSQLiteDatabase.insert(TABLE_NAME, null, contentValues);
                }
            }
            mSQLiteDatabase.setTransactionSuccessful();
            mSQLiteDatabase.endTransaction();
        }
    }

    public String getUpdateDate() {
        SharedPreferenceSettings updateDate = SharedPreferenceSettings.EXCHANGE_ORDER_UPDATE_TIME;
        String updateDateStr = mSharedPreferences.getString(
                updateDate.getId(),
                (String) updateDate.getDefaultValue());
        return updateDateStr;
    }

    public void clearMoreData() {
        Cursor cursor = mSQLiteDatabase.rawQuery("select order_id from " + TABLE_NAME +
                " order by order_id desc limit 20", null);
        cursor.moveToLast();

        if(cursor.getCount() > 0) {
            Long orderId = cursor.getLong(0);

            if(mOrderMinId < orderId) {
                mOrderMinId = orderId;
            }

            SharedPreferences.Editor editor = mSharedPreferences.edit();
            SharedPreferenceSettings minId = SharedPreferenceSettings.EXCHANGE_ORDER_MIN_ID;
            editor.putLong(minId.getId(), orderId);
            editor.apply();
            mSQLiteDatabase.delete(TABLE_NAME, "order_id<?", new String[]{orderId + ""});
        }

        cursor.close();
    }

    public List<ExchangeOrder> searchOrders() {
        String updateDateStr = getUpdateDate();

        Date curDate = new Date(System.currentTimeMillis());
        String curDateStr = mDateFormat.format(curDate);

        SharedPreferences.Editor editor = mSharedPreferences.edit();
        SharedPreferenceSettings updateDate = SharedPreferenceSettings.EXCHANGE_ORDER_UPDATE_TIME;
        editor.putString(updateDate.getId(), curDateStr);
        editor.apply();

        if(!updateDateStr.isEmpty() && !curDateStr.equals(updateDateStr)) {
            clearMoreData();
        }

        String limit = null;
        String queryStr;
        String[] selectionArgs;
        if(mOrderMinId == 0) {
            limit = 10 + "";
            selectionArgs = new String[]{};
            queryStr = null;
        } else {
            queryStr = "order_id>=?";
            selectionArgs = new String[]{mOrderMinId + ""};
        }

        Cursor cursor;

        if(limit == null) {
            cursor = mSQLiteDatabase.query(TABLE_NAME, null, queryStr,
                    selectionArgs, null, null, "order_id desc");
        } else {
            cursor = mSQLiteDatabase.query(TABLE_NAME, null, queryStr,
                    selectionArgs, null, null, "order_id desc", limit);
        }

        List<ExchangeOrder> exchangeOrderList = new LinkedList<>();
        while (cursor.moveToNext()) {
            exchangeOrderList.add(new ExchangeOrder(
                            cursor.getLong(cursor.getColumnIndex("order_id")),
                            cursor.getInt(cursor.getColumnIndex("status")),
                            cursor.getString(cursor.getColumnIndex("create_time")),
                            cursor.getString(cursor.getColumnIndex("update_time")),
                            cursor.getInt(cursor.getColumnIndex("count")),
                            cursor.getLong(cursor.getColumnIndex("reward_id")),
                            cursor.getString(cursor.getColumnIndex("reward_name")),
                            cursor.getString(cursor.getColumnIndex("image_url")),
                            cursor.getInt(cursor.getColumnIndex("record")),
                            cursor.getInt(cursor.getColumnIndex("exchange_count")),
                            cursor.getString(cursor.getColumnIndex("introduction")),
                            cursor.getLong(cursor.getColumnIndex("version")))
            );
        }

        if(exchangeOrderList.size() > 0) {
            mOrderMinId = exchangeOrderList.get(exchangeOrderList.size() - 1).getOrderId();
        }

        cursor.close();
        return exchangeOrderList;
    }

    public List<ExchangeOrder> getMoreOrders(int count, long version, boolean hasLoaded) {
        Cursor cursor = mSQLiteDatabase.query(TABLE_NAME, null, "version<=? and order_id<?",
                new String[]{version + "", mOrderMinId + ""}, null, null, "order_id desc", count + "");
        List<ExchangeOrder> exchangeOrderList = new LinkedList<>();
        if(cursor.getCount() == count || hasLoaded) {
            while (cursor.moveToNext()) {
                exchangeOrderList.add(new ExchangeOrder(
                                cursor.getLong(cursor.getColumnIndex("order_id")),
                                cursor.getInt(cursor.getColumnIndex("status")),
                                cursor.getString(cursor.getColumnIndex("create_time")),
                                cursor.getString(cursor.getColumnIndex("update_time")),
                                cursor.getInt(cursor.getColumnIndex("count")),
                                cursor.getLong(cursor.getColumnIndex("reward_id")),
                                cursor.getString(cursor.getColumnIndex("reward_name")),
                                cursor.getString(cursor.getColumnIndex("image_url")),
                                cursor.getInt(cursor.getColumnIndex("record")),
                                cursor.getInt(cursor.getColumnIndex("exchange_count")),
                                cursor.getString(cursor.getColumnIndex("introduction")),
                                cursor.getLong(cursor.getColumnIndex("version")))
                );
            }
        }

        if(exchangeOrderList.size() > 0) {
            mOrderMinId = exchangeOrderList.get(exchangeOrderList.size() - 1).getOrderId();
        }

        cursor.close();
        return exchangeOrderList;
    }

    public long getVersion() {
        SharedPreferenceSettings exchangeOrderVersion = SharedPreferenceSettings.EXCHANGE_ORDER_VERSION;
        long versionValue = mSharedPreferences.getLong(
                exchangeOrderVersion.getId(),
                (long) exchangeOrderVersion.getDefaultValue());
        return versionValue;
    }

    public long getOrderMinId() {
        SharedPreferenceSettings minId = SharedPreferenceSettings.EXCHANGE_ORDER_MIN_ID;
        long orderMinId = mSharedPreferences.getLong(
                minId.getId(),
                (long) minId.getDefaultValue());
        return orderMinId;
    }
}
