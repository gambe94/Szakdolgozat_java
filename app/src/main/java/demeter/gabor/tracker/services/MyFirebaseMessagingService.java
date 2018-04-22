package demeter.gabor.tracker.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;

import demeter.gabor.tracker.MainActivity;
import demeter.gabor.tracker.R;
import demeter.gabor.tracker.UserMapsActivity;
import demeter.gabor.tracker.Util.Constants;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FirebaseMessagingServce";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {


        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle(); //get title
            String message = remoteMessage.getNotification().getBody(); //get message

            Map<String, String> extras = new HashMap<>();

            if(remoteMessage.getData().size() > 0){
                extras.put(Constants.LONGITUDE, remoteMessage.getData().get(Constants.LONGITUDE));
                extras.put(Constants.LATITUDE, remoteMessage.getData().get(Constants.LATITUDE));
                extras.put(Constants.USERNAME, remoteMessage.getData().get(Constants.USERNAME));
                extras.put(Constants.CURRENTUSER_UID, remoteMessage.getData().get(Constants.CURRENTUSER_UID));
            }

            Log.d(TAG, "Message Notification Title: " + title);
            Log.d(TAG, "Message Notification Body: " + message);

            sendNotification(title, message, extras);
        }
    }

    @Override
    public void onDeletedMessages() {

    }

    private void sendNotification(String title,String messageBody, Map<String,String> extras) {
        Intent intent = new Intent(this, UserMapsActivity.class);
        for(String key : extras.keySet()){
            if(key == Constants.LATITUDE || key== Constants.LONGITUDE){
                intent.putExtra(key, Double.parseDouble(extras.get(key)));
            }else{
                intent.putExtra(key, extras.get(key));
            }
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);



        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setChannelId(getString(R.string.default_notification_channel_id))
                .setContentTitle(title)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}