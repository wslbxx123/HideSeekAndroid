package dlmj.hideseek.UI.View;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import dlmj.hideseek.BusinessLogic.Cache.GoalCache;
import dlmj.hideseek.BusinessLogic.Cache.UserCache;
import dlmj.hideseek.BusinessLogic.Network.NetworkHelper;
import dlmj.hideseek.BusinessLogic.Network.PushManager;
import dlmj.hideseek.Common.Factory.ErrorMessageFactory;
import dlmj.hideseek.Common.Interfaces.OnUserLoginListener;
import dlmj.hideseek.Common.Interfaces.UIDataListener;
import dlmj.hideseek.Common.Model.Bean;
import dlmj.hideseek.Common.Params.UrlParams;
import dlmj.hideseek.Common.Util.BaseInfoUtil;
import dlmj.hideseek.Common.Util.LogUtil;
import dlmj.hideseek.R;

/**
 * Created by Two on 13/10/2016.
 */
public class LoginDialog extends Dialog implements UIDataListener<Bean> {
    private static String TAG = "LoginDialog";
    private final static int LOADING_END = 1;
    private Context mContext;
    private Button mLoginButton;
    private TextView mRegisterTextView;
    private EditText mAccountEditText;
    private EditText mPasswordEditText;
    private ImageButton mCloseButton;
    private LoadingDialog mLoadingDialog;
    private NetworkHelper mNetworkHelper;
    private String mPhone;
    private String mPassword;
    private OnUserLoginListener mOnUserLoginListener;
    private CustomSuperToast mToast;
    private ErrorMessageFactory mErrorMessageFactory;

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

    public LoginDialog(Context context) {
        super(context, R.style.AlertDialog);
        this.mContext = context;
        initData();
        findView();
        setListener();
    }

    private View.OnClickListener mOnLoginClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Map<String, String> params = new HashMap<>();
            params.put("phone", mPhone);
            params.put("password", mPassword);
            params.put("app_platform", "1");

            if (!mLoadingDialog.isShowing()) {
                mLoadingDialog.show();
            }
            mNetworkHelper.sendPostRequestWithoutSid(UrlParams.LOGIN_URL, params);
        }
    };

    private View.OnClickListener mOnRegisterClickListener = new View.OnClickListener() {
        //注册
        @Override
        public void onClick(View view) {
            mOnUserLoginListener.showRegisterDialog();
        }
    };

    private View.OnClickListener mOnLoginCloseClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            dismiss();
        }
    };

    TextWatcher mPhoneTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
            mPhone = charSequence.toString();
            Log.d("Phone", mPhone);
            checkLoginEnabled();
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    TextWatcher mPasswordTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
            mPassword = charSequence.toString();
            Log.d("Password", mPassword);
            checkLoginEnabled();
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    public void initData() {
        mNetworkHelper = new NetworkHelper(mContext);
        mErrorMessageFactory = new ErrorMessageFactory(mContext);
    }

    public void findView() {
        View loginView = LayoutInflater.from(mContext).inflate(R.layout.login, null);
        setContentView(loginView);

        mLoginButton = (Button) loginView.findViewById(R.id.loginButton);
        mRegisterTextView = (TextView) loginView.findViewById(R.id.registerTextView);
        mAccountEditText = (EditText) loginView.findViewById(R.id.accountEditText);
        mPasswordEditText = (EditText) loginView.findViewById(R.id.passwordEditText);
        mCloseButton = (ImageButton) loginView.findViewById(R.id.closeBtn);
        mLoadingDialog = new LoadingDialog(mContext, mContext.getString(R.string.loading));
        mToast = new CustomSuperToast(mContext);
    }

    public void setListener() {
        mNetworkHelper.setUiDataListener(this);

        mLoginButton.setOnClickListener(mOnLoginClickListener);
        mRegisterTextView.setOnClickListener(mOnRegisterClickListener);
        mCloseButton.setOnClickListener(mOnLoginCloseClickListener);
        mAccountEditText.addTextChangedListener(mPhoneTextWatcher);
        mPasswordEditText.addTextChangedListener(mPasswordTextWatcher);
    }

    public void setOnUserLoginListener(OnUserLoginListener onUserLoginListener) {
        this.mOnUserLoginListener = onUserLoginListener;
    }

    public void resetData() {
        mAccountEditText.setText("");
        mPasswordEditText.setText("");
    }

    public void dismiss() {
        super.dismiss();
    }

    private void checkLoginEnabled() {
        if (mPhone == null || mPhone.isEmpty() || mPassword == null || mPassword.isEmpty()) {
            mLoginButton.setEnabled(false);
        } else {
            mLoginButton.setEnabled(true);
        }
    }

    @Override
    public void onDataChanged(Bean data) {
        LogUtil.d(TAG, data.getResult());
        UserCache.getInstance().setUser(data.getResult());
        GoalCache.getInstance().setIfNeedClearMap(true);
        dismiss();

        PushManager.getInstance(BaseInfoUtil.getContext()).register();
        Message handlerMessage = new Message();
        handlerMessage.what = LOADING_END;
        mHandler.sendMessage(handlerMessage);
    }

    @Override
    public void onErrorHappened(int errorCode, String errorMessage) {
        LogUtil.d(TAG, "Error message when login: " + errorMessage);
        String errorCodeMessage = mErrorMessageFactory.get(errorCode);
        mToast.show(errorCodeMessage);

        Message handlerMessage = new Message();
        handlerMessage.what = LOADING_END;
        mHandler.sendMessage(handlerMessage);
    }
}
