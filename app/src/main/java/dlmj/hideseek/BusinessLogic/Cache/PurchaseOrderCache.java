package dlmj.hideseek.BusinessLogic.Cache;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

import dlmj.hideseek.Common.Model.PurchaseOrder;
import dlmj.hideseek.Common.Util.LogUtil;
import dlmj.hideseek.DataAccess.PurchaseOrderTableManager;

/**
 * Created by Two on 22/10/2016.
 */
public class PurchaseOrderCache extends BaseCache<PurchaseOrder> {
    private static final String TAG = "PurchaseOrderCache";
    private static PurchaseOrderCache mInstance;
    private PurchaseOrderTableManager mPurchaseOrderTableManager;
    private long mVersion = 0;

    public static PurchaseOrderCache getInstance(Context context){
        synchronized (PurchaseOrderCache.class){
            if(mInstance == null){
                mInstance = new PurchaseOrderCache(context);
            }
        }
        return mInstance;
    }

    public PurchaseOrderCache(Context context) {
        super();
        mPurchaseOrderTableManager = PurchaseOrderTableManager.getInstance(context);
    }

    public List<PurchaseOrder> getList() {
        if(super.getList().size() > 0) {
            return mList;
        }

        mList = mPurchaseOrderTableManager.searchOrders();
        return mList;
    }

    public boolean getMoreOrders(int count, boolean hasLoaded) {
        List<PurchaseOrder> orderList = mPurchaseOrderTableManager.getMoreOrders(count, mVersion, hasLoaded);

        mList.addAll(orderList);
        return orderList.size() > 0;
    }

    public void setOrders(String orderStr) {
        saveOrders(orderStr);

        mList = mPurchaseOrderTableManager.searchOrders();
        mVersion = mPurchaseOrderTableManager.getVersion();
    }

    public void saveOrders(String orderStr) {
        List<PurchaseOrder> list = new LinkedList<>();
        try {
            JSONObject result = new JSONObject(orderStr);
            String order = result.getString("orders");

            long version = result.has("version") ? result.getLong("version") :
                    mPurchaseOrderTableManager.getVersion();
            long orderMinId = result.getLong("order_min_id");
            JSONArray orderList = new JSONArray(order);
            String orderInfoStr;
            for (int i = 0; i < orderList.length(); i++) {
                orderInfoStr = orderList.getString(i);
                JSONObject orderInfo = new JSONObject(orderInfoStr);

                list.add(new PurchaseOrder(
                                orderInfo.getLong("pk_id"),
                                orderInfo.getInt("status"),
                                orderInfo.getString("create_time"),
                                orderInfo.getString("update_time"),
                                orderInfo.getInt("count"),
                                orderInfo.getString("trade_no"),
                                orderInfo.getLong("store_id"),
                                orderInfo.getString("product_name"),
                                orderInfo.getString("product_image_url"),
                                orderInfo.getDouble("price"),
                                orderInfo.getInt("purchase_count"),
                                orderInfo.getString("introduction"),
                                orderInfo.getLong("version"))
                );
            }

            mPurchaseOrderTableManager.updateOrders(orderMinId, version, list);
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
