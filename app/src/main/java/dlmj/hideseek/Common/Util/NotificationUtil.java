package dlmj.hideseek.Common.Util;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;

/**
 * Created by Two on 20/10/2016.
 */
public class NotificationUtil {
    public static final String TAG = "NotificationUtil";

    public static Notification buildNotification(Context context, int icon,
                                                 String contentTitle, String contentText,
                                                 PendingIntent intent) {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
            Notification.Builder builder = new Notification.Builder(context);
            builder.setLights(Color.GREEN, 300, 1000)
                    .setSmallIcon(icon)
                    .setContentTitle(contentTitle)
                    .setContentText(contentText)
                    .setContentIntent(intent);

            builder.setDefaults(Notification.DEFAULT_VIBRATE);
            return builder.getNotification();
        }

        Notification notification = new Notification();
        notification.flags = (Notification.FLAG_SHOW_LIGHTS | notification.flags);
        notification.ledARGB = Color.GREEN;
        notification.ledOnMS = 300;
        notification.ledOffMS = 1000;
        notification.icon = icon;
        notification.defaults = Notification.DEFAULT_VIBRATE;
        return notification;
    }
}
