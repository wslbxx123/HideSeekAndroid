package dlmj.hideseek.UI.Adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import dlmj.hideseek.R;

/**
 * 创建者     ZPL
 * 创建时间   2016/8/2 19:47
 * 描述	      ${TODO}
 * <p/>
 * 更新者     $Author$
 * 更新时间   $Date$
 * 更新描述   ${TODO}
 */
public class MyOrderAdapter extends BaseAdapter {
    private Context mContext;
    public MyOrderAdapter(Context context) {
        this.mContext = context;
    }

    @Override
    public int getCount() {
        return 2;
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
        convertView = View.inflate(mContext, R.layout.my_order_item,null);
        return convertView;
    }
}
