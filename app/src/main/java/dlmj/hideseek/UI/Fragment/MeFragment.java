package dlmj.hideseek.UI.Fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;

import java.text.SimpleDateFormat;

import dlmj.hideseek.BusinessLogic.Cache.ImageCacheManager;
import dlmj.hideseek.BusinessLogic.Cache.UserCache;
import dlmj.hideseek.Common.Model.User;
import dlmj.hideseek.Common.Params.IntentExtraParam;
import dlmj.hideseek.R;
import dlmj.hideseek.UI.Activity.BaseFragmentActivity;
import dlmj.hideseek.UI.Activity.FriendActivity;
import dlmj.hideseek.UI.Activity.IntroduceActivity;
import dlmj.hideseek.UI.Activity.MyOrderActivity;
import dlmj.hideseek.UI.Activity.MyProfileActivity;
import dlmj.hideseek.UI.Activity.SettingActivity;
import dlmj.hideseek.UI.View.CircleNetworkImageView;

/**
 * Created by Two on 4/30/16.
 * Me
 */
public class MeFragment extends BaseFragment {
    private static String TAG = "MeFragment";
    private String mTitle;
    private CircleNetworkImageView mPhotoCircleNetworkImageView;
    private LinearLayout mProfileLayout;

    private ImageLoader mImageLoader;
    private LinearLayout mUserInfoLayout;
    private TextView mNotLoginTextView;
    private TextView mNicknameTextView;
    private TextView mDateTextView;
    private LinearLayout mSettingsLayout;
    private View rootView;

    private TextView mFriendTextView;
    private TextView mScoreTextView;
    private LinearLayout mScoreLayout;
    private LinearLayout mFriendLayout;
    private ImageView mRoleImageView;
    private LinearLayout mMyOrder;
    private SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.me, null);

            initData();
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
        setUserInfo();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private void initData() {
        mTitle = getActivity().getString(R.string.me_title);
        mImageLoader = ImageCacheManager.getInstance(getActivity()).getImageLoader();
    }

    private void findView(View view) {
        mPhotoCircleNetworkImageView = (CircleNetworkImageView) view.findViewById(R.id.photoCircleImageView);
        mPhotoCircleNetworkImageView.setDefaultImageResId(R.drawable.default_photo);

        mProfileLayout = (LinearLayout) view.findViewById(R.id.profileLayout);
        mUserInfoLayout = (LinearLayout) view.findViewById(R.id.userInfoLayout);
        mNotLoginTextView = (TextView) view.findViewById(R.id.notLoginTextView);
        mNicknameTextView = (TextView) view.findViewById(R.id.nicknameTextView);
        mDateTextView = (TextView) view.findViewById(R.id.dateTextView);
        mSettingsLayout = (LinearLayout) view.findViewById(R.id.settingsLayout);
        mFriendTextView = (TextView) view.findViewById(R.id.friendTextView);
        mScoreTextView = (TextView) view.findViewById(R.id.scoreTextView);
        mScoreLayout = (LinearLayout) view.findViewById(R.id.scoreLayout);
        mRoleImageView = (ImageView) view.findViewById(R.id.roleImageView);
        mFriendLayout = (LinearLayout) view.findViewById(R.id.friendLayout);
        mMyOrder = (LinearLayout) view.findViewById(R.id.myOrder);

        setUserInfo();
    }

    private void setListener() {
        ((BaseFragmentActivity)getActivity()).getLoginDialog().setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                setUserInfo();
            }
        });

        mMyOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //订单详情页
                Intent intent = new Intent(getActivity(), MyOrderActivity.class);
                startActivity(intent);
            }
        });

        mProfileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (UserCache.getInstance().ifLogin()) {
                    //个人信息
                    Intent intent = new Intent();
                    intent.setClass(getActivity(), MyProfileActivity.class);
                    startActivity(intent);
                } else {
                    ((BaseFragmentActivity)getActivity()).getLoginDialog().resetData();
                    ((BaseFragmentActivity)getActivity()).getLoginDialog().show();
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
                IntroduceActivity activity = (IntroduceActivity) MeFragment.this.getActivity();
                FragmentTabHost fragmentTabHost = activity.getFragmentTabHost();
                fragmentTabHost.setCurrentTab(1);
            }
        });

        mFriendLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(getActivity(), FriendActivity.class);
                intent.putExtra(IntentExtraParam.LAST_TITLE, mTitle);
                startActivity(intent);
            }
        });
    }

    public void setUserInfo() {
        //判断是否登录
        if (UserCache.getInstance().ifLogin()) {
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
}
