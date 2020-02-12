package io.puzzlebox.jigsaw.wear;

//public class ListenerService {
//}

import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

public class ListenerService extends WearableListenerService {

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.i("test", "onMessageReceived()");
//        if(messageEvent.getPath().equals(Constants.PATH_NOTIFICAITON_MESSAGE)) {
//            final String message = new String(messageEvent.getData());
//            NotificationCompat.Builder b = new NotificationCompat.Builder(this);
//            b.setContentText(message);
//            b.setSmallIcon(R.drawable.ic_launcher);
//            b.setContentTitle("Test Notification");
//            b.setLocalOnly(true);
//            NotificationManagerCompat man = NotificationManagerCompat.from(this);
//            man.notify(0, b.build());
//        } else {
        super.onMessageReceived(messageEvent);
//        }
    }
}