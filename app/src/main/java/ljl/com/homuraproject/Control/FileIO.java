package ljl.com.homuraproject.Control;

import android.net.Uri;
import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import ljl.com.homuraproject.Activity.FileActivity;
import ljl.com.homuraproject.Adapter.FileAdapter;
import ljl.com.homuraproject.Constants;
import ljl.com.homuraproject.MusicData;
import ljl.com.homuraproject.PlayService;
import ljl.com.homuraproject.PostMan;

/**
 * file control
 * Created by hzfd on 2016/1/7.
 */
public class FileIO {
    private final static File Folder = Environment.getExternalStorageDirectory();

    /**
     * save lyrics to lrc file
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
            File f = new File(Constants.LyricFolder + fileName + ".lrc");
            if (f.exists()) {
                f.delete();
            }
            f.createNewFile();
            f.setWritable(true);
            fos = new FileOutputStream(f);
            osw = new OutputStreamWriter(fos, "GBK");
            bw = new BufferedWriter(osw);
        } catch (FileNotFoundException e) {
            return false;
        }
        bw.write(lyrics);
        bw.close();
        osw.close();
        fos.close();
        return true;
    }

    /**
     * delete file or directory
     *
     * @param file_path absolute file path of target file
     */
    public static void DeleteFile(String file_path) {
        File target_file = new File(file_path);
        //if target file is directory
        if (target_file.isDirectory()) {
            //delete directory
            File parent_file = target_file.getParentFile();
            if (FileActivity.currentPlayingFile.getAbsolutePath().contains(target_file.getAbsolutePath())) {
                PlayService.stop();
            }
            //delete each file in dorectory
            for (File f : target_file.listFiles()
                    ) {
                DeleteFile(f.getAbsolutePath());
            }
            //delete directory
            target_file.delete();
            //sort parent file
            FileAdapter.files = FileIO.SortFiles(parent_file);
            FileActivity.NotifyDataChangd();
        } else {
            File parent_file = target_file.getParentFile();
            //delete file
            if (target_file.getAbsolutePath().equals(FileActivity.currentPlayingFile.getAbsolutePath())) {
                if (target_file.delete()) {
                    //delete success
                    //if file is last music on list
                    if (!PlayService.next()) {
                        FileActivity.currentPlayingFile = null;
                        FileActivity.resetLastPlayingState();
                        FileActivity.RecordPlayingInformation();
                    } else {
                        PlayService.generatePlayList(FileActivity.currentPlayingFile, FileIO.SortFiles(parent_file));
                        FileAdapter.files = FileIO.SortFiles(parent_file);
                    }
                    FileActivity.NotifyDataChangd();
                    PostMan.sendMessage(Constants.ViewControl, Constants.ViewControl_ToastSuccess);
                } else {
                    PostMan.sendMessage(Constants.ViewControl, Constants.ViewControl_ToastMiss);
                }
            } else {
                target_file.delete();
                FileAdapter.files = FileIO.SortFiles(parent_file);
                PlayService.generatePlayList(FileActivity.currentPlayingFile, FileIO.SortFiles(FileActivity.currentPlayingFile.getParentFile()));
                FileActivity.NotifyDataChangd();
            }
        }
    }

    /**
     * sort files
     * @param file_directory absolute file path of directory
     * @return sorted files
     */
    public static File[] SortFiles(File file_directory) {
        ArrayList<File> unknown_file_list = new ArrayList<File>();
        ArrayList<MusicData> music_data_list = new ArrayList<MusicData>();
        MusicData[] sorted_music_data;
        File[] sorted_files = new File[file_directory.listFiles().length];
        int directory_flag = 0;
        //diretory first
        for (File file : file_directory.listFiles()) {
            if (file.isDirectory()) {
                sorted_files[directory_flag++] = file;
            } else {
                //get music data if not directory
                MusicData music_data = MusicDataControl.getMusicDataFromFile(file);
                if (music_data != null) {
                    music_data_list.add(music_data);
                } else {
                    unknown_file_list.add(file);
                }
            }
        }
        //then music file
        sorted_music_data = MusicDataControl.SortedMusicData(music_data_list.toArray(new MusicData[music_data_list.size()]));
        for (int i = 0; i < sorted_music_data.length; i++) {
            sorted_files[directory_flag++] = new File(sorted_music_data[i].getSource());
        }
        //last unknown file
        for (int i = 0; i < unknown_file_list.size(); i++) {
            sorted_files[directory_flag++] = unknown_file_list.get(i);
        }
        return sorted_files;
    }

    /**
     * get Image under file directory
     * @param targeDirectory
     * @return image uri or null
     */
    public static Uri getImageUri(File targeDirectory) {
        Uri resultUri = null;
        for (File f : targeDirectory.listFiles()
                ) {
            if (f.isDirectory()) {
                resultUri = getImageUri(f);
                if (resultUri != null) {
                    break;
                }
            } else if (f.getName().toLowerCase().endsWith("jpg") ||
                    f.getName().toLowerCase().endsWith("jpeg")) {
                resultUri = Uri.fromFile(f);
                break;
            }
        }
        return resultUri;
    }
}
