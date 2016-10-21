package dlmj.hideseek.DataAccess;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import dlmj.hideseek.BusinessLogic.Cache.UserCache;
import dlmj.hideseek.Common.Model.User;
import dlmj.hideseek.Common.Util.LogUtil;

/**
 * Created by Two on 20/10/2016.
 */
public class NewFriendTableManager {
    private final static String TAG = "NewFriendTableManager";
    private static String TABLE_NAME = "new_friend";
    private static NewFriendTableManager mInstance;
    private SQLiteDatabase mSQLiteDatabase;
    private SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static NewFriendTableManager getInstance(Context context){
        synchronized (NewFriendTableManager.class){
            if(mInstance == null){
                mInstance = new NewFriendTableManager(context);
            }
        }
        return mInstance;
    }

    public NewFriendTableManager(Context context) {
        if(UserCache.getInstance().ifLogin()) {
            refreshTable(context, UserCache.getInstance().getUser().getPKId());
        }
    }

    public void refreshTable(Context context, long accountId){
        mSQLiteDatabase = DatabaseManager.getInstance(context).getDatabase();

        TABLE_NAME = "new_friend_" + accountId;
        mSQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(" +
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
                "pinyin varchar, " +
                "add_time varchar, " +
                "message varchar, " +
                "is_friend int)");
    }

    public List<User> searchFriends() {
        List<User> friendList = new LinkedList<>();

        try {
            Cursor cursor = mSQLiteDatabase.query(TABLE_NAME, null, null,
                    null, null, null, null);
            while (cursor.moveToNext()) {
                User user = new User(
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
                        cursor.getString(cursor.getColumnIndex("pinyin")));

                user.setAddTime(cursor.getString(cursor.getColumnIndex("add_time")));
                user.setRequestMessage(cursor.getString(cursor.getColumnIndex("message")));
                user.setIsFriend(cursor.getInt(cursor.getColumnIndex("is_friend")) == 1);

                friendList.add(user);
            }
        } catch(ParseException ex) {
            LogUtil.e(TAG, ex.getMessage());
            ex.printStackTrace();
        }

        return friendList;
    }

    public void updateFriends(User user) {
        synchronized (NewFriendTableManager.class) {
            mSQLiteDatabase.beginTransaction();
            ContentValues contentValues = new ContentValues();
            contentValues.put("phone", user.getPhone());
            contentValues.put("nickname", user.getNickname());
            contentValues.put("register_date", mDateFormat.format(new Date()));
            contentValues.put("photo_url", user.getPhotoUrl());
            contentValues.put("small_photo_url", user.getSmallPhotoUrl());
            contentValues.put("sex", user.getSex().getValue());
            contentValues.put("region", user.getRegion());
            contentValues.put("role", user.getRole().getValue());
            contentValues.put("version", user.getVersion());
            contentValues.put("pinyin", user.getPinyin());
            contentValues.put("add_time", user.getAddTime());
            contentValues.put("message", user.getRequestMessage());
            contentValues.put("is_friend", user.getIsFriend());

            String[] args = { String.valueOf(user.getPKId()) };
            int count = mSQLiteDatabase.update(TABLE_NAME, contentValues, "account_id=?", args);

            if(count == 0) {
                contentValues.put("account_id", user.getPKId());
                mSQLiteDatabase.insert(TABLE_NAME, null, contentValues);
            }

            mSQLiteDatabase.setTransactionSuccessful();
            mSQLiteDatabase.endTransaction();
        }
    }

    public void updateFriendStatus(long friendId) {
        synchronized (NewFriendTableManager.class) {
            mSQLiteDatabase.beginTransaction();
            ContentValues contentValues = new ContentValues();
            contentValues.put("is_friend", 1);
            String[] args = { String.valueOf(friendId) };
            mSQLiteDatabase.update(TABLE_NAME, contentValues, "account_id=?", args);
            mSQLiteDatabase.setTransactionSuccessful();
            mSQLiteDatabase.endTransaction();
        }
    }

    public void removeFriend(long friendId) {
        synchronized (NewFriendTableManager.class) {
            mSQLiteDatabase.beginTransaction();
            mSQLiteDatabase.delete(TABLE_NAME, "account_id=", new String[]{friendId + ""});
            mSQLiteDatabase.setTransactionSuccessful();
            mSQLiteDatabase.endTransaction();
        }
    }

    public void clear() {
        synchronized (FriendTableManager.class) {
            mSQLiteDatabase.beginTransaction();
            mSQLiteDatabase.execSQL("delete from " + TABLE_NAME + "; " +
                    "update sqlite_sequence SET seq = 0 where name ='" + TABLE_NAME + "'");
            mSQLiteDatabase.setTransactionSuccessful();
            mSQLiteDatabase.endTransaction();
        }
    }
}
