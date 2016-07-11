package dlmj.hideseek.UI.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import dlmj.hideseek.Common.Model.DomesticCity;
import dlmj.hideseek.R;

/**
 * Created by Two on 5/11/16.
 */
public class RegionResultListAdapter extends BaseAdapter {
    private Context mContext;
    private List<DomesticCity> mCities = new ArrayList<DomesticCity>();

    public RegionResultListAdapter(Context context, List<DomesticCity> cities) {
        this.mCities = cities;
        this.mContext = context;
    }

    @Override
    public int getCount() {
        if(mCities != null) {
            return mCities.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return mCities.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(R.layout.city_item, null);
            viewHolder = new ViewHolder();
            viewHolder.mNameTextView = (TextView) convertView.findViewById(R.id.nameTextVIew);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.mNameTextView.setText(mCities.get(position).getName());
        return convertView;
    }

    class ViewHolder {
        TextView mNameTextView;
    }
}
