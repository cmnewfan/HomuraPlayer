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
        if (intent.getStringExtra("Extra").equals(PlayService.NOTIFICATION_PAUSE)) {
            PlayService.pause();
            PlayService.ControlNotificationView(R.id.notification_play, View.VISIBLE);
            PlayService.ControlNotificationView(R.id.notification_pause, View.GONE);
            PostMan.sendMessage(Constants.PlayServiceCommand, Constants.PlayServiceCommand_Pause);
        } else if (intent.getStringExtra("Extra").equals(PlayService.NOTIFICATION_PLAY)) {
            PlayService.replay();
            PlayService.ControlNotificationView(R.id.notification_pause, View.VISIBLE);
            PlayService.ControlNotificationView(R.id.notification_play, View.GONE);
            PostMan.sendMessage(Constants.PlayServiceCommand, Constants.PlayServiceCommand_PlayFromNotification);
        } else if (intent.getStringExtra("Extra").equals(PlayService.NOTIFICATION_NEXT)) {
            PlayService.next();
        }
    }
}
