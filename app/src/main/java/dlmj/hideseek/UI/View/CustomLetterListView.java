package dlmj.hideseek.UI.View;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import dlmj.hideseek.Common.Interfaces.OnTouchingLetterChangedListener;
import dlmj.hideseek.R;

/**
 * Created by Two on 5/10/16.
 */
public class CustomLetterListView extends View {
    private static final int TYPE_ALL = 0;
    private static final int TYPE_ALPHA = 1;
    OnTouchingLetterChangedListener onTouchingLetterChangedListener;
    private Context mContext;
    private String[] mAlphas;
    private Paint mPaint = new Paint();
    private boolean mShowBkg = false;
    private int mChoose = -1;

    public CustomLetterListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;

        int type = 0;
        TypedArray array = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.CustomLetterListView, defStyle, 0);
        int count = array.getIndexCount();
        for (int i = 0; i < count; i++) {
            int attr = array.getIndex(i);
            switch (attr) {
                case R.styleable.CustomLetterListView_letter_type:
                    type = array.getInt(attr, 0);
                    break;
            }
        }
        array.recycle();

        switch(type) {
            case TYPE_ALL:
                mAlphas = new String[]{
                        context.getString(R.string.location),
                        context.getString(R.string.recent),
                        context.getString(R.string.hot),
                        context.getString(R.string.all), "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K",
                        "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X",
                        "Y", "Z" };
                break;
            case TYPE_ALPHA:
                mAlphas = new String[]{
                    "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K",
                    "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X",
                    "Y", "Z" };
                break;
        }
    }

    public CustomLetterListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomLetterListView(Context context) {
        this(context,null);
    }

    public void setAlphas(String[] alphas) {
        mAlphas = alphas;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mShowBkg) {
            canvas.drawColor(R.color.gray_40000000);
        }

        int height = getHeight();
        int width = getWidth();
        int singleHeight = height / mAlphas.length;

        for(int i = 0 ; i < mAlphas.length; i++) {
            mPaint.setColor(mContext.getResources().getColor(R.color.gray_8c8c8c));
            mPaint.setTextSize(20);
            mPaint.setAntiAlias(true);
            float xPos = width / 2 - mPaint.measureText(mAlphas[i]) / 2;
            float yPos = singleHeight * i + singleHeight;
            canvas.drawText(mAlphas[i], xPos, yPos, mPaint);
            mPaint.reset();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        final int action = event.getAction();
        final float y = event.getY();
        final OnTouchingLetterChangedListener listener = onTouchingLetterChangedListener;
        final int choose = (int) (y / getHeight() * mAlphas.length);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mShowBkg = true;
                motionInvalidate(choose, listener);
                break;
            case MotionEvent.ACTION_MOVE:
                motionInvalidate(choose, listener);
                break;
            case MotionEvent.ACTION_UP:
                mShowBkg = false;
                mChoose = -1;
                invalidate();
                break;
        }
        return true;
    }

    public void motionInvalidate(int choose, OnTouchingLetterChangedListener listener) {
        final int oldChoose = mChoose;

        if(oldChoose != choose && listener != null) {
            if(choose > 0 && choose < mAlphas.length) {
                listener.onTouchingLetterChanged(mAlphas[choose]);
                mChoose = choose;
                invalidate();
            }
        }
    }

    public void setOnTouchingLetterChangedListener(
            OnTouchingLetterChangedListener onTouchingLetterChangedListener) {
        this.onTouchingLetterChangedListener = onTouchingLetterChangedListener;
    }
}
