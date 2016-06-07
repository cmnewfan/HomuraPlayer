package ljl.com.homuraproject.Control;

import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import ljl.com.homuraproject.Activity.FileActivity;
import ljl.com.homuraproject.Constants;
import ljl.com.homuraproject.PlayService;
import ljl.com.homuraproject.PostMan;

/**
 * Created by hzfd on 2016/1/7.
 */
public class FileIO {
    private final static File Folder = Environment.getExternalStorageDirectory();

    /**
     * save lyrics to lrc file
     *
     * @param lyrics:text   of lyrics
     * @param fileName:name of new lrc file
     * @return the result
     * @throws IOException
     */
    public static boolean SaveLyric(String lyrics, String fileName) throws IOException {
        FileOutputStream fos = null;
        OutputStreamWriter osw = null;
        BufferedWriter bw = null;
        try {
            if (Environment.getExternalStorageState().equals(
                    Environment.MEDIA_MOUNTED)) {
                File f = new File(Folder + "/Lyrics/" + fileName + ".lrc");
                if (f.exists()) {
                    f.delete();
                }
                f.createNewFile();
                f.setWritable(true);
                fos = new FileOutputStream(f);
                osw = new OutputStreamWriter(fos, "GBK");
                bw = new BufferedWriter(osw);
            }
        } catch (FileNotFoundException e) {
            return false;
        }
        bw.write(lyrics);
        bw.close();
        osw.close();
        fos.close();
        return true;
    }

    public static void DeleteFile(String file_path) {
        File target_file = new File(file_path);
        if (target_file.isDirectory()) {
            //delete directory
            if (FileActivity.currentPlayingFile.getAbsolutePath().contains(target_file.getAbsolutePath())) {
                PlayService.stop();
                for (File f : target_file.listFiles()
                        ) {
                    f.delete();
                }
            }
        } else {
            //delete file
            if (target_file.getAbsolutePath().equals(FileActivity.currentPlayingFile.getAbsolutePath())) {
                PlayService.next();
                if (target_file.delete()) {
                    //delete success
                    PostMan.sendMessage(Constants.ViewControl, Constants.ViewControl_ToastSuccess);
                } else {
                    PostMan.sendMessage(Constants.ViewControl, Constants.ViewControl_ToastMiss);
                }
            } else {
                target_file.delete();
            }
        }
    }
}
