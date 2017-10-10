package ljl.com.homuraproject.Control;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;
import java.util.Random;

import ljl.com.homuraproject.MusicData;
import ljl.com.homuraproject.MyApplication;

/**
 * Created by hzfd on 2017/10/9.
 */

public class FavouriteDatabase {
    private static String DB_Name = "db_lyric";
    private static SQLiteDatabase fav_db;
    private static FavouriteDatabase mLyricDatabase = new FavouriteDatabase();
    private static String Table_Name = "FavouriteTable";

    private FavouriteDatabase() {
        ;
    }

    public static FavouriteDatabase getInstance() {
        if (fav_db == null) {
            fav_db = MyApplication.getAppContext().openOrCreateDatabase(DB_Name, Context.MODE_PRIVATE, null);
            fav_db.execSQL("Create TABLE if not exists " + Table_Name + " ( _id INTEGER primary key AUTOINCREMENT, track_path Text, track_name Text, track INTEGER, track_artist Text)");
        }
        return mLyricDatabase;
    }

    private boolean IsTrackAvailible(String track_path) {
        try {
            if (new File(track_path).exists()) {
                return true;
            } else {
                return false;
            }
        } catch (SecurityException ex) {
            return false;
        }
    }


    public MusicData[] query() {
        Cursor cursor = fav_db.query(Table_Name, new String[]{"_id", "track_path", "track_name", "track_artist", "track"}, null, null, null, null, null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            int count = cursor.getCount();
            MusicData[] result = new MusicData[cursor.getCount()];
            Random random = new Random(cursor.getCount());
            while (count > 0) {
                int random_index = random.nextInt(cursor.getCount());
                if (result[random_index] == null) {
                    result[random_index] = new MusicData(cursor.getString(2), cursor.getString(3), cursor.getInt(4), cursor.getString(1));
                    count--;
                    cursor.moveToNext();
                }
            }
            cursor.close();
            return result;
        } else {
            cursor.close();
            return null;
        }
    }

    public String isTagged(String track_path, String track_name) {
        Cursor cursor = fav_db.query(Table_Name, new String[]{"_id"}, "track_name=? and track_path=?", new String[]{track_name, track_path}, null, null, null);
        if (cursor.getCount() > 0) {
            cursor.close();
            return "Tagged";
        } else {
            cursor.close();
            return "Untagged";
        }
    }

    private boolean query(String track_name, String track_path) {
        Cursor cursor = fav_db.query(Table_Name, new String[]{"_id", "track_path"}, "track_name=?", new String[]{track_name}, null, null, null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            while (true) {
                if (track_path.equals(cursor.getString(1))) {
                    return false;
                } else {
                    if (!IsTrackAvailible(cursor.getString(1))) {
                        delete(cursor.getInt(0));
                    }
                }
                if (cursor.isLast()) {
                    break;
                } else {
                    cursor.moveToNext();
                }
            }
            cursor.close();
            return true;
        } else {
            cursor.close();
            return true;
        }
    }

    private void delete(int id) {
        fav_db.delete(Table_Name, "_id=?", new String[]{String.valueOf(id)});
    }

    public void delete(String track_path, String track_name) {
        fav_db.execSQL("delete from " + Table_Name + " where track_path=\'" + track_path + "\' and track_name=\'" + track_name + "\'");
        //fav_db.delete(Table_Name,"track_path=? and track_name=?",new String[]{track_path, track_name});
    }

    public void insert(String track_name, String track_path, int track, String track_artist) {
        try {
            if (query(track_name, track_path)) {
                ContentValues values = new ContentValues();
                values.put("track_path", track_path);
                values.put("track_artist", track_artist);
                values.put("track", track);
                values.put("track_name", track_name);
                fav_db.insert(Table_Name, null, values);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void update(String track_name, String track_path) {
        ContentValues cValues = new ContentValues();
        cValues.put("track_path", track_path);
        try {
            fav_db.update(Table_Name, cValues, "track_name=?", new String[]{track_name});
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void close() {

    }
}
