package dlmj.hideseek.DataAccess;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import dlmj.hideseek.Common.Model.DomesticCity;

/**
 * Created by Two on 5/11/16.
 */
public class DomesticCityTableManager {
    private SQLiteDatabase mSQLiteDatabase;
    private static DomesticCityTableManager mInstance;
    private SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private Context mContext;

    public DomesticCityTableManager(Context context) {
        mSQLiteDatabase = DatabaseManager.getInstance(context).getDatabase();
        mSQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS recent_city (" +
                "id integer primary key autoincrement, " +
                "name varchar(40), " +
                "pinyin varchar(40), " +
                "timestamp datetime default current_timestamp)");
        mContext = context;
    }

    public static DomesticCityTableManager getInstance(Context context){
        synchronized (DomesticCityTableManager.class){
            if(mInstance == null){
                mInstance = new DomesticCityTableManager(context);
            }
        }
        return mInstance;
    }

    public List<DomesticCity> getAllCities(){
        Cursor cursor = mSQLiteDatabase.rawQuery("SELECT * FROM domestic_city order by pinyin", null);
        List<DomesticCity> cities = new LinkedList<>();
        while (cursor.moveToNext()) {
            cities.add(new DomesticCity(
                    cursor.getString(cursor.getColumnIndex("name")),
                    cursor.getString(cursor.getColumnIndex("pinyin"))));
        }
        cursor.close();
        return cities;
    }

    public List<DomesticCity> searchCities(String keyword) {
        Cursor cursor = mSQLiteDatabase.rawQuery(
                "select * from domestic_city where name like \"%" + keyword
                        + "%\" or pinyin like \"%" + keyword + "%\" order by pinyin", null);
        List<DomesticCity> cities = new LinkedList<>();
        while (cursor.moveToNext()) {
            cities.add(new DomesticCity(
                    cursor.getString(cursor.getColumnIndex("name")),
                    cursor.getString(cursor.getColumnIndex("pinyin"))));
        }
        cursor.close();
        return cities;
    }

    public void insertRecentCity(DomesticCity city) {
        synchronized (DomesticCityTableManager.class) {
            mSQLiteDatabase.beginTransaction();
            ContentValues contentValues = new ContentValues();
            contentValues.put("pinyin", city.getPinyin());
            contentValues.put("timestamp", mDateFormat.format(new Date()));

            String[] args = {city.getName()};
            int count = mSQLiteDatabase.update("recent_city", contentValues, "name=?", args);

            if(count == 0) {
                contentValues.put("name", city.getName());
                mSQLiteDatabase.insert("recent_city", null, contentValues);
            }

            mSQLiteDatabase.setTransactionSuccessful();
            mSQLiteDatabase.endTransaction();
        }
    }

    public List<DomesticCity> getRecentCities() {
        Cursor cursor = mSQLiteDatabase.rawQuery("select * from recent_city order by timestamp desc " +
                "limit 0, 3", null);
        List<DomesticCity> cities = new LinkedList<>();
        while (cursor.moveToNext()) {
            cities.add(new DomesticCity(
                    cursor.getString(cursor.getColumnIndex("name")),
                    cursor.getString(cursor.getColumnIndex("pinyin"))));
        }
        cursor.close();
        return cities;
    }
}
