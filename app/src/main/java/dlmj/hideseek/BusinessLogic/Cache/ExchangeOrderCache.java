package dlmj.hideseek.BusinessLogic.Cache;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

import dlmj.hideseek.Common.Model.ExchangeOrder;
import dlmj.hideseek.Common.Util.LogUtil;
import dlmj.hideseek.DataAccess.ExchangeOrderTableManager;

/**
 * Created by Two on 22/10/2016.
 */
public class ExchangeOrderCache extends BaseCache<ExchangeOrder> {
    private static final String TAG = "ExchangeOrderCache";
    private static ExchangeOrderCache mInstance;
    private ExchangeOrderTableManager mExchangeOrderTableManager;
    private long mVersion = 0;

    public static ExchangeOrderCache getInstance(Context context){
        synchronized (ExchangeOrderCache.class){
            if(mInstance == null){
                mInstance = new ExchangeOrderCache(context);
            }
        }
        return mInstance;
    }

    public ExchangeOrderCache(Context context) {
        super();
        mExchangeOrderTableManager = ExchangeOrderTableManager.getInstance(context);
    }

    public List<ExchangeOrder> getList() {
        if(super.getList().size() > 0) {
            return mList;
        }

        mList = mExchangeOrderTableManager.searchOrders();
        return mList;
    }

    public boolean getMoreOrders(int count, boolean hasLoaded) {
        List<ExchangeOrder> orderList = mExchangeOrderTableManager.getMoreOrders(count, mVersion, hasLoaded);

        mList.addAll(orderList);
        return orderList.size() > 0;
    }

    public void setOrders(String orderStr) {
        saveOrders(orderStr);

        mList = mExchangeOrderTableManager.searchOrders();
        mVersion = mExchangeOrderTableManager.getVersion();
    }

    public void saveOrders(String orderStr) {
        List<ExchangeOrder> list = new LinkedList<>();
        try {
            JSONObject result = new JSONObject(orderStr);
            String order = result.getString("orders");

            long version = result.has("version") ? result.getLong("version") :
                    mExchangeOrderTableManager.getVersion();
            long orderMinId = result.getLong("order_min_id");
            JSONArray orderList = new JSONArray(order);
            String orderInfoStr;
            for (int i = 0; i < orderList.length(); i++) {
                orderInfoStr = orderList.getString(i);
                JSONObject orderInfo = new JSONObject(orderInfoStr);

                list.add(new ExchangeOrder(
                                orderInfo.getLong("pk_id"),
                                orderInfo.getInt("status"),
                                orderInfo.getString("create_time"),
                                orderInfo.getString("update_time"),
                                orderInfo.getInt("count"),
                                orderInfo.getLong("reward_id"),
                                orderInfo.getString("reward_name"),
                                orderInfo.getString("reward_image_url"),
                                orderInfo.getInt("record"),
                                orderInfo.getInt("exchange_count"),
                                orderInfo.getString("introduction"),
                                orderInfo.getLong("version"))
                );
            }

            mExchangeOrderTableManager.updateOrders(orderMinId, version, list);
        } catch (JSONException e) {
            LogUtil.e(TAG, e.getMessage());
            e.printStackTrace();
        }
    }

    public void addOrders(String orderStr) {
        saveOrders(orderStr);

        getMoreOrders(10, true);
    }
}
