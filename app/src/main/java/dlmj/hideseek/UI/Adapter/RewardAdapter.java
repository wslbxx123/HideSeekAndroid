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
 * Created by Two on 5/31/16.
 */
public class RewardAdapter extends BaseAdapter {
    private Context mContext;
    private List<Reward> mRewardList;
    private ImageLoader mImageLoader;

    public RewardAdapter(Context context, List<Reward> rewardList) {
        this.mContext = context;
        this.mRewardList = rewardList;
        this.mImageLoader = ImageCacheManager.getInstance(context).getImageLoader();
    }

    @Override
    public int getCount() {
        if (mRewardList != null) {
            return mRewardList.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return mRewardList.get(position);
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
            convertView = View.inflate(mContext, R.layout.reward_item,null);
            holder.mProductNameTextView = (TextView) convertView.findViewById(R.id.rewardNameTextView);
            holder.mProductImageView = (NetworkImageView) convertView.findViewById(R.id.productImageView);
            holder.mRecordTextView = (TextView) convertView.findViewById(R.id.recordTextView);
            holder.mExchangeCountTextView = (TextView) convertView.findViewById(R.id.exchangeCountTextView);
            holder.mIntroductionTextView = (TextView) convertView.findViewById(R.id.introductionTextView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Reward reward = mRewardList.get(position);
        holder.mProductNameTextView.setText(reward.getName());
        holder.mProductImageView.setImageUrl(reward.getImageUrl(), mImageLoader);
        holder.mRecordTextView.setText(reward.getRecord() + "");
        holder.mExchangeCountTextView.setText(reward.getExchangeCount() + "");
        holder.mIntroductionTextView.setText(reward.getIntroduction());
        return convertView;
    }

    class ViewHolder {
        TextView mProductNameTextView;
        NetworkImageView mProductImageView;
        TextView mRecordTextView;
        TextView mExchangeCountTextView;
        TextView mIntroductionTextView;
    }
}
