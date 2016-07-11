package dlmj.hideseek.Hardware;

import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.IOException;

import dlmj.hideseek.Common.Util.CamParamUtil;

/**
 * Created by Two on 3/13/16.
 */
public class CameraInterface {
    private static final String TAG = "Camera";
    private static CameraInterface mCameraInterface;
    private Camera mCamera;
    private Camera.Parameters mParams;
    private boolean isPreviewing = false;
    private Context mContext;

    public interface CamOpenOverCallback{
        public void  cameraHasOpened();
    }

    public CameraInterface(Context context) {
        mContext = context;
    }

    public static synchronized CameraInterface getInstance(Context context) {
        if(mCameraInterface == null) {
            mCameraInterface = new CameraInterface(context);
        }

        return mCameraInterface;
    }

    /**
     * Open Camera
     * @param callback
     */
    public void doOpenCamera(CamOpenOverCallback callback) {
        Log.i(TAG, "Camera open...");
        mCamera = Camera.open();
        Log.i(TAG, "Camera open over...");
        callback.cameraHasOpened();
    }

    public void doStartPreview(SurfaceHolder holder, float previewRate) {
        Log.i(TAG, "doStartPreview");
        if(isPreviewing) {
            mCamera.stopPreview();
            return;
        }

        if(mCamera != null) {
            mParams = mCamera.getParameters();
            mParams.setPictureFormat(PixelFormat.JPEG);

            Camera.Size previewSize = CamParamUtil.getInstance().getPropPreviewSize(
                    mParams.getSupportedPreviewSizes(), previewRate, 800);
            mParams.setPreviewSize(previewSize.width, previewSize.height);

            mCamera.setDisplayOrientation(90);

            synchronized(mParams) {
                mCamera.setParameters(mParams);
            }

            try {
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Stop Previewing, Release Camera.
     */
    public void doStopCamera() {
        if(mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            isPreviewing = false;
            mCamera.release();
            mCamera = null;
        }
    }
}
