package dlmj.hideseek.DataAccess;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;

import dlmj.hideseek.Common.Util.SharedPreferenceUtil;

/**
 * 创建者     ZPL
 * 创建时间   2016/7/20 16:12
 * 描述	      ${TODO}
 * <p/>
 * 更新者     $Author$
 * 更新时间   $Date$
 * 更新描述   ${TODO}
 */
public class ShopTableManager {

    private static ShopTableManager mInstance;
    private final SQLiteDatabase mSQLiteDatabase;
    private final SharedPreferences mSharedPreferences;

    public ShopTableManager(Context context) {
        mSharedPreferences = SharedPreferenceUtil.getSharedPreferences();
        mSQLiteDatabase = DatabaseManager.getInstance(context).getDatabase();
        mSQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS shop (" +
                "record_id bigint, " +
                "photo_url varchar, " +
                "nickname varchar, " +
                "time datetime, " +
                "goal_type integer, " +
                "score integer, " +
                "score_sum integer, " +
                "version bigint)");
    }

    public static ShopTableManager getInstance(Context context){
        synchronized (ShopTableManager.class){
            if(mInstance == null){
                mInstance = new ShopTableManager(context);
            }
        }
        return mInstance;
    }


}
