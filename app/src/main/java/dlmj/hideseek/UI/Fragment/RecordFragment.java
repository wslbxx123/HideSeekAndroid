package dlmj.hideseek.UI.Fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import dlmj.hideseek.Common.Interfaces.UIDataListener;
import dlmj.hideseek.Common.Model.Bean;
import dlmj.hideseek.Common.Model.Record;
import dlmj.hideseek.Common.Params.UrlParams;
import dlmj.hideseek.Common.Util.LogUtil;
import dlmj.hideseek.DataAccess.RecordTableManager;
import dlmj.hideseek.R;
import dlmj.hideseek.UI.Adapter.RecordAdapter;

/**
 * Created by Two on 4/29/16.
 */
public class RecordFragment extends Fragment implements UIDataListener<Bean> {
    private static String TAG = "RecordFragment";
    private PullToRefreshListView mRecordListView;
    private RecordAdapter mRecordAdapter;
    private NetworkHelper mNetworkHelper;
    private TextView mScoreTextView;
    private List<Record> mRecordList = new LinkedList<>();
    private View rootView;
    private NetworkHelper mGetRecordNetworkHelper;

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
    }

    private void findView(View view) {
        mRecordListView = (PullToRefreshListView) view.findViewById(R.id.recordListView);
        mRecordListView.setAdapter(mRecordAdapter);
        mRecordListView.setMode(PullToRefreshBase.Mode.BOTH);

        ILoadingLayout startLabels = mRecordListView.getLoadingLayoutProxy(true, false);
        startLabels.setPullLabel("");
        startLabels.setRefreshingLabel("");
        startLabels.setReleaseLabel("");

        ILoadingLayout endLabels = mRecordListView.getLoadingLayoutProxy(false, true);
        endLabels.setLoadingDrawable(null);
        mScoreTextView = (TextView) view.findViewById(R.id.scoreTextView);
    }

    private void setListener() {
        mNetworkHelper.setUiDataListener(this);

        mGetRecordNetworkHelper.setUiDataListener(new UIDataListener<Bean>() {
            @Override
            public void onDataChanged(Bean data) {
                RecordCache.getInstance(getActivity()).setRecords(data.getResult());

                mRecordList.clear();
                mRecordList.addAll(RecordCache.getInstance(getActivity()).getList());
                mRecordAdapter.notifyDataSetChanged();

                mRecordListView.onRefreshComplete();
            }

            @Override
            public void onErrorHappened(int errorCode, String errorMessage) {
                LogUtil.e(TAG, errorMessage);
            }
        });

        mRecordListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                Map<String, String> params = new HashMap<>();
                params.put("version", RecordTableManager.getInstance(getActivity()).getVersion() + "");
                params.put("record_min_id", RecordTableManager.getInstance(getActivity()).getRecordMinId() + "");
                mNetworkHelper.sendPostRequest(UrlParams.REFRESH_RECORD_URL, params);
                RecordCache.getInstance(getActivity()).clearList();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                if(getRecordCount() >= 10) {
                    List<Record> recordList = RecordCache.getInstance(getActivity()).
                            getMoreRecords(10);

                    if(recordList.size() == 0) {
                        Map<String, String> params = new HashMap<>();
                        params.put("version", RecordTableManager.getInstance(getActivity()).getVersion() + "");
                        params.put("record_min_id", RecordTableManager.getInstance(getActivity()).getRecordMinId() + "");
                        mGetRecordNetworkHelper.sendPostRequest(UrlParams.GET_RECORD_URL, params);
                    } else {
                        mRecordList.addAll(recordList);
                        mRecordAdapter.notifyDataSetChanged();
                    }
                }
            }
        });
    }

    @Override
    public void onDataChanged(Bean data) {
        LogUtil.d(TAG, data.getResult());

        RecordCache.getInstance(getActivity()).setRecords(data.getResult());
        mScoreTextView.setText(RecordCache.getInstance(getActivity()).getScoreSum() + "");
        mRecordList.clear();
        mRecordList.addAll(RecordCache.getInstance(getActivity()).getList());
        mRecordAdapter.notifyDataSetChanged();
        mRecordListView.onRefreshComplete();
    }

    @Override
    public void onErrorHappened(int errorCode, String errorMessage) {

    }

    public int getRecordCount() {
        int count = 0;

        for(Record record : mRecordList) {
            count += record.getRecordItems().size();
        }

        return count;
    }
}
