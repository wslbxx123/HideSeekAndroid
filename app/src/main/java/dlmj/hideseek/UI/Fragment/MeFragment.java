package dlmj.hideseek.UI.Fragment;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;
import dlmj.hideseek.BusinessLogic.Cache.GoalCache;
import dlmj.hideseek.BusinessLogic.Cache.ImageCacheManager;
import dlmj.hideseek.BusinessLogic.Cache.UserCache;
import dlmj.hideseek.BusinessLogic.Network.NetworkHelper;
import dlmj.hideseek.Common.Factory.ErrorMessageFactory;
import dlmj.hideseek.Common.Interfaces.UIDataListener;
import dlmj.hideseek.Common.Model.Bean;
import dlmj.hideseek.Common.Model.User;
import dlmj.hideseek.Common.Params.IntentExtraParam;
import dlmj.hideseek.Common.Params.UrlParams;
import dlmj.hideseek.Common.Util.LogUtil;
import dlmj.hideseek.R;
import dlmj.hideseek.UI.Activity.FriendActivity;
import dlmj.hideseek.UI.Activity.IntroduceActivity;
import dlmj.hideseek.UI.Activity.MyOrderActivity;
import dlmj.hideseek.UI.Activity.MyProfileActivity;
import dlmj.hideseek.UI.Activity.SettingActivity;
import dlmj.hideseek.UI.Activity.UploadPhotoActivity;
import dlmj.hideseek.UI.View.CircleNetworkImageView;
import dlmj.hideseek.UI.View.CustomSuperToast;
import dlmj.hideseek.UI.View.LoadingDialog;

/**
 * Created by Two on 4/30/16.
 * Me
 */
public class MeFragment extends Fragment implements UIDataListener<Bean> {
    private final static int COUNT_DOWN_REFRESH = 1;
    private final static int COUNT_DOWN_START = 2;
    private final static int LOADING_END = 3;
    private static String TAG = "ProfileFragment";
    private CircleNetworkImageView mPhotoCircleNetworkImageView;
    private LinearLayout mProfileLayout;
    private Dialog mLoginDialog;
    private Dialog mRegisterDialog;
    private Button mLoginButton;
    private String mPhone;
    private String mPassword;

    private Button mRegisterButton;
    private String mRegisterPhone;
    private String mCode;
    private String mNickname;
    private String mRegisterPassword;
    private String mReInputPassword;
    private NetworkHelper mNetworkHelper;
    private NetworkHelper mCheckUserHelper;
    private ErrorMessageFactory mErrorMessageFactory;
    private ImageLoader mImageLoader;
    private LinearLayout mUserInfoLayout;
    private TextView mNotLoginTextView;
    private TextView mNicknameTextView;
    private TextView mDateTextView;
    private LinearLayout mSettingsLayout;
    private View rootView;
    private CustomSuperToast mToast;
    private TextView mFriendTextView;
    private TextView mScoreTextView;
    private Button mCodeButton;
    private LinearLayout mScoreLayout;
    private LinearLayout mFriendLayout;
    private ImageView mRoleImageView;
    private LoadingDialog mLoadingDialog;
    private LinearLayout mMyOrder;
    private SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private Timer mTimer = new Timer();
    private TimerTask mTask;
    private int mCountDown = 60;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case COUNT_DOWN_REFRESH:
                    mCodeButton.setText(mCountDown + "s");
                    mCountDown--;

