package ljl.com.homuraproject;

import android.net.Uri;

/**
 * Created by hzfd on 2016/1/5.
 */
public class MusicRunnable {
    public static Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if (FileActivity.seekBar.getProgress() < FileActivity.seekBar.getMax() - 1) {
                try {
                        /*if (FileActivity.currentMediaPlayer.isPlaying()) {
                            FileActivity.seekBar.incrementProgressBy(1);
                            sendMessage("PlayLrc");
                        }*/
                    if (HomuraPlayer.getCurrentInstance() != null && HomuraPlayer.getCurrentInstance().getPlayerState().equals("Playing")) {
                        FileActivity.seekBar.incrementProgressBy(1);
                        FileAdapter.sendMessage("PlayLrc");
                    }
                    FileActivity.seekBar.postDelayed(this, 1000);
                } catch (IllegalStateException ex) {
                    ex.printStackTrace();
                }
            } else {
                if (HomuraPlayer.getCurrentInstance() != null && HomuraPlayer.getCurrentInstance().getPlayerState().equals("Playing")) {
                    HomuraPlayer.getCurrentInstance().stop();
                }
                if (FileActivity.currentPlayList.indexOf(FileActivity.currentPlayingFile) < FileActivity.currentPlayList.size() - 1) {
                    //Test
                    HomuraPlayer player = HomuraPlayer.getInstance(Uri.fromFile(FileActivity.currentPlayList.get(FileActivity.currentPlayList.indexOf(FileActivity.currentPlayingFile) + 1)), MyApplication.getAppContext());
                    FileActivity.currentPlayingFile = FileActivity.currentPlayList.get(FileActivity.currentPlayList.indexOf(FileActivity.currentPlayingFile) + 1);
                    FileAdapter.sendCurrentLyric();
                    player.play();
                    FileAdapter.sendMessage("Play");
                    FileActivity.fileAdapter.notifyDataSetChanged();
                } else {
                    FileAdapter.sendMessage("Stop");
                    FileAdapter.sendMessage("SetTitle");
                }
            }
        }
    };
    ;

}
