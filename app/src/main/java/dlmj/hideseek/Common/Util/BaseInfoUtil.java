package dlmj.hideseek.Common.Util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.text.TextUtils;

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

    public static String getSdCardImageDir(Context context) {
        String dirPath = getProjectDir(context) + File.separator + APP_IMAGE_NAME;
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

    public static String getImageDir(Context context) {
        String dirPath = context.getExternalCacheDir() + File.separator + APP_IMAGE_NAME;
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

    public static String getImagePath(Context context, String fileName, boolean isSdCard) {
        String dirPath = isSdCard? getSdCardImageDir(context) : getImageDir(context);

        String path = dirPath + File.separator + fileName + "_" + new Date().getTime() + ".jpg";
        LogUtil.d(TAG, path);
        return path;
    }

    /**
     * 获取路径
     */
    private static String getPath(Context context,boolean isSdCard)
    {
        return isSdCard? getSdCardImageDir(context) : getImageDir(context);
    }

    /**
     * 获取文件夹内的文件大小(只获取一级目录)
     * @param path
     * @return
     */
    private static long getFileSize(String path)
    {
        if (TextUtils.isEmpty(path))
        {
            return 0;
        }
        File filePath=new File(path);
        File[] itemList=filePath.listFiles();
        long totalSize=0;
        if (null!=itemList)
        {
            for (File f:itemList)
            {
                if (f.exists() && f.isFile() && f.length()>0)
                {
                    totalSize=totalSize+f.length();
                }
            }
        }
        return totalSize;
    }

    /**
     * 删除路径下的所有文件
     * @param path
     */
    private static void deleteFlie(String path)
    {
        if (TextUtils.isEmpty(path))
        {
            return ;
        }
        File filePath=new File(path);
        File[] itemList=filePath.listFiles();
        long totalSize=0;
        if (null!=itemList)
        {
            for (File f:itemList)
            {
                if (f.exists() && f.isFile())
                {
                    f.delete();
                }
            }
        }
    }

    /**
     * 缓存大小
     */
    public static long getCacheSize(Context context)
    {
        String sdCardFilePath=getPath(context,true);
        String phoneFilePath=getPath(context,false);
        return getFileSize(sdCardFilePath)+getFileSize(phoneFilePath);
    }

    /**
     * 清空缓存
     */
    public static void clearImageCache(Context context)
    {
        String sdCardFilePath=getPath(context,true);
        String phoneFilePath=getPath(context,false);
        deleteFlie(sdCardFilePath);
        deleteFlie(phoneFilePath);
    }

    //版本号
    public static String getVersion(Context context) {
        return getPackageInfo(context).versionName + "." + getPackageInfo(context).versionCode;
    }

    public static PackageInfo getPackageInfo(Context context) {
        PackageInfo packageInfo = null;

        try {
            PackageManager pm = context.getPackageManager();
            packageInfo = pm.getPackageInfo(context.getPackageName(),
                    PackageManager.GET_CONFIGURATIONS);

            return packageInfo;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return packageInfo;
    }
}
