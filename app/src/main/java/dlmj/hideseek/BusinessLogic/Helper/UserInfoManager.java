package dlmj.hideseek.BusinessLogic.Helper;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import dlmj.hideseek.BusinessLogic.Cache.UserCache;
import dlmj.hideseek.R;
import dlmj.hideseek.UI.Activity.MyOrderActivity;

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
}
