package ljl.com.homuraproject;

/**
 * Created by hzfd on 2016/4/29.
 */
public class MusicData {
    String mTitle;
    String mArtist;

    /**
     * to describe music file
     *
     * @param title:  title of music
     * @param artist: artist of music
     */
    public MusicData(String title, String artist) {
        this.mTitle = title;
        this.mArtist = artist;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getArtist() {
        return mArtist;
    }
}
