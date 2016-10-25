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

import dlmj.hideseek.BusinessLogic.Cache.ExchangeOrderCache;
import dlmj.hideseek.BusinessLogic.Cache.UserCache;
import dlmj.hideseek.BusinessLogic.Network.NetworkHelper;
import dlmj.hideseek.Common.Factory.ErrorMessageFactory;
import dlmj.hideseek.Common.Interfaces.UIDataListener;
import dlmj.hideseek.Common.Model.Bean;
import dlmj.hideseek.Common.Model.ExchangeOrder;
import dlmj.hideseek.Common.Params.CodeParams;
import dlmj.hideseek.Common.Params.UrlParams;
import dlmj.hideseek.Common.Util.LogUtil;
import dlmj.hideseek.DataAccess.ExchangeOrderTableManager;
import dlmj.hideseek.R;
import dlmj.hideseek.UI.Activity.BaseFragmentActivity;
import dlmj.hideseek.UI.Adapter.ExchangeOrderAdapter;

/**
 * Created by Two on 23/10/2016.
 */
public class ExchangeOrderFragment extends BaseFragment implements UIDataListener<Bean>, ListView.OnScrollListener{
    private final static String TAG = "ExchangeOrderFragment";
    private final static int MSG_REFRESH_LIST = 1;
    private static final int VISIBLE_REFRESH_COUNT = 3;
    private View mRootView;
    private PullToRefreshListView mExchangeOrderListView;
    private ExchangeOrderAdapter mExchangeOrderAdapter;
    private List<ExchangeOrder> mExchangeOrderList = new LinkedList<>();
    private NetworkHelper mNetworkHelper;
    private NetworkHelper mGetExchangeOrderNetworkHelper;
    private boolean mIsLoading = false;
    private View mLoadMoreView;
    private ErrorMessageFactory mErrorMessageFactory;
    private ExchangeOrderTableManager mExchangeOrderTableManager;
    private Handler mUiHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REFRESH_LIST:
                    mExchangeOrderAdapter.notifyDataSetChanged();
                    mExchangeOrderListView.onRefreshComplete();
                    break;
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(mRootView == null) {
            mRootView = inflater.inflate(R.layout.exchange_order, null);
            initData();
            findView();
            setListener();
        }

        ViewGroup parent = (ViewGroup) mRootView.getParent();
        if (parent != null) {
            parent.removeView(mRootView);
        }

        mExchangeOrderList.clear();
        if(ExchangeOrderCache.getInstance(getActivity()).getList() != null) {
            mExchangeOrderList.addAll(ExchangeOrderCache.getInstance(getActivity()).getList());
        }
        mExchangeOrderAdapter.notifyDataSetChanged();

        if(UserCache.getInstance().ifLogin()) {
            mExchangeOrderListView.setRefreshing(true);
        }
        return mRootView;
    }

    private void initData() {
        mExchangeOrderAdapter = new ExchangeOrderAdapter(getActivity(), mExchangeOrderList);
        mNetworkHelper = new NetworkHelper(getActivity());
        mGetExchangeOrderNetworkHelper = new NetworkHelper(getActivity());
        mErrorMessageFactory = new ErrorMessageFactory(getActivity());
        mExchangeOrderTableManager = ExchangeOrderTableManager.getInstance(getActivity());
    }

    private void findView() {
        mExchangeOrderListView = (PullToRefreshListView) mRootView.findViewById(R.id.exchangeOrderListView);
        mExchangeOrderListView.setAdapter(mExchangeOrderAdapter);
        mExchangeOrderListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);

        ILoadingLayout startLabels = mExchangeOrderListView.getLoadingLayoutProxy(true, false);
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
                    mExchangeOrderListView.setRefreshing(true);
                }
            }
        });

        mGetExchangeOrderNetworkHelper.setUiDataListener(new UIDataListener<Bean>() {
            @Override
            public void onDataChanged(Bean data) {
                LogUtil.d(TAG, data.getResult());
                mResponseCode = CodeParams.SUCCESS;
                ExchangeOrderCache.getInstance(getActivity()).addOrders(data.getResult());

                mExchangeOrderList.clear();
                mExchangeOrderList.addAll(ExchangeOrderCache.getInstance(getActivity()).getList());
                mExchangeOrderAdapter.notifyDataSetChanged();

                mIsLoading = false;
                mExchangeOrderListView.getRefreshableView().removeFooterView(mLoadMoreView);
            }

            @Override
            public void onErrorHappened(int errorCode, String errorMessage) {
                LogUtil.d(TAG, errorMessage);
                mResponseCode = errorCode;

                String message = mErrorMessageFactory.get(errorCode);
                mToast.show(message);
            }
        });

        mExchangeOrderListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                Map<String, String> params = new HashMap<>();
                params.put("version", mExchangeOrderTableManager.getVersion() + "");
                params.put("order_min_id", mExchangeOrderTableManager.getOrderMinId() + "");
                mResponseCode = 0;
                mNetworkHelper.sendPostRequest(UrlParams.REFRESH_EXCHANGE_ORDERS_URL, params);
                ExchangeOrderCache.getInstance(getActivity()).clearList();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {

            }
        });

        mExchangeOrderListView.setOnScrollListener(this);
    }

    @Override
    public void onDataChanged(Bean data) {
        LogUtil.d(TAG, data.getResult());
        mResponseCode = CodeParams.SUCCESS;
        ExchangeOrderCache.getInstance(getActivity()).setOrders(data.getResult());
        mExchangeOrderList.clear();
        mExchangeOrderList.addAll(ExchangeOrderCache.getInstance(getActivity()).getList());
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
            if(mExchangeOrderList.size() >= 10) {
                mIsLoading = true;
                mExchangeOrderListView.getRefreshableView().addFooterView(mLoadMoreView);
                boolean hasData = ExchangeOrderCache.getInstance(getActivity()).
                        getMoreOrders(10, false);

                if(!hasData) {
                    Map<String, String> params = new HashMap<>();
                    params.put("version", mExchangeOrderTableManager.getVersion() + "");
                    params.put("order_min_id", mExchangeOrderTableManager.getOrderMinId() + "");
                    mResponseCode = 0;
                    mGetExchangeOrderNetworkHelper.sendPostRequest(UrlParams.GET_EXCHANGE_ORDERS_URL, params);
                } else {
                    mExchangeOrderList.clear();
                    mExchangeOrderList.addAll(ExchangeOrderCache.getInstance(getActivity()).getList());
                    mExchangeOrderAdapter.notifyDataSetChanged();

                    mIsLoading = false;
                    mExchangeOrderListView.getRefreshableView().removeFooterView(mLoadMoreView);
                }
            }
        }
    }
}
