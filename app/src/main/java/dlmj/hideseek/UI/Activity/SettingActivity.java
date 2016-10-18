package dlmj.hideseek.UI.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import dlmj.hideseek.BusinessLogic.Cache.GoalCache;
import dlmj.hideseek.BusinessLogic.Cache.UserCache;
import dlmj.hideseek.BusinessLogic.Helper.UserInfoManager;
import dlmj.hideseek.R;

/**
 * Created by Two on 5/3/16.
 */
public class SettingActivity extends Activity {
    private LinearLayout mLogoutLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        findView();
        setListener();
    }

    private void findView() {
        mLogoutLayout = (LinearLayout) findViewById(R.id.logoutLayout);

        if(UserCache.getInstance().ifLogin()) {
            mLogoutLayout.setVisibility(View.VISIBLE);
        } else{
            mLogoutLayout.setVisibility(View.GONE);
        }
    }

    private void setListener() {
        mLogoutLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserInfoManager.getInstance().clearData(getApplicationContext());
                GoalCache.getInstance().setIfNeedClearMap(true);
                finish();
            }
        });
        findViewById(R.id.feedbackLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingActivity.this,FeedBackActivity.class));
            }
        });
    }
}
