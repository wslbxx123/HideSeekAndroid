package dlmj.hideseek.UI.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import java.util.List;

import dlmj.hideseek.BusinessLogic.Cache.ImageCacheManager;
import dlmj.hideseek.Common.Model.ExchangeOrder;
import dlmj.hideseek.R;


/**
 * Created by Two on 11/10/2016.
 */
public class ExchangeOrderAdapter extends BaseAdapter {
    private List<ExchangeOrder> mExchangeOrderList;
    private Context mContext;
    private ImageLoader mImageLoader;

    public ExchangeOrderAdapter(Context context, List<ExchangeOrder> exchangeOrderList){
        this.mExchangeOrderList = exchangeOrderList;
        this.mContext = context;
        this.mImageLoader = ImageCacheManager.getInstance(context).getImageLoader();
    }

    @Override
    public int getCount() {
        if (mExchangeOrderList != null) {
            return mExchangeOrderList.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return mExchangeOrderList.get(position);
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
            convertView = inflater.inflate(R.layout.exchange_order_item, null, false);
            viewHolder = new ViewHolder();
            viewHolder.mProductImageView = (NetworkImageView) convertView.findViewById(R.id.productImageView);
            viewHolder.mProductNameTextView = (TextView) convertView.findViewById(R.id.productNameTextView);
            viewHolder.mAmountTextView = (TextView) convertView.findViewById(R.id.amountTextView);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        ExchangeOrder exchangeOrder = (ExchangeOrder)getItem(position);
        viewHolder.mProductImageView.setImageUrl(exchangeOrder.getImageUrl(), mImageLoader);
        viewHolder.mProductNameTextView.setText(exchangeOrder.getRewardName());
        String score = String.format(mContext.getString(R.string.exchange_amount_title),
                exchangeOrder.getRecord() * exchangeOrder.getCount());
        viewHolder.mAmountTextView.setText(score);

        return convertView;
    }

    class ViewHolder {
        NetworkImageView mProductImageView;
        TextView mProductNameTextView;
        TextView mAmountTextView;
    }
}
