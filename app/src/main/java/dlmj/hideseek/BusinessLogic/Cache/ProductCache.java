package dlmj.hideseek.BusinessLogic.Cache;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

import dlmj.hideseek.Common.Model.Product;
import dlmj.hideseek.Common.Util.LogUtil;
import dlmj.hideseek.DataAccess.ProductTableManager;

/**
 * Created by Two on 22/10/2016.
 */
public class ProductCache extends BaseCache<Product> {
    private final static String TAG = "ProductCache";
    private static ProductCache mInstance;
    private ProductTableManager mProductTableManager;
    private long mVersion = 0;

    public static ProductCache getInstance(Context context){
        synchronized (ProductCache.class){
            if(mInstance == null){
                mInstance = new ProductCache(context);
            }
        }
        return mInstance;
    }

    public ProductCache(Context context) {
        super();
        mProductTableManager = ProductTableManager.getInstance(context);
    }

    public List<Product> getList() {
        if(super.getList().size() > 0) {
            return mList;
        }

        mList = mProductTableManager.searchProducts();
        return mList;
    }

    public boolean getMoreProducts(int count, boolean hasLoaded) {
        List<Product> productList = mProductTableManager.getMoreProducts(count, mVersion, hasLoaded);

        mList.addAll(productList);
        return productList.size() > 0;
    }

    public void setProducts(String productStr) {
        saveProducts(productStr);

        mList = mProductTableManager.searchProducts();
        mVersion = mProductTableManager.getVersion();
    }

    public void saveProducts(String productStr) {
        List<Product> list = new LinkedList<>();
        try {
            JSONObject result = new JSONObject(productStr);
            String product = result.getString("products");

            long version = result.has("version") ? result.getLong("version") :
                    mProductTableManager.getVersion();
            long productMinId = result.getLong("product_min_id");
            JSONArray productList = new JSONArray(product);
            String productInfoStr;
            for (int i = 0; i < productList.length(); i++) {
                productInfoStr = productList.getString(i);
                JSONObject productInfo = new JSONObject(productInfoStr);

                list.add(new Product(
                        productInfo.getLong("pk_id"),
                        productInfo.getString("product_name"),
                        productInfo.getString("product_image_url"),
                        productInfo.getDouble("price"),
                        productInfo.getInt("purchase_count"),
                        productInfo.getString("introduction"),
                        productInfo.getLong("version"))
                );
            }

            mProductTableManager.updateProducts(productMinId, version, list);
        } catch (JSONException e) {
            LogUtil.e(TAG, e.getMessage());
            e.printStackTrace();
        }
    }

    public void addProducts(String productStr) {
        saveProducts(productStr);

        getMoreProducts(10, true);
    }
}
