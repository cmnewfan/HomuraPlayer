package ljl.com.homuraproject.Control;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import ljl.com.homuraproject.CUE_Model;
import ljl.com.homuraproject.CUE_Track;
import ljl.com.homuraproject.Track_Time;

/**
 * Created by hzfd on 2016/8/1.
 */
public class CUE_Control {
    public static CUE_Model getCUE_ModelFromFile(File file) {
        String codeType = FileControl.getEncoding(file);
        String line = "";
        ArrayList<String> TrackInfo = new ArrayList<>();
        try {
            String Date = "";
            String Genre = "";
            String file_path = "";
            ArrayList<CUE_Track> tracks = new ArrayList<>();
            Boolean TrackFlag = false;
            BufferedInputStream is = new BufferedInputStream(new FileInputStream(file));
            BufferedReader bufReader = new BufferedReader(new InputStreamReader(is, codeType));
            while ((line = bufReader.readLine()) != null) {
                if (line.contains("DATE")) {
                    Date = line.substring(line.lastIndexOf("DATE") + 5, line.length());
                } else if (line.contains("GENRE")) {
                    Genre = line.substring(line.lastIndexOf("GENRE") + 6, line.length());
                } else if (line.contains("FILE")) {
                    file_path = file.getAbsolutePath().substring(0, file.getAbsolutePath().lastIndexOf(File.separator)) + File.separator + line.substring(line.indexOf("\"") + 1, line.lastIndexOf("\""));
                } else if (line.contains("TRACK")) {
                    TrackFlag = true;
                    if (TrackInfo.size() > 0) {
                        CUE_Track track = getTrack(TrackInfo.toArray(new String[TrackInfo.size()]));
                        tracks.add(track);
                    }
                    TrackInfo.clear();
                    TrackInfo.add(line.trim());
                } else if (TrackFlag) {
                    TrackInfo.add(line.trim());
                }
            }
            return new CUE_Model(Date, Genre, file_path, tracks);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private static CUE_Track getTrack(String[] track) {
        String track_id = "";
        String title = "";
        String performer = "";
        int startTime = 0;
        int empty_time = 0;
        for (String info : track
                ) {
            if (info.contains("TRACK")) {
                track_id = info.substring(6, 8);
            } else if (info.contains("TITLE")) {
                title = info.substring(info.indexOf("\"") + 1, info.lastIndexOf("\""));
            } else if (info.contains("PERFORMER")) {
                performer = info.substring(info.indexOf("\"") + 1, info.lastIndexOf("\""));
            } else if (info.contains("INDEX 01")) {
                startTime = new Track_Time(info.substring(9)).getTime();
            } else if (info.contains("INDEX 00")) {
                empty_time = new Track_Time(info.substring(9)).getTime();
            }
        }
        return new CUE_Track(track_id, title, performer, startTime, empty_time);
    }

    private static ArrayList<CUE_Track> getTracks(String result) {

        return null;
    }
}
