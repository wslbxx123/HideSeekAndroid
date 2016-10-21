package dlmj.hideseek.UI.Activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import dlmj.hideseek.BusinessLogic.Cache.UserCache;
import dlmj.hideseek.BusinessLogic.Network.NetworkHelper;
import dlmj.hideseek.Common.Factory.ErrorMessageFactory;
import dlmj.hideseek.Common.Interfaces.UIDataListener;
import dlmj.hideseek.Common.Model.Bean;
import dlmj.hideseek.Common.Model.User;
import dlmj.hideseek.Common.Params.CodeParams;
import dlmj.hideseek.Common.Params.UrlParams;
import dlmj.hideseek.Common.Util.LogUtil;
import dlmj.hideseek.R;
import dlmj.hideseek.UI.View.CustomSuperToast;
import dlmj.hideseek.UI.View.LoadingDialog;

/**
 * <br/>
 * Created on 2016/10/18
 *
 * @author yekangqi
 */

public class ModifyNickNameActivity extends BaseActivity implements UIDataListener<Bean> {
    private static final String TAG="ModifyNickNameActivity";
    private static final int LOADING_END = 1;
    private View mCancelTextView;
    private View mSubmitTextView;
    private User mUser;
    private EditText mNinameEditText;
    private NetworkHelper mNetworkHelper;
    private LoadingDialog mLoadingDialog;
    private CustomSuperToast mToast;
    private ErrorMessageFactory mErrorMessageFactory;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LOADING_END:
                    if (mLoadingDialog.isShowing()) {
                        mLoadingDialog.dismiss();
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nickname);
        initData();
        findView();
        setListener();
    }

    private void initData() {
        mNetworkHelper = new NetworkHelper(this);
        mUser = UserCache.getInstance().getUser();
        mErrorMessageFactory = new ErrorMessageFactory(this);
    }

    private void findView() {
        mCancelTextView = findViewById(R.id.cancelTextView);
        mSubmitTextView = findViewById(R.id.submitTextView);
        mNinameEditText = (EditText) findViewById(R.id.nicknameEditText);
        mNinameEditText.setText(mUser.getNickname());
        mNinameEditText.setSelection(mUser.getNickname().length());

        mLoadingDialog = new LoadingDialog(this, getString(R.string.loading));
        mToast = new CustomSuperToast(this);
    }

    private void setListener() {
        mCancelTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mSubmitTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mLoadingDialog.isShowing()) {
                    mLoadingDialog.show();
                }
                Map<String, String> params = new HashMap<>();
                params.put("nickname",mNinameEditText.getText().toString());
                mNetworkHelper.sendPostRequest(UrlParams.UPDATE_NICKNAME_URL,params);
            }
        });
        mNinameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                mSubmitTextView.setEnabled(!TextUtils.isEmpty(editable));
            }
        });
        mNetworkHelper.setUiDataListener(this);
    }

    @Override
    public void onDataChanged(Bean data) {
        mResponseCode = CodeParams.SUCCESS;
        try {
            JSONObject result=new JSONObject(data.getResult());
            String nickname =result.optString("nickname");
            mUser.setNickname(nickname);
            UserCache.getInstance().update(mUser,"nickname",nickname);
            setResult(Activity.RESULT_OK);
            finish();
        } catch (Exception e) {
            LogUtil.e(TAG, e.getMessage());
        }
    }

    @Override
    public void onErrorHappened(int errorCode, String errorMessage) {
        mHandler.sendEmptyMessage(LOADING_END);
        mResponseCode = errorCode;
        String message = mErrorMessageFactory.get(errorCode);
        mToast.show(message);
    }
}
