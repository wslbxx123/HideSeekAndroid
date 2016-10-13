package dlmj.hideseek.UI.View;

import android.content.Context;
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
        show(context, MessageType.error);
    }

    public void show(String context, MessageType type) {
        mSuperToast.setText(context);
        switch(type) {
            case success:
                mSuperToast.setBackground(R.color.green_24c557);
            case warning:
                mSuperToast.setBackground(R.color.yellow_ffcc00);
            case error:
                mSuperToast.setBackground(R.color.red_f82a52);

        }
        mSuperToast.show();
    }

    public void setListener(SuperToast.OnDismissListener onDismissListener) {
        mSuperToast.setOnDismissListener(onDismissListener);
    }

    public enum MessageType {
        success(0), warning(1), error(2);

        private int value = 0;

        private MessageType(int value) {    //    必须是private的，否则编译错误
            this.value = value;
        }

        public static MessageType valueOf(int value) {
            switch (value) {
                case 0:
                    return success;
                case 1:
                    return warning;
                case 2:
                    return error;
                default:
                    return null;
            }
        }

        public int getValue() {
            return this.value;
        }
    }
}
