package dlmj.hideseek.UI.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import java.util.List;

import dlmj.hideseek.BusinessLogic.Cache.ImageCacheManager;
import dlmj.hideseek.Common.Model.ExchangeOrder;
import dlmj.hideseek.Common.Model.PurchaseOrder;
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
            convertView = inflater.inflate(R.layout.my_order_item, null, false);
            viewHolder = new ViewHolder();
//            viewHolder.mPhotoImageView = (NetworkImageView) convertView.findViewById(R.id.photoImageView);
//            viewHolder.mNameTextView = (TextView) convertView.findViewById(R.id.nameTextView);
//            viewHolder.mGoalImageView = (ImageView) convertView.findViewById(R.id.goalImageView);
//            viewHolder.mMessageTextView = (TextView) convertView.findViewById(R.id.messageTextView);
//            viewHolder.mScoreTextView = (TextView) convertView.findViewById(R.id.scoreTextView);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        return convertView;
    }

    class ViewHolder {
        NetworkImageView mPhotoImageView;
        TextView mNameTextView;
        ImageView mGoalImageView;
        TextView mMessageTextView;
        TextView mScoreTextView;
    }
}
