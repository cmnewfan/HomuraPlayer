package ljl.com.homuraproject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.RemoteViews;

import java.io.File;
import java.util.ArrayList;

import ljl.com.homuraproject.Activity.FileActivity;
import ljl.com.homuraproject.Control.LyricControl;

/**
 * service for playing music
 * Created by hzfd on 2016/3/4.
 */
public class PlayService extends Service {
    final public static String NOTIFICATION_PAUSE = "Notification_Pause";
    final public static String NOTIFICATION_PLAY = "Notification_Play";
    final public static String NOTIFICATION_NEXT = "Notification_Next";
    final private static String BroadCastName = "com.Broadcast.PlayServiceBroadcast";
    final private static String[] SupportedCodec = new String[]{".ogg", ".mp3", ".m4a", ".flac", ".wmv"};
    final private static int NOTIFY_ID = 100;
    private static MediaPlayer myPlayer;
    private static String state;
    private static String lastFile;
    private static Notification notification;
    private static NotificationManager mNotificationManager;
    private static PlayService current;
    private static RemoteViews remoteViews;
    private static PendingIntent pendingIntent;

    /**
     * control view and view visibility
     *
     * @param viewid         view id
     * @param viewvisibility view visibility
     */
    public static void ControlNotificationView(int viewid, int viewvisibility) {
        remoteViews.setViewVisibility(viewid, viewvisibility);
        mNotificationManager.notify(NOTIFY_ID, notification);
    }

    public static int GetLength() {
        return myPlayer.getDuration() / 1000;
    }

    /**
     * get progress for seekbar
     * @return progress for seekbar
     */
    public static int GetProgress() {
        return myPlayer.getCurrentPosition() / 1000;
    }

    /**
     * control service to play music
     */
    public static void play() {
        if (myPlayer != null) {
            FileActivity.initSeekbarRunnable();
            myPlayer.start();
            FileActivity.RecordPlayingInformation();
            state = "Playing";
        }
    }

    /**
     * for notification to play music
     */
    public static void replay() {
        if (myPlayer != null && state.equals("NOTIFICATION_PAUSE")) {
            myPlayer.start();
            state = "Playing";
        }
    }

    /**
     * pause music
     */
    public static void pause() {
        if (myPlayer != null) {
            myPlayer.pause();
            state = "NOTIFICATION_PAUSE";
        }
    }

    /**
     * stop music
     */
    public static void stop() {
        if (myPlayer != null && (!state.equals("Stop"))) {
            myPlayer.stop();
            state = "Stop";
        }
    }

