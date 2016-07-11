package dlmj.hideseek.UI.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import dlmj.hideseek.Common.Model.ForeignCity;
import dlmj.hideseek.Common.Util.PinYinUtil;
import dlmj.hideseek.R;

/**
 * Created by Two on 5/25/16.
 */
public class ForeignCityListAdapter extends BaseAdapter{
    private Context mContext;
    private List<ForeignCity> mCityList = new ArrayList<>();

    public ForeignCityListAdapter(Context context, List<ForeignCity> cityList) {
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
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        ViewHolder viewHolder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(R.layout.city_item, null);
            viewHolder = new ViewHolder();
            viewHolder.mAlphaTextView = (TextView) convertView.findViewById(R.id.alphaTextView);
            viewHolder.mNameTextView = (TextView) convertView.findViewById(R.id.nameTextVIew);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.mNameTextView.setText(mCityList.get(position).getName());
        String currentStr = PinYinUtil.getAlpha(mCityList.get(position).getName());
        String previewStr = (position - 1) >= 0 ? PinYinUtil.getAlpha(mCityList
                .get(position - 1).getName()) : " ";
        if (!previewStr.equals(currentStr)) {
            viewHolder.mAlphaTextView.setVisibility(View.VISIBLE);
            viewHolder.mAlphaTextView.setText(currentStr);
        } else {
            viewHolder.mAlphaTextView.setVisibility(View.GONE);
        }

        return convertView;
    }

    private class ViewHolder {
        TextView mAlphaTextView;
        TextView mNameTextView;
    }
}
