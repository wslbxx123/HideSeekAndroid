package dlmj.hideseek.BusinessLogic.Helper;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import dlmj.hideseek.Common.Model.User;
import dlmj.hideseek.Common.Util.BaseInfoUtil;
import dlmj.hideseek.Common.Util.LogUtil;
import dlmj.hideseek.Common.Util.NotificationUtil;
import dlmj.hideseek.R;
import dlmj.hideseek.UI.Activity.FriendActivity;

/**
 * Created by Two on 20/10/2016.
 */
public class HideSeekNotificationManager {
    private static HideSeekNotificationManager mInstance;
    private Context mContext;

    public static HideSeekNotificationManager getInstance() {
        if(mInstance == null) {
            mInstance = new HideSeekNotificationManager(BaseInfoUtil.getContext());
        }

        return mInstance;
    }

    private HideSeekNotificationManager(Context context){
        mContext = context;
    }

    private void cancel() {
        NotificationManager notificationManager = (NotificationManager) BaseInfoUtil
                .getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if(notificationManager == null) {
            return;
        }

        notificationManager.cancel(0);
    }

    public void forceCancelNotification() {
        cancel();
    }

    public void showCustomNewMessageNotification(Context context, String body, User user) {
        int identifier = context.getResources().getIdentifier(
                body, "string", context.getPackageName());

        String message = context.getString(identifier);
        Intent intent = new Intent(mContext, FriendActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 35, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = NotificationUtil.buildNotification(context,
                R.drawable.ic_launcher, context.getString(R.string.app_name),
                message, pendingIntent);
        notification.flags = (Notification.FLAG_AUTO_CANCEL | notification.flags);
        ((NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE))
                .notify(user == null ? 0 : (int)user.getPKId(), notification);
    }
}
