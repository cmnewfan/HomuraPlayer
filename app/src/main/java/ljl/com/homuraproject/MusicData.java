package ljl.com.homuraproject;

/**
 * Created by hzfd on 2016/4/29.
 */
public class MusicData {
    String mTitle;
    String mArtist;
    int mTrack;
    String mSource;
    /**
     * to describe music file
     *
     * @param title:  title of music
     * @param artist: artist of music
     */
    public MusicData(String title, String artist, int track, String source) {
        this.mTitle = title;
        this.mArtist = artist;
        this.mTrack = track;
        this.mSource = source;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getArtist() {
        return mArtist;
    }

    public String getSource() {
        return mSource;
    }
}
