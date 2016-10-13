package dlmj.hideseek.UI.Fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.TextView;

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
import dlmj.hideseek.Common.Model.Shop;
import dlmj.hideseek.Common.Params.UrlParams;
import dlmj.hideseek.Common.Util.LogUtil;
import dlmj.hideseek.R;
import dlmj.hideseek.UI.Adapter.ShopAdapter;

/**
 * 创建者     ZPL
 * 创建时间   2016/7/16 20:20
 * 描述	     商城详情页
 * <p/>
 * 更新者     $Author$
 * 更新时间   $Date$
 * 更新描述   ${TODO}
 */
public class ShopFragment extends BaseFragment implements UIDataListener<Bean> {
    private static final String TAG = "ShopFragment";
    private static final int MSG_REFRESH_LIST = 1;
    private PullToRefreshGridView mPTRGridView;
    private View mView;
    private NetworkHelper mNetworkHelper;
    private NetworkHelper mGetShopNetworkHelper;
    private List<Shop.ProductsEntity> mProductsEntity = new LinkedList<>();
    private Handler mUiHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REFRESH_LIST:
                    mShopAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };
    private ShopAdapter mShopAdapter;
    private GridView mGridView;
    private ErrorMessageFactory mErrorMessageFactory;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mView == null) {
            mView = View.inflate(getContext(), R.layout.fragment_shop, null);
        }
        initView();
        initData();
        initListener();
        String data = ShopRewardCache.getDataFromLocal(UrlParams.GET_PRODUCTS_URL);
        //使用本地缓存
        if (data!=null) {
            setData(data);
        } else {
            //自动刷新
           mPTRGridView.setRefreshing(true);
        }
        return mView;
    }

    private void initListener() {
        mNetworkHelper.setUiDataListener(this);
        mGetShopNetworkHelper.setUiDataListener(new UIDataListener<Bean>() {
            @Override
            public void onDataChanged(Bean data) {
                //保存缓存
                String result = data.getResult();
                ShopRewardCache.saveCache(result,UrlParams.GET_PRODUCTS_URL);
                setData(result);
            }

            @Override
            public void onErrorHappened(int errorCode, String errorMessage) {
                LogUtil.d(TAG, errorMessage);

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
                    params.put("product_min_id", 10 + "");
                    mGetShopNetworkHelper.sendPostRequestWithoutSid(UrlParams.GET_PRODUCTS_URL, params);
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<GridView> refreshView) {
                if (mProductsEntity.size() > 10) {
                    //上拉加载
                    Map<String, String> params = new HashMap<>();
                    params.put("version", 0 + "");
                    params.put("product_min_id", 0 + "");
                    mNetworkHelper.sendPostRequestWithoutSid(UrlParams.REFRESH_PRODUCTS_URL, params);
                }
                mPTRGridView.onRefreshComplete();
            }
        });
    }

    private void setData(String result) {
        Gson gson = new Gson();
        Shop shop = gson.fromJson(result, Shop.class);
        mProductsEntity = shop.products;
        mShopAdapter = new ShopAdapter(getContext(), mProductsEntity);
        mGridView.setAdapter(mShopAdapter);
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

        mShopAdapter = new ShopAdapter(getContext(), mProductsEntity);
        mGridView.setAdapter(mShopAdapter);
        mErrorMessageFactory = new ErrorMessageFactory(getActivity());
    }

    private void initView() {
        mPTRGridView = (PullToRefreshGridView) mView.findViewById(R.id.pullToRefreshGridView);
        mNetworkHelper = new NetworkHelper(getActivity());
        mGetShopNetworkHelper = new NetworkHelper(getActivity());
    }

    @Override
    public void onDataChanged(Bean data) {
        String result = data.getResult();
        Gson gson = new Gson();
        Shop shop = gson.fromJson(result, Shop.class);
        //mProductsEntity = shop.products;
        List productsEntity = shop.products;
        mProductsEntity.addAll(productsEntity);
        mShopAdapter = new ShopAdapter(getContext(), mProductsEntity);
        mGridView.setAdapter(mShopAdapter);
        Message.obtain(mUiHandler, MSG_REFRESH_LIST).sendToTarget();
    }

    @Override
    public void onErrorHappened(int errorCode, String errorMessage) {
        LogUtil.d(TAG, errorMessage);

        String message = mErrorMessageFactory.get(errorCode);
        mToast.show(message);
    }
}
