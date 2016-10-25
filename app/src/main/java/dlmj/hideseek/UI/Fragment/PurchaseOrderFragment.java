package dlmj.hideseek.UI.Fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alipay.sdk.app.PayTask;
import com.google.gson.Gson;
import com.handmark.pulltorefresh.library.ILoadingLayout;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import dlmj.hideseek.BusinessLogic.Cache.PurchaseOrderCache;
import dlmj.hideseek.BusinessLogic.Cache.UserCache;
import dlmj.hideseek.BusinessLogic.Network.AlipayManager;
import dlmj.hideseek.BusinessLogic.Network.NetworkHelper;
import dlmj.hideseek.Common.Factory.ErrorMessageFactory;
import dlmj.hideseek.Common.Interfaces.UIDataListener;
import dlmj.hideseek.Common.Model.Bean;
import dlmj.hideseek.Common.Model.CreateOrder;
import dlmj.hideseek.Common.Model.PurchaseOrder;
import dlmj.hideseek.Common.Params.CodeParams;
import dlmj.hideseek.Common.Params.UrlParams;
import dlmj.hideseek.Common.Util.LogUtil;
import dlmj.hideseek.Common.Util.PayResult;
import dlmj.hideseek.DataAccess.PurchaseOrderTableManager;
import dlmj.hideseek.R;
import dlmj.hideseek.UI.Activity.BaseFragmentActivity;
import dlmj.hideseek.UI.Adapter.PurchaseOrderAdapter;

/**
 * Created by Two on 23/10/2016.
 */
public class PurchaseOrderFragment extends BaseFragment implements UIDataListener<Bean>, ListView.OnScrollListener{
    private final static String TAG = "PurchaseOrderFragment";
    private final static int MSG_REFRESH_LIST = 1;
    private static final int SDK_PAY_FLAG = 2;
    private static final int VISIBLE_REFRESH_COUNT = 3;
    private View mRootView;
    private PullToRefreshListView mPurchaseOrderListView;
    private PurchaseOrderAdapter mPurchaseOrderAdapter;
    private List<PurchaseOrder> mPurchaseOrderList = new LinkedList<>();
    private NetworkHelper mNetworkHelper;
    private NetworkHelper mGetPurchaseOrderNetworkHelper;
    private NetworkHelper mCreateOrderNetworkHelper;
    private boolean mIsLoading = false;
    private View mLoadMoreView;
    private ErrorMessageFactory mErrorMessageFactory;
    private PurchaseOrderTableManager mPurchaseOrderTableManager;
    private TextView mProductCountTextView;
    private TextView mTotalTextView;
    private AlertDialog mAlertDialog;
    private AlipayManager mAlipayManager;
    private Handler mUiHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REFRESH_LIST:
                    mPurchaseOrderAdapter.notifyDataSetChanged();
                    mPurchaseOrderListView.onRefreshComplete();
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(mRootView == null) {
            mRootView = inflater.inflate(R.layout.purchase_order, null);
            initData();
            findView();
            setListener();
        }

        ViewGroup parent = (ViewGroup) mRootView.getParent();
        if (parent != null) {
            parent.removeView(mRootView);
        }

        mPurchaseOrderList.clear();
        if(PurchaseOrderCache.getInstance(getActivity()).getList() != null) {
            mPurchaseOrderList.addAll(PurchaseOrderCache.getInstance(getActivity()).getList());
        }
        mPurchaseOrderAdapter.notifyDataSetChanged();

