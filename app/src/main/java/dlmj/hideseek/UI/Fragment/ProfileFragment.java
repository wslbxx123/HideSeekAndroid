package dlmj.hideseek.UI.Fragment;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
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

import java.text.DateFormat;
import java.util.HashMap;
import java.util.Map;

import dlmj.hideseek.BusinessLogic.Cache.FriendCache;
import dlmj.hideseek.BusinessLogic.Cache.GoalCache;
import dlmj.hideseek.BusinessLogic.Cache.ImageCacheManager;
import dlmj.hideseek.BusinessLogic.Cache.RecordCache;
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
public class ProfileFragment extends Fragment implements UIDataListener<Bean> {
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
    private NetworkHelper mRecordNetworkHelper;
    private NetworkHelper mFriendNetworkHelper;
    private UIDataListener<Bean> mRecordUIDataListener;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(rootView == null) {
            rootView = inflater.inflate(R.layout.profile, null);
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
        mRecordNetworkHelper = new NetworkHelper(getActivity());
        mFriendNetworkHelper = new NetworkHelper(getActivity());
        mImageLoader = ImageCacheManager.getInstance(getActivity()).getImageLoader();
        mErrorMessageFactory = new ErrorMessageFactory(getActivity());
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
        mLoadingDialog = new LoadingDialog(getActivity());

        setUserInfo();

        Window window = mLoginDialog.getWindow();
        window.setWindowAnimations(R.style.AnimationStyle);
    }

    private void setListener() {
        mNetworkHelper.setUiDataListener(this);

        mRecordNetworkHelper.setUiDataListener(new UIDataListener<Bean>() {
            @Override
            public void onDataChanged(Bean data) {
                LogUtil.d(TAG, data.getResult());
                RecordCache.getInstance(getActivity()).setRecords(data.getResult());

                mScoreTextView.setText(RecordCache.getInstance(getActivity()).getScoreSum() + "");
            }

            @Override
            public void onErrorHappened(int errorCode, String errorMessage) {
                LogUtil.e(TAG, errorMessage);

                mScoreTextView.setText(0 + "");
            }
        });

        mFriendNetworkHelper.setUiDataListener(new UIDataListener<Bean>() {
            @Override
            public void onDataChanged(Bean data) {
                LogUtil.d(TAG, data.getResult());
                FriendCache.getInstance(getActivity()).setFriends(data.getResult());

                mFriendTextView.setText(RecordCache.getInstance(getActivity()).getList().size() + "");
            }

            @Override
            public void onErrorHappened(int errorCode, String errorMessage) {
                mFriendTextView.setText(0 + "");
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
                IntroduceActivity activity = (IntroduceActivity)ProfileFragment.this.getActivity();
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
        }
    };

    private View.OnClickListener mOnRegisterBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
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

            if(mRegisterPhone == null || mRegisterPhone.isEmpty()) {
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
            mPhotoCircleNetworkImageView.setImageUrl(user.getPhotoUrl(), mImageLoader);
            mNicknameTextView.setText(user.getNickname());
            String date = DateFormat.getDateInstance(DateFormat.DEFAULT).format(user.getRegisterDate());
            mDateTextView.setText(date + " " + getString(R.string.join));
            mRoleImageView.setImageResource(user.getRoleImageDrawableId());

            int scoreSum = RecordCache.getInstance(getActivity()).getScoreSum();
            if(scoreSum == 0) {
                mRecordNetworkHelper.sendPostRequest(UrlParams.REFRESH_RECORD_URL, new HashMap<String, String>());
            } else {
                mScoreTextView.setText(scoreSum + "");
            }

            int friendCount = FriendCache.getInstance(getActivity().getApplicationContext()).getList().size();
            if(friendCount == 0) {
                mFriendNetworkHelper.sendPostRequest(UrlParams.GET_FRIEND_URL, new HashMap<String, String>());
            } else {
                mFriendTextView.setText(friendCount + "");
            }
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
        GoalCache.getInstance().clearData();
        mLoginDialog.dismiss();

        setUserInfo();
        if(mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }
    }

    @Override
    public void onErrorHappened(int errorCode, String errorMessage) {
        LogUtil.d(TAG, "Error message when login: " + errorMessage);
        String errorCodeMessage = mErrorMessageFactory.get(errorCode);

        if(mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }
        mToast.show(errorCodeMessage);
    }
}
