package dlmj.hideseek.UI.Fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import java.util.ArrayList;
import java.util.List;

import dlmj.hideseek.R;
import dlmj.hideseek.UI.Adapter.ShopAdapter;

/**
 * 创建者     ZPL
 * 创建时间   2016/7/16 20:20
 * 描述	      ${TODO}
 * <p/>
 * 更新者     $Author$
 * 更新时间   $Date$
 * 更新描述   ${TODO}
 */
public class ShopFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = View.inflate(getContext(), R.layout.fragment_shop, null);
        GridView gridView = (GridView) view.findViewById(R.id.gridView);
        List list = new ArrayList();
        gridView.setAdapter(new ShopAdapter(getContext(),list));
        return view;
    }

}
