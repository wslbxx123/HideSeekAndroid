package dlmj.hideseek;

import android.app.Application;

import com.baidu.navisdk.adapter.BaiduNaviManager;

import java.io.File;

import dlmj.hideseek.Common.Util.BaseInfoUtil;
import dlmj.hideseek.Common.Util.LogUtil;
import dlmj.hideseek.DataAccess.DatabaseManager;

/**
 * Created by Two on 4/2/16.
 */
public class HideSeekApplication extends Application {
    public final static String TAG = "HideSeekApplication";
    private static HideSeekApplication mInstance;
    private BaiduNaviManager.NaviInitListener mNavigationInitListener;

    /**
     * 获取应用程序实例(其实我是来测试的)
     * @return HideSeekApplication
     */
    public static HideSeekApplication getInstance() {
        if (mInstance == null) {
            LogUtil.w(TAG, "[HideSeekApplication] instance is null.");
        }
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        init();
    }

    @Override
    public void onTerminate() {
        DatabaseManager.getInstance(this).closeDatabase();
        super.onTerminate();
    }

    public void init() {
        initDirs();
    }

    /**
     * Initialize the directory of this project.初始化
     * @return the boolean that indicates if initialize the directory successfully.
     */
    private boolean initDirs() {
        String sdCardPath = BaseInfoUtil.getSdCardDir(this);
        if (sdCardPath == null) {
            return false;
        }
        File file = new File(sdCardPath, BaseInfoUtil.APP_FOLDER_NAME);
        if (!file.exists()) {
            try {
                file.mkdir();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }
}
