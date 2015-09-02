package ljl.com.homuraproject;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Administrator on 2015/7/31.
 */
public class FileAdapter extends BaseAdapter {
    private int count = 0;
    private Context context;
    private LayoutInflater inflater;
    private File tempFile;
    public static File[] files;
    public static Runnable musicRunnable;

    public FileAdapter(Context context){
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        final Context tempContext = context;
        this.musicRunnable = new Runnable() {
            @Override
            public void run() {
                if(FileActivity.currentMediaPlayer.getCurrentPosition() < FileActivity.currentMediaPlayer.getDuration()) {
                    try {
                        if (FileActivity.currentMediaPlayer.isPlaying()) {
                            FileActivity.seekBar.incrementProgressBy(1);
                            sendMessage("PlayLrc");
                        }
                        FileActivity.seekBar.postDelayed(this, 1000);
                    }
                    catch(IllegalStateException ex){
                        ex.printStackTrace();
                    }
                }
                else{
                    FileActivity.currentMediaPlayer.stop();
                    if(FileActivity.currentPlayList.indexOf(FileActivity.currentPlayingFile) < FileActivity.currentPlayList.size()-1) {
                        FileActivity.currentMediaPlayer = MediaPlayer.create(tempContext,
                                Uri.fromFile(FileActivity.currentPlayList.get(FileActivity.currentPlayList.indexOf(FileActivity.currentPlayingFile)+1)));
                        FileActivity.seekBar.setMax(FileActivity.currentMediaPlayer.getDuration() / 1000);
                        FileActivity.seekBar.setProgress(0);
                        FileActivity.currentPlayingFile = FileActivity.currentPlayList.get(FileActivity.currentPlayList.indexOf(FileActivity.currentPlayingFile)+1);
                        while(FileActivity.seekBar.removeCallbacks(this));
                        FileActivity.seekBar.postDelayed(this, 1000);
                        sendCurrentLyric();
                        FileActivity.currentMediaPlayer.start();
                        sendMessage("Play");
                        notifyDataSetChanged();
                    }
                    else{
                        sendMessage("Stop");
                        sendMessage("SetTitle");
                    }
                }
            }
        };
    }

