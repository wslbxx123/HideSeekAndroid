package dlmj.hideseek.DataAccess;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;

import dlmj.hideseek.Common.Model.User;
import dlmj.hideseek.Common.Params.SharedPreferenceSettings;
import dlmj.hideseek.Common.Util.LogUtil;
import dlmj.hideseek.Common.Util.SharedPreferenceUtil;

/**
 * Created by Two on 6/6/16.
 */
public class FriendTableManager {
    private final static String TAG = "FriendTableManager";
    private SQLiteDatabase mSQLiteDatabase;
    private static FriendTableManager mInstance;
    private SharedPreferences mSharedPreferences;

    public static FriendTableManager getInstance(Context context){
        synchronized (FriendTableManager.class){
            if(mInstance == null){
                mInstance = new FriendTableManager(context);
            }
        }
        return mInstance;
    }

    public FriendTableManager(Context context){
        mSharedPreferences = SharedPreferenceUtil.getSharedPreferences();
        mSQLiteDatabase = DatabaseManager.getInstance(context).getDatabase();
        mSQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS friend (" +
                "account_id bigint, " +
                "phone varchar, " +
                "nickname varchar, " +
                "register_date varchar, " +
                "photo_url varchar, " +
                "small_photo_url varchar, " +
                "sex int, " +
                "region varchar, " +
                "role int, " +
                "version bigint, " +
                "pinyin varchar)");
    }

    public void updateFriend(long version, List<User> friendList) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        SharedPreferenceSettings raceGroupVersion = SharedPreferenceSettings.FRIEND_VERSION;
        editor.putLong(raceGroupVersion.getId(), version);

        synchronized (FriendTableManager.class) {
            mSQLiteDatabase.beginTransaction();
            for(User user : friendList){
                ContentValues contentValues = new ContentValues();
                contentValues.put("nickname", user.getNickname());
                contentValues.put("photo_url", user.getPhotoUrl());
                contentValues.put("small_photo_url", user.getSmallPhotoUrl());
                contentValues.put("sex", user.getSex().getValue());
                contentValues.put("region", user.getRegion());
                contentValues.put("role", user.getRole().getValue());
                contentValues.put("version", user.getVersion());
                contentValues.put("pinyin", user.getPinyin());

                String[] args = { String.valueOf(user.getPKId()) };
                int count = mSQLiteDatabase.update("friend", contentValues, "account_id=?", args);

                if(count == 0) {
                    contentValues.put("account_id", user.getPKId());
                    mSQLiteDatabase.insert("friend", null, contentValues);
                }
            }

            mSQLiteDatabase.setTransactionSuccessful();
            mSQLiteDatabase.endTransaction();
        }
    }

    public List<User> getFriends() {
        List<User> friendList = new LinkedList<>();

        try {
            Cursor cursor = mSQLiteDatabase.query("friend", null, null,
                    null, null, null, null);
            while (cursor.moveToNext()) {
                friendList.add(new User(
                        cursor.getLong(cursor.getColumnIndex("account_id")),
                        cursor.getString(cursor.getColumnIndex("phone")),
                        cursor.getString(cursor.getColumnIndex("nickname")),
                        cursor.getString(cursor.getColumnIndex("register_date")),
                        cursor.getString(cursor.getColumnIndex("photo_url")),
                        cursor.getString(cursor.getColumnIndex("small_photo_url")),
                        User.SexEnum.valueOf(cursor.getInt(cursor.getColumnIndex("sex"))),
                        cursor.getString(cursor.getColumnIndex("region")),
                        User.RoleEnum.valueOf(cursor.getInt(cursor.getColumnIndex("role"))),
                        cursor.getLong(cursor.getColumnIndex("version")),
                        cursor.getString(cursor.getColumnIndex("pinyin"))));
            }
        } catch(ParseException ex) {
            LogUtil.e(TAG, ex.getMessage());
            ex.printStackTrace();
        }

        return friendList;
    }

    public long getVersion() {
        SharedPreferenceSettings friendVersion = SharedPreferenceSettings.FRIEND_VERSION;
        return mSharedPreferences.getLong(
                friendVersion.getId(),
                (long) friendVersion.getDefaultValue());
    }

    public List<User> searchFriends(String keyword) {
        List<User> friendList = new LinkedList<>();

        try {
            Cursor cursor = mSQLiteDatabase.rawQuery(
                    "select * from friend where nickname like \"%" + keyword + "%\" or pinyin " +
                            "like \"%" + keyword + "%\" order by pinyin", null);
            while (cursor.moveToNext()) {
                friendList.add(new User(cursor.getLong(cursor.getColumnIndex("account_id")),
                        cursor.getString(cursor.getColumnIndex("phone")),
                        cursor.getString(cursor.getColumnIndex("nickname")),
                        cursor.getString(cursor.getColumnIndex("register_date")),
                        cursor.getString(cursor.getColumnIndex("photo_url")),
                        cursor.getString(cursor.getColumnIndex("small_photo_url")),
                        User.SexEnum.valueOf(cursor.getInt(cursor.getColumnIndex("sex"))),
                        cursor.getString(cursor.getColumnIndex("region")),
                        User.RoleEnum.valueOf(cursor.getInt(cursor.getColumnIndex("role"))),
                        cursor.getLong(cursor.getColumnIndex("version")),
                        cursor.getString(cursor.getColumnIndex("pinyin"))));
            }
            cursor.close();
        } catch (ParseException ex) {
            LogUtil.e(TAG, ex.getMessage());
            ex.printStackTrace();
        }

        return friendList;
    }

    public void clear() {
        synchronized (FriendTableManager.class) {
            mSQLiteDatabase.beginTransaction();
            mSQLiteDatabase.execSQL("delete from friend; " +
                    "update sqlite_sequence SET seq = 0 where name ='friend'");
            mSQLiteDatabase.setTransactionSuccessful();
            mSQLiteDatabase.endTransaction();
        }

        SharedPreferenceSettings friendVersion = SharedPreferenceSettings.FRIEND_VERSION;
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.remove(friendVersion.getId());
        editor.apply();
    }
}
