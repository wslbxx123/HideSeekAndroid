package dlmj.hideseek.UI.Activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import dlmj.hideseek.BusinessLogic.Cache.NewFriendCache;
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
import dlmj.hideseek.UI.Adapter.NewFriendAdapter;
import dlmj.hideseek.UI.View.LoadingDialog;

/**
 * Created by Two on 20/10/2016.
 */
public class NewFriendActivity extends BaseActivity implements UIDataListener<Bean> {
    private final static String TAG = "NewFriendActivity";
    private final static int MSG_REFRESH_LIST = 1;
    private final static int LOADING_END = 2;
    private String mLastTitle;
    private NewFriendAdapter mNewFriendAdapter;
    private List<User> mNewFriendList = new LinkedList<>();
    private PullToRefreshListView mNewFriendListView;
    private TextView mLastTitleTextView;
    private LinearLayout mBackLayout;
    private LoadingDialog mLoadingDialog;
    private NetworkHelper mNetworkHelper;
    private ErrorMessageFactory mErrorMessageFactory;
    private long mCurrentFriendId;

    private Handler mUiHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REFRESH_LIST:
                    mNewFriendAdapter.notifyDataSetChanged();
                case LOADING_END:
                    if (mLoadingDialog.isShowing()) {
                        mLoadingDialog.dismiss();
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_friends);
        initData();
        findView();
        setListener();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mNewFriendList.clear();
        mNewFriendList.addAll(NewFriendCache.getInstance(this).getFriendList());
        mNewFriendAdapter.notifyDataSetChanged();
    }

    private void initData() {
        mLastTitle = getIntent().getStringExtra(IntentExtraParam.LAST_TITLE);
        mNewFriendAdapter = new NewFriendAdapter(this, mNewFriendList);
        mNetworkHelper = new NetworkHelper(this);
        mErrorMessageFactory = new ErrorMessageFactory(this);
    }

    private void findView() {
        mLastTitleTextView = (TextView) findViewById(R.id.lastTitleTextView);
        mLastTitleTextView.setText(mLastTitle);
        mNewFriendListView = (PullToRefreshListView) findViewById(R.id.newFriendListView);
        mNewFriendListView.setAdapter(mNewFriendAdapter);
        mNewFriendListView.setMode(PullToRefreshBase.Mode.DISABLED);
        mLoadingDialog = new LoadingDialog(this, this.getString(R.string.loading));
        mBackLayout = (LinearLayout) findViewById(R.id.backLayout);
    }

    private void setListener() {
        mNetworkHelper.setUiDataListener(this);
        mNewFriendAdapter.setAcceptBtnOnClickedListener(new NewFriendAdapter.AcceptBtnOnClickedListener() {
            @Override
            public void acceptBtnOnClicked(long friendId) {
                mCurrentFriendId = friendId;

                Map<String, String> params = new HashMap<>();
                params.put("friend_id", friendId + "");

                if (!mLoadingDialog.isShowing()) {
                    mLoadingDialog.show();
                }

                mResponseCode = 0;
                mNetworkHelper.sendPostRequest(UrlParams.ACCEPT_FRIEND_URL, params);
            }
        });

        mBackLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    public void onDataChanged(Bean data) {
        mResponseCode = CodeParams.SUCCESS;
        int friendNum = Integer.parseInt(data.getResult());
        User user = UserCache.getInstance().getUser();
        user.setFriendNum(friendNum);
        UserCache.getInstance().update(user, "friend_num", friendNum);
        NewFriendCache.getInstance(this).updateFriendStatus(mCurrentFriendId);

        mNewFriendList.clear();
        mNewFriendList.addAll(NewFriendCache.getInstance(this).getFriendList());
        Message.obtain(mUiHandler, MSG_REFRESH_LIST).sendToTarget();
    }

    @Override
    public void onErrorHappened(int errorCode, String errorMessage) {
        LogUtil.d(TAG, errorMessage);
        mResponseCode = errorCode;

        String message = mErrorMessageFactory.get(errorCode);
        mToast.show(message);

        Message handlerMessage = new Message();
        handlerMessage.what = LOADING_END;
        mUiHandler.sendMessage(handlerMessage);
    }
}
