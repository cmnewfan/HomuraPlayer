package ljl.com.homuraproject.Control;

import android.os.Environment;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import ljl.com.homuraproject.Activity.FileActivity;
import ljl.com.homuraproject.Constants;
import ljl.com.homuraproject.MusicData;
import ljl.com.homuraproject.PlayService;
import ljl.com.homuraproject.PostMan;

/**
 * Created by hzfd on
 */
public class LyricControl {
    private static File[] lyrics;
    private static String currentLyric;
    private static String currentPlayingTitle;
    private static String currentArtist;
    private static String currentLyricFileName;
    /**
     * init lyric control, including default catalog
     */
    public static void Init() {
        if (HasSdCard()) {
            File file = new File(Constants.LyricFolder);
            if (!file.exists()) {
                file.mkdirs();
            }
            lyrics = new File(Constants.LyricFolder).listFiles();
        } else {
            Constants.LyricFolder = Environment.getDataDirectory() + "/Lyrics/";
            File file = new File(Constants.LyricFolder);
            if (!file.exists()) {
                file.mkdirs();
            }
            lyrics = new File(Constants.LyricFolder).listFiles();
        }
    }

    /**
     * if device has sd card
     *
     * @return true means having sd card, false means not
     */
    private static boolean HasSdCard() {
        String status = Environment.getExternalStorageState();
        return status.equals(Environment.MEDIA_MOUNTED);
    }

    public static String getCurrentPlayingTitle() {
        return currentPlayingTitle;
    }

    public static void setCurrentPlayingTitleFromCUE(String title) {
        currentPlayingTitle = title;
    }

    public static void setCurrentArtistFromCUE(String performer) {
        currentArtist = performer;
    }

    public static String getCurrentArtist() {
        return currentArtist;
    }

    /**
     * get lrc of current playing file and send to lrcView in FileActivity
     */
    public static void sendCurrentLyric() {
        //get music data of current playing file
        //MusicData mMusicData = MusicDataControl.query(MyApplication.getAppContext(), new String[]{FileActivity.currentPlayingFile.getName()});
        MusicData mMusicData = MusicDataControl.getMusicDataFromFile(FileActivity.getCurrentPlayingFile());
        if (mMusicData == null) {
            //needs broadcast
            currentPlayingTitle = FileActivity.getCurrentPlayingFile().getName();
            currentArtist = "";
            /*String artistName = "";
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
            }*/
            PostMan.sendMessage(Constants.ViewControl, Constants.ViewControl_ToastUpdate);
        } else {
            currentPlayingTitle = mMusicData.getTitle();
            currentArtist = mMusicData.getArtist();
        }
        PostMan.sendMessage(Constants.ViewControl, Constants.ViewControl_SetMusicTitle);
        //find lyric of current playing file
        File targetLyricFile = getTargetLyricFile();
        setCurrentLyricFromFile(targetLyricFile);
    }

    /**get encoding type of lrc file
     * @param currentLyric lrc file
     * @return string if has lrc, "" if not
     */
    private static String getLyric(File currentLyric) {
        try {
            BufferedInputStream is = new BufferedInputStream(new FileInputStream(currentLyric));
            String codeType = FileControl.getEncoding(currentLyric);
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

    public static String getCurrentLyric() {
        return currentLyric;
    }

    /**
     * update lyric to lrcView in FileActivity
     */
    public static void Update() {
        lyrics = new File(Constants.LyricFolder).listFiles();
        if (!PlayService.HasCueModel()) {
            File targetLyricFile = getTargetLyricFile();
            currentLyricFileName = targetLyricFile.getName().substring(0, targetLyricFile.getName().lastIndexOf("."));
            setCurrentLyricFromFile(targetLyricFile);
        } else {
            File targetLyricFile = getTargetLyricFileFromCUE(currentArtist, currentPlayingTitle);
            currentLyricFileName = currentArtist + "-" + currentPlayingTitle;
            setCurrentLyricFromFile(targetLyricFile);
        }
    }

    public static String getCurrentLyricFileName() {
        return currentLyricFileName;
    }

    private static void setCurrentLyricFromFile(File targetLyricFile) {
        if (targetLyricFile != null) {
            currentLyric = getLyric(targetLyricFile);
            PostMan.sendMessage(Constants.ViewControl, Constants.ViewControl_UpdateLyric);
        } else {
            currentLyric = null;
            PostMan.sendMessage(Constants.ViewControl, Constants.ViewControl_UpdateLyric);
        }
    }

    private static File getTargetLyricFile() {
        File targetLyricFile = null;
        for (int i = 0; i < lyrics.length; i++) {
            if (lyrics[i].getName().substring(lyrics[i].getName().lastIndexOf("/") + 1, lyrics[i].getName().lastIndexOf("."))
                    .contains(currentPlayingTitle)) {
                targetLyricFile = lyrics[i];
                break;
            }
        }
        return targetLyricFile;
    }

    private static File getTargetLyricFileFromCUE(String performer, String title) {
        File targetLyricFile = null;
        for (int i = 0; i < lyrics.length; i++) {
            if (lyrics[i].getName().substring(lyrics[i].getName().lastIndexOf("/") + 1, lyrics[i].getName().lastIndexOf("."))
                    .equals(performer + "-" + title)) {
                targetLyricFile = lyrics[i];
                break;
            }
        }
        return targetLyricFile;
    }
}
