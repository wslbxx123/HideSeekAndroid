package dlmj.hideseek.Common.Model;

import java.util.List;

/**
 * 创建者     ZPL
 * 创建时间   2016/7/23 19:00
 * 描述	      ${TODO}
 * <p/>
 * 更新者     $Author$
 * 更新时间   $Date$
 * 更新描述   ${TODO}
 */
public class Reward {

        public String reward_min_id;
        public String version;
        /**
         * exchange_count : 50
         * introduction : 测试
         * pk_id : 3
         * record : 50
         * reward_image_url : http://www.hideseek.cn/Public/Image/Reward/1.jpg
         * reward_name : 测试3
         * version : 1
         */

        public List<RewardEntity> reward;

        public static class RewardEntity {
            public String exchange_count;
            public String introduction;
            public String pk_id;
            public String record;
            public String reward_image_url;
            public String reward_name;
            public String version;
        }

}
