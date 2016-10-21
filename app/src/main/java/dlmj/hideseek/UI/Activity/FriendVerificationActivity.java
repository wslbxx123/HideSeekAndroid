package dlmj.hideseek.UI.Activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import dlmj.hideseek.BusinessLogic.Cache.UserCache;
import dlmj.hideseek.BusinessLogic.Network.NetworkHelper;
import dlmj.hideseek.Common.Factory.ErrorMessageFactory;
import dlmj.hideseek.Common.Interfaces.UIDataListener;
import dlmj.hideseek.Common.Model.Bean;
import dlmj.hideseek.Common.Model.User;
import dlmj.hideseek.Common.Params.CodeParams;
import dlmj.hideseek.Common.Params.IntentExtraParam;
import dlmj.hideseek.Common.Params.UrlParams;
import dlmj.hideseek.Common.Util.LogUtil;
import dlmj.hideseek.R;
import dlmj.hideseek.UI.View.LoadingDialog;

/**
 * Created by Two on 18/10/2016.
 */
public class FriendVerificationActivity extends BaseActivity implements UIDataListener<Bean> {
    private final static String TAG = "FriendVerificationActivity";
    private final static int LOADING_END = 1;
    private TextView mCancelTextView;
    private TextView mSubmitTextView;
    private EditText mMessageEditText;
    private LoadingDialog mLoadingDialog;
    private int mCount = 0;

    private NetworkHelper mNetworkHelper;
    private ErrorMessageFactory mErrorMessageFactory;
    private User mFriend;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LOADING_END:
                    if (mLoadingDialog.isShowing()) {
                        mLoadingDialog.dismiss();
                    }
            }
            super.handleMessage(msg);
        }
    };

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
        mErrorMessageFactory = new ErrorMessageFactory(this);
        mFriend = getIntent().getParcelableExtra(IntentExtraParam.PROFILE_INFO);
    }

    private void findView() {
        mCancelTextView = (TextView) findViewById(R.id.cancelTextView);
        mSubmitTextView = (TextView) findViewById(R.id.submitTextView);
        mMessageEditText = (EditText) findViewById(R.id.messageEditText);
        mMessageEditText.setText(String.format(this.getString(R.string.self_introduction),
                UserCache.getInstance().getUser().getNickname()));
        mLoadingDialog = new LoadingDialog(this, this.getString(R.string.loading));
    }

    private void setListener() {
        mNetworkHelper.setUiDataListener(this);

        mCancelTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mSubmitTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCount = 0;
                sendFriendRequest();
            }
        });

        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                mSubmitTextView.setEnabled(!TextUtils.isEmpty(editable));
            }
        });
    }

    private void sendFriendRequest() {
        mCount++;
        Map<String, String> params = new HashMap<>();
        params.put("friend_id", mFriend.getPKId() + "");

        if (!mLoadingDialog.isShowing()) {
            mLoadingDialog.show();
        }
        mNetworkHelper.sendPostRequest(UrlParams.ADD_FRIENDS_URL, params);
    }

    @Override
    public void onDataChanged(Bean data) {
        LogUtil.d(TAG, data.getResult());
        mResponseCode = CodeParams.SUCCESS;
        Message handlerMessage = new Message();
        handlerMessage.what = LOADING_END;
        mHandler.sendMessage(handlerMessage);
    }

    @Override
    public void onErrorHappened(int errorCode, String errorMessage) {
        mResponseCode = errorCode;
        if(errorCode == CodeParams.ERROR_FAIL_SEND_MESSAGE && mCount <= 5) {
            sendFriendRequest();
        } else {
            Message handlerMessage = new Message();
            handlerMessage.what = LOADING_END;
            mHandler.sendMessage(handlerMessage);

            String message = mErrorMessageFactory.get(errorCode);
            mToast.show(message);
        }
    }
}
