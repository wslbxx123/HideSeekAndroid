package dlmj.hideseek.Common.Util;

import android.content.Context;
import android.content.SharedPreferences;

import dlmj.hideseek.Common.Params.SharedPreferenceParams;
import dlmj.hideseek.HideSeekApplication;

/**
 * Created by Two on 4/30/16.
 */
public class SharedPreferenceUtil {
    public static SharedPreferences getSharedPreferences() {
        HideSeekApplication application = HideSeekApplication.getInstance();
        return application.getSharedPreferences(
                getDefaultSharedPreferencesFileName(), Context.MODE_PRIVATE);
    }

    public static String getDefaultSharedPreferencesFileName() {
        return SharedPreferenceParams.CALL_UP_FILE;
    }
}
