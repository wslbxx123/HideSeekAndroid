package dlmj.hideseek.UI.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;

import dlmj.hideseek.BusinessLogic.Cache.ImageCacheManager;
import dlmj.hideseek.BusinessLogic.Cache.UserCache;
import dlmj.hideseek.Common.Model.User;
import dlmj.hideseek.R;
import dlmj.hideseek.UI.View.CircleNetworkImageView;

/**
 * Created by Two on 5/4/16.
 */
public class MyProfileActivity extends Activity {
    private static String TAG="MyProfileActivity";
    public static final int MODIFY_CODE=1;
    public static final int RESULT_CODE_SEX_SUCCESS=2;//修改性别成功的Code
    public static final int RESULT_CODE_NICKNAME_SUCCESS=3;//修改昵称
    public static final int RESULT_CODE_REGION_SUCCESS=4;//修改地址
    public static final int RESULT_CODE_PROFILE_SUCCESS=5;//修改头像

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

    private User mUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_profile);
        initData();
        findView();
        setListener();
    }

    private void initData() {
        mUser = UserCache.getInstance().getUser();
        mImageLoader = ImageCacheManager.getInstance(getApplicationContext()).getImageLoader();
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
                startActivityForResult(new Intent(MyProfileActivity.this,ModifyNickNameActivity.class),MODIFY_CODE);
            }
        });
        mSexLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //性别
                startActivityForResult(new Intent(MyProfileActivity.this,ModifySexActivity.class),MODIFY_CODE);
            }
        });
        mRegionLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //地址
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==MODIFY_CODE)
        {
            switch (resultCode)
            {
                case RESULT_CODE_PROFILE_SUCCESS:
                    //头像
                    break;
                case RESULT_CODE_NICKNAME_SUCCESS:
                    //昵称
                    break;
                case RESULT_CODE_SEX_SUCCESS:
                    //性别
                    mSexTextView.setText(mUser.getSex().toString(this));
                    break;
                case RESULT_CODE_REGION_SUCCESS:
                    //地址
                    break;
            }
        }
    }
}
