package ljl.com.homuraproject;

import java.util.ArrayList;

/**
 * Created by hzfd on 2016/8/1.
 */
public class CUE_Model {
    private String mFilePath;
    private String mDate;
    private String mGenre;
    private ArrayList<CUE_Track> mTracks;
    private int mCurrentIndex;

    public CUE_Model(String date, String genre, String file_path, ArrayList<CUE_Track> tracks) {
        this.mDate = date;
        this.mGenre = genre;
        this.mFilePath = file_path;
        this.mTracks = tracks;
        this.mCurrentIndex = 0;
    }

    public String getFilePath() {
        return mFilePath;
    }

    public CUE_Track getCurrentTrack(int time) {
        mCurrentIndex = 0;
        for (int i = 0; i < mTracks.size(); i++) {
            if (time <= mTracks.get(i).getStart_time()) {
                mCurrentIndex = i - 1;
                break;
            }
        }
        return mTracks.get(mCurrentIndex);
    }

    public CUE_Track getNextTrack(int index) {
        if (mCurrentIndex + index >= 0 && mCurrentIndex + index < mTracks.size()) {
            mCurrentIndex = mCurrentIndex + index;
            return mTracks.get(mCurrentIndex);
        }
        return null;
    }

    public Boolean IsEnableToMoveToNextOrPrev(int index) {
        if (mCurrentIndex + index >= 0 && mCurrentIndex + index < mTracks.size()) {
            return true;
        }
        return false;
    }
}
