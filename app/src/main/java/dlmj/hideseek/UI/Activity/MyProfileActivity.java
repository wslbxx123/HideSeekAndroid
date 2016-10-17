package dlmj.hideseek.UI.Activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import dlmj.hideseek.BusinessLogic.Cache.ImageCacheManager;
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
import dlmj.hideseek.UI.View.CircleNetworkImageView;
import dlmj.hideseek.UI.View.CustomSuperToast;
import dlmj.hideseek.UI.View.LoadingDialog;

/**
 * Created by Two on 5/4/16.
 */
public class MyProfileActivity extends BaseActivity{
    private static String TAG="MyProfileActivity";
    private static final int LOADING_END = 1;
    private static final int CHANGE_SEX_SUCCESS = 2;
    private View mProfileLayout;
    private CircleNetworkImageView mPhotoCircleNetworkImageView;
    private View mNicknameLayout;
    private TextView mNicknameTextView;
    private TextView mPhoneTextView;
    private View mSexLayout;
    private TextView mSexTextView;
    private TextView mRoleTextView;
    private View mRegionLayout;
    private TextView mRegionTextView;
    private ImageLoader mImageLoader;
    private NetworkHelper mNetworkHelper;
    private LoadingDialog mLoadingDialog;
    private CustomSuperToast mToast;
    private ErrorMessageFactory mErrorMessageFactory;

    private User mUser;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LOADING_END:
                    if (mLoadingDialog.isShowing()) {
                        mLoadingDialog.dismiss();
                    }
                    break;
                case CHANGE_SEX_SUCCESS:
                    if (mLoadingDialog.isShowing()) {
                        mLoadingDialog.dismiss();
                    }
                    mSexTextView.setText(msg.obj.toString());
                    break;
            }
            super.handleMessage(msg);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_profile);
        initData();
        findView();
        setListener();
    }

    private void initData() {
        mNetworkHelper = new NetworkHelper(this);
        mUser = UserCache.getInstance().getUser();
        mImageLoader = ImageCacheManager.getInstance(getApplicationContext()).getImageLoader();
        mErrorMessageFactory = new ErrorMessageFactory(this);
    }

    private void findView() {
        mProfileLayout=findViewById(R.id.profileLayout);
        mPhotoCircleNetworkImageView = (CircleNetworkImageView) findViewById(R.id.photoCircleImageView);
        mPhotoCircleNetworkImageView.setDefaultImageResId(R.drawable.default_photo);
        mPhotoCircleNetworkImageView.setImageUrl(mUser.getSmallPhotoUrl(), mImageLoader);
        mNicknameLayout = findViewById(R.id.nicknameLayout);
        mNicknameTextView = (TextView) findViewById(R.id.nicknameTextView);
        mNicknameTextView.setText(mUser.getNickname());
        mPhoneTextView = (TextView) findViewById(R.id.phoneTextView);
        mPhoneTextView.setText(mUser.getPhone());
        mSexLayout = findViewById(R.id.sexLayout);
        mSexTextView = (TextView) findViewById(R.id.sexTextView);
        mSexTextView.setText(mUser.getSex().toString(this));
        mRoleTextView = (TextView) findViewById(R.id.roleTextView);
        mRoleTextView.setText(mUser.getRole().toString(this));
        mRegionLayout = findViewById(R.id.regionLayout);
        mRegionTextView = (TextView) findViewById(R.id.regionTextView);
        mRegionTextView.setText(mUser.getRegion());
        mLoadingDialog = new LoadingDialog(this, getString(R.string.loading));
        mToast = new CustomSuperToast(this);
    }

    private void setListener() {
        mProfileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //头像
            }
        });
        mNicknameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //昵称
            }
        });
        mSexLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //性别
                AlertDialog.Builder builder = new AlertDialog.Builder(MyProfileActivity.this);
                String[] sexes = {getString(R.string.male), getString(R.string.female),
                        getString(R.string.secret)};
                builder.setItems(sexes, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        if (!mLoadingDialog.isShowing()) {
                            mLoadingDialog.show();
                        }
                        //更新后的性别 0：未设置，1：女，2：男，3：秘密
                        String selectSex="";
                        switch (which)
                        {
                            case 0://弹窗里面第一个是男
                                selectSex="2";
                                break;
                            case 1:
                                selectSex="1";
                                break;
                            case 2:
                                selectSex="3";
                                break;
                        }
                        Map<String, String> params = new HashMap<>();
                        params.put("sex",selectSex);
                        mNetworkHelper.sendPostRequest(UrlParams.UPDATE_SEX_URL,params);
                    }
                });
                builder.show();
            }
        });
        mRegionLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //地址
            }
        });

        //修改性别监听
        mNetworkHelper.setUiDataListener(new UIDataListener<Bean>() {
            @Override
            public void onDataChanged(Bean data) {
                mResponseCode = CodeParams.SUCCESS;
                try {
                    //改变UI
                    JSONObject result=new JSONObject(data.getResult());
                    Message m=new Message();
                    int updateSexInt=result.optInt("sex");
                    User.SexEnum disPlaySex=User.SexEnum.valueOf(updateSexInt);
                    m.obj= disPlaySex.toString(MyProfileActivity.this);
                    m.what=CHANGE_SEX_SUCCESS;
                    mHandler.sendMessage(m);
                    //更新缓存
                    mUser.setSex(disPlaySex);
                    UserCache.getInstance().update(mUser,"sex",updateSexInt);
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
        });
    }


}
