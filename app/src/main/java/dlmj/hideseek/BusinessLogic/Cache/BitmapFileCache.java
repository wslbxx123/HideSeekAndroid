package dlmj.hideseek.BusinessLogic.Cache;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import dlmj.hideseek.Common.Util.BaseInfoUtil;
import dlmj.hideseek.Common.Util.LogUtil;

/**
 * Created by Two on 6/2/16.
 */
public class BitmapFileCache {
    private final static String TAG = "BitmapFileCache";
    private Context mContext;

    private static BitmapFileCache mInstance;

    public static BitmapFileCache getInstance(Context context){
        synchronized (UserCache.class){
            if(mInstance == null){
                mInstance = new BitmapFileCache(context);
            }
        }
        return mInstance;
    }

    public BitmapFileCache(Context context) {
        this.mContext = context;
    }

    public Bitmap getBitmap(String url) {
        try {
            String fileName = url.substring(url.lastIndexOf("/") + 1);
            String imagePath = BaseInfoUtil.getImagePath(mContext, fileName);
            FileInputStream fileInputStream = new FileInputStream(imagePath);

            return BitmapFactory.decodeStream(fileInputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            LogUtil.e(TAG, "Failed to read file" + e.getMessage());
        }
        return null;
    }

    public void putBitmap(String url, Bitmap bitmap) {
        try {
            String fileName = url.substring(url.lastIndexOf("/") + 1);
            String imagePath = BaseInfoUtil.getImagePath(mContext, fileName);

            File file = new File(imagePath);

            if(!file.exists()) {
                FileOutputStream fileOutputStream = new FileOutputStream(imagePath);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            LogUtil.e(TAG, "Failed to write file" + e.getMessage());
        }
    }
}
