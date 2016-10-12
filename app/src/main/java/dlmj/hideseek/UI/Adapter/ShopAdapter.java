package dlmj.hideseek.UI.Adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alipay.sdk.app.PayTask;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dlmj.hideseek.BusinessLogic.Cache.ImageCacheManager;
import dlmj.hideseek.BusinessLogic.Cache.UserCache;
import dlmj.hideseek.BusinessLogic.Network.NetworkHelper;
import dlmj.hideseek.Common.Interfaces.UIDataListener;
import dlmj.hideseek.Common.Model.Bean;
import dlmj.hideseek.Common.Model.CreateOrder;
import dlmj.hideseek.Common.Model.Shop;
import dlmj.hideseek.Common.Params.UrlParams;
import dlmj.hideseek.R;
import dlmj.hideseek.Common.Util.PayResult;

/**
 * 创建者     ZPL
 * 创建时间   2016/7/16 20:50
 * 描述	      ${TODO}
 * <p/>
 * 更新者     $Author$
 * 更新时间   $Date$
 * 更新描述   ${TODO}
 */
public class ShopAdapter extends BaseAdapter  {
    private Context mContext;
    private List<Shop.ProductsEntity> mList;
    private ImageLoader mImageLoader;
    private TextView mNum;
    private TextView mTotal;
    private int mPrice;
    private int mPosition;
    private NetworkHelper mNetworkHelper;
    private boolean mHasGuide;
    private int mMax_count;
    private AlertDialog mAlertDialog;
    private static final int SDK_PAY_FLAG = 1;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @SuppressWarnings("unused")
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SDK_PAY_FLAG: {
                    PayResult payResult = new PayResult((String) msg.obj);
                    /**
                     * 同步返回的结果必须放置到服务端进行验证（验证的规则请看https://doc.open.alipay.com/doc2/
                     * detail.htm?spm=0.0.0.0.xdvAU6&treeId=59&articleId=103665&
                     * docType=1) 建议商户依赖异步通知
                     */
                    String resultInfo = payResult.getResult();// 同步返回需要验证的信息

                    String resultStatus = payResult.getResultStatus();
                    // 判断resultStatus 为“9000”则代表支付成功，具体状态码代表含义可参考接口文档
                    if (TextUtils.equals(resultStatus, "9000")) {
                        Toast.makeText(mContext, "支付成功", Toast.LENGTH_SHORT).show();
                    } else {
                        // 判断resultStatus 为非"9000"则代表可能支付失败
                        // "8000"代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
                        if (TextUtils.equals(resultStatus, "8000")) {
                            Toast.makeText(mContext, "支付结果确认中", Toast.LENGTH_SHORT).show();
                        } else {
                            // 其他值就可以判断为支付失败，包括用户主动取消支付，或者系统返回的错误
                            Toast.makeText(mContext, "支付失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;
                }
                default:
                    break;
            }
        };
    };

