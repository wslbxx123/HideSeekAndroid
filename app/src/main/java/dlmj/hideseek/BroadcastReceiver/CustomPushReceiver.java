package dlmj.hideseek.BroadcastReceiver;

import android.content.Context;
import android.content.Intent;

import com.tencent.android.tpush.XGPushBaseReceiver;
import com.tencent.android.tpush.XGPushClickedResult;
import com.tencent.android.tpush.XGPushRegisterResult;
import com.tencent.android.tpush.XGPushShowedResult;
import com.tencent.android.tpush.XGPushTextMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import dlmj.hideseek.BusinessLogic.Cache.NewFriendCache;
import dlmj.hideseek.BusinessLogic.Cache.UserCache;
import dlmj.hideseek.BusinessLogic.Helper.HideSeekNotificationManager;
import dlmj.hideseek.Common.Model.CustomNotification;
import dlmj.hideseek.Common.Model.User;
import dlmj.hideseek.Common.Util.LogUtil;

/**
 * Created by Two on 19/10/2016.
 */
public class CustomPushReceiver extends XGPushBaseReceiver {
    private Intent mIntent = new Intent("dlmj.hideseek.updateListView");
    private final static String TAG = "CustomPushReceiver";
    private final static int SEND_FRIEND_REQUEST = 1;
    private final static int ACCEPT_FRIEND = 2;

    @Override
    public void onRegisterResult(Context context, int i, XGPushRegisterResult xgPushRegisterResult) {

    }

    @Override
    public void onUnregisterResult(Context context, int i) {

    }

    @Override
    public void onSetTagResult(Context context, int i, String s) {

    }

    @Override
    public void onDeleteTagResult(Context context, int i, String s) {

    }

    @Override
    public void onTextMessage(Context context, XGPushTextMessage xgPushTextMessage) {
        String customContent = xgPushTextMessage.getCustomContent();

        try {
            JSONObject result = new JSONObject(customContent);
            int type = result.getInt("type");
            String body = result.getString("body");
            String accountStr = result.getString("object");
            User friend = null;

            switch(type) {
                case SEND_FRIEND_REQUEST:
                    String message = result.getString("extra");
                    friend = NewFriendCache.getInstance(context).setFriend(accountStr, message, false);
                    break;
                case ACCEPT_FRIEND:
                    int friendNum = result.getInt("extra");
                    if(UserCache.getInstance().ifLogin()) {
                        User user = UserCache.getInstance().getUser();
                        UserCache.getInstance().update(user, "friend_num", friendNum);
                    }
                    friend = NewFriendCache.getInstance(context).setFriend(accountStr, "", true);
                    break;

            }
            context.sendBroadcast(mIntent);
            HideSeekNotificationManager.getInstance().showCustomNewMessageNotification(context,
                    body, friend);

        } catch (JSONException e) {
            LogUtil.e(TAG, e.getMessage());
            e.printStackTrace();
        }

        LogUtil.d(TAG, customContent);
    }

    @Override
    public void onNotifactionClickedResult(Context context, XGPushClickedResult xgPushClickedResult) {

    }

    @Override
    public void onNotifactionShowedResult(Context context, XGPushShowedResult xgPushShowedResult) {
        if (context == null || xgPushShowedResult == null) {
            return;
        }
        CustomNotification notification = new CustomNotification();
        notification.setMessageId(xgPushShowedResult.getMsgId());
        notification.setTitle(xgPushShowedResult.getTitle());
        notification.setContent(xgPushShowedResult.getContent());
        notification.setNotificationActionType(xgPushShowedResult.getNotificationActionType());
        notification.setActivity(xgPushShowedResult.getActivity());
        notification.setUpdateTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                .format(Calendar.getInstance().getTime()));
//        NotificationService.getInstance(context).save(notification);
//        context.sendBroadcast(intent);
//        show(context, "您有1条新消息, " + "通知被展示 ， " + notifiShowedRlt.toString());
    }
}
