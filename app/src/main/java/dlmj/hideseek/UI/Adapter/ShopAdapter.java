package dlmj.hideseek.UI.Adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

import dlmj.hideseek.R;

/**
 * 创建者     ZPL
 * 创建时间   2016/7/16 20:50
 * 描述	      ${TODO}
 * <p/>
 * 更新者     $Author$
 * 更新时间   $Date$
 * 更新描述   ${TODO}
 */
public class ShopAdapter extends BaseAdapter {
    private Context mContext;
    private List mList;
    public ShopAdapter(Context context, List list) {
        this.mContext = context;
        this.mList = list;
    }
    @Override
    public int getCount() {
        return 8;
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
        View view = View.inflate(mContext, R.layout.view_shop_content,null);
        return view;
    }
}
