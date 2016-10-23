package dlmj.hideseek.UI.Adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alipay.sdk.app.PayTask;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dlmj.hideseek.BusinessLogic.Cache.ImageCacheManager;
import dlmj.hideseek.BusinessLogic.Cache.UserCache;
import dlmj.hideseek.BusinessLogic.Network.NetworkHelper;
import dlmj.hideseek.Common.Interfaces.UIDataListener;
import dlmj.hideseek.Common.Model.Bean;
import dlmj.hideseek.Common.Model.CreateOrder;
import dlmj.hideseek.Common.Model.Product;
import dlmj.hideseek.Common.Params.UrlParams;
import dlmj.hideseek.R;
import dlmj.hideseek.Common.Util.PayResult;

/**
 * 创建者     ZPL
 * 创建时间   2016/7/16 20:50
 * 描述	      ${TODO}
 * <p/>
 * 更新者     $Author$
 * 更新时间   $Date$
 * 更新描述   ${TODO}
 */
public class ProductAdapter extends BaseAdapter  {
    private Context mContext;
    private List<Product> mProductList;
    private ImageLoader mImageLoader;
    private OnPurchaseBtnClickedListener mOnPurchaseBtnClickedListener;

    public ProductAdapter(Context context, List<Product> productList) {
        this.mContext = context;
        this.mProductList = productList;
        this.mImageLoader = ImageCacheManager.getInstance(context).getImageLoader();
    }

    @Override
    public int getCount() {
        if (mProductList != null) {
            return mProductList.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return mProductList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = View.inflate(mContext, R.layout.product_item, null);
            holder.mProductNameTextView = (TextView) convertView.findViewById(R.id.productNameTextView);
            holder.mProductImageView = (NetworkImageView) convertView.findViewById(R.id.productImageView);
            holder.mPriceTextView = (TextView) convertView.findViewById(R.id.priceTextView);
            holder.mPurchaseCountTextView = (TextView) convertView.findViewById(R.id.purchaseCountTextView);
            holder.mIntroductionTextView = (TextView) convertView.findViewById(R.id.introductionTextView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Product product = mProductList.get(position);
        holder.mProductNameTextView.setText(product.getName());
        holder.mProductImageView.setImageUrl(product.getImageUrl(), mImageLoader);
        holder.mPriceTextView.setText(product.getPrice() + "");
        holder.mPurchaseCountTextView.setText(product.getPurchaseCount() + "");
        holder.mIntroductionTextView.setText(product.getIntroduction());
        initListener(convertView, product);
        return convertView;
    }

    private void initListener(View convertView, final Product product) {
        Button btn_buy = (Button) convertView.findViewById(R.id.buyBtn);
        btn_buy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //弹出对话框
                mOnPurchaseBtnClickedListener.purchaseBtnOnClicked(product);
            }
        });
    }

    public interface OnPurchaseBtnClickedListener {
        void purchaseBtnOnClicked(Product product);
    }

    public void setOnPurchaseBtnClickedListener(OnPurchaseBtnClickedListener onPurchaseBtnClickedListener) {
        this.mOnPurchaseBtnClickedListener = onPurchaseBtnClickedListener;
    }

    static class ViewHolder {
        TextView mProductNameTextView;
        NetworkImageView mProductImageView;
        TextView mPriceTextView;
        TextView mPurchaseCountTextView;
        TextView mIntroductionTextView;
    }
}
