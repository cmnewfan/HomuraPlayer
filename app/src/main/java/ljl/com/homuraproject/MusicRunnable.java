package ljl.com.homuraproject;

import android.os.Bundle;

import ljl.com.homuraproject.Activity.FileActivity;
import ljl.com.homuraproject.Control.LyricControl;

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
                        if (PlayService.HasCueModel()) {
                            int progress = FileActivity.GetSeekbarProgress() * 1000;
                            String title = PlayService.getCueModel().getCurrentTrack(progress).getTitle();
                            String performer = PlayService.getCueModel().getCurrentTrack(progress).getPerformer();
                            int start_time = PlayService.getCueModel().getCurrentTrack(progress).getStart_time();
                            LyricControl.setCurrentArtistFromCUE(performer);
                            LyricControl.setCurrentPlayingTitleFromCUE(title);
                            if (!(performer + "-" + title).equals(LyricControl.getCurrentLyricFileName())) {
                                LyricControl.Update();
                            }
                            Bundle bundle = new Bundle();
                            bundle.putString("Title", title);
                            bundle.putString("Performer", performer);
                            bundle.putInt("LyricTime", progress - start_time);
                            PostMan.sendMessage(Constants.ViewControl, Constants.ViewControl_SetMusicTitleFromCue, bundle);
                            PostMan.sendMessage(Constants.ViewControl, Constants.ViewControl_PlayLrcFromCue, bundle);
                        } else {
                            PostMan.sendMessage(Constants.ViewControl, Constants.ViewControl_PlayLrc);
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
