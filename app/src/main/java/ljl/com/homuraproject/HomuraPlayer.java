package ljl.com.homuraproject;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;

/**
 * Created by hzfd on 2015/12/30.
 */
public class HomuraPlayer {
    private MediaPlayer myPlayer;
    private static HomuraPlayer homura = null;
    private String state = "None";

    private HomuraPlayer(Uri uri, Context context) {
        this.myPlayer = MediaPlayer.create(context, uri);
        FileActivity.seekBar.setMax(this.myPlayer.getDuration() / 1000);
        FileActivity.seekBar.setProgress(0);
        this.myPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                FileActivity.seekBar.setProgress(FileActivity.seekBar.getMax());
            }
        });
    }

    public static HomuraPlayer getInstance(Uri uri, Context context) {
        if (homura != null && homura.myPlayer.isPlaying()) {
            homura.myPlayer.stop();
        }
        homura = new HomuraPlayer(uri, context);
        return homura;
    }

    public static HomuraPlayer getCurrentInstance() {
        return homura;
    }

    public void start() {
        FileActivity.seekBar.removeCallbacks(FileAdapter.musicRunnable);
        FileActivity.seekBar.postDelayed(FileAdapter.musicRunnable, 1000);
    }

    public void play() {
        if (this.myPlayer != null) {
            this.start();
            this.myPlayer.start();
            this.state = "Playing";
        }
    }

    public void pause() {
        if (this.myPlayer != null) {
            this.myPlayer.pause();
            this.state = "Pause";
        }
    }

    public void stop() {
        if (this.myPlayer != null) {
            this.myPlayer.stop();
            this.state = "Stop";
        }
    }

    public String getPlayerState() {
        return this.state;
    }

    public void seekTo(int position) {
        this.myPlayer.seekTo(position);
    }

    public void release() {
        this.myPlayer.release();
        this.state = "release";
    }
}