    @Override
    public int getCount() {
        if(files==null)
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
        if(FileActivity.currentPlayingFile.getAbsolutePath().contains(files[i].getAbsolutePath())){
            Drawable rightDrawable = context.getResources().getDrawable(R.drawable.play_icon);
            rightDrawable.setBounds(0, 0, rightDrawable.getMinimumWidth(), rightDrawable.getMinimumHeight());
            if(files[i].isDirectory()) {
                Drawable leftDrawable = context.getResources().getDrawable(R.drawable.abc_ic_menu_copy_mtrl_am_alpha);
                leftDrawable.setBounds(0, 0, leftDrawable.getMinimumWidth(), leftDrawable.getMinimumHeight());
                fileName.setCompoundDrawables(leftDrawable, null, rightDrawable, null);
            }
            else{
                fileName.setCompoundDrawables(null,null,rightDrawable,null);
            }
        }
        else{
            if(files[i].isDirectory()) {
                Drawable leftDrawable = context.getResources().getDrawable(R.drawable.abc_ic_menu_copy_mtrl_am_alpha);
                leftDrawable.setBounds(0, 0, leftDrawable.getMinimumWidth(), leftDrawable.getMinimumHeight());
                fileName.setCompoundDrawables(leftDrawable, null, null, null);
            }
            else{
                fileName.setCompoundDrawables(null,null,null,null);
            }
        }

        if(files[i].isDirectory()) {
            Drawable leftDrawable = context.getResources().getDrawable(R.drawable.abc_ic_menu_copy_mtrl_am_alpha);
            leftDrawable.setBounds(0, 0, leftDrawable.getMinimumWidth(), leftDrawable.getMinimumHeight());
            fileName.setCompoundDrawables(leftDrawable, fileName.getCompoundDrawables()[1], fileName.getCompoundDrawables()[2], fileName.getCompoundDrawables()[3]);
        }
        /*else if(files[i].getName().substring(files[i].getName().lastIndexOf(".")).equals(".mp3")||
                files[i].getName().substring(files[i].getName().lastIndexOf(".")).equals(".m4a")){
            Drawable drawable = context.getResources().getDrawable(R.drawable.abc_ic_commit_search_api_mtrl_alpha);
            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
            fileName.setCompoundDrawables(null, null, drawable, null);
        }*/

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
                    FileActivity.currentMediaPlayer = MediaPlayer.create(context, Uri.fromFile(tempFile));
                    FileActivity.seekBar.setMax(FileActivity.currentMediaPlayer.getDuration() / 1000);
                    FileActivity.seekBar.setProgress(0);
                    sendMessage("Play");
                    FileActivity.currentMediaPlayer.start();
                    notifyDataSetChanged();
                } else if (tempFile.getName().substring(tempFile.getName().lastIndexOf(".")).equals(".jpg") ||
                        tempFile.getName().substring(tempFile.getName().lastIndexOf(".")).equals(".JPG")) {
                    Uri data = Uri.fromFile(tempFile);
                    Intent sendIntent = new Intent(Intent.ACTION_VIEW, data).setDataAndType(data, "image/*");
                    context.startActivity(Intent.createChooser(sendIntent, ""));
                }
            }
        });
        return view;
    }

    public static void beforeMusicPlay(File tempFile, File[] files) {
        FileActivity.currentPlayingFile = tempFile;
        FileActivity.currentPlayList = new ArrayList<File>();
        FileActivity.currentPlayList.add(tempFile);
        Boolean flag = false;
        //File currentLyric = new File(tempFile.getAbsolutePath().replace(tempFile.getName().substring(tempFile.getName().lastIndexOf(".") + 1), "lrc"));
        for(int i=0; i<files.length;i++) {
            File tFile = files[i];
            if (!tFile.isDirectory()) {
                if ((tFile.getName().substring(tFile.getName().lastIndexOf(".")).equals(".mp3") ||
                        tFile.getName().substring(tFile.getName().lastIndexOf(".")).equals(".m4a") ||
                        tFile.getName().substring(tFile.getName().lastIndexOf(".")).equals(".flac"))) {
                    if(!tFile.getAbsolutePath().equals(tempFile.getAbsolutePath())&&flag) {
                        FileActivity.currentPlayList.add(tFile);
                    }
                    else if(tFile.getAbsolutePath().equals(tempFile.getAbsolutePath())){
                        flag = true;
                    }
                }
            }
        }
        if(FileActivity.currentMediaPlayer!=null && FileActivity.currentMediaPlayer.isPlaying()){
            FileActivity.currentMediaPlayer.stop();
        }
        sendCurrentLyric();
        while(FileActivity.seekBar.removeCallbacks(musicRunnable));
        FileActivity.seekBar.postDelayed(musicRunnable, 1000);
    }

    public static void sendMessage(String message) {
        Message mes = FileActivity.handler.obtainMessage();
        mes.obj = message;
        FileActivity.handler.sendMessage(mes);
    }

    public static void sendCurrentLyric(){
        try {
            Mp3File mp3File = new Mp3File(FileActivity.currentPlayingFile);
            if (mp3File.hasId3v1Tag()) {
                ID3v1 id3v1Tag = mp3File.getId3v1Tag();
                String test = id3v1Tag.getTitle();
                FileActivity.currentPlayingTitle = new String(test.getBytes("ISO-8859-1"),"GBK");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UnsupportedTagException e) {
            e.printStackTrace();
        } catch (InvalidDataException e) {
            FileActivity.currentPlayingTitle = FileActivity.currentPlayingFile.getAbsolutePath().substring
                    (FileActivity.currentPlayingFile.getAbsolutePath().lastIndexOf("/")+1,FileActivity.currentPlayingFile.getAbsolutePath().lastIndexOf("."));
        }

        sendMessage("SetMusicTitle");
        File currentLyric = new File(FileActivity.LyricFolder);
        File[] lyrics = currentLyric.listFiles();
        currentLyric = null;
        for(int i=0;i<lyrics.length;i++){
            if(lyrics[i].getName().substring(lyrics[i].getName().lastIndexOf("/")+1,lyrics[i].getName().lastIndexOf("."))
                    .contains(FileActivity.currentPlayingTitle ))
            {
                currentLyric = lyrics[i];
                break;
            }
        }
        if(currentLyric != null){
            FileActivity.currentLyric = getLyric(currentLyric);
            sendMessage("UpdateLyric");
        }
    }

    private static String getLyric(File currentLyric) {
        try {
            InputStream is = new FileInputStream(currentLyric);
            BufferedReader bufReader = new BufferedReader(new InputStreamReader(is,"GBK"));
            String line="";
            String Result="";
            while((line = bufReader.readLine()) != null){
                if(line.trim().equals(""))
                    continue;
                Result += line + "\r\n";
            }
            return Result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
