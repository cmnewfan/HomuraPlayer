package ljl.com.homuraproject;

/**
 * Created by hzfd on 2016/1/5.
 */
public class MusicRunnable {
    public static Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if (FileActivity.seekBar.getProgress() < FileActivity.seekBar.getMax() - 1) {
                try {
                    if (HomuraPlayer.getCurrentInstance() != null && HomuraPlayer.getCurrentInstance().getPlayerState().equals("Playing")) {
                        FileActivity.seekBar.incrementProgressBy(1);
                        FileAdapter.sendMessage("PlayLrc");
                    }
                    FileActivity.seekBar.postDelayed(this, 1000);
                } catch (IllegalStateException ex) {
                    ex.printStackTrace();
                }
            }
        }
    };
    ;

}
