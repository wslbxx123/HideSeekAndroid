package dlmj.hideseek.UI.Activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.android.volley.toolbox.StringRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import dlmj.hideseek.BusinessLogic.Cache.UserCache;
import dlmj.hideseek.BusinessLogic.Network.NetworkHelper;
import dlmj.hideseek.Common.Interfaces.UIDataListener;
import dlmj.hideseek.Common.Model.Bean;
import dlmj.hideseek.Common.Model.User;
import dlmj.hideseek.Common.Params.IntentExtraParam;
import dlmj.hideseek.Common.Params.UrlParams;
import dlmj.hideseek.Common.Util.LogUtil;
import dlmj.hideseek.R;

/**
 * Created by Two on 18/10/2016.
 */
public class FriendVerificationActivity extends Activity implements UIDataListener<Bean> {
    private final static String TAG = "FriendVerificationActivity";
    private TextView mCancelTextView;
    private TextView mSubmitTextView;
    private TextView mMessageTextView;

    private NetworkHelper mNetworkHelper;
    private User mFriend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friend_verification);

        initData();
        findView();
        setListener();
    }

    private void initData() {
        mNetworkHelper = new NetworkHelper(this);
        mFriend = getIntent().getParcelableExtra(IntentExtraParam.PROFILE_INFO);
    }

    private void findView() {
        mCancelTextView = (TextView) findViewById(R.id.cancelTextView);
        mSubmitTextView = (TextView) findViewById(R.id.submitTextView);
        mMessageTextView = (TextView) findViewById(R.id.messageTextView);
    }

    private void setListener() {
        mCancelTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mSubmitTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Map<String, String> params = new HashMap<>();
                params.put("friend_id", mFriend.getPKId() + "");
                mNetworkHelper.sendPostRequest(UrlParams.ADD_FRIENDS_URL, params);
            }
        });
    }

    @Override
    public void onDataChanged(Bean data) {
        try {
            LogUtil.d(TAG, data.getResult());
        } catch (Exception e) {
            LogUtil.e(TAG, e.getMessage());
        }
    }

    @Override
    public void onErrorHappened(int errorCode, String errorMessage) {

    }
}
