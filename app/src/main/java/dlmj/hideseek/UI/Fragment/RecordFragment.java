package dlmj.hideseek.UI.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.ILoadingLayout;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import dlmj.hideseek.BusinessLogic.Cache.RecordCache;
import dlmj.hideseek.BusinessLogic.Cache.UserCache;
import dlmj.hideseek.BusinessLogic.Network.NetworkHelper;

import dlmj.hideseek.Common.Factory.ErrorMessageFactory;
import dlmj.hideseek.Common.Interfaces.UIDataListener;
import dlmj.hideseek.Common.Model.Bean;
import dlmj.hideseek.Common.Model.Record;
import dlmj.hideseek.Common.Params.CodeParams;
import dlmj.hideseek.Common.Params.UrlParams;
import dlmj.hideseek.Common.Util.LogUtil;
import dlmj.hideseek.DataAccess.RecordTableManager;
import dlmj.hideseek.R;
import dlmj.hideseek.UI.Adapter.RecordAdapter;

/**
 * Created by Two on 4/29/16.
 */
public class RecordFragment extends BaseFragment implements UIDataListener<Bean>, ListView.OnScrollListener {
    private static String TAG = "RecordFragment";
    private static final int VISIBLE_REFRESH_COUNT = 3;
    private PullToRefreshListView mRecordListView;
    private RecordAdapter mRecordAdapter;
    private NetworkHelper mNetworkHelper;
    private TextView mScoreTextView;
    private List<Record> mRecordList = new LinkedList<>();
    private View rootView;
    private NetworkHelper mGetRecordNetworkHelper;
    private boolean mIsLoading = false;
    private View mLoadMoreView;
    private ErrorMessageFactory mErrorMessageFactory;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(rootView == null) {
            rootView = inflater.inflate(R.layout.record, null);
            initData();
            findView(rootView);
            setListener();
        }

        ViewGroup parent = (ViewGroup) rootView.getParent();
        if (parent != null) {
            parent.removeView(rootView);
        }

        mRecordList.clear();
        if(RecordCache.getInstance(getActivity()).getList() != null) {
            mRecordList.addAll(RecordCache.getInstance(getActivity()).getList());
        }
        mRecordAdapter.notifyDataSetChanged();

        if(UserCache.getInstance().ifLogin()) {
            mRecordListView.setRefreshing(true);
        } else {
            mScoreTextView.setText(0 + "");
        }
        return rootView;
    }

    private void initData() {
        mNetworkHelper = new NetworkHelper(getActivity());
        mRecordAdapter = new RecordAdapter(getActivity(), mRecordList);
        mGetRecordNetworkHelper = new NetworkHelper(getActivity());
        mErrorMessageFactory = new ErrorMessageFactory(getActivity());
    }

    private void findView(View view) {
        mRecordListView = (PullToRefreshListView) view.findViewById(R.id.recordListView);
        mRecordListView.setAdapter(mRecordAdapter);
        mRecordListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);

        ILoadingLayout startLabels = mRecordListView.getLoadingLayoutProxy(true, false);
        startLabels.setPullLabel("");
        startLabels.setRefreshingLabel("");
        startLabels.setReleaseLabel("");

        mScoreTextView = (TextView) view.findViewById(R.id.scoreTextView);
        mLoadMoreView = LayoutInflater.from(getActivity()).inflate(R.layout.load_more_footer, null);
    }

    private void setListener() {
        mNetworkHelper.setUiDataListener(this);

        mGetRecordNetworkHelper.setUiDataListener(new UIDataListener<Bean>() {
            @Override
            public void onDataChanged(Bean data) {
                LogUtil.d(TAG, data.getResult());
                mResponseCode = CodeParams.SUCCESS;
                RecordCache.getInstance(getActivity()).addRecords(data.getResult());

                mRecordList.clear();
                mRecordList.addAll(RecordCache.getInstance(getActivity()).getList());
                mRecordAdapter.notifyDataSetChanged();

                mIsLoading = false;
                mRecordListView.getRefreshableView().removeFooterView(mLoadMoreView);
            }

            @Override
            public void onErrorHappened(int errorCode, String errorMessage) {
                LogUtil.d(TAG, errorMessage);
                mResponseCode = errorCode;

                String message = mErrorMessageFactory.get(errorCode);
                mToast.show(message);
            }
        });

        mRecordListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                Map<String, String> params = new HashMap<>();
                params.put("version", RecordTableManager.getInstance(getActivity()).getVersion() + "");
                params.put("record_min_id", RecordTableManager.getInstance(getActivity()).getRecordMinId() + "");
                mResponseCode = 0;
                mNetworkHelper.sendPostRequest(UrlParams.REFRESH_RECORD_URL, params);
                RecordCache.getInstance(getActivity()).clearList();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {

            }
        });

        mRecordListView.setOnScrollListener(this);
    }

    @Override
    public void onDataChanged(Bean data) {
        LogUtil.d(TAG, data.getResult());
        mResponseCode = CodeParams.SUCCESS;
        RecordCache.getInstance(getActivity()).setRecords(data.getResult());
        mScoreTextView.setText(RecordCache.getInstance(getActivity()).getScoreSum() + "");
        mRecordList.clear();
        mRecordList.addAll(RecordCache.getInstance(getActivity()).getList());
        mRecordAdapter.notifyDataSetChanged();
        mRecordListView.onRefreshComplete();
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
            if(mRecordList.size() >= 10) {
                mIsLoading = true;
                mRecordListView.getRefreshableView().addFooterView(mLoadMoreView);
                boolean hasData = RecordCache.getInstance(getActivity()).
                        getMoreRecords(10, false);

                if(!hasData) {
                    Map<String, String> params = new HashMap<>();
                    params.put("version", RecordTableManager.getInstance(getActivity()).getVersion() + "");
                    params.put("record_min_id", RecordTableManager.getInstance(getActivity()).getRecordMinId() + "");
                    mResponseCode = 0;
                    mGetRecordNetworkHelper.sendPostRequest(UrlParams.GET_RECORD_URL, params);
                } else {
                    mRecordList.clear();
                    mRecordList.addAll(RecordCache.getInstance(getActivity()).getList());
                    mRecordAdapter.notifyDataSetChanged();

                    mIsLoading = false;
                    mRecordListView.getRefreshableView().removeFooterView(mLoadMoreView);
                }
            }
        }
    }
}
