package ljl.com.homuraproject;

import android.os.Bundle;

import ljl.com.homuraproject.Activity.FileActivity;

/**
 * runnable for seekbar in FileActivity
 * Created by hzfd on 2016/1/5.
 */
public class MusicRunnable {
    /**
     * to control progress bar and lyric view
     */
    public static Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if (FileActivity.GetSeekbarProgress() < FileActivity.GetSeekBarMax() - 1) {
                try {
                    if (PlayService.exist() && PlayService.getPlayerState().equals("Playing")) {
                        //increment seekbar and update lyric per second
                        FileActivity.SeekbarIncrement(1);
                        PostMan.sendMessage(Constants.ViewControl, Constants.ViewControl_PlayLrc);
                        if (PlayService.HasCueModel()) {
                            Bundle bundle = new Bundle();
                            bundle.putString("Title", PlayService.getCueModel().getCurrentTrack(FileActivity.GetSeekbarProgress() * 1000).getTitle());
                            bundle.putString("Performer", PlayService.getCueModel().getCurrentTrack(FileActivity.GetSeekbarProgress() * 1000).getPerformer());
                            PostMan.sendMessage(Constants.ViewControl, Constants.ViewControl_SetMusicTitleFromCue, bundle);
                        }
                    }
                    FileActivity.SeekBarPost(this, 1000);
                } catch (IllegalStateException ex) {
                    ex.printStackTrace();
                }
            }
        }
    };
}
