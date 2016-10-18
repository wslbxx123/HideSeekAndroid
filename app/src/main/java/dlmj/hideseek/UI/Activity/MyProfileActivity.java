package dlmj.hideseek.UI.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
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
import dlmj.hideseek.Common.Params.IntentExtraParam;
import dlmj.hideseek.Common.Params.UrlParams;
import dlmj.hideseek.Common.Util.LogUtil;
import dlmj.hideseek.R;
import dlmj.hideseek.UI.View.CircleNetworkImageView;
import dlmj.hideseek.UI.View.CustomSuperToast;
import dlmj.hideseek.UI.View.LoadingDialog;

/**
 * Created by Two on 5/4/16.
 */
public class MyProfileActivity extends BaseActivity {
    private static String TAG="MyProfileActivity";
    private static final int LOADING_END = 1;
    private static final int REQUEST_CODE_SEX=1;//修改性别
    private static final int REQUEST_CODE_NICKNAME=2;//修改昵称
    private static final int REQUEST_CODE_PROFILE=3;//修改头像
    private static final int REQUEST_CODE_CHOOSE_REGION = 4;//修改地址

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

    private NetworkHelper mRegionNetworkHelper;
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
        mRegionNetworkHelper = new NetworkHelper(this);
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
        mRegionTextView.setText(null==mUser.getRegion()?"":mUser.getRegion());

        mLoadingDialog = new LoadingDialog(this, getString(R.string.loading));
        mToast = new CustomSuperToast(this);
    }

    private void setListener() {
        mProfileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //头像
                startActivityForResult(new Intent(MyProfileActivity.this,ModifyHeadPhotoActivity.class),REQUEST_CODE_PROFILE);
            }
        });
        mNicknameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //昵称
                startActivityForResult(new Intent(MyProfileActivity.this,ModifyNickNameActivity.class),REQUEST_CODE_NICKNAME);
            }
        });
        mSexLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //性别
                startActivityForResult(new Intent(MyProfileActivity.this,ModifySexActivity.class),REQUEST_CODE_SEX);
            }
        });
        mRegionLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //地址
                startActivityForResult(new Intent(MyProfileActivity.this, RegionActivity.class), REQUEST_CODE_CHOOSE_REGION);
            }
        });
        //地区监听
        mRegionNetworkHelper.setUiDataListener(new UIDataListener<Bean>() {
            @Override
            public void onDataChanged(Bean data) {
                mResponseCode = CodeParams.SUCCESS;
                mHandler.sendEmptyMessage(LOADING_END);
                try {
                    JSONObject result=new JSONObject(data.getResult());
                    String region =result.optString("region");
                    mUser.setRegion(region);
                    UserCache.getInstance().update(mUser,"region",region);
                    mRegionTextView.setText(region);
                } catch (Exception e) {
                    LogUtil.e(TAG, e.getMessage());
                }
            }

            @Override
            public void onErrorHappened(int errorCode, String errorMessage) {
                if(mLoadingDialog.isShowing()) {
                    mLoadingDialog.dismiss();
                }
                mResponseCode = errorCode;
                String message = mErrorMessageFactory.get(errorCode);
                mToast.show(message);
                mToast.show(getString(R.string.error_connect_network_failed));
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode)
        {
            case REQUEST_CODE_PROFILE:
                //头像
                if (resultCode==RESULT_OK)
                {
                    mUser=UserCache.getInstance().getUser();
                    mPhotoCircleNetworkImageView.setImageUrl(mUser.getSmallPhotoUrl(), mImageLoader);
                }
                break;
            case REQUEST_CODE_NICKNAME:
                //昵称
                mUser=UserCache.getInstance().getUser();
                mNicknameTextView.setText(mUser.getNickname());
                break;
            case REQUEST_CODE_SEX:
                //性别
                mUser=UserCache.getInstance().getUser();
                mSexTextView.setText(mUser.getSex().toString(this));
                break;
            case REQUEST_CODE_CHOOSE_REGION:
                //地址
                String region = null==data?null:data.getStringExtra(IntentExtraParam.REGION_NAME);
                if (!TextUtils.isEmpty(region))
                {
                    Map<String, String> params = new HashMap<>();
                    params.put("region",region);
                    mRegionNetworkHelper.sendPostRequest(UrlParams.UPDATEREGION_URL,params);
                }
                break;
        }
    }
}