    public ShopAdapter(Context context, List<Shop.ProductsEntity> list) {
        this.mContext = context;
        this.mList = list;
        mNetworkHelper = new NetworkHelper(context);
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

        Shop.ProductsEntity mProductsEntity = mList.get(position);
        holder.product_name.setText(mProductsEntity.product_name);
        holder.product_image_url.setImageUrl(mProductsEntity.product_image_url, mImageLoader);
        holder.product_image_url.setDefaultImageResId(R.drawable.hsbomb);
        holder.price.setText(mProductsEntity.price);
        holder.purchase_count.setText(mProductsEntity.purchase_count);
        holder.introduction.setText(mProductsEntity.introduction);
        initListener(convertView,mProductsEntity);
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

    private void showDialog(final Shop.ProductsEntity productsEntity) {
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

        //商品价格
        mPrice = Integer.parseInt(productsEntity.price);
        mTotal.setText(productsEntity.price);
        name.setText(productsEntity.product_name);

        upArrows.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i = Integer.parseInt(mNum.getText().toString());
                mNum.setText((i + 1) + "");
                mTotal.setText((i + 1) * mPrice + "");
            }
        });
        downArrows.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i = Integer.parseInt(mNum.getText().toString());
                if (i > 1) {
                    mNum.setText((i - 1) + "");
                    mTotal.setText((i - 1) * mPrice + "");
                }
            }
        });
        ensurePay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               int i = Integer.parseInt(mNum.getText().toString());
                //超过最大数量,则不允许购买
                if (i <= mMax_count) {
                    //创建订单
                    Map<String, String> params = new HashMap<>();
                    params.put("store_id", productsEntity.pk_id);
                    params.put("count", mNum.getText().toString());
                    System.out.println("count "+mNum.getText().toString()+"productsEntity.pk_id  "+productsEntity.pk_id);
                    mNetworkHelper.sendPostRequest(UrlParams.CREATE_ORDER_URL, params);

                    //关闭对话框
                    mAlertDialog.dismiss();
                } else {
                    Toast.makeText(mContext, R.string.buy_too_more, Toast.LENGTH_SHORT).show();
                }
            }
        });

        mHasGuide = UserCache.getInstance().getUser().getHasGuide();
        mMax_count = productsEntity.max_count;
        if (mPosition == 0 && mHasGuide) {
            //已拥有图鉴,无需再次购买

        } else {
            mAlertDialog = builder.create();
            mAlertDialog.show();
        }

        mNetworkHelper.setUiDataListener(new UIDataListener<Bean>() {
            @Override
            public void onDataChanged(Bean data) {
                String result = data.getResult();
                Gson gson = new Gson();
                CreateOrder order = gson.fromJson(result, CreateOrder.class);
                CreateOrder.ParamsEntity params = order.params;
                String sign = order.sign;
                //调用支付宝进行支付
                pay(sign,params);
            }

            @Override
            public void onErrorHappened(int errorCode, String errorMessage) {
                Toast.makeText(mContext,"网络错误",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void pay(String sign,CreateOrder.ParamsEntity params) {
        String orderInfo = getOrderInfo(params);

        /**
         * 完整的符合支付宝参数规范的订单信息
         */
        final String payInfo = orderInfo + "&sign=\"" + sign + "\"&" + getSignType();

        Runnable payRunnable = new Runnable() {
            @Override
            public void run() {
                // 构造PayTask 对象
                PayTask alipay = new PayTask((Activity)mContext);
                // 调用支付接口，获取支付结果
                String result = alipay.pay(payInfo, true);

                Message msg = new Message();
                msg.what = SDK_PAY_FLAG;
                msg.obj = result;
                mHandler.sendMessage(msg);
            }
        };
        // 必须异步调用
        Thread payThread = new Thread(payRunnable);
        payThread.start();
    }

    /**
     * create the order info. 创建订单信息
     */
    private String getOrderInfo(CreateOrder.ParamsEntity params) {

        // 服务接口名称， 固定值
       String orderInfo = "service="+params.service;

        // 签约合作者身份ID
        orderInfo += "&partner=" + params.partner;

        // 参数编码， 固定值
        orderInfo += "&_input_charset="+params._input_charset;

        // 服务器异步通知页面路径
        orderInfo += "&notify_url=" + params.notify_url;

        // 商户网站唯一订单号
        orderInfo += "&out_trade_no=" + params.out_trade_no;

        // 商品名称
        orderInfo += "&subject=" + params.subject;

        // 支付类型， 固定值
        orderInfo += "&payment_type=\"1\"";

        // 签约卖家支付宝账号
        orderInfo += "&seller_id=" + params.seller_id;

        // 商品金额
        orderInfo += "&total_fee=" + params.total_fee;

        // 商品详情
        orderInfo += "&body=" + params.body;

        // 设置未付款交易的超时时间
        // 默认30分钟，一旦超时，该笔交易就会自动被关闭。
        // 取值范围：1m～15d。
        // m-分钟，h-小时，d-天，1c-当天（无论交易何时创建，都在0点关闭）。
        // 该参数数值不接受小数点，如1.5h，可转换为90m。
        orderInfo += "&it_b_pay="+params.it_b_pay;
        // extern_token为经过快登授权获取到的alipay_open_id,带上此参数用户将使用授权的账户进行支付
        // orderInfo += "&extern_token=" + "\"" + extern_token + "\"";
        // 支付宝处理完请求后，当前页面跳转到商户指定页面的路径，可空
        orderInfo += "&show_url="+params.show_url;
        // 调用银行卡支付，需配置此参数，参与签名， 固定值 （需要签约《无线银行卡快捷支付》才能使用）
        // orderInfo += "&paymethod=\"expressGateway\"";
        return orderInfo;
    }

    /**
     * get the sign type we use. 获取签名方式
     *
     */
    private String getSignType() {
        return "sign_type=\"RSA\"";
    }

    static class ViewHolder {
        TextView product_name;
        NetworkImageView product_image_url;
        TextView price;
        TextView purchase_count;
        TextView introduction;
    }
}
