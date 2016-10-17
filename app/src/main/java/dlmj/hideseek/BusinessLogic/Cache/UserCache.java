package dlmj.hideseek.BusinessLogic.Cache;

import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;

import dlmj.hideseek.Common.Model.User;
import dlmj.hideseek.Common.Params.SharedPreferenceSettings;
import dlmj.hideseek.Common.Util.LogUtil;
import dlmj.hideseek.Common.Util.PinYinUtil;
import dlmj.hideseek.Common.Util.SharedPreferenceUtil;

/**
 * Created by Two on 5/1/16.
 */
public class UserCache {
    private static final String TAG = "UserCache";
    private User mUser;
    private static UserCache mInstance;
    private SharedPreferences mSharedPreferences;

    private UserCache() {
        mSharedPreferences = SharedPreferenceUtil.getSharedPreferences();
    }

    public static UserCache getInstance(){
        synchronized (UserCache.class){
            if(mInstance == null){
                mInstance = new UserCache();
            }
        }
        return mInstance;
    }

    public User getUser() {
        if(mUser != null) {
            return mUser;
        }

        SharedPreferenceSettings accountInfo = SharedPreferenceSettings.USER_INFO;
        String userInfoStr = mSharedPreferences.getString(accountInfo.getId(),
                accountInfo.getDefaultValue().toString());

        return from(userInfoStr);
    }

    public void update(User user,String key,Object value)
    {
        try {
            mUser=user;
            //读取缓存
            SharedPreferenceSettings accountInfo = SharedPreferenceSettings.USER_INFO;
            String userInfoStr = mSharedPreferences.getString(accountInfo.getId(),
                    accountInfo.getDefaultValue().toString());
            JSONObject localJsonObj=new JSONObject(userInfoStr);
            localJsonObj.put(key,value);
            //更新回去
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            SharedPreferenceSettings sessionToken = SharedPreferenceSettings.SESSION_TOKEN;
            SharedPreferenceSettings userInfo = SharedPreferenceSettings.USER_INFO;
            editor.putString(sessionToken.getId(), mUser.getSessionId());
            editor.putString(userInfo.getId(), localJsonObj.toString());
            editor.apply();
        } catch (Exception e) {
            LogUtil.e(TAG, e.getMessage());
        }
    }
    public void setUser(String userInfoStr) {
        mUser = from(userInfoStr);

        if(mUser != null) {
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            SharedPreferenceSettings sessionToken = SharedPreferenceSettings.SESSION_TOKEN;
            SharedPreferenceSettings userInfo = SharedPreferenceSettings.USER_INFO;
            editor.putString(sessionToken.getId(), mUser.getSessionId());
            editor.putString(userInfo.getId(), userInfoStr);
            editor.apply();
        }
    }

    public User from(String userInfoStr) {
        try {
            JSONObject userInfo = new JSONObject(userInfoStr);
            User user = new User(
                    userInfo.getLong("pk_id"),
                    userInfo.getString("phone"),
                    userInfo.getString("session_id"),
                    userInfo.getString("nickname"),
                    userInfo.getString("register_date"),
                    userInfo.getInt("record"),
                    User.RoleEnum.valueOf(userInfo.getInt("role")),
                    userInfo.getLong("version"),
                    PinYinUtil.converterToFirstSpell(userInfo.getString("nickname")),
                    userInfo.getInt("bomb_num"),
                    userInfo.getInt("has_guide") == 1,
                    userInfo.getInt("friend_num"),
                    User.SexEnum.valueOf(userInfo.getInt("sex")),
                    userInfo.getString("photo_url"),
                    userInfo.getString("small_photo_url"),
                    userInfo.getString("region"),
                    userInfo.getString("default_area"),
                    userInfo.getString("default_address")
            );

            return user;
        } catch (JSONException e) {
            LogUtil.e(TAG, e.getMessage());
            return null;
        } catch(ParseException e) {
            LogUtil.e(TAG, e.getMessage());
            return null;
        }
    }

    public boolean ifLogin() {
        SharedPreferenceSettings sessionToken = SharedPreferenceSettings.SESSION_TOKEN;
        String sessionTokenValue = mSharedPreferences.getString(
                sessionToken.getId(),
                (String)sessionToken.getDefaultValue());
        return !sessionTokenValue.isEmpty();
    }
}
