package dlmj.hideseek.Common.Util;

import android.content.Context;
import android.graphics.Point;
import android.hardware.Camera;
import android.util.DisplayMetrics;
import android.util.Log;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Two on 3/5/16.
 */
public class CamParamUtil {
    private static String TAG = "Camera Params";
    private static CamParamUtil myCamParam = null;
    private CameraSizeComparator mSizeComparator = new CameraSizeComparator();

    public static CamParamUtil getInstance() {
        if (myCamParam == null) {
            myCamParam = new CamParamUtil();
            return myCamParam;
        } else {
            return myCamParam;
        }
    }

    public Camera.Size getPropPreviewSize(List<Camera.Size> list, float previewRate, int minWidth) {
        Collections.sort(list, mSizeComparator);
        float diff = Float.MAX_VALUE;
        int newIndex = 0;

        int i = 0;
        for (Camera.Size size : list) {
            if((size.width >= minWidth) && equalRate(size, previewRate)) {
                break;
            }

            if(Math.abs((float) size.width / size.height - previewRate) < diff) {
                newIndex = i;
                diff = Math.abs((float) size.width / size.height - previewRate);
            }
            i++;
        }

        if(i == list.size()) {
            i = newIndex;
        }

        Log.i(TAG, "Preview width: w = " + list.get(i).width + ", preview height h = " + list.get(i).height);
        return list.get(i);
    }

    public boolean equalRate(Camera.Size s, float rate) {
        float r = (float) (s.width) / (float) (s.height);

        if (Math.abs(r - rate) <= 0.03) {
            return true;
        } else {
            return false;
        }
    }

    public class CameraSizeComparator implements Comparator<Camera.Size> {

        @Override
        public int compare(Camera.Size lhs, Camera.Size rhs) {
            if (lhs.width == rhs.width) {
                return 0;
            } else if (lhs.width > rhs.width) {
                return -1;
            } else {
                return 1;
            }
        }
    }
}
