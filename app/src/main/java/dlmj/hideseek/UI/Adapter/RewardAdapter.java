package dlmj.hideseek.UI.Adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import java.util.List;

import dlmj.hideseek.BusinessLogic.Cache.ImageCacheManager;
import dlmj.hideseek.Common.Model.Reward;
import dlmj.hideseek.R;

/**
 * 创建者     ZPL
 * 创建时间   2016/7/23 19:05
 * 描述	      ${TODO}
 * <p/>
 * 更新者     $Author$
 * 更新时间   $Date$
 * 更新描述   ${TODO}
 */
public class RewardAdapter extends BaseAdapter {
    private Context mContext;
    private List<Reward.RewardEntity> mList;
    private ImageLoader mImageLoader;
    public RewardAdapter(Context context, List<Reward.RewardEntity> list) {
        this.mContext = context;
        this.mList = list;
        this.mImageLoader = ImageCacheManager.getInstance(context).getImageLoader();
    }

    @Override
    public int getCount() {
        if (mList!=null) {
            return mList.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = View.inflate(mContext, R.layout.view_shop_content,null);
            holder.product_name = (TextView) convertView.findViewById(R.id.product_name);
            holder.product_image_url = (NetworkImageView) convertView.findViewById(R.id.product_image_url);
            holder.price=(TextView) convertView.findViewById(R.id.price);
            holder.purchase_count=(TextView) convertView.findViewById(R.id.purchase_count);
            holder.introduction=(TextView) convertView.findViewById(R.id.introduction);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Reward.RewardEntity rewardEntity = mList.get(position);
        holder.product_name.setText(rewardEntity.reward_name);
        holder.product_image_url.setImageUrl(rewardEntity.reward_image_url,mImageLoader);
        //holder.product_image_url.setDefaultImageResId(R.drawable.hsbomb);
        holder.price.setText(rewardEntity.record);
        holder.purchase_count.setText(rewardEntity.exchange_count);
        holder.introduction.setText(rewardEntity.introduction);
        return convertView;
    }

    class ViewHolder {
        TextView product_name;
        NetworkImageView product_image_url;
        TextView price;
        TextView purchase_count;
        TextView introduction;
    }
}
