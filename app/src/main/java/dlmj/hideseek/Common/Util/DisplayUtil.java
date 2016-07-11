package dlmj.hideseek.Common.Util;

import android.content.Context;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.util.Log;

/**
 * Created by Two on 3/19/16.
 */
public class DisplayUtil {
    private static final String TAG = "DisplayUtil";

    public static float getScreenRate(Context context) {
        Point point = getScreenMetrics(context);
        float height = point.y;
        float width = point.x;

        return (height / width);
    }

    public static Point getScreenMetrics(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        int wScreen = dm.widthPixels;
        int hScreen = dm.heightPixels;
        Log.i(TAG, "Screen: Width = " + wScreen + " Height = " + hScreen
                + " DensityDpi = " + dm.densityDpi);
        return new Point(wScreen, hScreen - 38);
    }
}
