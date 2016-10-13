package dlmj.hideseek.UI.Activity;

import android.os.Bundle;
import android.widget.ListView;

import java.util.HashMap;
import java.util.Map;

import dlmj.hideseek.BusinessLogic.Network.NetworkHelper;
import dlmj.hideseek.Common.Factory.ErrorMessageFactory;
import dlmj.hideseek.Common.Interfaces.UIDataListener;
import dlmj.hideseek.Common.Model.Bean;
import dlmj.hideseek.Common.Params.CodeParams;
import dlmj.hideseek.Common.Params.UrlParams;
import dlmj.hideseek.Common.Util.LogUtil;
import dlmj.hideseek.R;
import dlmj.hideseek.UI.Adapter.MyOrderAdapter;

/**
 * 创建者     ZPL
 * 创建时间   2016/8/1 22:31
 * 描述	    订单详情页
 * <p/>
 * 更新者     $Author$
 * 更新时间   $Date$
 * 更新描述   ${TODO}
 */
public class MyOrderActivity extends BaseFragmentActivity {
    private final static String TAG = "MyOrderActivity";
    private ListView mListView;
    private NetworkHelper mNetworkHelper;
    private ErrorMessageFactory mErrorMessageFactory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_order);
        initView();
        initData();
        initListener();
    }

    private void initListener() {
        mNetworkHelper.setUiDataListener(new UIDataListener<Bean>() {
            @Override
            public void onDataChanged(Bean data) {
                System.out.println(data.getResult());
                mResponseCode = CodeParams.SUCCESS;
            }

            @Override
            public void onErrorHappened(int errorCode, String errorMessage) {
                LogUtil.e(TAG, errorMessage);
                mResponseCode = errorCode;

                String message = mErrorMessageFactory.get(errorCode);
                mToast.show(message);
            }
        });
    }

    private void initData() {
        mErrorMessageFactory = new ErrorMessageFactory(this);
        mNetworkHelper = new NetworkHelper(MyOrderActivity.this);
        Map<String, String> params = new HashMap<>();
        mResponseCode = 0;
        mNetworkHelper.sendPostRequest(UrlParams.GET_ORDERS_URL, params);
        mListView.setAdapter(new MyOrderAdapter(MyOrderActivity.this));
    }

    private void initView() {
        mListView = (ListView) findViewById(R.id.my_order_listView);
    }
}