        if(UserCache.getInstance().ifLogin()) {
            mPurchaseOrderListView.setRefreshing(true);
        }
        return mRootView;
    }

    private void initData() {
        mPurchaseOrderAdapter = new PurchaseOrderAdapter(getActivity(), mPurchaseOrderList);
        mNetworkHelper = new NetworkHelper(getActivity());
        mGetPurchaseOrderNetworkHelper = new NetworkHelper(getActivity());
        mErrorMessageFactory = new ErrorMessageFactory(getActivity());
        mPurchaseOrderTableManager = PurchaseOrderTableManager.getInstance(getActivity());
        mCreateOrderNetworkHelper = new NetworkHelper(getActivity());
        mAlipayManager = AlipayManager.getInstance();
    }

    private void findView() {
        mPurchaseOrderListView = (PullToRefreshListView) mRootView.findViewById(R.id.purchaseOrderListView);
        mPurchaseOrderListView.setAdapter(mPurchaseOrderAdapter);
        mPurchaseOrderListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);

        ILoadingLayout startLabels = mPurchaseOrderListView.getLoadingLayoutProxy(true, false);
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
                    mPurchaseOrderListView.setRefreshing(true);
                }
            }
        });

        mGetPurchaseOrderNetworkHelper.setUiDataListener(new UIDataListener<Bean>() {
            @Override
            public void onDataChanged(Bean data) {
                LogUtil.d(TAG, data.getResult());
                mResponseCode = CodeParams.SUCCESS;
                PurchaseOrderCache.getInstance(getActivity()).addOrders(data.getResult());

                mPurchaseOrderList.clear();
                mPurchaseOrderList.addAll(PurchaseOrderCache.getInstance(getActivity()).getList());
                mPurchaseOrderAdapter.notifyDataSetChanged();

                mIsLoading = false;
                mPurchaseOrderListView.getRefreshableView().removeFooterView(mLoadMoreView);
            }

            @Override
            public void onErrorHappened(int errorCode, String errorMessage) {
                LogUtil.d(TAG, errorMessage);
                mResponseCode = errorCode;

                String message = mErrorMessageFactory.get(errorCode);
                mToast.show(message);
            }
        });

        mPurchaseOrderListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                Map<String, String> params = new HashMap<>();
                params.put("version", mPurchaseOrderTableManager.getVersion() + "");
                params.put("order_min_id", mPurchaseOrderTableManager.getOrderMinId() + "");
                mResponseCode = 0;
                mNetworkHelper.sendPostRequest(UrlParams.REFRESH_PURCHASE_ORDERS_URL, params);
                PurchaseOrderCache.getInstance(getActivity()).clearList();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {

            }
        });

        mPurchaseOrderAdapter.setOnPurchaseBtnClickedListener(new PurchaseOrderAdapter.OnPurchaseBtnClickedListener() {
            @Override
            public void purchaseBtnOnClicked(PurchaseOrder purchaseOrder) {
                showDialog(purchaseOrder);
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

        mPurchaseOrderListView.setOnScrollListener(this);
    }

    private void pay(String sign,CreateOrder.ParamsEntity params) {
        String orderInfo = AlipayManager.getInstance().getOrderInfo(params);

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

    private void showDialog(final PurchaseOrder purchaseOrder) {
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

        mTotalTextView.setText(purchaseOrder.getPrice() + "");
        productNameTextView.setText(purchaseOrder.getProductName());

        mAlertDialog = builder.create();

        upArrowImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i = Integer.parseInt(mProductCountTextView.getText().toString());
                mProductCountTextView.setText((i + 1) + "");
                mTotalTextView.setText((i + 1) * purchaseOrder.getPrice() + "");
            }
        });

        downArrowImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i = Integer.parseInt(mProductCountTextView.getText().toString());
                if (i > 1) {
                    mProductCountTextView.setText((i - 1) + "");
                    mTotalTextView.setText((i - 1) * purchaseOrder.getPrice() + "");
                }
            }
        });

        ensurePay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i = Integer.parseInt(mProductCountTextView.getText().toString());
                //超过最大数量,则不允许购买
                Map<String, String> params = new HashMap<>();
                params.put("store_id", purchaseOrder.getProductId() + "");
                params.put("count", mProductCountTextView.getText().toString());
                System.out.println("count "+ mProductCountTextView.getText().toString()
                        + "productsEntity.pk_id  "+ purchaseOrder.getProductId());
                mCreateOrderNetworkHelper.sendPostRequest(UrlParams.CREATE_ORDER_URL, params);

                //关闭对话框
                mAlertDialog.dismiss();
            }
        });

        if (purchaseOrder.getProductId() == 2 && UserCache.getInstance().getUser().getHasGuide()) {
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

    @Override
    public void onDataChanged(Bean data) {
        LogUtil.d(TAG, data.getResult());
        mResponseCode = CodeParams.SUCCESS;
        PurchaseOrderCache.getInstance(getActivity()).setOrders(data.getResult());
        mPurchaseOrderList.clear();
        mPurchaseOrderList.addAll(PurchaseOrderCache.getInstance(getActivity()).getList());
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
            if(mPurchaseOrderList.size() >= 10) {
                mIsLoading = true;
                mPurchaseOrderListView.getRefreshableView().addFooterView(mLoadMoreView);
                boolean hasData = PurchaseOrderCache.getInstance(getActivity()).
                        getMoreOrders(10, false);

                if(!hasData) {
                    Map<String, String> params = new HashMap<>();
                    params.put("version", mPurchaseOrderTableManager.getVersion() + "");
                    params.put("order_min_id", mPurchaseOrderTableManager.getOrderMinId() + "");
                    mResponseCode = 0;
                    mGetPurchaseOrderNetworkHelper.sendPostRequest(UrlParams.GET_PURCHASE_ORDERS_URL, params);
                } else {
                    mPurchaseOrderList.clear();
                    mPurchaseOrderList.addAll(PurchaseOrderCache.getInstance(getActivity()).getList());
                    mPurchaseOrderAdapter.notifyDataSetChanged();

                    mIsLoading = false;
                    mPurchaseOrderListView.getRefreshableView().removeFooterView(mLoadMoreView);
                }
            }
        }
    }
}
