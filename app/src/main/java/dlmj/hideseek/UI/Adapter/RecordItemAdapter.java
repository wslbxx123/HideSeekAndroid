package dlmj.hideseek.UI.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import dlmj.hideseek.Common.Model.Record;
import dlmj.hideseek.Common.Model.RecordItem;
import dlmj.hideseek.R;

/**
 * Created by Two on 5/1/16.
 */
public class RecordItemAdapter extends BaseAdapter {
    private Context mContext;
    private List<RecordItem> mRecordItems;

    public RecordItemAdapter(Context context, List<RecordItem> recordItems) {
        mContext = context;
        mRecordItems = recordItems;
    }

    @Override
    public int getCount() {
        if(mRecordItems != null) {
            return mRecordItems.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return mRecordItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        ViewHolder viewHolder;

        if(convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(R.layout.record_info_item, null, false);
            viewHolder = new ViewHolder();
            viewHolder.mTimeTextView = (TextView) convertView.findViewById(R.id.timeTextView);
            viewHolder.mSearchTypeImageView = (ImageView) convertView.findViewById(R.id.searchTypeImageView);
            viewHolder.mScoreTextView = (TextView) convertView.findViewById(R.id.scoreTextView);
            convertView.setTag(viewHolder);
        } else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        RecordItem recordItem = (RecordItem)getItem(position);
        viewHolder.mTimeTextView.setText(recordItem.getTime());
        viewHolder.mScoreTextView.setText(recordItem.getScore() + "");

        switch (recordItem.getGoalType()) {
            case bomb:
                viewHolder.mSearchTypeImageView.setImageResource(R.drawable.bomb);
                break;
            case mushroom:
                viewHolder.mSearchTypeImageView.setImageResource(R.drawable.mushroom);
                break;
            case monster:
                viewHolder.mSearchTypeImageView.setImageResource(R.drawable.monster);
                break;
            default:
                break;
        }
        return convertView;
    }

    class ViewHolder{
        private TextView mTimeTextView;
        private ImageView mSearchTypeImageView;
        private TextView mScoreTextView;
    }
}
