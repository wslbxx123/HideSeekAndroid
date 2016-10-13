package dlmj.hideseek.BusinessLogic.Helper;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;

import dlmj.hideseek.BusinessLogic.Cache.FriendCache;
import dlmj.hideseek.BusinessLogic.Cache.GoalCache;
import dlmj.hideseek.BusinessLogic.Cache.RaceGroupCache;
import dlmj.hideseek.BusinessLogic.Cache.RecordCache;
import dlmj.hideseek.BusinessLogic.Cache.UserCache;
import dlmj.hideseek.Common.Interfaces.OnUserLoginListener;
import dlmj.hideseek.Common.Params.SharedPreferenceSettings;
import dlmj.hideseek.Common.Util.SharedPreferenceUtil;
import dlmj.hideseek.DataAccess.FriendTableManager;
import dlmj.hideseek.DataAccess.RaceGroupTableManager;
import dlmj.hideseek.DataAccess.RecordTableManager;
import dlmj.hideseek.R;
import dlmj.hideseek.UI.Activity.MyOrderActivity;
import dlmj.hideseek.UI.View.LoginDialog;

/**
 * Created by Two on 08/10/2016.
 */
public class UserInfoManager {
    private static UserInfoManager mInstance;

    public static UserInfoManager getInstance(){
        synchronized (UserCache.class){
            if(mInstance == null){
                mInstance = new UserInfoManager();
            }
        }
        return mInstance;
    }

    public void clearData(Context context) {
        SharedPreferences sharedPreferences = SharedPreferenceUtil.getSharedPreferences();
        SharedPreferenceSettings sessionToken = SharedPreferenceSettings.SESSION_TOKEN;
        SharedPreferenceSettings userInfo = SharedPreferenceSettings.USER_INFO;
        SharedPreferenceSettings friendVersion = SharedPreferenceSettings.FRIEND_VERSION;
        SharedPreferenceSettings recordVersion = SharedPreferenceSettings.RECORD_VERSION;
        SharedPreferenceSettings recordMinId = SharedPreferenceSettings.RECORD_MIN_ID;
        SharedPreferenceSettings raceGroupVersion = SharedPreferenceSettings.RACE_GROUP_VERSION;
        SharedPreferenceSettings raceGroupRecordMinId = SharedPreferenceSettings.RACE_GROUP_RECORD_MIN_ID;
        SharedPreferenceSettings productVersion = SharedPreferenceSettings.PRODUCT_VERSION;
        SharedPreferenceSettings productMinId = SharedPreferenceSettings.PRODUCT_MIN_ID;
        SharedPreferenceSettings rewardVersion = SharedPreferenceSettings.REWARD_VERSION;
        SharedPreferenceSettings rewardMinId = SharedPreferenceSettings.REWARD_MIN_ID;
        SharedPreferenceSettings purchaseOrderVersion = SharedPreferenceSettings.PURCHASE_ORDER_VERSION;
        SharedPreferenceSettings purchaseOrderMinId = SharedPreferenceSettings.PURCHASE_ORDER_MIN_ID;
        SharedPreferenceSettings exchangeOrderVersion = SharedPreferenceSettings.EXCHANGE_ORDER_VERSION;
        SharedPreferenceSettings exchangeOrderMinId = SharedPreferenceSettings.EXCHANGE_ORDER_MIN_ID;
        SharedPreferenceSettings scoreSum = SharedPreferenceSettings.SCORE_SUM;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(sessionToken.getId());
        editor.remove(userInfo.getId());
        editor.remove(friendVersion.getId());
        editor.remove(recordVersion.getId());
        editor.remove(recordMinId.getId());
        editor.remove(raceGroupVersion.getId());
        editor.remove(raceGroupRecordMinId.getId());
        editor.remove(productVersion.getId());
        editor.remove(productMinId.getId());
        editor.remove(rewardVersion.getId());
        editor.remove(rewardMinId.getId());
        editor.remove(purchaseOrderVersion.getId());
        editor.remove(purchaseOrderMinId.getId());
        editor.remove(exchangeOrderVersion.getId());
        editor.remove(exchangeOrderMinId.getId());
        editor.remove(scoreSum.getId());

        editor.apply();
        RecordCache.getInstance(context).clearList();
        RecordTableManager.getInstance(context).clear();
        GoalCache.getInstance().setIfNeedClearMap(true);
        RaceGroupCache.getInstance(context).clearList();
        RaceGroupTableManager.getInstance(context).clear();
        FriendCache.getInstance(context).clearList();
        FriendTableManager.getInstance(context).clear();
    }

    public void checkIfGoToLogin(Context context) {
        final Context currentContext = context;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(context.getString(R.string.not_login));
        builder.setPositiveButton(context.getResources().getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(currentContext, MyOrderActivity.class);
                        currentContext.startActivity(intent);
                    }
                });
        builder.setNegativeButton(context.getResources().getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        builder.show();
    }

    public boolean logout(Context context) {
        clearData(context);

        LoginDialog loginDialog = new LoginDialog(context);
        loginDialog.setOnUserLoginListener((OnUserLoginListener)context);
        loginDialog.show();

        return true;
    }
}
