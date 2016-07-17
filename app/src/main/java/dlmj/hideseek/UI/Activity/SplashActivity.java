package dlmj.hideseek.UI.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;

import dlmj.hideseek.R;

/**
 * Created by Two on 6/26/16.
 */
public class SplashActivity extends Activity {
    private static boolean FIRST_TIME_ENTER_APP = true;
    private final static long INTERVAL = 2000;
    private CountDownTimer mTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        findView();

        goToIntroduce(INTERVAL);
    }

    private void findView() {
    }

    //第一次启动动画
    private void goToIntroduce(long interval) {
        if (interval < 0)
            interval = 0;
        mTimer = new CountDownTimer(interval, interval) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                Intent intent = new Intent();
                intent.setClass(SplashActivity.this, IntroduceActivity.class);
                startActivity(intent);
                finish();
            }
        };

        if(FIRST_TIME_ENTER_APP) {
            mTimer.start();
            FIRST_TIME_ENTER_APP = false;
        } else {
            Intent intent = new Intent();
            intent.setClass(SplashActivity.this, IntroduceActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
