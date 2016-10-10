package dlmj.hideseek.UI.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import dlmj.hideseek.Common.Factory.GoalImageFactory;
import dlmj.hideseek.Common.Model.Record;
import dlmj.hideseek.R;

/**
 * Created by Two on 4/30/16.
 */
public class RecordAdapter extends BaseAdapter{
    private Context mContext;
    private List<Record> mRecords;
    private GoalImageFactory mGoalImageFactory;

    public RecordAdapter(Context context, List<Record> records) {
        this.mContext = context;
        this.mGoalImageFactory = new GoalImageFactory(mContext);
        this.mRecords = records;
    }

    @Override
    public int getCount() {
        if(mRecords != null) {
            return mRecords.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return mRecords.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if(convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(R.layout.record_info, null, false);
            viewHolder = new ViewHolder();
            viewHolder.mDateTextView = (TextView) convertView.findViewById(R.id.dateTextView);
            viewHolder.mTimeTextView = (TextView) convertView.findViewById(R.id.timeTextView);
            viewHolder.mSearchTypeImageView = (ImageView) convertView.findViewById(R.id.searchTypeImageView);
            viewHolder.mScoreTextView = (TextView) convertView.findViewById(R.id.scoreTextView);
            viewHolder.mDateLayout = (LinearLayout) convertView.findViewById(R.id.dateLayout);
            convertView.setTag(viewHolder);
        } else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Record record = (Record)getItem(position);
        viewHolder.mDateTextView.setText(record.getDate());
        viewHolder.mTimeTextView.setText(record.getTime());
        viewHolder.mScoreTextView.setText(record.getScore() > 0 ?
            " " + record.getScore() : record.getScore() + "");

        if(position == 0 || !((Record)getItem(position - 1)).getDate().equals(record.getDate())) {
            viewHolder.mDateLayout.setVisibility(View.VISIBLE);
        } else {
            viewHolder.mDateLayout.setVisibility(View.GONE);
        }

        viewHolder.mSearchTypeImageView.setImageResource(
                mGoalImageFactory.get(record.getGoalType(),
                        record.getShowTypeName()));

        return convertView;
    }

    class ViewHolder{
        TextView mDateTextView;
        TextView mTimeTextView;
        ImageView mSearchTypeImageView;
        TextView mScoreTextView;
        LinearLayout mDateLayout;
    }
}
