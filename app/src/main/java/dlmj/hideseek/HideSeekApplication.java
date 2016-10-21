package dlmj.hideseek;

import android.app.Application;
import android.util.Log;

import com.baidu.navisdk.adapter.BaiduNaviManager;
import com.tencent.android.tpush.XGIOperateCallback;
import com.tencent.android.tpush.XGPushManager;

import java.io.File;

import cn.smssdk.SMSSDK;
import dlmj.hideseek.BusinessLogic.Cache.UserCache;
import dlmj.hideseek.Common.Model.User;
import dlmj.hideseek.Common.Util.BaseInfoUtil;
import dlmj.hideseek.Common.Util.LogUtil;
import dlmj.hideseek.DataAccess.DatabaseManager;
import dlmj.hideseek.Common.Util.UiUtil;

/**
 * Created by Two on 4/2/16.
 */
public class HideSeekApplication extends Application {
    public final static String TAG = "HideSeekApplication";
    private static HideSeekApplication mInstance;
    private BaiduNaviManager.NaviInitListener mNavigationInitListener;

    /**
     * 获取应用程序实例
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
        UiUtil.init(this);
        SMSSDK.initSDK(this, getString(R.string.sms_key), getString(R.string.sms_secret));
    }

    @Override
    public void onTerminate() {
        DatabaseManager.getInstance(this).closeDatabase();
        super.onTerminate();
    }

    public void init() {
        initDirs();
        BaseInfoUtil.setContext(getApplicationContext());
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
