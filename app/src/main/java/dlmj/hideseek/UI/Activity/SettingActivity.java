package dlmj.hideseek.UI.Activity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import dlmj.hideseek.BusinessLogic.Cache.FriendCache;
import dlmj.hideseek.BusinessLogic.Cache.GoalCache;
import dlmj.hideseek.BusinessLogic.Cache.RaceGroupCache;
import dlmj.hideseek.BusinessLogic.Cache.RecordCache;
import dlmj.hideseek.BusinessLogic.Cache.UserCache;
import dlmj.hideseek.BusinessLogic.Helper.UserInfoManager;
import dlmj.hideseek.Common.Params.SharedPreferenceSettings;
import dlmj.hideseek.Common.Util.SharedPreferenceUtil;
import dlmj.hideseek.DataAccess.FriendTableManager;
import dlmj.hideseek.DataAccess.RaceGroupTableManager;
import dlmj.hideseek.DataAccess.RecordTableManager;
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
                UserInfoManager.getInstance().logout(getApplicationContext());
                GoalCache.getInstance().setIfNeedClearMap(true);
                finish();
            }
        });
    }
}
