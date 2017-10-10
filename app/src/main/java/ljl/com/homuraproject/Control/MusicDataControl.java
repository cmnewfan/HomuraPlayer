package ljl.com.homuraproject.Control;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import java.io.File;

import ljl.com.homuraproject.MusicData;
import ljl.com.homuraproject.MyApplication;
/**
 * music database controller
 * Created by hzfd on 2016/4/29.
 */
public class MusicDataControl {
    /**
     * to get the MusicData of music file
     *
     * @param mContext
     * @param fileName: name of music file
     * @return MusicData or null
     */
    public static MusicData query(Context mContext, String[] fileName) {
        MusicData mData = null;
        Cursor c = mContext.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Video.Media.TITLE,
                        MediaStore.Audio.Media.DATA,
                        MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media.DISPLAY_NAME,
                        MediaStore.Audio.Media.TRACK
                },
                "_display_name=?",
                fileName,
                null);
        if (c.getCount() > 0) {
            c.moveToFirst();
            mData = new MusicData(c.getString(0), c.getString(2), c.getInt(4), c.getString(1));
            c.close();
            return mData;
        } else {
            c.close();
            return mData;
        }
    }

    /**
     * get music data from its file
     *
     * @param file
     * @return
     */
    public static MusicData getMusicDataFromFile(File file) {
        MusicData mData = null;
        //use content resolver to query
        Cursor c = MyApplication.getAppContext().getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Video.Media.TITLE,
                        MediaStore.Audio.Media.DATA,
                        MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media.DISPLAY_NAME,
                        MediaStore.Audio.Media.TRACK,
                        MediaStore.Audio.Media.DEFAULT_SORT_ORDER,
                        MediaStore.Audio.AudioColumns.TRACK,
                },
                "_data=?",
                new String[]{file.getAbsolutePath()},
                null);
        if (c.getCount() > 0) {
            c.moveToFirst();
            mData = new MusicData(c.getString(0), c.getString(2), c.getInt(4), c.getString(1));
            c.close();
            return mData;
        } else {
            c.close();
            return mData;
        }
    }

    public static Integer getFileTrack(File file) {
        Cursor c = MyApplication.getAppContext().getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Media.TRACK
                },
                "_data=?",
                new String[]{file.getAbsolutePath()},
                null);
        if (c.getCount() > 0) {
            return Integer.getInteger(c.getString(0));
        } else {
            return 0;
        }
    }

    /**
     * sort music data by track id
     * use insertion sort
     * @param source_data source to be sorted
     * @return sorted music data array
     */
    public static MusicData[] SortedMusicData(MusicData[] source_data) {
        MusicData temp;
        for (int i = 0; i < source_data.length; i++) {
            temp = source_data[i];
            int j;
            for (j = i - 1; j >= 0; j--) {
                if (source_data[j].getTrack() > temp.getTrack()) {
                    source_data[j + 1] = source_data[j];
                } else {
                    break;
                }
            }
            source_data[j + 1] = temp;
        }
        return source_data;
    }
}