    /**
     * play the next song in the list
     *
     * @return false means it is the last song in the list, true means there are more than one songs on the list
     */
    public static boolean next() {
        if (FileActivity.currentPlayList != null) {
            // if there is more than one songs on the list, go to the next.
            if (FileActivity.currentPlayList.indexOf(FileActivity.currentPlayingFile) < FileActivity.currentPlayList.size() - 1) {
                if (state.equals("Playing")) {
                    PlayService.stop();
                }
                FileActivity.currentPlayingFile = FileActivity.currentPlayList.get(FileActivity.currentPlayList.indexOf(FileActivity.currentPlayingFile) + 1);
                LyricControl.sendCurrentLyric();
                Bundle bundle = new Bundle();
                bundle.putInt("op", 1);
                bundle.putInt("LastTime", 0);
                bundle.putString("file_path", FileActivity.currentPlayingFile.getAbsolutePath());
                PostMan.sendMessage(Constants.PlayServiceCommand, Constants.PlayServiceCommand_Play, bundle);
                FileActivity.NotifyDataChangd();
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * @param targetCodec
     * @return
     */
    public static Boolean isSupportedCodec(String targetCodec) {
        for (String codec : SupportedCodec
                ) {
            if (codec.equals(targetCodec)) {
                return true;
            }
        }
        return false;
    }

    public static void UpdateNotification(String title, String artist) {
        remoteViews.setTextViewText(R.id.Title, title);
        remoteViews.setTextViewText(R.id.Artist, artist);
        mNotificationManager.notify(NOTIFY_ID, notification);
    }

    public static void RemoveNotification() {
        mNotificationManager.cancel(NOTIFY_ID);
    }

    public static Intent CreateNewIntent(int operation, int last_time, String file_path) {
        Intent intent = new Intent("com.service.PlayService");
        Bundle bundle = new Bundle();
        bundle.putInt("op", operation);
        bundle.putInt("LastTime", last_time);
        bundle.putString("file_path", file_path);
        intent.putExtras(bundle);
        return intent;
    }

    public static boolean exist() {
        return myPlayer != null;
    }

    public static String getPlayerState() {
        return state;
    }

    public static void seekTo(int position) {
        if (myPlayer != null) {
            myPlayer.seekTo(position);
        }
    }

    public static void release() {
        myPlayer.release();
        state = "release";
        //current.stopForeground(false);
        current.stopForeground(true);
        mNotificationManager.cancel(100);
        current.stopSelf();
    }

    public static String getLastFile() {
        return lastFile;
    }

    public static int getLastProgress() {
        return myPlayer.getCurrentPosition() / 1000;
    }

    public static void generatePlayList(File tempFile, File[] files) {
        FileActivity.currentPlayingFile = tempFile;
        FileActivity.currentPlayList = new ArrayList<File>();
        FileActivity.currentPlayList.add(tempFile);
        Boolean flag = false;
        for (int i = 0; i < files.length; i++) {
            File tFile = files[i];
            if (!tFile.isDirectory()) {
                if (isSupportedCodec(tempFile.getName().substring(tempFile.getName().lastIndexOf(".")).toLowerCase())) {
                    if (!tFile.getAbsolutePath().equals(tempFile.getAbsolutePath()) && flag) {
                        FileActivity.currentPlayList.add(tFile);
                    } else if (tFile.getAbsolutePath().equals(tempFile.getAbsolutePath())) {
                        flag = true;
                    }
                }
            }
        }
        LyricControl.sendCurrentLyric();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //init notification
        remoteViews = new RemoteViews(getPackageName(), R.layout.custom_notification);
        final PendingIntent PlayIntent = getPendingSelfIntent(this, BroadCastName, NOTIFICATION_PLAY);
        final PendingIntent PauseIntent = getPendingSelfIntent(this, BroadCastName, NOTIFICATION_PAUSE);
        final PendingIntent NextIntent = getPendingSelfIntent(this, BroadCastName, NOTIFICATION_NEXT);
        final Intent deleteIntent = new Intent(this, PlayService.class);
        deleteIntent.putExtra("Del", 1);
        PendingIntent deletePendingIntent = PendingIntent.getService(this,
                1,
                deleteIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.notification_next, NextIntent);
        remoteViews.setOnClickPendingIntent(R.id.notification_play, PlayIntent);
        remoteViews.setOnClickPendingIntent(R.id.notification_pause, PauseIntent);
        mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent notificationIntent = new Intent(this, FileActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        notification = new Notification.Builder(this).setSmallIcon(R.drawable.icon).
                setTicker("HomuHomu").
                setContentTitle("HomuHomu").
                setContentIntent(pendingIntent).
                setContent(remoteViews).
                setDeleteIntent(deletePendingIntent).
                build();
        startForeground(NOTIFY_ID, notification);
        current = this;
    }

    private PendingIntent getPendingSelfIntent(Context context, String action, String extra) {
        Intent intent = new Intent();
        intent.setAction(action);
        intent.setPackage(getPackageName());
        intent.putExtra("Extra", extra);
        if (extra.equals(NOTIFICATION_PLAY))
            return PendingIntent.getBroadcast(context, 0, intent, 0);
        else if (extra.equals(NOTIFICATION_PAUSE))
            return PendingIntent.getBroadcast(context, 1, intent, 0);
        else
            return PendingIntent.getBroadcast(context, 2, intent, 0);
    }

    @Override
    public void onDestroy() {
        release();
        stopForeground(true);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                int op = bundle.getInt("op");
                int del = bundle.getInt("Del", 0);
                if (del == 1) {
                    stopSelf();
                }
                int lastTime = bundle.getInt("LastTime");
                switch (op) {
                    case 1:
                        stop();
                        init(Uri.fromFile(new File(bundle.getString("file_path"))), lastTime / 1000);
                        seekTo(lastTime);
                        play();
                        break;
                    case 2:
                        stop();
                        break;
                    case 3:
                        pause();
                        break;
                }
            }
        }
        return START_NOT_STICKY;
    }

    private void init(Uri uri, int startTime) {
        this.myPlayer = MediaPlayer.create(this, uri);
        if (this.myPlayer == null) {
            return;
        }
        FileActivity.SetSeekbarMax(this.myPlayer.getDuration() / 1000);
        FileActivity.SetSeekbarProgress(startTime);
        PostMan.sendMessage(Constants.PlayServiceCommand, Constants.PlayServiceCommand_PlayFromService);
        lastFile = new File(String.valueOf(uri)).getName();
        this.myPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                FileActivity.SetSeekbarProgress(FileActivity.GetSeekBarMax());
                if (FileActivity.currentPlayList.indexOf(FileActivity.currentPlayingFile) < FileActivity.currentPlayList.size() - 1) {
                    if (myPlayer != null && myPlayer.isPlaying()) {
                        myPlayer.stop();
                    }
                    init(Uri.fromFile(FileActivity.currentPlayList.get(FileActivity.currentPlayList.indexOf(FileActivity.currentPlayingFile) + 1)), 0);
                    FileActivity.currentPlayingFile = FileActivity.currentPlayList.get(FileActivity.currentPlayList.indexOf(FileActivity.currentPlayingFile) + 1);
                    LyricControl.sendCurrentLyric();
                    play();
                    PostMan.sendMessage(Constants.PlayServiceCommand, Constants.PlayServiceCommand_PlayFromService);
                    FileActivity.NotifyDataChangd();
                } else {
                    FileActivity.SetSeekbarProgress(0);
                    PostMan.sendMessage(Constants.PlayServiceCommand, Constants.PlayServiceCommand_Stop);
                    PostMan.sendMessage(Constants.ViewControl, Constants.ViewControl_SetTitle);
                }
            }
        });
    }
}
