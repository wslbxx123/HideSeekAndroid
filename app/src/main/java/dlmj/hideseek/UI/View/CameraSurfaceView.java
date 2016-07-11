package dlmj.hideseek.UI.View;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import dlmj.hideseek.Hardware.CameraInterface;

/**
 * Created by Two on 3/3/16.
 */
public class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback{
    private static final String TAG = "Photo Surface View";
    SurfaceHolder mSurfaceHolder;
    OnCreateListener mOnCreateListener;

    public CameraSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mSurfaceHolder = getHolder();
        mSurfaceHolder.setFormat(PixelFormat.TRANSPARENT);
        mSurfaceHolder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.i(TAG, "surfaceCreated...");
        mOnCreateListener.onSurfaceViewCreated();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.i(TAG, "surfaceChanged...");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.i(TAG, "surfaceDestroyed...");
        CameraInterface.getInstance(getContext()).doStopCamera();
    }

    public SurfaceHolder getSurfaceHolder() {
        return mSurfaceHolder;
    }

    public void setOnCreateListener(OnCreateListener onCreateListener) {
        mOnCreateListener = onCreateListener;
    }

    public interface OnCreateListener {
        void onSurfaceViewCreated();
    }
}
