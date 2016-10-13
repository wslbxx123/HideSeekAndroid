package dlmj.hideseek.UI.Activity;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

import com.github.johnpersano.supertoasts.SuperToast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import dlmj.hideseek.BusinessLogic.Helper.UserInfoManager;
import dlmj.hideseek.BusinessLogic.Network.NetworkHelper;
import dlmj.hideseek.Common.Factory.ErrorMessageFactory;
import dlmj.hideseek.Common.Interfaces.UIDataListener;
import dlmj.hideseek.Common.Model.Bean;
import dlmj.hideseek.Common.Model.User;
import dlmj.hideseek.Common.Params.CodeParams;
import dlmj.hideseek.Common.Params.UrlParams;
import dlmj.hideseek.Common.Util.LogUtil;
import dlmj.hideseek.Common.Util.PinYinUtil;
import dlmj.hideseek.R;
import dlmj.hideseek.UI.View.CustomSuperToast;
import dlmj.hideseek.UI.View.LoadingDialog;

public class AddFriendActivity extends BaseActivity implements UIDataListener<Bean> {
    private final static String TAG = "AddFriendActivity";
    private NetworkHelper mNetworkHelper;
    private EditText mSearchEditText;
    private LoadingDialog mLoadingDialog;
    private ErrorMessageFactory mErrorMessageFactory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_friend);
        initData();
        findView();
        setListener();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            if(!mSearchEditText.getText().toString().isEmpty()) {
                refreshData();
            }
        }
        return true;
    }

    public void initData() {
        mNetworkHelper = new NetworkHelper(this);
        mErrorMessageFactory = new ErrorMessageFactory(this);
    }

    public void findView() {
        mNetworkHelper.setUiDataListener(this);
        mSearchEditText = (EditText) findViewById(R.id.searchEditText);
        mLoadingDialog = new LoadingDialog(this, getString(R.string.loading));
    }

    public void setListener() {

    }

    public void refreshData() {
        Map<String, String> params = new HashMap<>();
        params.put("search_word", mSearchEditText.getText().toString());

        if (!mLoadingDialog.isShowing()) {
            mLoadingDialog.show();
        }
        mResponseCode = 0;
        mNetworkHelper.sendPostRequestWithoutSid(UrlParams.SEARCH_FRIENDS_URL, params);
    }

    @Override
    public void onDataChanged(Bean data) {
        LogUtil.d(TAG, data.getResult());
        mResponseCode = CodeParams.SUCCESS;
        getUsers(data.getResult());
    }

    @Override
    public void onErrorHappened(int errorCode, String errorMessage) {
        mResponseCode = errorCode;

        String message = mErrorMessageFactory.get(errorCode);
        mToast.show(message);
    }

    private List<User> getUsers(String friendInfoStr) {
        List<User> list = new LinkedList<>();
        try {
            JSONArray friendList = new JSONArray(friendInfoStr);
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
                user.setIsFriend(friend.getInt("is_friend") == 1);
                list.add(user);
            }
        } catch (JSONException ex) {
            ex.printStackTrace();
        } catch (ParseException ex) {
            ex.printStackTrace();
        }

        return list;
    }
}