                    if(mCountDown == -1) {
                        mCodeButton.setText(getString(R.string.send_verification_code));
                        mTimer.cancel();
                    }
                    break;
                case COUNT_DOWN_START:
                    mCodeButton.setEnabled(false);
                    mTimer.schedule(mTask, 0, 1000);
                    break;
                case LOADING_END:
                    if(mLoadingDialog.isShowing()) {
                        mLoadingDialog.dismiss();
                    }
            }
            super.handleMessage(msg);
        };
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(rootView == null) {
            rootView = inflater.inflate(R.layout.me, null);
            initializeData();
            findView(rootView);
            setListener();
        }

        ViewGroup parent = (ViewGroup) rootView.getParent();
        if (parent != null) {
            parent.removeView(rootView);
        }
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStart() {
        setUserInfo();
        super.onStart();
    }

    private void initializeData() {
        mNetworkHelper = new NetworkHelper(getActivity());
        mCheckUserHelper = new NetworkHelper(getActivity());
        mImageLoader = ImageCacheManager.getInstance(getActivity()).getImageLoader();
        mErrorMessageFactory = new ErrorMessageFactory(getActivity());
        mTask = new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = COUNT_DOWN_REFRESH;
                mHandler.sendMessage(message);
            }
        };
    }

    private void findView(View view) {
        mPhotoCircleNetworkImageView = (CircleNetworkImageView) view.findViewById(R.id.photoCircleImageView);
        mPhotoCircleNetworkImageView.setDefaultImageResId(R.drawable.default_photo);

        mProfileLayout = (LinearLayout) view.findViewById(R.id.profileLayout);
        mLoginDialog = new Dialog(getActivity(), R.style.AlertDialog);
        mRegisterDialog = new Dialog(getActivity(), R.style.AlertDialog);
        mUserInfoLayout = (LinearLayout) view.findViewById(R.id.userInfoLayout);
        mNotLoginTextView = (TextView) view.findViewById(R.id.notLoginTextView);
        mNicknameTextView = (TextView) view.findViewById(R.id.nicknameTextView);
        mDateTextView = (TextView) view.findViewById(R.id.dateTextView);
        mSettingsLayout = (LinearLayout) view.findViewById(R.id.settingsLayout);
        mFriendTextView = (TextView) view.findViewById(R.id.friendTextView);
        mScoreTextView = (TextView) view.findViewById(R.id.scoreTextView);
        mToast = new CustomSuperToast(getActivity());
        mScoreLayout = (LinearLayout) view.findViewById(R.id.scoreLayout);
        mRoleImageView = (ImageView) view.findViewById(R.id.roleImageView);
        mFriendLayout = (LinearLayout) view.findViewById(R.id.friendLayout);
        mLoadingDialog = new LoadingDialog(getActivity(), getContext().getString(R.string.loading));
        mMyOrder = (LinearLayout) view.findViewById(R.id.myOrder);
        setUserInfo();

        Window window = mLoginDialog.getWindow();
        window.setWindowAnimations(R.style.AnimationStyle);
    }

    private void setListener() {
        EventHandler eventHandler = new EventHandler(){

            @Override
            public void afterEvent(int event, int result, Object data) {

                if (result == SMSSDK.RESULT_COMPLETE) {
                    if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
                        if(!mRegisterPassword.equals(mReInputPassword)) {
                            mToast.show(getString(R.string.error_password_not_same));
                            return;
                        }

                        Intent intent = new Intent();
                        intent.setClass(getActivity(), UploadPhotoActivity.class);
                        intent.putExtra(IntentExtraParam.PHONE, mRegisterPhone);
                        intent.putExtra(IntentExtraParam.PASSWORD, mRegisterPassword);
                        intent.putExtra(IntentExtraParam.NICKNAME, mNickname);
                        startActivityForResult(intent, IntroduceActivity.REGISTER_CODE);
                        mRegisterDialog.dismiss();
                        mLoginDialog.dismiss();
                    }else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE){
                        //获取验证码成功
                    }else if (event ==SMSSDK.EVENT_GET_SUPPORTED_COUNTRIES){
                        //返回支持发送验证码的国家列表
                    }
                }else{
                    ((Throwable)data).printStackTrace();
                }
            }
        };
        SMSSDK.registerEventHandler(eventHandler);

        mNetworkHelper.setUiDataListener(this);
        mMyOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //订单详情页
                Intent intent = new Intent(getActivity(),MyOrderActivity.class);
                startActivity(intent);
            }
        });

        mCheckUserHelper.setUiDataListener(new UIDataListener() {
            @Override
            public void onDataChanged(Object data) {
                SMSSDK.submitVerificationCode("86", mRegisterPhone, mCode);

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

        mProfileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(UserCache.getInstance().ifLogin()) {
                    //个人信息
                    Intent intent = new Intent();
                    intent.setClass(getActivity(), MyProfileActivity.class);
                    startActivity(intent);
                } else {
                    //登录注册
                    View loginView = LayoutInflater.from(getActivity()).inflate(R.layout.login, null);
                    mLoginDialog.setContentView(loginView);
                    mLoginDialog.show();

                    mLoginButton = (Button) loginView.findViewById(R.id.loginButton);
                    TextView registerTextView = (TextView) loginView.findViewById(R.id.registerTextView);
                    EditText accountEditText = (EditText) loginView.findViewById(R.id.accountEditText);
                    EditText passwordEditText = (EditText) loginView.findViewById(R.id.passwordEditText);
                    TextView closeTextView = (TextView) loginView.findViewById(R.id.closeTextView);
                    mLoginButton.setOnClickListener(mOnLoginClickListener);
                    registerTextView.setOnClickListener(mOnRegisterClickListener);
                    closeTextView.setOnClickListener(mOnLoginCloseClickListener);
                    accountEditText.addTextChangedListener(phoneTextWatcher);
                    passwordEditText.addTextChangedListener(passwordTextWatcher);
                }
            }
        });

        mSettingsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(getActivity(), SettingActivity.class);
                startActivity(intent);
            }
        });

        mScoreLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IntroduceActivity activity = (IntroduceActivity)MeFragment.this.getActivity();
                FragmentTabHost fragmentTabHost = activity.getFragmentTabHost();
                fragmentTabHost.setCurrentTab(1);
            }
        });

        mFriendLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(getActivity(), FriendActivity.class);
                startActivity(intent);
            }
        });
    }

    private View.OnClickListener mOnLoginClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Map<String, String> params = new HashMap<>();
            params.put("phone", mPhone);
            params.put("password", mPassword);

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
            View registerView = LayoutInflater.from(getActivity()).inflate(R.layout.register, null);
            TextView closeTextView = (TextView) registerView.findViewById(R.id.closeTextView);
            closeTextView.setOnClickListener(mOnRegisterCloseClickListener);
            mRegisterDialog.setContentView(registerView);
            mRegisterDialog.show();

            EditText phoneEditText = (EditText) registerView.findViewById(R.id.phoneEditText);
            EditText codeEditText = (EditText) registerView.findViewById(R.id.codeEditText);
            EditText nicknameEditText = (EditText) registerView.findViewById(R.id.nicknameEditText);
            EditText passwordEditText = (EditText) registerView.findViewById(R.id.passwordEditText);
            EditText reInputPasswordEditText = (EditText)
                    registerView.findViewById(R.id.reInputPasswordEditText);
            mCodeButton = (Button) registerView.findViewById(R.id.codeButton);
            mRegisterButton = (Button) registerView.findViewById(R.id.registerButton);
            phoneEditText.addTextChangedListener(registerPhoneTextWatcher);
            codeEditText.addTextChangedListener(codeTextWatcher);
            nicknameEditText.addTextChangedListener(nicknameTextWatcher);
            passwordEditText.addTextChangedListener(registerPasswordTextWatcher);
            reInputPasswordEditText.addTextChangedListener(reInputPasswordTextWatcher);
            mRegisterButton.setOnClickListener(mOnRegisterBtnClickListener);
            mCodeButton.setOnClickListener(mCodeBtnClickListener);
        }
    };

    private View.OnClickListener mOnRegisterBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!mLoadingDialog.isShowing()) {
                mLoadingDialog.show();
            }

            Map<String, String> params = new HashMap<>();
            params.put("phone", mRegisterPhone);

            mCheckUserHelper.sendPostRequest(UrlParams.CHECK_IF_USER_EXIST_URL, params);
        }
    };

    private View.OnClickListener mOnLoginCloseClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mLoginDialog.dismiss();
        }
    };

    private View.OnClickListener mOnRegisterCloseClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mRegisterDialog.dismiss();
        }
    };

    private View.OnClickListener mCodeBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Message message = new Message();
            message.what = COUNT_DOWN_START;
            mHandler.sendMessage(message);

            SMSSDK.getSupportedCountries();
            SMSSDK.getVerificationCode("86", mRegisterPhone);
        }
    };

    TextWatcher phoneTextWatcher = new TextWatcher() {
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

    TextWatcher passwordTextWatcher = new TextWatcher() {
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

    TextWatcher registerPhoneTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
            mRegisterPhone = charSequence.toString();
            Log.d("Register Phone", mRegisterPhone);
            checkRegisterEnabled();

            if(mRegisterPhone == null || mRegisterPhone.length() < 6) {
                mCodeButton.setEnabled(false);
            } else {
                mCodeButton.setEnabled(true);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    TextWatcher codeTextWatcher = new TextWatcher() {
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

    TextWatcher nicknameTextWatcher = new TextWatcher() {
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

    TextWatcher registerPasswordTextWatcher = new TextWatcher() {
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

    TextWatcher reInputPasswordTextWatcher = new TextWatcher() {
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

    private void checkLoginEnabled() {
        if (mPhone == null || mPhone.isEmpty() || mPassword == null || mPassword.isEmpty()) {
            mLoginButton.setEnabled(false);
        } else {
            mLoginButton.setEnabled(true);
        }
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

    private void setUserInfo() {
        //判断是否登录
        if(UserCache.getInstance().ifLogin()) {
            mNotLoginTextView.setVisibility(View.GONE);
            mUserInfoLayout.setVisibility(View.VISIBLE);
            User user = UserCache.getInstance().getUser();
            mPhotoCircleNetworkImageView.setImageUrl(user.getSmallPhotoUrl(), mImageLoader);
            mNicknameTextView.setText(user.getNickname());
            String date = mDateFormat.format(user.getRegisterDate());
            mDateTextView.setText(date + " " + getString(R.string.join));
            mRoleImageView.setImageResource(user.getRoleImageDrawableId());
            mScoreTextView.setText(user.getRecord() + "");
            mFriendTextView.setText(user.getFriendNum() + "");
        } else{
            mNotLoginTextView.setVisibility(View.VISIBLE);
            mUserInfoLayout.setVisibility(View.GONE);
            mPhotoCircleNetworkImageView.setImageUrl(null, mImageLoader);
            mScoreTextView.setText(0 + "");
            mFriendTextView.setText(0 + "");
        }
    }

    @Override
    public void onDataChanged(Bean data) {
        LogUtil.d(TAG, data.getResult());
        UserCache.getInstance().setUser(data.getResult());
        GoalCache.getInstance().setIfNeedClearMap(true);
        mLoginDialog.dismiss();

        setUserInfo();

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
