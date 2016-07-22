package dlmj.hideseek.UI.Fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.ILoadingLayout;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import dlmj.hideseek.BusinessLogic.Cache.RaceGroupCache;
import dlmj.hideseek.BusinessLogic.Cache.UserCache;
import dlmj.hideseek.BusinessLogic.Network.NetworkHelper;
import dlmj.hideseek.Common.Interfaces.UIDataListener;
import dlmj.hideseek.Common.Model.Bean;
import dlmj.hideseek.Common.Model.RaceGroup;
import dlmj.hideseek.Common.Params.UrlParams;
import dlmj.hideseek.Common.Util.LogUtil;
import dlmj.hideseek.DataAccess.RaceGroupTableManager;
import dlmj.hideseek.R;
import dlmj.hideseek.UI.Adapter.RaceGroupAdapter;

/**
 * Created by Two on 5/14/16.
 */
public class RaceGroupFragment extends Fragment implements UIDataListener<Bean>, ListView.OnScrollListener {
    private final static String TAG = "RaceGroupFragment";
    private final static int MSG_REFRESH_LIST = 1;
    private static final int VISIBLE_REFRESH_COUNT = 3;
    private View rootView;
    private PullToRefreshListView mRaceGroupListView;
    private RaceGroupAdapter mRaceGroupAdapter;
    private List<RaceGroup> mRaceGroupList = new LinkedList<>();
    private NetworkHelper mNetworkHelper;
    private NetworkHelper mGetRaceGroupNetworkHelper;
    private boolean mIsLoading = false;
    private View mLoadMoreView;
    private Handler mUiHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REFRESH_LIST:
                    mRaceGroupAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(rootView == null) {
            rootView = inflater.inflate(R.layout.race_group, null);
            initData();
            findView(rootView);
            setListener();
        }

        ViewGroup parent = (ViewGroup) rootView.getParent();
        if (parent != null) {
            parent.removeView(rootView);
        }

        mRaceGroupList.clear();
        if(RaceGroupCache.getInstance(getActivity()).getList() != null) {
            mRaceGroupList.addAll(RaceGroupCache.getInstance(getActivity()).getList());
        }
        mRaceGroupAdapter.notifyDataSetChanged();

        if(UserCache.getInstance().ifLogin()) {
            mRaceGroupListView.setRefreshing(true);
        }

        return rootView;
    }

    private void initData() {
        mRaceGroupAdapter = new RaceGroupAdapter(getActivity(), mRaceGroupList);
        mNetworkHelper = new NetworkHelper(getActivity());
        mGetRaceGroupNetworkHelper = new NetworkHelper(getActivity());
    }

    private void findView(View view) {
        mRaceGroupListView = (PullToRefreshListView) view.findViewById(R.id.raceGroupListView);
        mRaceGroupListView.setAdapter(mRaceGroupAdapter);
        mRaceGroupListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);

        ILoadingLayout startLabels = mRaceGroupListView.getLoadingLayoutProxy(true, false);
        startLabels.setPullLabel("");
        startLabels.setRefreshingLabel("");
        startLabels.setReleaseLabel("");

        mLoadMoreView = LayoutInflater.from(getActivity()).inflate(R.layout.load_more_footer, null);
    }

    private void setListener() {
        mNetworkHelper.setUiDataListener(this);

        mGetRaceGroupNetworkHelper.setUiDataListener(new UIDataListener<Bean>() {
            @Override
            public void onDataChanged(Bean data) {
                LogUtil.d(TAG, data.getResult());
                RaceGroupCache.getInstance(getActivity()).addRaceGroup(data.getResult());

                mRaceGroupList.clear();
                mRaceGroupList.addAll(RaceGroupCache.getInstance(getActivity()).getList());
                mRaceGroupAdapter.notifyDataSetChanged();

                mIsLoading = false;
                mRaceGroupListView.getRefreshableView().removeFooterView(mLoadMoreView);
            }

            @Override
            public void onErrorHappened(int errorCode, String errorMessage) {
                LogUtil.e(TAG, errorMessage);
            }
        });

        mRaceGroupListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                Map<String, String> params = new HashMap<>();
                params.put("version", RaceGroupTableManager.getInstance(getActivity()).getVersion() + "");
                params.put("record_min_id", RaceGroupTableManager.getInstance(getActivity()).getRecordMinId() + "");
                mNetworkHelper.sendPostRequest(UrlParams.REFRESH_RACE_GROUP_URL, params);
                RaceGroupCache.getInstance(getActivity()).clearList();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {

            }
        });

        mRaceGroupListView.setOnScrollListener(this);
    }

    @Override
    public void onDataChanged(Bean data) {
        LogUtil.d(TAG, data.getResult());

        RaceGroupCache.getInstance(getActivity()).setRaceGroup(data.getResult());
        mRaceGroupList.clear();
        mRaceGroupList.addAll(RaceGroupCache.getInstance(getActivity()).getList());
        Message.obtain(mUiHandler, MSG_REFRESH_LIST).sendToTarget();
        mRaceGroupListView.onRefreshComplete();
    }

    @Override
    public void onErrorHappened(int errorCode, String errorMessage) {
        LogUtil.d(TAG, errorMessage);
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {

    }

    @Override
    public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount,
                         int totalItemCount) {
        if(totalItemCount - visibleItemCount - firstVisibleItem <= VISIBLE_REFRESH_COUNT
                && !mIsLoading) {
            if(mRaceGroupList.size() >= 10) {
                mIsLoading = true;
                mRaceGroupListView.getRefreshableView().addFooterView(mLoadMoreView);
                boolean hasData = RaceGroupCache.getInstance(getActivity()).
                        getMoreRaceGroup(10, false);

                if(!hasData) {
                    Map<String, String> params = new HashMap<>();
                    params.put("version", RaceGroupTableManager.getInstance(getActivity()).getVersion() + "");
                    params.put("record_min_id", RaceGroupTableManager.getInstance(getActivity()).getRecordMinId() + "");
                    mGetRaceGroupNetworkHelper.sendPostRequest(UrlParams.GET_RACE_GROUP_URL, params);
                } else {
                    mRaceGroupList.clear();
                    mRaceGroupList.addAll(RaceGroupCache.getInstance(getActivity()).getList());
                    mRaceGroupAdapter.notifyDataSetChanged();

                    mIsLoading = false;
                    mRaceGroupListView.getRefreshableView().removeFooterView(mLoadMoreView);
                }
            }
        }
    }
}
