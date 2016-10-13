package dlmj.hideseek.UI.Fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.handmark.pulltorefresh.library.ILoadingLayout;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshGridView;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import dlmj.hideseek.BusinessLogic.Cache.ShopRewardCache;
import dlmj.hideseek.BusinessLogic.Network.NetworkHelper;
import dlmj.hideseek.Common.Factory.ErrorMessageFactory;
import dlmj.hideseek.Common.Interfaces.UIDataListener;
import dlmj.hideseek.Common.Model.Bean;
import dlmj.hideseek.Common.Model.Reward;
import dlmj.hideseek.Common.Params.UrlParams;
import dlmj.hideseek.Common.Util.LogUtil;
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
    private PullToRefreshGridView mPTRGridView;
    private View mView;
    private NetworkHelper mNetworkHelper;
    private NetworkHelper mGetRewardNetworkHelper;
    private List<Reward.RewardEntity> mRewardEntity = new LinkedList<>();
    private Handler mUiHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REFRESH_LIST:
                    mRewardAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };
    private RewardAdapter mRewardAdapter;
    private ErrorMessageFactory mErrorMessageFactory;
    private GridView mGridView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mView == null) {
            mView = View.inflate(getContext(), R.layout.fragment_shop, null);
        }
        initView();
        initData();
        initListener();
        String data = ShopRewardCache.getDataFromLocal(UrlParams.GET_REWARD_URL);
        //使用本地缓存
        if (data!=null) {
            setData(data);
        } else {
            mPTRGridView.setRefreshing(true);
        }
        return mView;
    }

    private void initListener() {
        mNetworkHelper.setUiDataListener(this);
        mGetRewardNetworkHelper.setUiDataListener(new UIDataListener<Bean>() {
            @Override
            public void onDataChanged(Bean data) {
                //保存缓存
                String result = data.getResult();
                ShopRewardCache.saveCache(result,UrlParams.GET_REWARD_URL);
                setData(result);
            }

            @Override
            public void onErrorHappened(int errorCode, String errorMessage) {
                LogUtil.d(TAG, errorMessage);
                mResponseCode = errorCode;

                String message = mErrorMessageFactory.get(errorCode);
                mToast.show(message);
            }
        });

        mPTRGridView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<GridView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<GridView> refreshView) {
                //下拉刷新
                Map<String, String> params = new HashMap<>();
                params.put("version", 1 + "");
                params.put("reward_min_id", 10 + "");
                mResponseCode = 0;
                mGetRewardNetworkHelper.sendPostRequestWithoutSid(UrlParams.GET_REWARD_URL, params);
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<GridView> refreshView) {
                if (mRewardEntity.size() > 10) {
                    //上拉加载
                    Map<String, String> params = new HashMap<>();
                    params.put("version", 0 + "");
                    params.put("reward_min_id", 0 + "");
                    mResponseCode = 0;
                    mNetworkHelper.sendPostRequestWithoutSid(UrlParams.REFRESH_REWARD_URL, params);
                }
                mPTRGridView.onRefreshComplete();
            }
        });
    }

    private void setData(String result) {
        Gson gson = new Gson();
        Reward reward = gson.fromJson(result, Reward.class);
        mRewardEntity = reward.reward;
        mRewardAdapter = new RewardAdapter(getContext(), mRewardEntity);
        mGridView.setAdapter(mRewardAdapter);
        Message.obtain(mUiHandler, MSG_REFRESH_LIST).sendToTarget();
        mPTRGridView.onRefreshComplete();
    }

    private void initData() {
        //下拉刷新和上拉加载
        mPTRGridView.setMode(PullToRefreshBase.Mode.BOTH);
        ILoadingLayout startLabels = mPTRGridView.getLoadingLayoutProxy(true, false);
        startLabels.setPullLabel("");
        startLabels.setRefreshingLabel("");
        startLabels.setReleaseLabel("");
        ILoadingLayout endLabels = mPTRGridView.getLoadingLayoutProxy(false, true);
        endLabels.setLoadingDrawable(null);
        mGridView = mPTRGridView.getRefreshableView();

        TextView tv = new TextView(getActivity());
        tv.setGravity(Gravity.CENTER);
        tv.setText("下拉试试手气");
        //当界面为空的时候显示的视图
        mPTRGridView.setEmptyView(tv);

        mRewardAdapter = new RewardAdapter(getActivity(), mRewardEntity);
        mGridView.setAdapter(mRewardAdapter);
        mErrorMessageFactory = new ErrorMessageFactory(getActivity());
    }

    private void initView() {
        mPTRGridView = (PullToRefreshGridView) mView.findViewById(R.id.pullToRefreshGridView);
        mNetworkHelper = new NetworkHelper(getActivity());
        mGetRewardNetworkHelper = new NetworkHelper(getActivity());
    }

    @Override
    public void onDataChanged(Bean data) {
        String result = data.getResult();
        Gson gson = new Gson();
        Reward reward = gson.fromJson(result, Reward.class);
        List rewardEntity = reward.reward;
        mRewardEntity.addAll(rewardEntity);
        mRewardAdapter = new RewardAdapter(getContext(), mRewardEntity);
        mGridView.setAdapter(mRewardAdapter);
        Message.obtain(mUiHandler, MSG_REFRESH_LIST).sendToTarget();
    }

    @Override
    public void onErrorHappened(int errorCode, String errorMessage) {
        LogUtil.d(TAG, errorMessage);

        String message = mErrorMessageFactory.get(errorCode);
        mToast.show(message);
    }
}
