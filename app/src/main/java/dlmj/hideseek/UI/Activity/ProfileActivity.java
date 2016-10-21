package dlmj.hideseek.UI.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
    private LinearLayout mBackLayout;
    private TextView mLastTitleTextView;
    private TextView mFriendNameTextView;
    private TextView mNicknameTextView;
    private ImageView mSexImageView;
    private TextView mRegionTextView;
    private TextView mRoleTextView;
    private ImageView mRoleImageView;
    private TextView mSetRemarkTextView;
    private ImageView mRightArrowImageView;
    private Button mAddButton;

    private String mLastTitle;

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
        mLastTitle = getIntent().getStringExtra(IntentExtraParam.LAST_TITLE);
        mImageLoader = ImageCacheManager.getInstance(this).getImageLoader();
    }

    private void findView() {
        mBackLayout = (LinearLayout) findViewById(R.id.backLayout);
        mLastTitleTextView = (TextView) findViewById(R.id.lastTitleTextView);
        mLastTitleTextView.setText(mLastTitle);
        mPhotoCircleImageView = (CircleNetworkImageView) findViewById(R.id.photoCircleImageView);
        mPhotoCircleImageView.setDefaultImageResId(R.drawable.default_photo);
        mPhotoCircleImageView.setImageUrl(mUser.getSmallPhotoUrl(), mImageLoader);
        mFriendNameTextView = (TextView) findViewById(R.id.friendNameTextView);
        mNicknameTextView = (TextView) findViewById(R.id.nicknameTextView);
        mFriendNameTextView.setText(mUser.getNickname());
        mSexImageView = (ImageView) findViewById(R.id.sexImageView);
        mSexImageView.setImageResource(mUser.getSexImageDrawableId());
        mRegionTextView = (TextView) findViewById(R.id.regionTextView);
        mRoleTextView = (TextView) findViewById(R.id.roleTextView);
        mRoleTextView.setText(mUser.getRoleName(this));
        mRoleImageView = (ImageView) findViewById(R.id.roleImageView);
        mRoleImageView.setImageResource(mUser.getRoleImageDrawableId());
        mSetRemarkTextView = (TextView) findViewById(R.id.setRemarkTextView);
        mRightArrowImageView = (ImageView) findViewById(R.id.rightArrowImageView);

        if(mUser.getRegion() != null) {
            mRegionTextView.setText(mUser.getRegion());
        }

        if(mUser.getIsFriend()) {
            mSetRemarkTextView.setVisibility(View.VISIBLE);
            mRightArrowImageView.setVisibility(View.VISIBLE);
            if(mUser.getAlias() == null || mUser.getAlias().isEmpty()) {
                mFriendNameTextView.setText(mUser.getNickname());
                mNicknameTextView.setVisibility(View.INVISIBLE);
            } else {
                mFriendNameTextView.setText(mUser.getAlias());
                mNicknameTextView.setText(mUser.getNickname());
                mNicknameTextView.setVisibility(View.VISIBLE);
            }
        } else {
            mSetRemarkTextView.setVisibility(View.INVISIBLE);
            mRightArrowImageView.setVisibility(View.INVISIBLE);
            mNicknameTextView.setVisibility(View.INVISIBLE);
        }

        mAddButton = (Button) findViewById(R.id.addBtn);
        if(mUser.getIsFriend()) {
            mAddButton.setEnabled(false);
        } else {
            mAddButton.setEnabled(true);
        }
    }

    private void setListener() {
        mBackLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileActivity.this, FriendVerificationActivity.class);
                intent.putExtra(IntentExtraParam.PROFILE_INFO, mUser);
                startActivity(intent);
            }
        });
    }
}
