package dlmj.hideseek.UI.Activity;

import android.app.Activity;
import android.os.Bundle;
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
public class MyProfileActivity extends Activity{
    private CircleNetworkImageView mPhotoCircleNetworkImageView;
    private TextView mNicknameTextView;
    private TextView mPhoneTextView;
    private TextView mSexTextView;
    private TextView mRoleTextView;
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
        mPhotoCircleNetworkImageView = (CircleNetworkImageView) findViewById(R.id.photoCircleImageView);
        mPhotoCircleNetworkImageView.setDefaultImageResId(R.drawable.default_photo);
        mPhotoCircleNetworkImageView.setImageUrl(mUser.getPhotoUrl(), mImageLoader);
        mNicknameTextView = (TextView) findViewById(R.id.nicknameTextView);
        mNicknameTextView.setText(mUser.getNickname());
        mPhoneTextView = (TextView) findViewById(R.id.phoneTextView);
        mPhoneTextView.setText(mUser.getPhone());
        mSexTextView = (TextView) findViewById(R.id.sexTextView);
        mSexTextView.setText(mUser.getSex().toString(this));
        mRoleTextView = (TextView) findViewById(R.id.roleTextView);
        mRoleTextView.setText(mUser.getRole().toString(this));
        mRegionTextView = (TextView) findViewById(R.id.regionTextView);
        mRegionTextView.setText(mUser.getRegion());
    }

    private void setListener() {

    }


}
