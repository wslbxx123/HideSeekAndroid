package dlmj.hideseek.DataAccess;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.LinkedList;
import java.util.List;

import dlmj.hideseek.Common.Model.ForeignCity;

/**
 * Created by Two on 5/25/16.
 */
public class ForeignCityTableManager {
    private SQLiteDatabase mSQLiteDatabase;
    private static ForeignCityTableManager mInstance;
    private Context mContext;

    public ForeignCityTableManager(Context context) {
        mSQLiteDatabase = DatabaseManager.getInstance(context).getDatabase();
        mContext = context;
    }

    public static ForeignCityTableManager getInstance(Context context){
        synchronized (ForeignCityTableManager.class){
            if(mInstance == null){
                mInstance = new ForeignCityTableManager(context);
            }
        }
        return mInstance;
    }

    public List<ForeignCity> getAllCities(){
        Cursor cursor = mSQLiteDatabase.rawQuery("SELECT * FROM foreign_city", null);
        List<ForeignCity> cities = new LinkedList<>();
        while (cursor.moveToNext()) {
            cities.add(new ForeignCity(
                    cursor.getString(cursor.getColumnIndex("name")),
                    cursor.getString(cursor.getColumnIndex("country"))));
        }
        cursor.close();
        return cities;
    }

    public List<ForeignCity> searchCities(String keyword) {
        Cursor cursor = mSQLiteDatabase.rawQuery(
                "select * from foreign_city where name like \"%" + keyword + "%\" order by name", null);
        List<ForeignCity> cities = new LinkedList<>();
        while (cursor.moveToNext()) {
            cities.add(new ForeignCity(
                    cursor.getString(cursor.getColumnIndex("name")),
                    cursor.getString(cursor.getColumnIndex("country"))));
        }
        cursor.close();
        return cities;
    }
}
