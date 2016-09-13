package ljl.com.homuraproject.Control;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQuery;

import java.io.File;

import ljl.com.homuraproject.Interfaces.DatabaseControl;
import ljl.com.homuraproject.MyApplication;

/**
 * Created by hzfd on 2016/9/8.
 */
public class LyricDatabase implements DatabaseControl {
    private static String DB_Name = "db_lyric";
    private SQLiteDatabase lrc_db;
    private static LyricDatabase mLyricDatabase = new LyricDatabase();
    private static String Table_Name = "LrcTable";
    private LyricDatabase(){
        ;
    }
    public static LyricDatabase getInstance(){
        return mLyricDatabase;
    }
    @Override
    public String query(File targetFile) {
        Cursor cursor = lrc_db.query(Table_Name,new String[]{"lyric_path"},"absolute_path=?",new String[]{targetFile.getAbsolutePath()},null, null, null);
        if(cursor.getCount()>0){
            String result = cursor.getString(0);
            cursor.close();
            return result;
        }
        else{
            cursor.close();
            return null;
        }
    }

    @Override
    public void insert(String targetFilePath, String targetLyricPath) {
        try {
            if(query(new File(targetFilePath))==null) {
                lrc_db.execSQL("insert into " + Table_Name + " values(" + targetFilePath + ", " + targetLyricPath + ")");
            }
            else{
                update(targetFilePath,targetLyricPath);
            }
        }
        catch(SQLException ex){
            ex.printStackTrace();
        }
    }

    @Override
    public void update(String targetFilePath, String targetLyricPath) {
        ContentValues cValues = new ContentValues();
        cValues.put("lyric_path",targetLyricPath);
        try {
            lrc_db.update(Table_Name, cValues, "absolute_path=?", new String[]{targetFilePath});
        }
        catch(SQLException ex){
            ex.printStackTrace();
        }
    }

    @Override
    public void close() {
        lrc_db.close();
    }

    @Override
    public void open() {
        lrc_db = MyApplication.getAppContext().openOrCreateDatabase(DB_Name,Context.MODE_PRIVATE,null);
        lrc_db.execSQL("Create TABLE if not exists "+Table_Name+" ( _id INTEGER primary key AUTOINCREMENT, absolute_path Text, lyric_path Text)");
    }
}
