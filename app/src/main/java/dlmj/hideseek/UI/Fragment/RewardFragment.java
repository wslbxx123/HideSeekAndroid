package dlmj.hideseek.UI.Fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.handmark.pulltorefresh.library.ILoadingLayout;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshGridView;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import dlmj.hideseek.BusinessLogic.Cache.RaceGroupCache;
import dlmj.hideseek.BusinessLogic.Cache.RewardCache;
import dlmj.hideseek.BusinessLogic.Network.NetworkHelper;
import dlmj.hideseek.Common.Factory.ErrorMessageFactory;
import dlmj.hideseek.Common.Interfaces.UIDataListener;
import dlmj.hideseek.Common.Model.Bean;
import dlmj.hideseek.Common.Model.Reward;
import dlmj.hideseek.Common.Params.UrlParams;
import dlmj.hideseek.Common.Util.LogUtil;
import dlmj.hideseek.DataAccess.ProductTableManager;
import dlmj.hideseek.DataAccess.RaceGroupTableManager;
import dlmj.hideseek.DataAccess.RewardTableManager;
import dlmj.hideseek.R;
import dlmj.hideseek.UI.Adapter.RewardAdapter;

/**
 * 创建者     ZPL
 * 创建时间   2016/7/16 20:22
 * 描述	      ${TODO}
 * <p/>
 * 更新者     $Author$
 * 更新时间   $Date$
 * 更新描述   ${TODO}
 */
public class RewardFragment extends BaseFragment implements UIDataListener<Bean> {
    private static final String TAG = "RewardFragment";
    private static final int MSG_REFRESH_LIST = 1;
    private PullToRefreshGridView mRewardGridView;
    private View mRootView;
    private NetworkHelper mNetworkHelper;
    private NetworkHelper mGetRewardNetworkHelper;
    private RewardTableManager mRewardTableManager;
    private List<Reward> mRewardList = new LinkedList<>();
    private RewardAdapter mRewardAdapter;
    private ErrorMessageFactory mErrorMessageFactory;
    private Handler mUiHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REFRESH_LIST:
                    mRewardAdapter.notifyDataSetChanged();
                    mRewardGridView.onRefreshComplete();
                    break;
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null) {
            mRootView = View.inflate(getContext(), R.layout.product, null);
            initData();
            findView();
            setListener();
        }

        ViewGroup parent = (ViewGroup) mRootView.getParent();
        if (parent != null) {
            parent.removeView(mRootView);
        }

        mRewardList.clear();
        if(RewardCache.getInstance(getActivity()).getList() != null) {
            mRewardList.addAll(RewardCache.getInstance(getActivity()).getList());
        }
        mRewardAdapter.notifyDataSetChanged();

        mRewardGridView.setRefreshing(true);

        return mRootView;
    }

    private void initData() {
        mRewardTableManager = RewardTableManager.getInstance(getActivity());
        mRewardAdapter = new RewardAdapter(getActivity(), mRewardList);
        mNetworkHelper = new NetworkHelper(getActivity());
        mGetRewardNetworkHelper = new NetworkHelper(getActivity());
        mErrorMessageFactory = new ErrorMessageFactory(getActivity());
    }

    private void findView() {
        mRewardGridView = (PullToRefreshGridView) mRootView.findViewById(R.id.pullToRefreshGridView);
        mRewardGridView.setAdapter(mRewardAdapter);
        mRewardGridView.setMode(PullToRefreshBase.Mode.BOTH);

        ILoadingLayout startLabels = mRewardGridView.getLoadingLayoutProxy(true, false);
        startLabels.setPullLabel("");
        startLabels.setRefreshingLabel("");
        startLabels.setReleaseLabel("");

        ILoadingLayout endLabels = mRewardGridView.getLoadingLayoutProxy(false, true);
        endLabels.setLoadingDrawable(null);
    }

    private void setListener() {
        mNetworkHelper.setUiDataListener(this);

        mGetRewardNetworkHelper.setUiDataListener(new UIDataListener<Bean>() {
            @Override
            public void onDataChanged(Bean data) {
                LogUtil.d(TAG, data.getResult());

                RewardCache.getInstance(getActivity()).addRewards(data.getResult());

                mRewardList.clear();
                mRewardList.addAll(RewardCache.getInstance(getActivity()).getList());
                mRewardAdapter.notifyDataSetChanged();

                Message.obtain(mUiHandler, MSG_REFRESH_LIST).sendToTarget();
            }

            @Override
            public void onErrorHappened(int errorCode, String errorMessage) {
                LogUtil.d(TAG, errorMessage);
                mResponseCode = errorCode;

                String message = mErrorMessageFactory.get(errorCode);
                mToast.show(message);
            }
        });

        mRewardGridView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<GridView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<GridView> refreshView) {
                //下拉刷新
                Map<String, String> params = new HashMap<>();
                params.put("version", mRewardTableManager.getVersion() + "");
                params.put("reward_min_id", mRewardTableManager.getRewardMinId() + "");
                mResponseCode = 0;
                mNetworkHelper.sendPostRequestWithoutSid(UrlParams.REFRESH_REWARD_URL, params);
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<GridView> refreshView) {
                if (mRewardList.size() > 10) {
                    //上拉加载
                    boolean hasData = RaceGroupCache.getInstance(getActivity()).
                            getMoreRaceGroup(10, false);

                    if(!hasData) {
                        Map<String, String> params = new HashMap<>();
                        params.put("version", mRewardTableManager.getVersion() + "");
                        params.put("reward_min_id", mRewardTableManager.getRewardMinId() + "");
                        mResponseCode = 0;
                        mGetRewardNetworkHelper.sendPostRequest(UrlParams.GET_REWARD_URL, params);
                    } else {
                        mRewardList.clear();
                        mRewardList.addAll(RewardCache.getInstance(getActivity()).getList());
                        mRewardAdapter.notifyDataSetChanged();

                        mRewardGridView.onRefreshComplete();
                    }
                }
            }
        });
    }

    @Override
    public void onDataChanged(Bean data) {
        LogUtil.d(TAG, data.getResult());
        RewardCache.getInstance(getActivity()).setRewards(data.getResult());
        mRewardList.clear();
        mRewardList.addAll(RewardCache.getInstance(getActivity()).getList());
        Message.obtain(mUiHandler, MSG_REFRESH_LIST).sendToTarget();
    }

    @Override
    public void onErrorHappened(int errorCode, String errorMessage) {
        LogUtil.d(TAG, errorMessage);

        String message = mErrorMessageFactory.get(errorCode);
        mToast.show(message);
    }
}
