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
                SharedPreferences sharedPreferences = SharedPreferenceUtil.getSharedPreferences();
                SharedPreferenceSettings sessionToken = SharedPreferenceSettings.SESSION_TOKEN;
                SharedPreferenceSettings userInfo = SharedPreferenceSettings.USER_INFO;
                SharedPreferenceSettings friendVersion = SharedPreferenceSettings.FRIEND_VERSION;
                SharedPreferenceSettings recordVersion = SharedPreferenceSettings.RECORD_VERSION;
                SharedPreferenceSettings recordMinId = SharedPreferenceSettings.RECORD_MIN_ID;
                SharedPreferenceSettings raceGroupVersion = SharedPreferenceSettings.RACE_GROUP_VERSION;
                SharedPreferenceSettings raceGroupRecordMinId = SharedPreferenceSettings.RACE_GROUP_RECORD_MIN_ID;
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.remove(sessionToken.getId());
                editor.remove(userInfo.getId());
                editor.remove(friendVersion.getId());
                editor.remove(recordVersion.getId());
                editor.remove(recordMinId.getId());
                editor.remove(raceGroupVersion.getId());
                editor.remove(raceGroupRecordMinId.getId());
                editor.apply();
                RecordCache.getInstance(SettingActivity.this).clearList();
                RecordTableManager.getInstance(getApplicationContext()).clear();
                GoalCache.getInstance().setIfNeedClearMap(true);
                RaceGroupCache.getInstance(SettingActivity.this).clearList();
                RaceGroupTableManager.getInstance(getApplicationContext()).clear();
                FriendCache.getInstance(getApplication()).clearList();
                FriendTableManager.getInstance(getApplicationContext()).clear();
                finish();
            }
        });
    }
}
