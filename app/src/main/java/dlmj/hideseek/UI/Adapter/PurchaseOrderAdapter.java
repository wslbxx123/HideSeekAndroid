package dlmj.hideseek.UI.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import java.util.List;

import dlmj.hideseek.BusinessLogic.Cache.ImageCacheManager;
import dlmj.hideseek.Common.Model.PurchaseOrder;
import dlmj.hideseek.R;

/**
 * Created by Two on 11/10/2016.
 */
public class PurchaseOrderAdapter extends BaseAdapter {
    private List<PurchaseOrder> mPurchaseOrderList;
    private Context mContext;
    private ImageLoader mImageLoader;
    private OnPurchaseBtnClickedListener mOnPurchaseBtnClickedListener;

    public PurchaseOrderAdapter(Context context, List<PurchaseOrder> purchaseOrderList){
        this.mPurchaseOrderList = purchaseOrderList;
        this.mContext = context;
        this.mImageLoader = ImageCacheManager.getInstance(context).getImageLoader();
    }

    @Override
    public int getCount() {
        if (mPurchaseOrderList != null) {
            return mPurchaseOrderList.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return mPurchaseOrderList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(R.layout.purchase_order_item, null, false);
            viewHolder = new ViewHolder();
            viewHolder.mProductImageView = (NetworkImageView) convertView.findViewById(R.id.productImageView);
            viewHolder.mProductNameTextView = (TextView) convertView.findViewById(R.id.productNameTextView);
            viewHolder.mAmountTextView = (TextView) convertView.findViewById(R.id.amountTextView);
            viewHolder.mSuccessTextView = (TextView) convertView.findViewById(R.id.successTextView);
            viewHolder.mPayLayout = (LinearLayout) convertView.findViewById(R.id.payLayout);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        PurchaseOrder purchaseOrder = (PurchaseOrder)getItem(position);
        viewHolder.mProductImageView.setImageUrl(purchaseOrder.getImageUrl(), mImageLoader);
        viewHolder.mProductNameTextView.setText(purchaseOrder.getProductName());
        String price = String.format(mContext.getString(R.string.amount_title),
                purchaseOrder.getPrice() * purchaseOrder.getCount());
        viewHolder.mAmountTextView.setText(price);

        if(purchaseOrder.getStatus() == 0) {
            viewHolder.mSuccessTextView.setVisibility(View.GONE);
            viewHolder.mPayLayout.setVisibility(View.VISIBLE);
        } else {
            viewHolder.mSuccessTextView.setVisibility(View.VISIBLE);
            viewHolder.mPayLayout.setVisibility(View.GONE);
        }
        initListener(convertView, purchaseOrder);

        return convertView;
    }

    private void initListener(View convertView, final PurchaseOrder purchaseOrder) {
        Button payBtn = (Button) convertView.findViewById(R.id.payBtn);
        payBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //弹出对话框
                mOnPurchaseBtnClickedListener.purchaseBtnOnClicked(purchaseOrder);
            }
        });
    }

    public interface OnPurchaseBtnClickedListener {
        void purchaseBtnOnClicked(PurchaseOrder purchaseOrder);
    }

    public void setOnPurchaseBtnClickedListener(OnPurchaseBtnClickedListener onPurchaseBtnClickedListener) {
        this.mOnPurchaseBtnClickedListener = onPurchaseBtnClickedListener;
    }

    class ViewHolder {
        NetworkImageView mProductImageView;
        TextView mProductNameTextView;
        TextView mAmountTextView;
        TextView mSuccessTextView;
        LinearLayout mPayLayout;
    }
}
