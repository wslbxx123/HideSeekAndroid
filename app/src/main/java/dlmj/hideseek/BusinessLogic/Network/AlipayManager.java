package dlmj.hideseek.BusinessLogic.Network;

import dlmj.hideseek.Common.Model.CreateOrder;

/**
 * Created by Two on 25/10/2016.
 */
public class AlipayManager {
    private static AlipayManager mInstance;

    public static AlipayManager getInstance(){
        synchronized (AlipayManager.class){
            if(mInstance == null){
                mInstance = new AlipayManager();
            }
        }
        return mInstance;
    }

    /**
     * 创建订单信息
     */
    public String getOrderInfo(CreateOrder.ParamsEntity params) {

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
    public String getSignType() {
        return "sign_type=\"RSA\"";
    }
}
