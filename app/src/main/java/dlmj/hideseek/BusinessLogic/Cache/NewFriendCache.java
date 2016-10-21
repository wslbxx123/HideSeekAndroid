package dlmj.hideseek.BusinessLogic.Cache;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.List;

import dlmj.hideseek.Common.Model.User;
import dlmj.hideseek.Common.Util.LogUtil;
import dlmj.hideseek.Common.Util.PinYinUtil;
import dlmj.hideseek.DataAccess.NewFriendTableManager;

/**
 * Created by Two on 20/10/2016.
 */
public class NewFriendCache extends BaseCache<User> {
    private final static String TAG = "NewFriendCache";
    private static NewFriendCache mInstance;
    private NewFriendTableManager mNewFriendTableManager;

    public static NewFriendCache getInstance(Context context){
        synchronized (NewFriendCache.class){
            if(mInstance == null){
                mInstance = new NewFriendCache(context);
            }
        }
        return mInstance;
    }

    public NewFriendCache(Context context) {
        super();
        mNewFriendTableManager = NewFriendTableManager.getInstance(context);
    }

    public List<User> getFriendList() {
        if(this.mList.size() == 0) {
            this.mList = mNewFriendTableManager.searchFriends();
        }

        return this.mList;
    }

    public User setFriend(String friendInfo, String message, boolean isFriend) {
        User user = saveFriend(friendInfo, message, isFriend);

        mList = mNewFriendTableManager.searchFriends();
        return user;
    }

    public User saveFriend(String friendInfo, String message, boolean isFriend) {
        try {
            JSONObject friend = new JSONObject(friendInfo);

            User user = new User(
                    friend.getLong("pk_id"),
                    friend.getString("phone"),
                    friend.getString("nickname"),
                    friend.getString("register_date"),
                    friend.getString("photo_url"),
                    friend.getString("small_photo_url"),
                    User.SexEnum.valueOf(friend.getInt("sex")),
                    friend.getString("region"),
                    User.RoleEnum.valueOf(friend.getInt("role")),
                    friend.getLong("version"),
                    PinYinUtil.converterToFirstSpell(friend.getString("nickname")));

            user.setRequestMessage(message);
            user.setIsFriend(isFriend);

            mNewFriendTableManager.updateFriends(user);

            return user;
        } catch (JSONException ex) {
            LogUtil.e(TAG, ex.getMessage());
            ex.printStackTrace();
        } catch (ParseException ex) {
            LogUtil.e(TAG, ex.getMessage());
            ex.printStackTrace();
        }

        return null;
    }

    public void updateFriendStatus(long friendId) {
        mNewFriendTableManager.updateFriendStatus(friendId);

        this.mList = mNewFriendTableManager.searchFriends();
    }

    public void removeFriend(User friend) {
        mNewFriendTableManager.removeFriend(friend.getPKId());

        this.mList = mNewFriendTableManager.searchFriends();
    }
}
