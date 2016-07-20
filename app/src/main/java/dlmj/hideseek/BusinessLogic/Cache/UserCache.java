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
    private final static String TAG = "UserCache";
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
                    userInfo.getInt("bomb_num"),
                    userInfo.getString("has_guide"),
                    userInfo.getLong("pk_id"),
                    userInfo.getString("phone"),
                    userInfo.getString("session_id"),
                    userInfo.getString("nickname"),
                    userInfo.getString("register_date"),
                    User.RoleEnum.valueOf(userInfo.getInt("role")),
                    userInfo.getLong("version"),
                    PinYinUtil.converterToFirstSpell(userInfo.getString("nickname"))
            );

            if(userInfo.has("photo_url")) {
                user.setPhotoUrl(userInfo.getString("photo_url"));
            }

            if(userInfo.has("sex")) {
                user.setSex(User.SexEnum.valueOf(userInfo.getInt("sex")));
            }

            if(userInfo.has("region")) {
                user.setRegion(userInfo.getString("region"));
            }

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
