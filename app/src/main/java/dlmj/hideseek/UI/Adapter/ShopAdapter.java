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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import dlmj.hideseek.BusinessLogic.Cache.ImageCacheManager;
import dlmj.hideseek.BusinessLogic.Cache.UserCache;
import dlmj.hideseek.BusinessLogic.Network.NetworkHelper;
import dlmj.hideseek.Common.Interfaces.UIDataListener;
import dlmj.hideseek.Common.Model.Bean;
import dlmj.hideseek.Common.Model.CreateOrder;
import dlmj.hideseek.Common.Model.Shop;
import dlmj.hideseek.Common.Params.UrlParams;
import dlmj.hideseek.R;
import dlmj.hideseek.Util.PayResult;

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
    private int mPrice;
    private int mPosition;
    private NetworkHelper mNetworkHelper;
    private int mGuideNum;
    private int mMax_count;

    // 商户PID
    public static final String PARTNER = "2088421519055042";
    // 商户收款账号
    public static final String SELLER = "wslbxx@hotmail.com";
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
    private Shop.ProductsEntity mProductsEntity;
    private AlertDialog mAlertDialog;

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
        mPosition = position;
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

        mProductsEntity = mList.get(position);
        holder.product_name.setText(mProductsEntity.product_name);
        holder.product_image_url.setImageUrl(mProductsEntity.product_image_url, mImageLoader);
        holder.product_image_url.setDefaultImageResId(R.drawable.hsbomb);
        holder.price.setText(mProductsEntity.price);
        holder.purchase_count.setText(mProductsEntity.purchase_count);
        holder.introduction.setText(mProductsEntity.introduction);

        initListener(convertView, mProductsEntity);
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

        //商品价格
        mPrice = Integer.parseInt(productsEntity.price);
        mTotal.setText(productsEntity.price);
        name.setText(productsEntity.product_name);
        upArrows.setOnClickListener(this);
        downArrows.setOnClickListener(this);
        ensurePay.setOnClickListener(this);
        mGuideNum = Integer.parseInt(UserCache.getInstance().getUser().has_guide);
        mMax_count = productsEntity.max_count;
        if (mPosition == 0 && mGuideNum > 0) {
            //已拥有图鉴,无需再次购买

        } else {
            mAlertDialog = builder.create();
            mAlertDialog.show();
        }
    }

    @Override
    public void onClick(View v) {
        int i = 1;
        //商品价格
        switch (v.getId()) {
            case R.id.buy_dialog_up_arrows:
                i = Integer.parseInt(mNum.getText().toString());
                mNum.setText((i + 1) + "");
                mTotal.setText((i + 1) * mPrice + "");
                break;
            case R.id.buy_dialog_down_arrows:
                i = Integer.parseInt(mNum.getText().toString());
                if (i > 1) {
                    mNum.setText((i - 1) + "");
                    mTotal.setText((i - 1) * mPrice + "");
                }
                break;
            case R.id.buy_dialog_btn:
                i = Integer.parseInt(mNum.getText().toString());
                //超过最大数量,则不允许购买
                if (i <= mMax_count) {
                    //创建订单
                    Map<String, String> params = new HashMap<>();
                    params.put("store_id", mPosition + "");
                    params.put("count", mNum.getText().toString());
                    mNetworkHelper.sendPostRequest(UrlParams.CREATE_ORDER_URL, params);
                } else {
                    Toast.makeText(mContext, "土豪,买一个就够啦!", Toast.LENGTH_SHORT).show();
                }
                //关闭对话框
                mAlertDialog.dismiss();
                break;
        }
        mNetworkHelper.setUiDataListener(new UIDataListener<Bean>() {
            @Override
            public void onDataChanged(Bean data) {
                String result = data.getResult();
                Gson gson = new Gson();
                System.out.println(result);
                CreateOrder order = gson.fromJson(result, CreateOrder.class);
                CreateOrder.ParamsEntity params = order.params;
                String sign = order.sign;
                //调用支付宝进行支付
                pay(mProductsEntity.product_name,mProductsEntity.introduction,mTotal.getText().toString(),sign,params);
            }

            @Override
            public void onErrorHappened(int errorCode, String errorMessage) {
                Toast.makeText(mContext,"网络错误",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void pay(String subject, String body, String price,String sign,CreateOrder.ParamsEntity params) {
        String orderInfo = getOrderInfo(subject, body, price,params);

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
    private String getOrderInfo(String subject, String body, String price,CreateOrder.ParamsEntity params) {

        // 服务接口名称， 固定值
       String orderInfo = "&service="+params.service;

        // 签约合作者身份ID
        orderInfo += "partner=" + params.partner;

        // 参数编码， 固定值
        orderInfo += "&_input_charset="+params._input_charset;

        // 服务器异步通知页面路径
        orderInfo += "&notify_url=" + params.notify_url;

        // 商户网站唯一订单号
        orderInfo += "&out_trade_no=" + params.out_trade_no;

        // 商品名称
        orderInfo += "&subject=" + "\"" + subject + "\"";

        // 支付类型， 固定值
        orderInfo += "&payment_type=\"1\"";

        // 签约卖家支付宝账号
        orderInfo += "&seller_id=" + params.seller_id;

        // 商品金额
        orderInfo += "&total_fee=" + "\"" + price + "\"";

        // 商品详情
        orderInfo += "&body=" + "\"" + body + "\"";

        // 设置未付款交易的超时时间
        // 默认30分钟，一旦超时，该笔交易就会自动被关闭。
        // 取值范围：1m～15d。
        // m-分钟，h-小时，d-天，1c-当天（无论交易何时创建，都在0点关闭）。
        // 该参数数值不接受小数点，如1.5h，可转换为90m。
        orderInfo += "&it_b_pay="+params.it_b_pay;

        // extern_token为经过快登授权获取到的alipay_open_id,带上此参数用户将使用授权的账户进行支付
        // orderInfo += "&extern_token=" + "\"" + extern_token + "\"";

        // 支付宝处理完请求后，当前页面跳转到商户指定页面的路径，可空
        orderInfo += "&return_url="+params.show_url;

        // 调用银行卡支付，需配置此参数，参与签名， 固定值 （需要签约《无线银行卡快捷支付》才能使用）
        // orderInfo += "&paymethod=\"expressGateway\"";

        return orderInfo;
    }

    /**
     * get the out_trade_no for an order. 生成商户订单号，该值在商户端应保持唯一（可自定义格式规范）
     *
     */
    private String getOutTradeNo() {
        SimpleDateFormat format = new SimpleDateFormat("MMddHHmmss", Locale.getDefault());
        Date date = new Date();
        String key = format.format(date);

        Random r = new Random();
        key = key + r.nextInt();
        key = key.substring(0, 15);
        return key;
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
