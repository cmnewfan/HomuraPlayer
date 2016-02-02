package ljl.com.homuraproject;

import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

/**
 * Created by hzfd on 2016/1/7.
 */
public class FileIO {
    public final static String LyricFolder = "/storage/sdcard1/Lyrics";

    public static boolean SaveLyric(String lyrics, String fileName) throws IOException {
        FileOutputStream fos = null;
        OutputStreamWriter osw = null;
        BufferedWriter bw = null;
        try {
            if (Environment.getExternalStorageState().equals(
                    Environment.MEDIA_MOUNTED)) {
                File f = new File(FileIO.LyricFolder + "/" + fileName + ".lrc");
                if (f.exists()) {
                    f.delete();
                }
                f.createNewFile();
                f.setWritable(true);
                fos = new FileOutputStream(f);
                osw = new OutputStreamWriter(fos, "GBK");
                bw = new BufferedWriter(osw);
            }
            //fos = new FileOutputStream(new File(LyricFolder,fileName+".lrc"));
            //fos = new FileOutputStream(android.os.Environment.getExternalStorageDirectory()+"/1.txt");
            //fos = new FileOutputStream(android.os.Environment.getExternalStorageDirectory() + "/gps.txt",true);
        } catch (FileNotFoundException e) {
            return false;
        }
        bw.write(lyrics);
        //fos.flush();
        bw.close();
        osw.close();
        fos.close();
        return true;
    }

    public static boolean SaveLyric(ArrayList lyrics, String fileName) throws IOException {
        FileOutputStream fos = null;
        OutputStreamWriter osw = null;
        BufferedWriter bw = null;
        try {
            if (Environment.getExternalStorageState().equals(
                    Environment.MEDIA_MOUNTED)) {
                File f = new File(FileIO.LyricFolder + "/" + fileName + ".lrc");
                if (f.exists()) {
                    f.delete();
                }
                f.createNewFile();
                f.setWritable(true);
                fos = new FileOutputStream(f);
                osw = new OutputStreamWriter(fos, "GBK");
                bw = new BufferedWriter(osw);
            }
            //fos = new FileOutputStream(new File(LyricFolder,fileName+".lrc"));
            //fos = new FileOutputStream(android.os.Environment.getExternalStorageDirectory()+"/1.txt");
            //fos = new FileOutputStream(android.os.Environment.getExternalStorageDirectory() + "/gps.txt",true);
        } catch (FileNotFoundException e) {
            return false;
        }
        if (lyrics != null && bw != null) {
            for (int i = 0; i < lyrics.size(); i++) {
                //fos.write(lyrics.get(i).toString().getBytes());
                bw.write(lyrics.get(i).toString());
                bw.newLine();
            }
        } else {
            return false;
        }
        //fos.flush();
        bw.close();
        osw.close();
        fos.close();
        return true;
    }
}
