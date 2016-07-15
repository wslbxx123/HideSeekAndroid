package dlmj.hideseek.UI.View;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.util.AttributeSet;

import java.lang.reflect.Field;

import dlmj.hideseek.Common.Model.Goal;
import dlmj.hideseek.Common.Util.LogUtil;
import dlmj.hideseek.R;

/**
 * Created by Two on 7/13/16.
 */
public class GameView extends CustomSurfaceView{
    private static String TAG = "GameView";
    private static int FLING_COUNT = 5;
    private Paint mPaint;
    private boolean mIfGoalDisplayed = false;
    private int height, width;
    private int x = 0;
    private int[] mBitmapIDList;
    private int mIndex = 0;
    private int mWaitIndex = 0;
    private Bitmap mGoalBitmap;
    private int mWaitingCount = 20;
    private boolean mFlingVisible = true;
    private boolean mIfGoalFling = false;
    private int mFlingIndex = 0;

    public GameView(Context context) {
        this(context, null);
    }

    public GameView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        mPaint = new Paint();
    }

    @Override
    protected void myDraw(Canvas canvas) {
        super.myDraw(canvas);

        if(this.getHolder() == null || canvas == null) {
            return;
        }

        height = canvas.getHeight();
        width = canvas.getWidth();
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        if(mIfGoalDisplayed && mFlingVisible) {
            drawGoal(canvas);
        }

        if(mIfGoalFling) {
            mFlingVisible = !mFlingVisible;
            LogUtil.d(TAG, mFlingVisible ? "true":"false");
            mFlingIndex++;

            if(mFlingIndex > FLING_COUNT) {
                mIfGoalFling = false;
                mFlingVisible = true;
                mFlingIndex = 0;
            }
        }

        drawBlade(canvas);
    }

    private void drawBlade(Canvas canvas) {

    }

    private void drawGoal(Canvas canvas) {
        if(mBitmapIDList != null) {
            if(mGoalBitmap != null) {
                mGoalBitmap.recycle();
            }

            if(mWaitIndex > mWaitingCount) {
                mIndex = 0;
                mWaitIndex = 0;
            }

            if(mIndex <= mBitmapIDList.length - 1) {
                mGoalBitmap = BitmapFactory.decodeResource(getResources(), mBitmapIDList[mIndex]);
            } else {
                mGoalBitmap = BitmapFactory.decodeResource(getResources(),
                        mBitmapIDList[mBitmapIDList.length - 1]);
                mWaitIndex++;
            }

            LogUtil.d(TAG, "Width: " + width + ", Height: " + height);
            LogUtil.d(TAG, "x: " + (width - mGoalBitmap.getWidth()) / 2 + ", " +
                    "y: " + (height - mGoalBitmap.getHeight()) / 2);
            canvas.drawBitmap(mGoalBitmap, (width - mGoalBitmap.getWidth()) / 2,
                    (height - mGoalBitmap.getHeight()) / 2, mPaint);
            mIndex++;
        }
    }

    public void hitMonster() {
        mIfGoalFling = true;
    }

    public void setGoal(Goal goal) {
        try {
            TypedArray array;
            switch(goal.getType()) {
                case bomb:
                    array = getResources().obtainTypedArray(R.array.bomb);
                    break;
                case monster:
                    Field field = R.array.class.getField(goal.getShowTypeName());
                    int arrayId = field.getInt(new R.array());
                    array = getResources().obtainTypedArray(arrayId);

                    mWaitingCount = 0;
                    break;
                case mushroom:
                default:
                    array = getResources().obtainTypedArray(R.array.mushroom);

                    mWaitingCount = 20;
                    break;
            }

            mBitmapIDList = new int[array.length()];
            for(int i = 0; i < array.length(); i++) {
                mBitmapIDList[i] = array.getResourceId(i, 0);
            }
            array.recycle();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void showGoal() {
        mIfGoalDisplayed = true;
    }

    public void hideGoal() {
        mIfGoalDisplayed = false;
        mIndex = 0;
        mWaitIndex = 0;
    }

    public boolean getGoalDisplayed() {
        return mIfGoalDisplayed;
    }
}
