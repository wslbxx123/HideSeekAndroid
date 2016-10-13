package dlmj.hideseek.UI.View;

import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;

import com.github.johnpersano.supertoasts.SuperToast;

import dlmj.hideseek.R;

/**
 * Created by Two on 5/3/16.
 */
public class CustomSuperToast {
    private SuperToast mSuperToast;

    public CustomSuperToast(Context context, int color, SuperToast.Animations animation, int textColor) {
        mSuperToast = new SuperToast(context);
        mSuperToast.setAnimations(animation);
        mSuperToast.setDuration(SuperToast.Duration.SHORT);
        mSuperToast.setTextSize(SuperToast.TextSize.MEDIUM);
        mSuperToast.setBackground(color);
        mSuperToast.setTextColor(textColor);
        mSuperToast.setGravity(Gravity.CENTER_HORIZONTAL, 0, -200);
    }

    public CustomSuperToast(Context context) {
        this(context, SuperToast.Background.RED, SuperToast.Animations.FADE, R.color.white);
    }

    public void show(String context) {
        mSuperToast.setText(context);
        mSuperToast.show();
    }

    public void setListener(SuperToast.OnDismissListener onDismissListener) {
        mSuperToast.setOnDismissListener(onDismissListener);
    }
}
