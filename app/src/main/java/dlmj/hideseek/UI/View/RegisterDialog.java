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

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;
import dlmj.hideseek.BusinessLogic.Network.NetworkHelper;
import dlmj.hideseek.BusinessLogic.Network.PushManager;
import dlmj.hideseek.Common.Factory.ErrorMessageFactory;
import dlmj.hideseek.Common.Interfaces.OnUserRegisterListener;
import dlmj.hideseek.Common.Interfaces.UIDataListener;
import dlmj.hideseek.Common.Params.UrlParams;
import dlmj.hideseek.Common.Util.BaseInfoUtil;
import dlmj.hideseek.Common.Util.LogUtil;
import dlmj.hideseek.R;

/**
 * Created by Two on 13/10/2016.
 */
public class RegisterDialog extends Dialog {
    private static String TAG = "RegisterDialog";
    private final static int COUNT_DOWN_REFRESH = 1;
    private final static int COUNT_DOWN_START = 2;
    private final static int LOADING_END = 3;
    private final static int VERIFICATION_SUCCESS = 4;
    private final static int VERIFICATION_FAILED = 5;
    private Context mContext;
    private NetworkHelper mNetworkHelper;
    private LoadingDialog mLoadingDialog;
    private Button mCodeButton;
    private Button mRegisterButton;
    private String mRegisterPhone;
    private ImageButton mCloseButton;
    private EditText mPhoneEditText;
    private EditText mCodeEditText;
    private EditText mNicknameEditText;
    private EditText mPasswordEditText;
    private EditText mReInputPasswordEditText;
    private String mCode;
    private String mNickname;
    private String mRegisterPassword;
    private String mReInputPassword;
    private CustomSuperToast mToast;
    private OnUserRegisterListener mOnUserRegisterListener;
    private ErrorMessageFactory mErrorMessageFactory;
    private EventHandler mEventHandler;
    private TimerTask mTask;
    private Timer mTimer = new Timer();
    private int mCountDown = 60;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case COUNT_DOWN_REFRESH:
                    mCodeButton.setText(mCountDown + "s");
                    mCountDown--;

