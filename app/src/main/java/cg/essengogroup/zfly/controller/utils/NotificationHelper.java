package cg.essengogroup.zfly.controller.utils;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import cg.essengogroup.zfly.R;
import cg.essengogroup.zfly.view.activities.ChatActivity;

import static cg.essengogroup.zfly.view.activities.LoginActivity.CHANNEL_ID;

public class NotificationHelper {
    public static void displayNotification(Context context, String title, String body) {

        Intent intent = new Intent(context, ChatActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                100,
                intent,
                PendingIntent.FLAG_CANCEL_CURRENT
        );
        Notification notification = new Notification();
        notification.defaults |= Notification.DEFAULT_SOUND;
        notification.defaults |= Notification.DEFAULT_VIBRATE;

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.icon)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat mNotificationMgr = NotificationManagerCompat.from(context);
        mBuilder.setAutoCancel(true);
        mNotificationMgr.notify(1, mBuilder.build());

    }
}
