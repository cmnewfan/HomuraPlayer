package ljl.com.homuraproject;

/**
 * music data, including title, artist and track id.
 * Created by hzfd on 2016/4/29.
 */
public class MusicData {
    String mTitle;
    String mArtist;
    int mTrack;
    String mSource;
    /**
     * to describe music file
     * @param track track id of music
     * @param source absolute path of music
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

    public int getTrack() {
        return mTrack;
    }
}
