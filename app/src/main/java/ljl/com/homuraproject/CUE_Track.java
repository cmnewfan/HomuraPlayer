package ljl.com.homuraproject;

/**
 * Created by hzfd on 2016/8/1.
 */
public class CUE_Track {
    private String mTrack_id;
    private String mTitle;
    private String mPerformer;
    private int mStart_time;
    private int mEmpty_time;

    public int getStart_time() {
        return mStart_time;
    }

    public String getTrack_id() {
        return mTrack_id;
    }

    public String getPerformer() {
        return mPerformer;
    }

    public String getTitle() {
        return mTitle;
    }

    public CUE_Track(String track_id, String title, String performer, int start_time, int empty_time) {
        this.mTrack_id = track_id;
        this.mTitle = title;
        this.mPerformer = performer;
        this.mStart_time = start_time;
        this.mEmpty_time = empty_time;
    }

}
