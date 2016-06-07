package ljl.com.homuraproject;

import ljl.com.homuraproject.Activity.FileActivity;

/**
 * Created by hzfd on 2016/1/5.
 */
public class MusicRunnable {
    /**
     * to control progress bar and lyric view
     */
    public static Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if (FileActivity.seekBar.getProgress() < FileActivity.seekBar.getMax() - 1) {
                try {
                    if (PlayService.exist() && PlayService.getPlayerState().equals("Playing")) {
                        FileActivity.seekBar.incrementProgressBy(1);
                        //FileAdapter.sendMessage("PlayLrc");
                        PostMan.sendMessage(Constants.ViewControl, Constants.ViewControl_PlayLrc);
                    }
                    FileActivity.seekBar.postDelayed(this, 1000);
                } catch (IllegalStateException ex) {
                    ex.printStackTrace();
                }
            }
        }
    };
}
