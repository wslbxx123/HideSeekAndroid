package dlmj.hideseek.UI.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;

import org.w3c.dom.Text;

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
    private TextView mFriendNameTextView;
    private TextView mNicknameTextView;
    private ImageView mSexImageView;
    private TextView mRegionTextView;
    private TextView mRoleTextView;
    private ImageView mRoleImageView;
    private Button mAddButton;

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
        mBackLayout = (LinearLayout) findViewById(R.id.backLayout);
        mPhotoCircleImageView = (CircleNetworkImageView) findViewById(R.id.photoCircleImageView);
        mPhotoCircleImageView.setImageUrl(mUser.getSmallPhotoUrl(), mImageLoader);
        mFriendNameTextView = (TextView) findViewById(R.id.friendNameTextView);
        mNicknameTextView = (TextView) findViewById(R.id.nicknameTextView);
        mSexImageView = (ImageView) findViewById(R.id.sexImageView);
        mSexImageView.setImageResource(mUser.getSexImageDrawableId());
        mRegionTextView = (TextView) findViewById(R.id.regionTextView);
        mRoleTextView = (TextView) findViewById(R.id.roleTextView);
        mRoleTextView.setText(mUser.getRoleName(this));
        mRoleImageView = (ImageView) findViewById(R.id.roleImageView);
        mRoleImageView.setImageResource(mUser.getRoleDrawableId());
        if(mUser.getRegion() != null) {
            mRegionTextView.setText(mUser.getRegion());
        }

        if(mUser.getAlias() == null || mUser.getAlias().isEmpty()) {
            mFriendNameTextView.setText(mUser.getNickname());
            mNicknameTextView.setVisibility(View.INVISIBLE);
        } else {
            mFriendNameTextView.setText(mUser.getAlias());
            mNicknameTextView.setText(mUser.getNickname());
            mNicknameTextView.setVisibility(View.VISIBLE);
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
    }
}
