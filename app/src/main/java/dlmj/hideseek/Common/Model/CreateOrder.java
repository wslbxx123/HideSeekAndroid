package dlmj.hideseek.Common.Model;

/**
 * 创建者     ZPL
 * 创建时间   2016/8/7 21:54
 * 描述	      ${TODO}
 * <p/>
 * 更新者     $Author$
 * 更新时间   $Date$
 * 更新描述   ${TODO}
 */
public class CreateOrder {
    /**
     * order_id : 15
     * sign : HG7vb%2B7lmiSP3wk81JxaY32aFQBceIhPTvdREUxJ%2FcwPI5dtGCxHt6Ro6Lax4FsSMhfpT9%2Fu9tTYx3miTzrfAcJ96eLzQoMrLE3ACFKR1HW1xCtXPSHOTjXItBMFw6aZiaY3bW2V%2FHRgn34aAcNZSqx9d3xdChypzGiPr6Xhf14%3D
     * trade_no : IGV051467907200
     * params : {"service":"\"mobile.securitypay.pay\"","partner":"\"2088421519055042\"","_input_charset":"\"utf-8\"","notify_url":"\"http://www.hideseek.cn/index.php/home/store/notifyUrl\"","out_trade_no":"\"IGV051467907200\"","subject":"\"\"","payment_type":"\"1\"","seller_id":"\"wslbxx@hotmail.com\"","total_fee":"\"0.00\"","body":"\"\"","it_b_pay":"\"30m\"","show_url":"\"m.alipay.com\""}
     */

    public String order_id;
    public String sign;
    public String trade_no;
    /**
     * service : "mobile.securitypay.pay"
     * partner : "2088421519055042"
     * _input_charset : "utf-8"
     * notify_url : "http://www.hideseek.cn/index.php/home/store/notifyUrl"
     * out_trade_no : "IGV051467907200"
     * subject : ""
     * payment_type : "1"
     * seller_id : "wslbxx@hotmail.com"
     * total_fee : "0.00"
     * body : ""
     * it_b_pay : "30m"
     * show_url : "m.alipay.com"
     */

    public ParamsEntity params;

    public  class ParamsEntity {
        public String service;
        public String partner;
        public String _input_charset;
        public String notify_url;
        public String out_trade_no;
        public String subject;
        public String payment_type;
        public String seller_id;
        public String total_fee;
        public String body;
        public String it_b_pay;
        public String show_url;

        @Override
        public String toString() {
            return "ParamsEntity{" +
                    "service='" + service + '\'' +
                    ", partner='" + partner + '\'' +
                    ", _input_charset='" + _input_charset + '\'' +
                    ", notify_url='" + notify_url + '\'' +
                    ", out_trade_no='" + out_trade_no + '\'' +
                    ", subject='" + subject + '\'' +
                    ", payment_type='" + payment_type + '\'' +
                    ", seller_id='" + seller_id + '\'' +
                    ", total_fee='" + total_fee + '\'' +
                    ", body='" + body + '\'' +
                    ", it_b_pay='" + it_b_pay + '\'' +
                    ", show_url='" + show_url + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "CreateOrder{" +
                "order_id='" + order_id + '\'' +
                ", sign='" + sign + '\'' +
                ", trade_no='" + trade_no + '\'' +
                ", params=" + params +
                '}';
    }
}
