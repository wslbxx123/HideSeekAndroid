package dlmj.hideseek.Hardware;

import android.content.Context;
import android.view.OrientationEventListener;

import dlmj.hideseek.Common.Interfaces.OnOrientationChangedListener;

/**
 * Created by Two on 5/19/16.
 */
public class MapOrientationEventListener extends OrientationEventListener {
    private OnOrientationChangedListener mOnOrientationChangedListener;

    public MapOrientationEventListener(Context context, OnOrientationChangedListener onOrientationChangedListener) {
        super(context);
        mOnOrientationChangedListener = onOrientationChangedListener;
    }

    public MapOrientationEventListener(Context context, int rate,
                                       OnOrientationChangedListener onOrientationChangedListener) {
        super(context, rate);
        mOnOrientationChangedListener = onOrientationChangedListener;
    }

    @Override
    public void onOrientationChanged(int orientation) {
        if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) {
            return;
        }

        mOnOrientationChangedListener.onOrientationChanged(orientation);
    }
}
