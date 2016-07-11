package dlmj.hideseek.BusinessLogic.Cache;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;

import com.android.volley.toolbox.ImageLoader;

import dlmj.hideseek.BusinessLogic.Network.VolleyQueueController;
import dlmj.hideseek.Common.Util.LogUtil;

/**
 * Created by Two on 5/2/16.
 */
public class ImageCacheManager implements ImageLoader.ImageCache{
    private final static String TAG = "ImageCacheManager";
    private ImageLoader mImageLoader;
    private static ImageCacheManager mInstance;
    private BitmapCache mBitmapCache;
    private Context mContext;

    private ImageCacheManager(Context context){
        int maxSize = ((ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE))
                .getMemoryClass() * 1024 * 1024 / 8;
        mBitmapCache = new BitmapCache(maxSize);
        VolleyQueueController controller = VolleyQueueController.getInstance(context);
        mImageLoader = new ImageLoader(controller.getRequestQueue(), mBitmapCache);
        mContext = context;
    }

    public static ImageCacheManager getInstance(Context context) {
        synchronized (ImageCacheManager.class) {
            if (mInstance == null) {
                mInstance = new ImageCacheManager(context);
            }
        }
        return mInstance;
    }

    public ImageLoader getImageLoader() {
        return mImageLoader;
    }

    @Override
    public Bitmap getBitmap(String url) {
        Bitmap bitmap = mBitmapCache.getBitmap(url);

        if(bitmap != null) {
            LogUtil.d(TAG, "Get image from the cache.");
            return bitmap;
        }

        bitmap = BitmapFileCache.getInstance(mContext).getBitmap(url);

        if(bitmap != null) {
            LogUtil.d(TAG, "Get image from the file.");
            mBitmapCache.putBitmap(url, bitmap);
        }
        return bitmap;
    }

    @Override
    public void putBitmap(String url, Bitmap bitmap) {
        mBitmapCache.putBitmap(url, bitmap);

        BitmapFileCache.getInstance(mContext).putBitmap(url, bitmap);
    }
}
