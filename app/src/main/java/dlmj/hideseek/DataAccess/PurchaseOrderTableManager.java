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

import dlmj.hideseek.Common.Model.PurchaseOrder;
import dlmj.hideseek.Common.Params.SharedPreferenceSettings;
import dlmj.hideseek.Common.Util.SharedPreferenceUtil;

/**
 * Created by Two on 22/10/2016.
 */
public class PurchaseOrderTableManager {
    private final static String TABLE_NAME = "reward";
    private SQLiteDatabase mSQLiteDatabase;
    private static PurchaseOrderTableManager mInstance;
    private SharedPreferences mSharedPreferences;
    private long mOrderMinId = 0;
    private SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public static PurchaseOrderTableManager getInstance(Context context){
        synchronized (PurchaseOrderTableManager.class){
            if(mInstance == null){
                mInstance = new PurchaseOrderTableManager(context);
            }
        }
        return mInstance;
    }

    public PurchaseOrderTableManager(Context context){
        mSharedPreferences = SharedPreferenceUtil.getSharedPreferences();
        mSQLiteDatabase = DatabaseManager.getInstance(context).getDatabase();
        mSQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                "order_id bigint, " +
                "status integer, " +
                "create_time varchar, " +
                "update_time varchar, " +
                "count integer, " +
                "trade_no varchar, " +
                "product_id bigint, " +
                "product_name varchar, " +
                "image_url varchar, " +
                "price double, " +
                "purchase_count integer, " +
                "introduction varchar, " +
                "version bigint)");
    }

    public void updateOrders(long orderMinId, long version, List<PurchaseOrder> orderList) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        SharedPreferenceSettings purchaseOrderVersion = SharedPreferenceSettings.PURCHASE_ORDER_VERSION;
        editor.putLong(purchaseOrderVersion.getId(), version);
        SharedPreferenceSettings minId = SharedPreferenceSettings.PURCHASE_ORDER_MIN_ID;
        editor.putLong(minId.getId(), orderMinId);
        editor.apply();

        synchronized (PurchaseOrderTableManager.class) {
            mSQLiteDatabase.beginTransaction();
            for(PurchaseOrder purchaseOrder : orderList){
                ContentValues contentValues = new ContentValues();
                contentValues.put("status", purchaseOrder.getStatus());
                contentValues.put("create_time", purchaseOrder.getCreateTime());
                contentValues.put("update_time", purchaseOrder.getUpdateTime());
                contentValues.put("order_count", purchaseOrder.getCount());
                contentValues.put("trade_no", purchaseOrder.getTradeNo());
                contentValues.put("product_id", purchaseOrder.getProductId());
                contentValues.put("product_name", purchaseOrder.getProductName());
                contentValues.put("image_url", purchaseOrder.getImageUrl());
                contentValues.put("price", purchaseOrder.getPrice());
                contentValues.put("purchase_count", purchaseOrder.getPurchaseCount());
                contentValues.put("introduction", purchaseOrder.getIntroduction());
                contentValues.put("version", purchaseOrder.getVersion());
                String[] args = { String.valueOf(purchaseOrder.getOrderId()) };
                int count = mSQLiteDatabase.update(TABLE_NAME, contentValues, "order_id=?", args);

                if(count == 0) {
                    contentValues.put("order_id", purchaseOrder.getOrderId());
                    mSQLiteDatabase.insert(TABLE_NAME, null, contentValues);
                }
            }
            mSQLiteDatabase.setTransactionSuccessful();
            mSQLiteDatabase.endTransaction();
        }
    }

    public String getUpdateDate() {
        SharedPreferenceSettings updateDate = SharedPreferenceSettings.PURCHASE_ORDER_UPDATE_TIME;
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
            SharedPreferenceSettings minId = SharedPreferenceSettings.PURCHASE_ORDER_MIN_ID;
            editor.putLong(minId.getId(), orderId);
            editor.apply();
            mSQLiteDatabase.delete(TABLE_NAME, "order_id<?", new String[]{orderId + ""});
        }

        cursor.close();
    }

    public List<PurchaseOrder> searchOrders() {
        String updateDateStr = getUpdateDate();

        Date curDate = new Date(System.currentTimeMillis());
        String curDateStr = mDateFormat.format(curDate);

        SharedPreferences.Editor editor = mSharedPreferences.edit();
        SharedPreferenceSettings updateDate = SharedPreferenceSettings.PURCHASE_ORDER_UPDATE_TIME;
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

        List<PurchaseOrder> purchaseOrderList = new LinkedList<>();
        while (cursor.moveToNext()) {
            purchaseOrderList.add(new PurchaseOrder(
                            cursor.getLong(cursor.getColumnIndex("order_id")),
                            cursor.getInt(cursor.getColumnIndex("status")),
                            cursor.getString(cursor.getColumnIndex("create_time")),
                            cursor.getString(cursor.getColumnIndex("update_time")),
                            cursor.getInt(cursor.getColumnIndex("order_count")),
                            cursor.getString(cursor.getColumnIndex("trade_no")),
                            cursor.getLong(cursor.getColumnIndex("product_id")),
                            cursor.getString(cursor.getColumnIndex("product_name")),
                            cursor.getString(cursor.getColumnIndex("image_url")),
                            cursor.getDouble(cursor.getColumnIndex("price")),
                            cursor.getInt(cursor.getColumnIndex("purchase_count")),
                            cursor.getString(cursor.getColumnIndex("introduction")),
                            cursor.getLong(cursor.getColumnIndex("version")))
            );
        }

        if(purchaseOrderList.size() > 0) {
            mOrderMinId = purchaseOrderList.get(purchaseOrderList.size() - 1).getOrderId();
        }

        cursor.close();
        return purchaseOrderList;
    }

    public List<PurchaseOrder> getMoreOrders(int count, long version, boolean hasLoaded) {
        Cursor cursor = mSQLiteDatabase.query(TABLE_NAME, null, "version<=? and order_id<?",
                new String[]{version + "", mOrderMinId + ""}, null, null, "order_id desc", count + "");
        List<PurchaseOrder> purchaseOrderList = new LinkedList<>();
        if(cursor.getCount() == count || hasLoaded) {
            while (cursor.moveToNext()) {
                purchaseOrderList.add(new PurchaseOrder(
                                cursor.getLong(cursor.getColumnIndex("order_id")),
                                cursor.getInt(cursor.getColumnIndex("status")),
                                cursor.getString(cursor.getColumnIndex("create_time")),
                                cursor.getString(cursor.getColumnIndex("update_time")),
                                cursor.getInt(cursor.getColumnIndex("order_count")),
                                cursor.getString(cursor.getColumnIndex("trade_no")),
                                cursor.getLong(cursor.getColumnIndex("product_id")),
                                cursor.getString(cursor.getColumnIndex("product_name")),
                                cursor.getString(cursor.getColumnIndex("image_url")),
                                cursor.getDouble(cursor.getColumnIndex("price")),
                                cursor.getInt(cursor.getColumnIndex("purchase_count")),
                                cursor.getString(cursor.getColumnIndex("introduction")),
                                cursor.getLong(cursor.getColumnIndex("version")))
                );
            }
        }

        if(purchaseOrderList.size() > 0) {
            mOrderMinId = purchaseOrderList.get(purchaseOrderList.size() - 1).getOrderId();
        }

        cursor.close();
        return purchaseOrderList;
    }

    public long getVersion() {
        SharedPreferenceSettings orderVersion = SharedPreferenceSettings.PURCHASE_ORDER_VERSION;
        long versionValue = mSharedPreferences.getLong(
                orderVersion.getId(),
                (long) orderVersion.getDefaultValue());
        return versionValue;
    }

    public long getOrderMinId() {
        SharedPreferenceSettings minId = SharedPreferenceSettings.PURCHASE_ORDER_MIN_ID;
        long orderMinId = mSharedPreferences.getLong(
                minId.getId(),
                (long) minId.getDefaultValue());
        return orderMinId;
    }
}
