package dlmj.hideseek.BusinessLogic.Cache;

import android.content.Context;

import dlmj.hideseek.Common.Model.Shop;

/**
 * 创建者     ZPL
 * 创建时间   2016/7/20 15:11
 * 描述	      ${TODO}
 * <p/>
 * 更新者     $Author$
 * 更新时间   $Date$
 * 更新描述   ${TODO}
 */
public class ShopCache extends BaseCache<Shop> {
    private static ShopCache mInstance;

    public ShopCache(Context context) {
        super();

    }

    public static ShopCache getInstance(Context context){
        synchronized (ShopCache.class){
            if(mInstance == null){
                mInstance = new ShopCache(context);
            }
        }
        return mInstance;
    }
}
