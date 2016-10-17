package dlmj.hideseek.BusinessLogic.Cache;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;

import dlmj.hideseek.Common.Model.User;
import dlmj.hideseek.Common.Util.LogUtil;
import dlmj.hideseek.Common.Util.PinYinUtil;
import dlmj.hideseek.DataAccess.FriendTableManager;

/**
 * Created by Two on 6/6/16.
 */
public class FriendCache extends BaseCache<User>{
    private final static String TAG = "FriendCache";
    private static FriendCache mInstance;
    private FriendTableManager mFriendTableManager;
    private Context mContext;

    public static FriendCache getInstance(Context context){
        synchronized (FriendCache.class){
            if(mInstance == null){
                mInstance = new FriendCache(context);
            }
        }
        return mInstance;
    }

    public FriendCache(Context context) {
        super();
        mFriendTableManager = FriendTableManager.getInstance(context);
        mContext = context;
    }

    public List<User> getList() {
        if(super.getList().size() > 0) {
            return mList;
        }

        return mFriendTableManager.getFriends();
    }

    public void setFriends(String friendResult) {
        List<User> list = new LinkedList<>();
        try {
            JSONObject result = new JSONObject(friendResult);
            String friends = result.getString("friends");
            long version = result.getLong("version");

            if(mFriendTableManager.getVersion() < version) {
                JSONArray friendList = new JSONArray(friends);
                String friendStr;
                for (int i = 0; i < friendList.length(); i++) {
                    friendStr = friendList.getString(i);
                    JSONObject friend = new JSONObject(friendStr);

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

                    user.setAlias(friend.getString("remark"));

                    list.add(user);
                }

                mFriendTableManager.updateFriend(version, list);
                mList = list;
            }
        }
        catch (ParseException e) {
            LogUtil.e(TAG, e.getMessage());
            e.printStackTrace();
        }
        catch (JSONException e) {
            LogUtil.e(TAG, e.getMessage());
            e.printStackTrace();
        }
    }
}
