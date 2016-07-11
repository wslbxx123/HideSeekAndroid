package dlmj.hideseek.UI.Thread;

import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;

/**
 * Created by Two on 5/11/16.
 */
public class OverlayThread implements Runnable{
    private TextView mOverlayTextView;
    private Handler mHandler;
    public final static int HIDE_OVERLAY = 1;

    public OverlayThread(TextView overlayTextView, Handler handler) {
        this.mOverlayTextView = overlayTextView;
        this.mHandler = handler;
    }

    @Override
    public void run() {
        Message message = new Message();
        message.what = HIDE_OVERLAY;
        mHandler.sendMessage(message);
        this.mOverlayTextView.setVisibility(View.GONE);
    }
}
