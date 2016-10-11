package dlmj.hideseek.UI.Adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

import dlmj.hideseek.Common.Model.PurchaseOrder;

/**
 * Created by Two on 11/10/2016.
 */
public class PurchaseOrderAdapter extends BaseAdapter {
    private List<PurchaseOrder> mPurchaseOrderList;
    private Context mContext;

    @Override
    public int getCount() {
        if (mPurchaseOrderList != null) {
            return mPurchaseOrderList.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return mPurchaseOrderList.get(position);
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
