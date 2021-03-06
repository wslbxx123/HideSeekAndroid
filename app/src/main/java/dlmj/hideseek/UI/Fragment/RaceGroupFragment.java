package dlmj.hideseek.UI.Fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import dlmj.hideseek.Common.Factory.ErrorMessageFactory;
import dlmj.hideseek.Common.Interfaces.UIDataListener;
import dlmj.hideseek.Common.Model.Bean;
import dlmj.hideseek.Common.Model.RaceGroup;
import dlmj.hideseek.Common.Params.CodeParams;
import dlmj.hideseek.Common.Params.UrlParams;
import dlmj.hideseek.Common.Util.LogUtil;
import dlmj.hideseek.DataAccess.RaceGroupTableManager;
import dlmj.hideseek.R;
import dlmj.hideseek.UI.Activity.BaseFragmentActivity;
import dlmj.hideseek.UI.Adapter.RaceGroupAdapter;

/**
 * Created by Two on 5/14/16.
 */
public class RaceGroupFragment extends BaseFragment implements UIDataListener<Bean>, ListView.OnScrollListener {
    private final static String TAG = "RaceGroupFragment";
    private final static int MSG_REFRESH_LIST = 1;
    private static final int VISIBLE_REFRESH_COUNT = 3;
    private View mRootView;
    private PullToRefreshListView mRaceGroupListView;
    private RaceGroupAdapter mRaceGroupAdapter;
    private List<RaceGroup> mRaceGroupList = new LinkedList<>();
    private NetworkHelper mNetworkHelper;
    private NetworkHelper mGetRaceGroupNetworkHelper;
    private boolean mIsLoading = false;
    private View mLoadMoreView;
    private ErrorMessageFactory mErrorMessageFactory;
    private Handler mUiHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REFRESH_LIST:
                    mRaceGroupAdapter.notifyDataSetChanged();
                    mRaceGroupListView.onRefreshComplete();
                    break;
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(mRootView == null) {
            mRootView = inflater.inflate(R.layout.race_group, null);
            initData();
            findView();
            setListener();
        }

        ViewGroup parent = (ViewGroup) mRootView.getParent();
        if (parent != null) {
            parent.removeView(mRootView);
        }

        mRaceGroupList.clear();
        if(RaceGroupCache.getInstance(getActivity()).getList() != null) {
            mRaceGroupList.addAll(RaceGroupCache.getInstance(getActivity()).getList());
        }
        mRaceGroupAdapter.notifyDataSetChanged();

        if(UserCache.getInstance().ifLogin()) {
            mRaceGroupListView.setRefreshing(true);
        }
        return mRootView;
    }

    private void initData() {
        mRaceGroupAdapter = new RaceGroupAdapter(getActivity(), mRaceGroupList);
        mNetworkHelper = new NetworkHelper(getActivity());
        mGetRaceGroupNetworkHelper = new NetworkHelper(getActivity());
        mErrorMessageFactory = new ErrorMessageFactory(getActivity());
    }

    private void findView() {
        mRaceGroupListView = (PullToRefreshListView) mRootView.findViewById(R.id.raceGroupListView);
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

        ((BaseFragmentActivity)getActivity()).getLoginDialog().setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if(UserCache.getInstance().ifLogin()) {
                    mRaceGroupListView.setRefreshing(true);
                }
            }
        });

        mGetRaceGroupNetworkHelper.setUiDataListener(new UIDataListener<Bean>() {
            @Override
            public void onDataChanged(Bean data) {
                LogUtil.d(TAG, data.getResult());
                mResponseCode = CodeParams.SUCCESS;
                RaceGroupCache.getInstance(getActivity()).addRaceGroup(data.getResult());

                mRaceGroupList.clear();
                mRaceGroupList.addAll(RaceGroupCache.getInstance(getActivity()).getList());
                mRaceGroupAdapter.notifyDataSetChanged();

                mIsLoading = false;
                mRaceGroupListView.getRefreshableView().removeFooterView(mLoadMoreView);
            }

            @Override
            public void onErrorHappened(int errorCode, String errorMessage) {
                LogUtil.d(TAG, errorMessage);
                mResponseCode = errorCode;

                String message = mErrorMessageFactory.get(errorCode);
                mToast.show(message);
            }
        });

        mRaceGroupListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                Map<String, String> params = new HashMap<>();
                params.put("version", RaceGroupTableManager.getInstance(getActivity()).getVersion() + "");
                params.put("record_min_id", RaceGroupTableManager.getInstance(getActivity()).getRecordMinId() + "");
                mResponseCode = 0;
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
        mResponseCode = CodeParams.SUCCESS;
        RaceGroupCache.getInstance(getActivity()).setRaceGroup(data.getResult());
        mRaceGroupList.clear();
        mRaceGroupList.addAll(RaceGroupCache.getInstance(getActivity()).getList());
        Message.obtain(mUiHandler, MSG_REFRESH_LIST).sendToTarget();
    }

    @Override
    public void onErrorHappened(int errorCode, String errorMessage) {
        LogUtil.d(TAG, errorMessage);
        mResponseCode = errorCode;

        String message = mErrorMessageFactory.get(errorCode);
        mToast.show(message);
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
                    mResponseCode = 0;
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
