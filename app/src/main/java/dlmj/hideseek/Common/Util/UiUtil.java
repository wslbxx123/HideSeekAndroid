package dlmj.hideseek.Common.Util;

import android.content.Context;
import android.os.Handler;

import java.io.File;

/**
 * 处理UI相关的工具类，读取资源（文本、图片）
 */
public class UiUtil {

    //保证context不为空，非activity类中也能很便捷拿到context
    private static Context mContext;
    //handler做成静态唯一对象，减少内存开销，方便错误排查
    private static Handler mHandler;

    public static void init(Context HideSeekApplication) {
        mContext = HideSeekApplication;
        mHandler = new Handler();
    }

    public static Context getContext() {
        return mContext;
    }

    /**
     * 延时提交一个任务
     */
    public static void post(Runnable task){
        mHandler.post(task);
    }

    /**
     * 提交一个任务
     */
    public static void postDelay(Runnable task,long delayTime){
        mHandler.postDelayed(task, delayTime);
    }

    /**
     * 移除一个任务
     */
    public static void remove(Runnable task){
        mHandler.removeCallbacks(task);
    }

    public static String getPackName() {
        return mContext.getPackageName();
    }

    public static File getCacheDir() {
        return  mContext.getCacheDir();
    }

    public static String getString(int resId, Object... formatArgs) {
        return mContext.getString(resId, formatArgs);
    }
}
