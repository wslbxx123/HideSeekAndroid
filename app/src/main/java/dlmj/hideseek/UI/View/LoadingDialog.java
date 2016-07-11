package dlmj.hideseek.UI.View;

import android.app.Dialog;
import android.content.Context;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import dlmj.hideseek.R;

/**
 * Created by Two on 6/29/16.
 */
public class LoadingDialog extends Dialog{
    private ImageView routeImageView;
    private TextView detailTextView;
    private RotateAnimation mAnim;

    public LoadingDialog(Context context) {
        super(context, R.style.LoadingDialog);
        setContentView(R.layout.loading_dialog);
        findView();
        initAnim();
    }

    private void findView() {
        routeImageView = (ImageView) findViewById(R.id.routeImageView);
        detailTextView = (TextView) findViewById(R.id.detailTextView);
        detailTextView.setText(getContext().getString(R.string.loading));
        setCancelable(false);
    }

    private void initAnim() {
        mAnim = new RotateAnimation(0, 360, Animation.RESTART, 0.5f, Animation.RESTART, 0.5f);
        mAnim.setDuration(2000);
        mAnim.setRepeatCount(Animation.INFINITE);
        mAnim.setRepeatMode(Animation.RESTART);
        mAnim.setStartTime(Animation.START_ON_FIRST_FRAME);
    }

    @Override
    public void setTitle(CharSequence title) {
        detailTextView.setText(title);
    }

    @Override
    public void setTitle(int titleId) {
        setTitle(getContext().getString(titleId));
    }

    @Override
    public void show() {
        routeImageView.startAnimation(mAnim);
        super.show();
    }

    @Override
    public void dismiss() {
        mAnim.cancel();
        super.dismiss();
    }
}
