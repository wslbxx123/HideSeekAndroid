package dlmj.hideseek.UI.Adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

import dlmj.hideseek.Common.Model.ExchangeOrder;


/**
 * Created by Two on 11/10/2016.
 */
public class ExchangeOrderAdapter extends BaseAdapter {
    private List<ExchangeOrder> mExchangeOrderList;
    private Context mContext;

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
    public View getView(int i, View view, ViewGroup viewGroup) {
        return null;
    }
}