                    if (mCountDown == -1) {
                        mCodeButton.setText(mContext.getString(R.string.send_verification_code));
                        if(mTask != null) {
                            mTask.cancel();
                        }
                    }
                    break;
                case COUNT_DOWN_START:
                    mCodeButton.setEnabled(false);
                    mTask = new TimerTask() {
                        @Override
                        public void run() {
                            Message message = new Message();
                            message.what = COUNT_DOWN_REFRESH;
                            mHandler.sendMessage(message);
                        }
                    };
                    mTimer.schedule(mTask, 0, 1000);
                    break;
                case LOADING_END:
                    if (mLoadingDialog.isShowing()) {
                        mLoadingDialog.dismiss();
                    }
                    break;
                case VERIFICATION_SUCCESS:
                    checkRegisterInfo();
                    break;
                case VERIFICATION_FAILED:
                    mToast.show(mContext.getString(R.string.error_verification_code));
                    break;

            }
            super.handleMessage(msg);
        }
    };

    public RegisterDialog(Context context) {
        super(context, R.style.AlertDialog);
        this.mContext = context;
        initData();
        findView();
        setListener();
    }

    private View.OnClickListener mOnRegisterBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!mLoadingDialog.isShowing()) {
                mLoadingDialog.show();
            }

            Map<String, String> params = new HashMap<>();
            params.put("phone", mRegisterPhone);

            mNetworkHelper.sendPostRequest(UrlParams.CHECK_IF_USER_EXIST_URL, params);
        }
    };

    private View.OnClickListener mCodeBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mCountDown = 60;
            Message message = new Message();
            message.what = COUNT_DOWN_START;
            mHandler.sendMessage(message);

            SMSSDK.getSupportedCountries();
            SMSSDK.getVerificationCode("86", mRegisterPhone);
        }
    };

    private View.OnClickListener mOnRegisterCloseClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            dismiss();
        }
    };

    private TextWatcher mRegisterPhoneTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
            mRegisterPhone = charSequence.toString();
            Log.d("Register Phone", mRegisterPhone);
            checkRegisterEnabled();

            if (mRegisterPhone == null || mRegisterPhone.isEmpty()) {
                mCodeButton.setEnabled(false);
            } else {
                mCodeButton.setEnabled(true);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

            mCodeButton.setText(R.string.send_verification_code);
            if(mTask != null) {
                mTask.cancel();
            }

            if (mRegisterPhone == null || mRegisterPhone.length() < 6) {
                mCodeButton.setEnabled(false);
            } else {
                mCodeButton.setEnabled(true);
            }
        }
    };

    private TextWatcher mCodeTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
            mCode = charSequence.toString();
            Log.d("Verification Code", mCode);
            checkRegisterEnabled();
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private TextWatcher mNicknameTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
            mNickname = charSequence.toString();
            Log.d("Nickname", mNickname);
            checkRegisterEnabled();
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private TextWatcher mRegisterPasswordTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            mRegisterPassword = charSequence.toString();
            Log.d("Register Password", mRegisterPassword);
            checkRegisterEnabled();
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    private TextWatcher mReInputPasswordTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            mReInputPassword = charSequence.toString();
            Log.d("Re-input Password", mReInputPassword);
            checkRegisterEnabled();
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    public void initData() {
        mNetworkHelper = new NetworkHelper(mContext);
        mErrorMessageFactory = new ErrorMessageFactory(mContext);
    }

    public void findView() {
        View registerView = LayoutInflater.from(mContext).inflate(R.layout.register, null);
        setContentView(registerView);

        mCloseButton = (ImageButton) registerView.findViewById(R.id.closeBtn);
        mCloseButton.setOnClickListener(mOnRegisterCloseClickListener);
        mPhoneEditText = (EditText) registerView.findViewById(R.id.phoneEditText);
        mCodeEditText = (EditText) registerView.findViewById(R.id.codeEditText);
        mNicknameEditText = (EditText) registerView.findViewById(R.id.nicknameEditText);
        mPasswordEditText = (EditText) registerView.findViewById(R.id.passwordEditText);
        mReInputPasswordEditText = (EditText) registerView.findViewById(R.id.reInputPasswordEditText);
        mCodeButton = (Button) registerView.findViewById(R.id.codeButton);
        mRegisterButton = (Button) registerView.findViewById(R.id.registerButton);
        mPhoneEditText.addTextChangedListener(mRegisterPhoneTextWatcher);
        mCodeEditText.addTextChangedListener(mCodeTextWatcher);
        mNicknameEditText.addTextChangedListener(mNicknameTextWatcher);
        mPasswordEditText.addTextChangedListener(mRegisterPasswordTextWatcher);
        mReInputPasswordEditText.addTextChangedListener(mReInputPasswordTextWatcher);
        mRegisterButton.setOnClickListener(mOnRegisterBtnClickListener);
        mCodeButton.setOnClickListener(mCodeBtnClickListener);
        mLoadingDialog = new LoadingDialog(mContext, getContext().getString(R.string.loading));
        mToast = new CustomSuperToast(mContext);
    }

    public void setOnUserRegisterListener(OnUserRegisterListener onUserRegisterListener) {
        this.mOnUserRegisterListener = onUserRegisterListener;
    }

    public void checkRegisterInfo() {
        if (!mRegisterPassword.equals(mReInputPassword)) {
            mToast.show(mContext.getString(R.string.error_password_not_same));
            return;
        }

        if (mRegisterPassword.length() < 6) {
            mToast.show(mContext.getString(R.string.error_password_short));
            return;
        }

        if (mRegisterPassword.length() > 45) {
            mToast.show(mContext.getString(R.string.error_password_long));
            return;
        }

        dismiss();
        mOnUserRegisterListener.goToUploadPhoto(mRegisterPhone, mRegisterPassword,
                mNickname);
    }

    public void resetData() {
        mPhoneEditText.setText("");
        mCodeEditText.setText("");
        mNicknameEditText.setText("");
        mPasswordEditText.setText("");
        mReInputPasswordEditText.setText("");
    }

    public void setListener() {
        mEventHandler = new EventHandler() {

            @Override
            public void afterEvent(int event, int result, Object data) {

                if (result == SMSSDK.RESULT_COMPLETE) {
                    if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
                        Message handlerMessage = new Message();
                        handlerMessage.what = VERIFICATION_SUCCESS;
                        mHandler.sendMessage(handlerMessage);
                    } else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {
                        //获取验证码成功
                    } else if (event == SMSSDK.EVENT_GET_SUPPORTED_COUNTRIES) {
                        //返回支持发送验证码的国家列表
                    }
                } else if(result == SMSSDK.RESULT_ERROR) {
                    if(event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
                        Message handlerMessage = new Message();
                        handlerMessage.what = VERIFICATION_FAILED;
                        mHandler.sendMessage(handlerMessage);
                    }
                } else {
                    ((Throwable) data).printStackTrace();
                }
            }
        };

        SMSSDK.registerEventHandler(mEventHandler);

        mNetworkHelper.setUiDataListener(new UIDataListener() {
            @Override
            public void onDataChanged(Object data) {
                SMSSDK.submitVerificationCode("86", mRegisterPhone, mCode);

                PushManager.getInstance(BaseInfoUtil.getContext()).register();
                Message handlerMessage = new Message();
                handlerMessage.what = LOADING_END;
                mHandler.sendMessage(handlerMessage);
            }

            @Override
            public void onErrorHappened(int errorCode, String errorMessage) {
                LogUtil.e(TAG, errorMessage);
                String message = mErrorMessageFactory.get(errorCode);
                mToast.show(message);

                Message handlerMessage = new Message();
                handlerMessage.what = LOADING_END;
                mHandler.sendMessage(handlerMessage);
            }
        });
    }

    private void checkRegisterEnabled() {
        if (mRegisterPhone == null || mRegisterPhone.isEmpty()
                || mCode == null || mCode.isEmpty()
                || mNickname == null || mNickname.isEmpty()
                || mRegisterPassword == null || mRegisterPassword.isEmpty()
                || mReInputPassword == null || mReInputPassword.isEmpty()) {
            mRegisterButton.setEnabled(false);
        } else {
            mRegisterButton.setEnabled(true);
        }
    }

    public void dismiss() {
        super.dismiss();
        SMSSDK.unregisterEventHandler(mEventHandler);
    }
}
