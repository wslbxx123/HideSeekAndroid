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
import dlmj.hideseek.Common.Factory.GoalImageFactory;
import dlmj.hideseek.Common.Factory.RaceGroupMessageFactory;
import dlmj.hideseek.Common.Model.RaceGroup;
import dlmj.hideseek.R;

/**
 * Created by Two on 5/31/16.
 */
public class RaceGroupAdapter extends BaseAdapter {
    private List<RaceGroup> mRaceGroupList;
    private Context mContext;
    private ImageLoader mImageLoader;
    private GoalImageFactory mGoalImageFactory;
    private RaceGroupMessageFactory mRaceGroupMessageFactory;

    public RaceGroupAdapter(Context context, List<RaceGroup> raceGroupList){
        this.mRaceGroupList = raceGroupList;
        this.mContext = context;
        this.mImageLoader = ImageCacheManager.getInstance(context).getImageLoader();
        this.mGoalImageFactory = new GoalImageFactory(mContext);
        this.mRaceGroupMessageFactory = new RaceGroupMessageFactory(mContext);
    }

    @Override
    public int getCount() {
        if (mRaceGroupList != null) {
            return mRaceGroupList.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return mRaceGroupList.get(position);
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
            convertView = inflater.inflate(R.layout.race_group_item, null, false);
            viewHolder = new ViewHolder();
            viewHolder.mPhotoImageView = (NetworkImageView) convertView.findViewById(R.id.photoImageView);
            viewHolder.mNameTextView = (TextView) convertView.findViewById(R.id.nameTextView);
            viewHolder.mGoalImageView = (ImageView) convertView.findViewById(R.id.goalImageView);
            viewHolder.mMessageTextView = (TextView) convertView.findViewById(R.id.messageTextView);
            viewHolder.mScoreTextView = (TextView) convertView.findViewById(R.id.scoreTextView);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        RaceGroup raceGroup = (RaceGroup)getItem(position);
        viewHolder.mPhotoImageView
                .setImageUrl(raceGroup.getPhotoUrl(), mImageLoader);
        viewHolder.mPhotoImageView.setDefaultImageResId(R.drawable.default_photo);
        viewHolder.mNameTextView.setText(raceGroup.getNickname());

        viewHolder.mGoalImageView.setImageResource(
                mGoalImageFactory.get(raceGroup.getRecordItem().getGoalType(),
                        raceGroup.getRecordItem().getShowTypeName()));
        viewHolder.mMessageTextView.setText(
                mRaceGroupMessageFactory.get(raceGroup.getRecordItem().getGoalType(),
                        raceGroup.getRecordItem().getShowTypeName()));

        viewHolder.mScoreTextView.setText(String.format(mContext.getString(R.string.score_title),
                raceGroup.getRecordItem().getScoreSum()));
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
