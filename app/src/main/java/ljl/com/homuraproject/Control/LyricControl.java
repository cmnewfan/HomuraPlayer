package ljl.com.homuraproject.Control;

import android.media.MediaMetadataRetriever;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import ljl.com.homuraproject.Activity.FileActivity;
import ljl.com.homuraproject.Constants;
import ljl.com.homuraproject.MusicData;
import ljl.com.homuraproject.MusicDatabase;
import ljl.com.homuraproject.MyApplication;
import ljl.com.homuraproject.PostMan;

/**
 * Created by hzfd on 2016/5/4.
 */
public class LyricControl {
    private static File[] lyrics;

    public static void Init() {
        lyrics = new File(Constants.LyricFolder).listFiles();
    }

    public static void sendCurrentLyric() {
        MusicData mMusicData = MusicDatabase.query(MyApplication.getAppContext(), new String[]{FileActivity.currentPlayingFile.getName()});
        if (mMusicData == null) {
            //needs broadcast
            String artistName = "";
            try {
                AudioFile currentAudioFile = AudioFileIO.read(FileActivity.currentPlayingFile);
                Tag tag = currentAudioFile.getTag();
                String test = "";
                if (FileActivity.currentPlayingFile.getAbsolutePath().endsWith("mp3")) {
                    if (tag == null) {
                        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
                        mediaMetadataRetriever.setDataSource(FileActivity.currentPlayingFile.getAbsolutePath());
                        if (mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) != null) {
                            FileActivity.currentArtist = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                            test = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                            FileActivity.currentPlayingTitle = test;
                        }
                    } else {
                        String title = tag.getFirst(FieldKey.TITLE);
                        artistName = tag.getFirst(FieldKey.ARTIST);
                        FileActivity.currentArtist = new String(artistName.getBytes("ISO-8859-1"), "GBK");
                        FileActivity.currentPlayingTitle = new String(title.getBytes("ISO-8859-1"), "GBK");
                    }
                } else {
                    String title = tag.getFirst(FieldKey.TITLE);
                    FileActivity.currentPlayingTitle = title;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception ex) {
                FileActivity.currentPlayingTitle = FileActivity.currentPlayingFile.getAbsolutePath().substring
                        (FileActivity.currentPlayingFile.getAbsolutePath().lastIndexOf("/") + 1, FileActivity.currentPlayingFile.getAbsolutePath().lastIndexOf("."));
            }
            //FileAdapter.sendMessage("4");
            PostMan.sendMessage(Constants.ViewControl, Constants.ViewControl_ToastUpdate);
        } else {
            FileActivity.currentPlayingTitle = mMusicData.getTitle();
            FileActivity.currentArtist = mMusicData.getArtist();
        }
        //FileAdapter.sendMessage("SetMusicTitle");
        PostMan.sendMessage(Constants.ViewControl, Constants.ViewControl_SetMusicTitle);
        File currentLyric = null;
        for (int i = 0; i < lyrics.length; i++) {
            if (lyrics[i].getName().substring(lyrics[i].getName().lastIndexOf("/") + 1, lyrics[i].getName().lastIndexOf("."))
                    .contains(FileActivity.currentPlayingTitle)) {
                currentLyric = lyrics[i];
                break;
            }
        }
        if (currentLyric != null) {
            FileActivity.currentLyric = getLyric(currentLyric);
            FileActivity.currentLyricFile = currentLyric;
            //FileAdapter.sendMessage("UpdateLyric");
            PostMan.sendMessage(Constants.ViewControl, Constants.ViewControl_UpdateLyric);
        } else {
            FileActivity.currentLyric = null;
            FileActivity.currentLyricFile = null;
        }
    }

    private static String getLyric(File currentLyric) {
        try {
            BufferedInputStream is = new BufferedInputStream(new FileInputStream(currentLyric));
            String codeType;
            if (is.markSupported()) {
                is.mark(4);
                byte[] first3bytes = new byte[3];
                is.read(first3bytes);
                is.reset();
                if (first3bytes[0] == (byte) 0xEF && first3bytes[1] == (byte) 0xBB
                        && first3bytes[2] == (byte) 0xBF) {// utf-8
                    codeType = "utf-8";
                } else if (first3bytes[0] == (byte) 0xFF
                        && first3bytes[1] == (byte) 0xFE) {
                    codeType = "unicode";
                } else if (first3bytes[0] == (byte) 0xFE
                        && first3bytes[1] == (byte) 0xFF) {
                    codeType = "utf-16be";
                } else if (first3bytes[0] == (byte) 0xFF
                        && first3bytes[1] == (byte) 0xFF) {
                    codeType = "utf-16le";
                } else {
                    codeType = "GBK";
                }
            } else {
                codeType = "GBK";
            }
            BufferedReader bufReader = new BufferedReader(new InputStreamReader(is, codeType));
            String line = "";
            String Result = "";
            while ((line = bufReader.readLine()) != null) {
                if (line.trim().equals(""))
                    continue;
                Result += line + "\r\n";
            }
            return Result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void Update() {
        lyrics = new File(Constants.LyricFolder).listFiles();
        File currentLyric = null;
        for (int i = 0; i < lyrics.length; i++) {
            if (lyrics[i].getName().substring(lyrics[i].getName().lastIndexOf("/") + 1, lyrics[i].getName().lastIndexOf("."))
                    .contains(FileActivity.currentPlayingTitle)) {
                currentLyric = lyrics[i];
                break;
            }
        }
        if (currentLyric != null) {
            FileActivity.currentLyric = getLyric(currentLyric);
            FileActivity.currentLyricFile = currentLyric;
            //FileAdapter.sendMessage("UpdateLyric");
            PostMan.sendMessage(Constants.ViewControl, Constants.ViewControl_UpdateLyric);
        } else {
            FileActivity.currentLyric = null;
            FileActivity.currentLyricFile = null;
        }
    }
}
