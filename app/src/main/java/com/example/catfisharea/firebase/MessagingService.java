package com.example.catfisharea.firebase;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.android.app.catfisharea.R;
import com.example.catfisharea.activities.alluser.ChatActivity;
import com.example.catfisharea.activities.alluser.IncomingConferenceActivity;
import com.example.catfisharea.models.User;
import com.example.catfisharea.ultilities.Constants;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;

public class MessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
    }

    @SuppressLint({"UnspecifiedImmutableFlag", "MissingPermission"})
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d("Calltest",  "onMessageReceived");
        String type = remoteMessage.getData().get(Constants.REMOTE_MSG_TYPE);
        Log.d("Calltest",  type);
        if (type != null) {
            if (type.equals(Constants.REMOTE_MSG_INVITATION)) {
                Intent intent = new Intent(getApplicationContext(), IncomingConferenceActivity.class);
                intent.putExtra(
                        Constants.REMOTE_MSG_MEETING_TYPE,
                        remoteMessage.getData().get(Constants.REMOTE_MSG_MEETING_TYPE)
                );
                intent.putExtra(
                        Constants.KEY_USER_ID,
                        remoteMessage.getData().get(Constants.KEY_USER_ID)
                );
                intent.putExtra(
                        Constants.REMOTE_MSG_INVITER_TOKEN,
                        remoteMessage.getData().get(Constants.REMOTE_MSG_INVITER_TOKEN)
                );
                intent.putExtra(
                        Constants.REMOTE_MSG_INVITER_TOKEN,
                        remoteMessage.getData().get(Constants.REMOTE_MSG_INVITER_TOKEN)
                );
                intent.putExtra(
                        Constants.REMOTE_MSG_MEETING_ROOM,
                        remoteMessage.getData().get(Constants.REMOTE_MSG_MEETING_ROOM)
                );
                Log.d("Calltest",  remoteMessage.getData().get(Constants.REMOTE_MSG_MEETING_ROOM).toString());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        } else {

            User user = new User();
            user.id = remoteMessage.getData().get(Constants.KEY_USER_ID);
            user.name = remoteMessage.getData().get(Constants.KEY_NAME);
            user.token = remoteMessage.getData().get(Constants.KEY_FCM_TOKEN);

            int notificationId = new Random().nextInt();
            String channelId = "catfish_area_message";

            Intent intent = new Intent(this, ChatActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra(Constants.KEY_USER, user);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            PendingIntent pendingIntent;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                pendingIntent = PendingIntent.getBroadcast(this, 1, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            } else {
                pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            }

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId);
            builder.setSmallIcon(R.drawable.icnotifications);
            builder.setContentTitle(user.name);
            builder.setContentText(remoteMessage.getData().get(Constants.KEY_MESSAGE));
            builder.setStyle(new NotificationCompat.BigTextStyle().bigText(
                    remoteMessage.getData().get(Constants.KEY_MESSAGE)
            ));
            builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
            builder.setAutoCancel(true);
            builder.setContentIntent(pendingIntent);


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                CharSequence channelName = "Chat Message";
                String channelDescription = "This notification channel is used for chat message notification";
                int importance = NotificationManager.IMPORTANCE_DEFAULT;
                NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
                channel.setDescription(channelDescription);
                //            NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                NotificationManager notificationManager = getSystemService(NotificationManager.class);
                notificationManager.createNotificationChannel(channel);
            }

            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
            notificationManagerCompat.notify(notificationId, builder.build());
        }


    }
}

