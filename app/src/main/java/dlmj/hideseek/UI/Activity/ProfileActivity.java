package dlmj.hideseek.UI.Activity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;

import dlmj.hideseek.BusinessLogic.Cache.ImageCacheManager;
import dlmj.hideseek.Common.Model.User;
import dlmj.hideseek.Common.Params.IntentExtraParam;
import dlmj.hideseek.R;
import dlmj.hideseek.UI.View.CircleNetworkImageView;

/**
 * Created by Two on 15/10/2016.
 */
public class ProfileActivity extends BaseActivity{
    private CircleNetworkImageView mPhotoCircleImageView;
    private TextView mNicknameTextView;
    private ImageView mSexImageView;

    private ImageLoader mImageLoader;
    private User mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);

        initData();
        findView();
        setListener();
    }

    private void initData() {
        mUser = getIntent().getParcelableExtra(IntentExtraParam.PROFILE_INFO);
        mImageLoader = ImageCacheManager.getInstance(this).getImageLoader();
    }

    private void findView() {
        mPhotoCircleImageView = (CircleNetworkImageView) findViewById(R.id.photoCircleImageView);
        mPhotoCircleImageView.setImageUrl(mUser.getSmallPhotoUrl(), mImageLoader);
        mNicknameTextView = (TextView) findViewById(R.id.nicknameTextView);
        mNicknameTextView.setText(mUser.getNickname());
        mSexImageView = (ImageView) findViewById(R.id.sexImageView);
        mSexImageView.setImageResource(mUser.getSexImageDrawableId());
    }

    private void setListener() {

    }
}
