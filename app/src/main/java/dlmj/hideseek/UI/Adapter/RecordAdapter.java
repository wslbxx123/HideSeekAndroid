package dlmj.hideseek.UI.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import dlmj.hideseek.Common.Model.Record;
import dlmj.hideseek.R;

/**
 * Created by Two on 4/30/16.
 */
public class RecordAdapter extends BaseAdapter{
    private Context mContext;
    private List<Record> mRecords;

    public RecordAdapter(Context context, List<Record> records) {
        mContext = context;
        mRecords = records;
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
            viewHolder.mRecordItemListView = (ListView) convertView.findViewById(R.id.recordItemListView);
            convertView.setTag(viewHolder);
        } else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Record record = (Record)getItem(position);
        RecordItemAdapter recordItemAdapter = new RecordItemAdapter(mContext, record.getRecordItems());
        viewHolder.mDateTextView.setText(record.getDate());
        viewHolder.mRecordItemListView.setAdapter(recordItemAdapter);

        int totalHeight = 0;
        for(int i = 0; i < recordItemAdapter.getCount(); i++) {
            View listItem = recordItemAdapter.getView(i, null, viewHolder.mRecordItemListView);
            if(listItem != null) {
                listItem.measure(0, 0);
                totalHeight += listItem.getMeasuredHeight();
            }
        }

        ViewGroup.LayoutParams params = viewHolder.mRecordItemListView.getLayoutParams();
        params.height = totalHeight + (viewHolder.mRecordItemListView.getDividerHeight() *
                (recordItemAdapter.getCount() - 1));
        ((ViewGroup.MarginLayoutParams)params).setMargins(0, 10, 0, 10);
        viewHolder.mRecordItemListView.setLayoutParams(params);

        return convertView;
    }

    class ViewHolder{
        TextView mDateTextView;
        ListView mRecordItemListView;
    }
}
