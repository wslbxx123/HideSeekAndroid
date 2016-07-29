package dlmj.hideseek.UI.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import java.util.List;

import dlmj.hideseek.BusinessLogic.Cache.ImageCacheManager;
import dlmj.hideseek.Common.Model.Shop;
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
public class ShopAdapter extends BaseAdapter implements View.OnClickListener {
    private Context mContext;
    private List<Shop.ProductsEntity> mList;
    private ImageLoader mImageLoader;
    private TextView mNum;
    private TextView mTotal;

    public ShopAdapter(Context context, List<Shop.ProductsEntity> list) {
        this.mContext = context;
        this.mList = list;
        this.mImageLoader = ImageCacheManager.getInstance(context).getImageLoader();
    }

    @Override
    public int getCount() {
        if (mList != null) {
            return mList.size();
        }
        return 0;
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
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = View.inflate(mContext, R.layout.view_shop_content, null);
            holder.product_name = (TextView) convertView.findViewById(R.id.product_name);
            holder.product_image_url = (NetworkImageView) convertView.findViewById(R.id.product_image_url);
            holder.price = (TextView) convertView.findViewById(R.id.price);
            holder.purchase_count = (TextView) convertView.findViewById(R.id.purchase_count);
            holder.introduction = (TextView) convertView.findViewById(R.id.introduction);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Shop.ProductsEntity productsEntity = mList.get(position);
        holder.product_name.setText(productsEntity.product_name);
        holder.product_image_url.setImageUrl(productsEntity.product_image_url, mImageLoader);
        holder.product_image_url.setDefaultImageResId(R.drawable.hsbomb);
        holder.price.setText(productsEntity.price);
        holder.purchase_count.setText(productsEntity.purchase_count);
        holder.introduction.setText(productsEntity.introduction);

        initListener(convertView,productsEntity);
        return convertView;
    }

    private void initListener(View convertView, final Shop.ProductsEntity productsEntity) {
        Button btn_buy = (Button) convertView.findViewById(R.id.btn_buy);
        btn_buy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //弹出对话框
                showDialog(productsEntity);
            }
        });
    }

    private void showDialog(Shop.ProductsEntity productsEntity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        View view = View.inflate(mContext, R.layout.view_shop_buy_dialog, null);
        builder.setView(view);

        //初始化控件
        TextView name = (TextView) view.findViewById(R.id.buy_dialog_name);
        mNum = (TextView) view.findViewById(R.id.buy_dialog_num);
        ImageView upArrows = (ImageView) view.findViewById(R.id.buy_dialog_up_arrows);
        ImageView downArrows = (ImageView) view.findViewById(R.id.buy_dialog_down_arrows);
        mTotal = (TextView) view.findViewById(R.id.buy_dialog_middle);
        Button ensurePay = (Button) view.findViewById(R.id.buy_dialog_btn);

        name.setText(productsEntity.product_name);
        upArrows.setOnClickListener(this);
        downArrows.setOnClickListener(this);
        ensurePay.setOnClickListener(this);
        builder.create().show();
    }

    @Override
    public void onClick(View v) {
        int i = 1;
        switch (v.getId()) {
            case R.id.buy_dialog_up_arrows:
                i = Integer.parseInt(mNum.getText().toString());
                mNum.setText((i+1)+"");
                mTotal.setText((i+1)*5+"");
                break;
            case R.id.buy_dialog_down_arrows:
                i = Integer.parseInt(mNum.getText().toString());
                if (i>1) {
                    mNum.setText((i-1)+"");
                    mTotal.setText((i-1)*5+"");
                }
                break;
            case R.id.buy_dialog_btn:

                break;
        }
    }

    static class ViewHolder {
        TextView product_name;
        NetworkImageView product_image_url;
        TextView price;
        TextView purchase_count;
        TextView introduction;
    }
}
