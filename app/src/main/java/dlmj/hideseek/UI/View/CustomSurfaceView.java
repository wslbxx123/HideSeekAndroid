package dlmj.hideseek.UI.View;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by Two on 7/13/16.
 */
public class CustomSurfaceView extends SurfaceView implements SurfaceHolder.Callback{
    private SurfaceHolder mHolder;
    private DrawThread mDrawThread;

    public CustomSurfaceView(Context context) {
        this(context, null);
    }

    public CustomSurfaceView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        mHolder = getHolder();
        mHolder.setFormat(PixelFormat.TRANSPARENT);
        setZOrderOnTop(true);
        mHolder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        mDrawThread = new DrawThread();
        Thread thread = new Thread(mDrawThread);
        thread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        mDrawThread.stop();
    }

    protected void myDraw(Canvas canvas){

    }

    private class DrawThread implements Runnable{

        private boolean mRun = true;
        @Override
        public void run() {
            // TODO Auto-generated method stub
            while(mRun){
                Canvas canvas = mHolder.lockCanvas();
                myDraw(canvas);
                if(canvas != null) {
                    mHolder.unlockCanvasAndPost(canvas);
                }
            }
        }

        public void stop(){
            mRun = false;
        }
    }
}
