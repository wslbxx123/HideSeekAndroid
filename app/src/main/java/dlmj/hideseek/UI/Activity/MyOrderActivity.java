package dlmj.hideseek.UI.Activity;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

import java.util.HashMap;
import java.util.Map;

import dlmj.hideseek.BusinessLogic.Network.NetworkHelper;
import dlmj.hideseek.Common.Interfaces.UIDataListener;
import dlmj.hideseek.Common.Model.Bean;
import dlmj.hideseek.Common.Params.UrlParams;
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
public class MyOrderActivity extends Activity{

    private ListView mListView;
    private NetworkHelper mNetworkHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_order);
        initView();
        initData();
        initListener();
    }

    private void initListener() {
        mNetworkHelper.setUiDataListener(new UIDataListener<Bean>() {
            @Override
            public void onDataChanged(Bean data) {
                //System.out.println(data.getResult());
                mListView.setAdapter(new MyOrderAdapter(MyOrderActivity.this));
            }

            @Override
            public void onErrorHappened(int errorCode, String errorMessage) {

            }
        });
    }

    private void initData() {
        mNetworkHelper = new NetworkHelper(MyOrderActivity.this);
        Map<String, String> params = new HashMap<>();
        mNetworkHelper.sendPostRequest(UrlParams.GET_ORDERS_URL, params);
    }

    private void initView() {
        mListView = (ListView) findViewById(R.id.my_order_listView);
    }
}
