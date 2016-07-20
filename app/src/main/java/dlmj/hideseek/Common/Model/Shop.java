package dlmj.hideseek.Common.Model;

import java.util.List;

/**
 * 创建者     ZPL
 * 创建时间   2016/7/20 15:06
 * 描述	      ${TODO}
 * <p/>
 * 更新者     $Author$
 * 更新时间   $Date$
 * 更新描述   ${TODO}
 */
public class Shop {

    /**
     * version : 1
     * product_min_id : 10
     * products : [{"pk_id":"2","product_name":"怪兽图鉴","product_image_url":"http://www.hideseek.cn/Public/Image/Store/2.png","price":"2","purchase_count":"0","introduction":"可获得怪兽信息，并包含拿下怪兽的规则","version":"1"},{"pk_id":"1","product_name":"定时炸弹","product_image_url":"http://www.hideseek.cn/Public/Image/Store/1.png","price":"1","purchase_count":"0","introduction":"放在指定位置，引诱他人接近扫描后，可触发爆炸，使他人丧失1积分","version":"1"}]
     */

        public String version;
        public String product_min_id;
        /**
         * pk_id : 2
         * product_name : 怪兽图鉴
         * product_image_url : http://www.hideseek.cn/Public/Image/Store/2.png
         * price : 2
         * purchase_count : 0
         * introduction : 可获得怪兽信息，并包含拿下怪兽的规则
         * version : 1
         */

        public List<ProductsEntity> products;

        public static class ProductsEntity {
            public String pk_id;
            public String product_name;
            public String product_image_url;
            public String price;
            public String purchase_count;
            public String introduction;
            public String version;
        }
    }

