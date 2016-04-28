package ljl.com.homuraproject;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;


/**
 * Created by Administrator on 2015/7/31.
 */
public class FileAdapter extends BaseAdapter {
    public static File[] files;
    private Context context;
    private LayoutInflater inflater;
    private File tempFile;

    public FileAdapter(Context context) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }

    public static void beforeMusicPlay(File tempFile, File[] files) {
        FileActivity.currentPlayingFile = tempFile;
        FileActivity.currentPlayList = new ArrayList<File>();
        FileActivity.currentPlayList.add(tempFile);
        Boolean flag = false;
        for (int i = 0; i < files.length; i++) {
            File tFile = files[i];
            if (!tFile.isDirectory()) {
                if ((tFile.getName().substring(tFile.getName().lastIndexOf(".")).equals(".mp3") ||
                        tFile.getName().substring(tFile.getName().lastIndexOf(".")).equals(".m4a") ||
                        tFile.getName().substring(tFile.getName().lastIndexOf(".")).equals(".flac"))) {
                    if (!tFile.getAbsolutePath().equals(tempFile.getAbsolutePath()) && flag) {
                        FileActivity.currentPlayList.add(tFile);
                    } else if (tFile.getAbsolutePath().equals(tempFile.getAbsolutePath())) {
                        flag = true;
                    }
                }
            }
        }
        sendCurrentLyric();
    }

    public static void sendMessage(String message) {
        Message mes = FileActivity.handler.obtainMessage();
        mes.obj = message;
        FileActivity.handler.sendMessage(mes);
    }

    public static void sendMessage(String message, Bundle bundle) {
        Message mes = FileActivity.handler.obtainMessage();
        mes.obj = message;
        mes.setData(bundle);
        FileActivity.handler.sendMessage(mes);
    }

    private static void sendMessage(Object obj) {
        Message mes = FileActivity.handler.obtainMessage();
        mes.obj = obj;
        FileActivity.handler.sendMessage(mes);
    }


    public static void sendCurrentLyric() {
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
        sendMessage("SetMusicTitle");
        File currentLyric = new File(FileActivity.LyricFolder);
        File[] lyrics = currentLyric.listFiles();
        currentLyric = null;
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
            sendMessage("UpdateLyric");
        } else {
            FileActivity.currentLyric = null;
            FileActivity.currentLyricFile = null;
        }
    }

    public static void Update() {
        File currentLyric = new File(FileActivity.LyricFolder);
        File[] lyrics = currentLyric.listFiles();
        currentLyric = null;
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
            sendMessage("UpdateLyric");
        } else {
            FileActivity.currentLyric = null;
            FileActivity.currentLyricFile = null;
        }
    }

    public static String GetLyricEncoding(File targerFile) {
        String codeType = "UTF-8";
        try {
            BufferedInputStream is = new BufferedInputStream(new FileInputStream(targerFile));
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
        } catch (FileNotFoundException ex) {
            ;
        } catch (IOException ex) {
            ;
        }
        return codeType;
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

    @Override
    public int getCount() {
        if (files == null)
            return 0;
        return files.length;
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = inflater.inflate(R.layout.listview_item, null);
        }
        final TextView fileName = (TextView) view.findViewById(R.id.itmMessage);
        fileName.setText(files[i].getName());
        if (FileActivity.currentPlayingFile != null && FileActivity.currentPlayingFile.getAbsolutePath().contains(files[i].getAbsolutePath())) {
            Drawable rightDrawable = context.getResources().getDrawable(R.drawable.play_icon);
            rightDrawable.setBounds(0, 0, rightDrawable.getMinimumWidth(), rightDrawable.getMinimumHeight());
            if (files[i].isDirectory()) {
                Drawable leftDrawable = context.getResources().getDrawable(R.drawable.abc_ic_menu_copy_mtrl_am_alpha);
                leftDrawable.setBounds(0, 0, leftDrawable.getMinimumWidth(), leftDrawable.getMinimumHeight());
                fileName.setCompoundDrawables(leftDrawable, null, rightDrawable, null);
            } else {
                fileName.setCompoundDrawables(null, null, rightDrawable, null);
            }
        } else {
            if (files[i].isDirectory()) {
                Drawable leftDrawable = context.getResources().getDrawable(R.drawable.abc_ic_menu_copy_mtrl_am_alpha);
                leftDrawable.setBounds(0, 0, leftDrawable.getMinimumWidth(), leftDrawable.getMinimumHeight());
                fileName.setCompoundDrawables(leftDrawable, null, null, null);
            } else {
                fileName.setCompoundDrawables(null, null, null, null);
            }
        }

        if (files[i].isDirectory()) {
            Drawable leftDrawable = context.getResources().getDrawable(R.drawable.abc_ic_menu_copy_mtrl_am_alpha);
            leftDrawable.setBounds(0, 0, leftDrawable.getMinimumWidth(), leftDrawable.getMinimumHeight());
            fileName.setCompoundDrawables(leftDrawable, fileName.getCompoundDrawables()[1], fileName.getCompoundDrawables()[2], fileName.getCompoundDrawables()[3]);
        }
        fileName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tempFile = new File(FileActivity.currentDirectory + File.separator + fileName.getText().toString());
                if (tempFile.isDirectory()) {
                    files = tempFile.listFiles();
                    Arrays.sort(files);
                    FileActivity.currentFile = tempFile;
                    FileActivity.currentDirectory = FileActivity.currentDirectory + File.separator + fileName.getText();
                    sendMessage("SetTitle");
                    FileActivity.fileAdapter.notifyDataSetChanged();
                } else if (tempFile.getName().substring(tempFile.getName().lastIndexOf(".")).equals(".mp3") ||
                        tempFile.getName().substring(tempFile.getName().lastIndexOf(".")).equals(".m4a") ||
                        tempFile.getName().substring(tempFile.getName().lastIndexOf(".")).equals(".flac")) {
                    beforeMusicPlay(tempFile, files);
                    Bundle bundle = new Bundle();
                    bundle.putInt("op", 1);
                    bundle.putInt("LastTime", 0);
                    bundle.putString("file_path", tempFile.getAbsolutePath());
                    sendMessage("Play", bundle);
                    notifyDataSetChanged();
                } else if (tempFile.getName().substring(tempFile.getName().lastIndexOf(".")).toLowerCase().equals(".jpg") ||
                        tempFile.getName().substring(tempFile.getName().lastIndexOf(".")).toLowerCase().equals(".png")) {
                    Uri data = Uri.fromFile(tempFile);
                    Intent sendIntent = new Intent(Intent.ACTION_VIEW, data).setDataAndType(data, "image/*");
                    context.startActivity(Intent.createChooser(sendIntent, ""));
                }
            }
        });
        fileName.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                LayoutInflater lf = LayoutInflater.from(context);
                View FileInfoView = lf.inflate(R.layout.file_info, null);
                final EditText artistText = (EditText) FileInfoView.findViewById(R.id.ArtistText);
                final EditText titleText = (EditText) FileInfoView.findViewById(R.id.TitleText);
                artistText.setText(FileActivity.currentArtist);
                if (FileActivity.currentPlayingTitle.contains("?")) {
                    titleText.setText(FileActivity.currentPlayingFile.getName());
                } else {
                    titleText.setText(FileActivity.currentPlayingTitle);
                }
                new AlertDialog.Builder(context).setTitle("确认信息").setIcon(android.R.drawable.ic_dialog_info).setView(FileInfoView).setNegativeButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Thread LyricThread = new Thread() {
                            public void run() {
                                String music_title = titleText.getText().toString();
                                String artist_name = artistText.getText().toString();
                                final ArrayList<QueryResult> queryList = TTDownloader.query(artist_name, music_title);
                                if (queryList != null && queryList.size() != 0) {
                                    final String[] list = new String[queryList.size()];
                                    for (int i = 0; i < list.length; i++) {
                                        list[i] = queryList.get(i).mTitle;
                                    }
                                    Object[] obj = new Object[]{queryList, list};
                                    sendMessage(obj);
                                } else {
                                    sendMessage("1");
                                }
                            }
                        };
                        LyricThread.start();
                    }
                }).show();
                return false;
            }
        });
        return view;
    }
}
