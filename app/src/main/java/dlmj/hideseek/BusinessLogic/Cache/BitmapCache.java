package dlmj.hideseek.BusinessLogic.Cache;

import android.graphics.Bitmap;
import android.util.LruCache;

import com.android.volley.toolbox.ImageLoader;

/**
 * Created by Two on 5/2/16.
 */
public class BitmapCache extends LruCache<String, Bitmap> implements ImageLoader.ImageCache {

    public BitmapCache(int maxSize) {
        super(maxSize);
    }

    @Override
    public Bitmap getBitmap(String url) {
        return super.get(url);
    }

    @Override
    public void putBitmap(String url, Bitmap bitmap) {
        super.put(url, bitmap);
    }

    @Override
    public int sizeOf(String key, Bitmap value) {
        return value.getRowBytes() * value.getHeight();
    }
}
