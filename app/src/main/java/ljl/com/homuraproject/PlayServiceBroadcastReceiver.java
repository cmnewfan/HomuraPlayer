package ljl.com.homuraproject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.View;

/**
 * Created by hzfd on 2016/3/8.
 */
public class PlayServiceBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String fuck = intent.getStringExtra("Extra");
        if (intent.getStringExtra("Extra").equals(PlayService.Pause)) {
            PlayService.pause();
            PlayService.ControlNotificationView(R.id.notification_play, View.VISIBLE);
            PlayService.ControlNotificationView(R.id.notification_pause, View.GONE);
            PlayService.SendMessageToMain("Pause");
        } else if (intent.getStringExtra("Extra").equals(PlayService.Play)) {
            PlayService.replay();
            PlayService.ControlNotificationView(R.id.notification_pause, View.VISIBLE);
            PlayService.ControlNotificationView(R.id.notification_play, View.GONE);
            PlayService.SendMessageToMain("Play2");
        } else if (intent.getStringExtra("Extra").equals(PlayService.Next)) {
            PlayService.next();
        }
    }
}
