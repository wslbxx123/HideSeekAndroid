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

import dlmj.hideseek.Common.Model.Product;
import dlmj.hideseek.Common.Params.SharedPreferenceSettings;
import dlmj.hideseek.Common.Util.SharedPreferenceUtil;

/**
 * Created by Two on 22/10/2016.
 */
public class ProductTableManager {
    private final static String TABLE_NAME = "product";
    private SQLiteDatabase mSQLiteDatabase;
    private static ProductTableManager mInstance;
    private SharedPreferences mSharedPreferences;
    private long mProductMinId = 0;
    private SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public static ProductTableManager getInstance(Context context){
        synchronized (ProductTableManager.class){
            if(mInstance == null){
                mInstance = new ProductTableManager(context);
            }
        }
        return mInstance;
    }

    public ProductTableManager(Context context){
        mSharedPreferences = SharedPreferenceUtil.getSharedPreferences();
        mSQLiteDatabase = DatabaseManager.getInstance(context).getDatabase();
        mSQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                "product_id bigint, " +
                "name varchar, " +
                "image_url varchar, " +
                "price double, " +
                "purchase_count integer, " +
                "introduction varchar, " +
                "version bigint)");
    }

    public void updateProducts(long productMinId, long version, List<Product> productList) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        SharedPreferenceSettings productVersion = SharedPreferenceSettings.PRODUCT_VERSION;
        editor.putLong(productVersion.getId(), version);
        SharedPreferenceSettings minId = SharedPreferenceSettings.PRODUCT_MIN_ID;
        editor.putLong(minId.getId(), productMinId);
        editor.apply();

        synchronized (ProductTableManager.class) {
            mSQLiteDatabase.beginTransaction();
            for(Product product : productList){
                ContentValues contentValues = new ContentValues();
                contentValues.put("name", product.getName());
                contentValues.put("image_url", product.getImageUrl());
                contentValues.put("price", product.getPrice());
                contentValues.put("purchase_count", product.getPurchaseCount());
                contentValues.put("introduction", product.getIntroduction());
                contentValues.put("version", product.getVersion());
                String[] args = { String.valueOf(product.getPkId()) };
                int count = mSQLiteDatabase.update(TABLE_NAME, contentValues, "product_id=?", args);

                if(count == 0) {
                    contentValues.put("product_id", product.getPkId());
                    mSQLiteDatabase.insert(TABLE_NAME, null, contentValues);
                }
            }
            mSQLiteDatabase.setTransactionSuccessful();
            mSQLiteDatabase.endTransaction();
        }
    }

    public String getUpdateDate() {
        SharedPreferenceSettings updateDate = SharedPreferenceSettings.PRODUCT_UPDATE_TIME;
        String updateDateStr = mSharedPreferences.getString(
                updateDate.getId(),
                (String) updateDate.getDefaultValue());
        return updateDateStr;
    }

    public void clearMoreData() {
        Cursor cursor = mSQLiteDatabase.rawQuery("select product_id from " + TABLE_NAME +
                " order by product_id desc limit 20", null);
        cursor.moveToLast();

        if(cursor.getCount() > 0) {
            Long productId = cursor.getLong(0);

            if(mProductMinId < productId) {
                mProductMinId = productId;
            }

            SharedPreferences.Editor editor = mSharedPreferences.edit();
            SharedPreferenceSettings minId = SharedPreferenceSettings.PRODUCT_MIN_ID;
            editor.putLong(minId.getId(), productId);
            editor.apply();
            mSQLiteDatabase.delete(TABLE_NAME, "product_id<?", new String[]{productId + ""});
        }

        cursor.close();
    }

    public List<Product> searchProducts() {
        String updateDateStr = getUpdateDate();

        Date curDate = new Date(System.currentTimeMillis());
        String curDateStr = mDateFormat.format(curDate);

        SharedPreferences.Editor editor = mSharedPreferences.edit();
        SharedPreferenceSettings updateDate = SharedPreferenceSettings.PRODUCT_UPDATE_TIME;
        editor.putString(updateDate.getId(), curDateStr);
        editor.apply();

        if(!updateDateStr.isEmpty() && !curDateStr.equals(updateDateStr)) {
            clearMoreData();
        }

        String limit = null;
        String queryStr;
        String[] selectionArgs;
        if(mProductMinId == 0) {
            limit = 10 + "";
            selectionArgs = new String[]{};
            queryStr = null;
        } else {
            queryStr = "product_id>=?";
            selectionArgs = new String[]{mProductMinId + ""};
        }

        Cursor cursor;

        if(limit == null) {
            cursor = mSQLiteDatabase.query(TABLE_NAME, null, queryStr,
                    selectionArgs, null, null, "product_id desc");
        } else {
            cursor = mSQLiteDatabase.query(TABLE_NAME, null, queryStr,
                    selectionArgs, null, null, "product_id desc", limit);
        }

        List<Product> productList = new LinkedList<>();
        while (cursor.moveToNext()) {
            productList.add(new Product(
                            cursor.getLong(cursor.getColumnIndex("product_id")),
                            cursor.getString(cursor.getColumnIndex("name")),
                            cursor.getString(cursor.getColumnIndex("image_url")),
                            cursor.getDouble(cursor.getColumnIndex("price")),
                            cursor.getInt(cursor.getColumnIndex("purchase_count")),
                            cursor.getString(cursor.getColumnIndex("introduction")),
                            cursor.getLong(cursor.getColumnIndex("version")))
            );
        }

        if(productList.size() > 0) {
            mProductMinId = productList.get(productList.size() - 1).getPkId();
        }

        cursor.close();
        return productList;
    }

    public List<Product> getMoreProducts(int count, long version, boolean hasLoaded) {
        Cursor cursor = mSQLiteDatabase.query(TABLE_NAME, null, "version<=? and product_id<?",
                new String[]{version + "", mProductMinId + ""}, null, null, "product_id desc", count + "");
        List<Product> productList = new LinkedList<>();
        if(cursor.getCount() == count || hasLoaded) {
            while (cursor.moveToNext()) {
                productList.add(new Product(
                                cursor.getLong(cursor.getColumnIndex("product_id")),
                                cursor.getString(cursor.getColumnIndex("name")),
                                cursor.getString(cursor.getColumnIndex("image_url")),
                                cursor.getDouble(cursor.getColumnIndex("price")),
                                cursor.getInt(cursor.getColumnIndex("purchase_count")),
                                cursor.getString(cursor.getColumnIndex("introduction")),
                                cursor.getLong(cursor.getColumnIndex("version")))
                );
            }
        }

        if(productList.size() > 0) {
            mProductMinId = productList.get(productList.size() - 1).getPkId();
        }

        cursor.close();
        return productList;
    }

    public long getVersion() {
        SharedPreferenceSettings productVersion = SharedPreferenceSettings.PRODUCT_VERSION;
        long versionValue = mSharedPreferences.getLong(
                productVersion.getId(),
                (long) productVersion.getDefaultValue());
        return versionValue;
    }

    public long getProductMinId() {
        SharedPreferenceSettings minId = SharedPreferenceSettings.PRODUCT_MIN_ID;
        long productMinId = mSharedPreferences.getLong(
                minId.getId(),
                (long) minId.getDefaultValue());
        return productMinId;
    }
}
