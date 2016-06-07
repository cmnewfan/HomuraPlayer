package ljl.com.homuraproject;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

/**
 * Created by hzfd on 2016/4/29.
 */
public class MusicDatabase {
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
                        MediaStore.Audio.Media.DURATION,
                        MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media.DISPLAY_NAME,
                },
                "_display_name=?",
                fileName,
                null);
        c.moveToFirst();
        if (c.getCount() > 0) {
            mData = new MusicData(c.getString(0), c.getString(2));
            return mData;
        } else {
            return mData;
        }
    }
}
