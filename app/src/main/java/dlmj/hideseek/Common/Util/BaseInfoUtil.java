package dlmj.hideseek.Common.Util;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.util.Date;

/**
 * Created by Two on 4/2/16.
 */
public class BaseInfoUtil {
    private static String TAG = "BaseInfoUtil";
    private static String SD_CARD_DIR;
    public static final String APP_FOLDER_NAME = "HideAndSeek";
    public static final String APP_IMAGE_NAME = "Image";

    public static boolean hasSdCard() {
        String state = Environment.getExternalStorageState();
        return state.equals(Environment.MEDIA_MOUNTED);
    }

    public static String getSdCardDir(Context context) {
        if(SD_CARD_DIR != null) {
            return SD_CARD_DIR;
        }

        if (hasSdCard()) {
            SD_CARD_DIR = Environment.getExternalStorageDirectory().toString();
            return SD_CARD_DIR;
        } else {
            return context.getCacheDir().getPath();
        }
    }

    public static String getProjectDir(Context context) {
        String sdCardDir = getSdCardDir(context);

        return sdCardDir + File.separator + APP_FOLDER_NAME;
    }

    public static String getImageDir(Context context) {
        String dirPath = context.getExternalCacheDir() + File.separator + BaseInfoUtil.APP_IMAGE_NAME;
        File file = new File(dirPath);

        if (!file.exists()) {
            try {
                boolean isCreate = file.mkdir();
                LogUtil.d(TAG, dirPath + "has created. " + isCreate);
                File noMedia = new File(dirPath  + File.separator + ".nomedia" );
                noMedia.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return dirPath;
    }

    public static String getImagePath(Context context, String fileName) {
        String dirPath = getImageDir(context);

        String path = dirPath + File.separator + fileName + "_" + new Date().getTime() + ".jpg";
        LogUtil.d(TAG, path);
        return path;
    }
}
