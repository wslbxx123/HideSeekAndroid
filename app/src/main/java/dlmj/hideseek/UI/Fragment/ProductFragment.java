package dlmj.hideseek.UI.Fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alipay.sdk.app.PayTask;
import com.google.gson.Gson;
import com.handmark.pulltorefresh.library.ILoadingLayout;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshGridView;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import dlmj.hideseek.BusinessLogic.Cache.ProductCache;
import dlmj.hideseek.BusinessLogic.Cache.RaceGroupCache;
import dlmj.hideseek.BusinessLogic.Cache.UserCache;
import dlmj.hideseek.BusinessLogic.Helper.UserInfoManager;
import dlmj.hideseek.BusinessLogic.Network.AlipayManager;
import dlmj.hideseek.BusinessLogic.Network.NetworkHelper;
import dlmj.hideseek.Common.Factory.ErrorMessageFactory;
import dlmj.hideseek.Common.Interfaces.UIDataListener;
import dlmj.hideseek.Common.Model.Bean;
import dlmj.hideseek.Common.Model.CreateOrder;
import dlmj.hideseek.Common.Model.Product;
import dlmj.hideseek.Common.Params.UrlParams;
import dlmj.hideseek.Common.Util.LogUtil;
import dlmj.hideseek.Common.Util.PayResult;
import dlmj.hideseek.DataAccess.ProductTableManager;
import dlmj.hideseek.R;
import dlmj.hideseek.UI.Activity.MyOrderActivity;
import dlmj.hideseek.UI.Adapter.ProductAdapter;

/**
 * 创建者     ZPL
 * 创建时间   2016/7/16 20:20
 * 描述	     商城详情页
 * <p/>
 * 更新者     $Author$
 * 更新时间   $Date$
 * 更新描述   ${TODO}
 */
public class ProductFragment extends BaseFragment implements UIDataListener<Bean> {
    private static final String TAG = "ShopFragment";
    private static final int MSG_REFRESH_LIST = 1;
    private static final int SDK_PAY_FLAG = 2;
    private PullToRefreshGridView mProductGridView;
    private View mRootView;
    private NetworkHelper mNetworkHelper;
    private NetworkHelper mGetProductNetworkHelper;
    private NetworkHelper mCreateOrderNetworkHelper;
    private ProductTableManager mProductTableManager;
    private TextView mProductCountTextView;
    private TextView mTotalTextView;
    private AlertDialog mAlertDialog;
    private List<Product> mProductList = new LinkedList<>();
    private AlipayManager mAlipayManager;
    private Handler mUiHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REFRESH_LIST:
                    mProductAdapter.notifyDataSetChanged();
                    mProductGridView.onRefreshComplete();
                    break;
                case SDK_PAY_FLAG: {
                    PayResult payResult = new PayResult((String) msg.obj);
                    /**
                     * 同步返回的结果必须放置到服务端进行验证（验证的规则请看https://doc.open.alipay.com/doc2/
                     * detail.htm?spm=0.0.0.0.xdvAU6&treeId=59&articleId=103665&
                     * docType=1) 建议商户依赖异步通知
                     */
                    String resultInfo = payResult.getResult();// 同步返回需要验证的信息

                    String resultStatus = payResult.getResultStatus();
                    // 判断resultStatus 为“9000”则代表支付成功，具体状态码代表含义可参考接口文档
                    if (TextUtils.equals(resultStatus, "9000")) {
                        Toast.makeText(getActivity(), "支付成功", Toast.LENGTH_SHORT).show();
                    } else {
                        // 判断resultStatus 为非"9000"则代表可能支付失败
                        // "8000"代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
                        if (TextUtils.equals(resultStatus, "8000")) {
                            Toast.makeText(getActivity(), "支付结果确认中", Toast.LENGTH_SHORT).show();
                        } else {
                            // 其他值就可以判断为支付失败，包括用户主动取消支付，或者系统返回的错误
                            Toast.makeText(getActivity(), "支付失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;
                }
            }
        }
    };
    private ProductAdapter mProductAdapter;
    private GridView mGridView;
    private ErrorMessageFactory mErrorMessageFactory;

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

