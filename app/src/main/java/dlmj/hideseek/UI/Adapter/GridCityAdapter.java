package dlmj.hideseek.UI.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import dlmj.hideseek.Common.Model.DomesticCity;
import dlmj.hideseek.R;

/**
 * Created by Two on 5/12/16.
 */
public class GridCityAdapter extends BaseAdapter {
    private Context mContext;
    private List<DomesticCity> mCityList;

    public GridCityAdapter(Context context, List<DomesticCity> cityList) {
        this.mContext = context;
        this.mCityList = cityList;
    }

    @Override
    public int getCount() {
        if(mCityList != null) {
            return mCityList.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return mCityList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(mContext);

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.grid_city_item, null);
            viewHolder = new ViewHolder();
            viewHolder.mCityTextView = (TextView) convertView.findViewById(R.id.cityTextView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.mCityTextView.setText(mCityList.get(position).getName());

        return convertView;
    }

    class ViewHolder {
        TextView mCityTextView;
    }
}