        mProductList.clear();
        if(ProductCache.getInstance(getActivity()).getList() != null) {
            mProductList.addAll(ProductCache.getInstance(getActivity()).getList());
        }
        mProductAdapter.notifyDataSetChanged();

        mProductGridView.setRefreshing(true);
        return mRootView;
    }

    private void initData() {
        mProductTableManager = ProductTableManager.getInstance(getActivity());
        mProductAdapter = new ProductAdapter(getContext(), mProductList);
        mNetworkHelper = new NetworkHelper(getActivity());
        mGetProductNetworkHelper = new NetworkHelper(getActivity());
        mCreateOrderNetworkHelper = new NetworkHelper(getActivity());
        mErrorMessageFactory = new ErrorMessageFactory(getActivity());
        mAlipayManager = AlipayManager.getInstance();
    }

    private void findView() {
        mProductGridView = (PullToRefreshGridView) mRootView.findViewById(R.id.pullToRefreshGridView);
        mProductGridView.setAdapter(mProductAdapter);
        mProductGridView.setMode(PullToRefreshBase.Mode.BOTH);

        ILoadingLayout startLabels = mProductGridView.getLoadingLayoutProxy(true, false);
        startLabels.setPullLabel("");
        startLabels.setRefreshingLabel("");
        startLabels.setReleaseLabel("");

        ILoadingLayout endLabels = mProductGridView.getLoadingLayoutProxy(false, true);
        endLabels.setLoadingDrawable(null);
    }

    private void setListener() {
        mNetworkHelper.setUiDataListener(this);

        mGetProductNetworkHelper.setUiDataListener(new UIDataListener<Bean>() {
            @Override
            public void onDataChanged(Bean data) {
                LogUtil.d(TAG, data.getResult());

                ProductCache.getInstance(getActivity()).addProducts(data.getResult());

                mProductList.clear();
                mProductList.addAll(ProductCache.getInstance(getActivity()).getList());
                mProductAdapter.notifyDataSetChanged();

                Message.obtain(mUiHandler, MSG_REFRESH_LIST).sendToTarget();
            }

            @Override
            public void onErrorHappened(int errorCode, String errorMessage) {
                LogUtil.d(TAG, errorMessage);

                String message = mErrorMessageFactory.get(errorCode);
                mToast.show(message);
            }
        });

        mProductGridView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<GridView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<GridView> refreshView) {
                    //下拉刷新
                    Map<String, String> params = new HashMap<>();
                    params.put("version", mProductTableManager.getVersion() + "");
                    params.put("product_min_id", mProductTableManager.getProductMinId() + "");
                    mResponseCode = 0;
                    mNetworkHelper.sendPostRequestWithoutSid(UrlParams.REFRESH_PRODUCTS_URL, params);
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<GridView> refreshView) {
                if (mProductList.size() > 10) {
                    //上拉加载
                    boolean hasData = RaceGroupCache.getInstance(getActivity()).
                            getMoreRaceGroup(10, false);

                    if(!hasData) {
                        Map<String, String> params = new HashMap<>();
                        params.put("version", mProductTableManager.getVersion() + "");
                        params.put("product_min_id", mProductTableManager.getProductMinId() + "");
                        mResponseCode = 0;
                        mGetProductNetworkHelper.sendPostRequest(UrlParams.GET_PRODUCTS_URL, params);
                    } else {
                        mProductList.clear();
                        mProductList.addAll(ProductCache.getInstance(getActivity()).getList());
                        mProductAdapter.notifyDataSetChanged();

                        mProductGridView.onRefreshComplete();
                    }
                }
            }
        });

        mCreateOrderNetworkHelper.setUiDataListener(new UIDataListener<Bean>() {
            @Override
            public void onDataChanged(Bean data) {
                String result = data.getResult();
                Gson gson = new Gson();
                CreateOrder order = gson.fromJson(result, CreateOrder.class);
                CreateOrder.ParamsEntity params = order.params;
                String sign = order.sign;
                //调用支付宝进行支付
                pay(sign,params);
            }

            @Override
            public void onErrorHappened(int errorCode, String errorMessage) {
                Toast.makeText(getActivity(), "网络错误", Toast.LENGTH_SHORT).show();
            }
        });

        mProductAdapter.setOnPurchaseBtnClickedListener(new ProductAdapter.OnPurchaseBtnClickedListener() {
            @Override
            public void purchaseBtnOnClicked(Product product) {
                if(!UserCache.getInstance().ifLogin()) {
                    UserInfoManager.getInstance().checkIfGoToLogin(getActivity());
                    return;
                }

                showDialog(product);
            }
        });
    }

    @Override
    public void onDataChanged(Bean data) {
        LogUtil.d(TAG, data.getResult());
        ProductCache.getInstance(getActivity()).setProducts(data.getResult());
        mProductList.clear();
        mProductList.addAll(ProductCache.getInstance(getActivity()).getList());
        Message.obtain(mUiHandler, MSG_REFRESH_LIST).sendToTarget();
    }

    @Override
    public void onErrorHappened(int errorCode, String errorMessage) {
        LogUtil.d(TAG, errorMessage);

        String message = mErrorMessageFactory.get(errorCode);
        mToast.show(message);
    }

    private void showDialog(final Product product) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = View.inflate(getActivity(), R.layout.purchase_dialog, null);
        builder.setView(view);

        //初始化控件
        TextView productNameTextView = (TextView) view.findViewById(R.id.productNameTextView);
        mProductCountTextView = (TextView) view.findViewById(R.id.productCountTextView);
        ImageView upArrowImageView = (ImageView) view.findViewById(R.id.upArrowImageView);
        ImageView downArrowImageView = (ImageView) view.findViewById(R.id.downArrowImageView);
        mTotalTextView = (TextView) view.findViewById(R.id.totalTextView);
        Button ensurePay = (Button) view.findViewById(R.id.buy_dialog_btn);

        mTotalTextView.setText(product.getPrice() + "");
        productNameTextView.setText(product.getName());
        mAlertDialog = builder.create();

        upArrowImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i = Integer.parseInt(mProductCountTextView.getText().toString());
                mProductCountTextView.setText((i + 1) + "");
                mTotalTextView.setText((i + 1) * product.getPrice() + "");
            }
        });

        downArrowImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i = Integer.parseInt(mProductCountTextView.getText().toString());
                if (i > 1) {
                    mProductCountTextView.setText((i - 1) + "");
                    mTotalTextView.setText((i - 1) * product.getPrice() + "");
                }
            }
        });

        ensurePay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i = Integer.parseInt(mProductCountTextView.getText().toString());
                //超过最大数量,则不允许购买
                Map<String, String> params = new HashMap<>();
                params.put("store_id", product.getPkId() + "");
                params.put("count", mProductCountTextView.getText().toString());
                System.out.println("count "+ mProductCountTextView.getText().toString()
                        + "productsEntity.pk_id  "+ product.getPkId());
                mCreateOrderNetworkHelper.sendPostRequest(UrlParams.CREATE_ORDER_URL, params);

                //关闭对话框
                mAlertDialog.dismiss();
            }
        });

        if (product.getPkId() == 2 && UserCache.getInstance().getUser().getHasGuide()) {
            checkIfPurchase();
        } else {
            mAlertDialog.show();
        }
    }

    private void checkIfPurchase() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getString(R.string.message_has_guide));
        builder.setPositiveButton(getResources().getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mAlertDialog.show();
                    }
                });
        builder.setNegativeButton(getResources().getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        builder.show();
    }

    private void pay(String sign,CreateOrder.ParamsEntity params) {
        String orderInfo = mAlipayManager.getOrderInfo(params);

        /**
         * 完整的符合支付宝参数规范的订单信息
         */
        final String payInfo = orderInfo + "&sign=\"" + sign + "\"&" + mAlipayManager.getSignType();

        Runnable payRunnable = new Runnable() {
            @Override
            public void run() {
                // 构造PayTask 对象
                PayTask alipay = new PayTask(getActivity());
                // 调用支付接口，获取支付结果
                String result = alipay.pay(payInfo, true);

                Message msg = new Message();
                msg.what = SDK_PAY_FLAG;
                msg.obj = result;
                mUiHandler.sendMessage(msg);
            }
        };
        // 必须异步调用
        Thread payThread = new Thread(payRunnable);
        payThread.start();
    }
}
