package ljl.com.homuraproject;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import douzi.android.view.DefaultLrcBuilder;
import douzi.android.view.ILrcBuilder;
import douzi.android.view.LrcRow;
import douzi.android.view.LrcView;

/**
 * Created by Administrator on 2015/7/31.
 */
public class FileActivity extends Activity{
    private ListView listView;
    private TextView current_Time;
    private ImageButton playButton;
    private ImageButton pauseButton;
    private ImageButton prevButton;
    private ImageButton nextButton;
    private TextView total_Time;
    private LrcView lrcView;
    private Timer mTimer;
    private TextView myTitle;
    private ImageView main_backgroundImage;
    private SharedPreferences sharedPreferences;
    private static final int mId = 1;
    boolean pauseFlag = false;

    public static Handler handler;
    public static SeekBar seekBar;
    public static FileAdapter fileAdapter;
    public static File currentFile;
    public static String currentDirectory;
    public static MediaPlayer currentMediaPlayer;
    public static ArrayList<File> currentPlayList;
    public static File currentPlayingFile;
    public static String currentLyric;
    public final static String LyricFolder = "/storage/sdcard1/Lyrics";
    public static String currentPlayingTitle;
    public static String LastPlayingFile;
    private int LastPlayingTime;
    private boolean Screen_Off_Flag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.file_explorer);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title_bar);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );
        //this.initView();
        // ע���¼�
        TelephonyManager manager = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
        manager.listen(new MyPhoneStateListener(), PhoneStateListener.LISTEN_CALL_STATE);
        registerReceiver(myReceiver, new IntentFilter("android.intent.action.PHONE_STATE"));
        this.sharedPreferences = this.getSharedPreferences("music_player_info", Context.MODE_PRIVATE);
        handler = new Handler(){
           @Override
           public void handleMessage(Message msg) {
               if(msg.obj.toString().equals("Play")){
                   pauseButton.setVisibility(View.VISIBLE);
                   playButton.setVisibility(View.GONE);
                   total_Time.setText(String.format("%02d",seekBar.getMax() / 60)+":"+String.format("%02d", seekBar.getMax() % 60));
                   //beginLrcPlay();
               }
               else if(msg.obj.toString().equals("Stop")){
                   playButton.setVisibility(View.VISIBLE);
                   pauseButton.setVisibility(View.GONE);
                   //stopLrcPlay();
               }
               else if(msg.obj.toString().equals("Pause")){
                   //pauseLrcPlay();
               }
               else if(msg.obj.toString().equals("SetTitle")){
                   setTitle(currentDirectory);
               }
               else if(msg.obj.toString().equals("UpdateLyric")){
                   ILrcBuilder builder = new DefaultLrcBuilder();
                   List<LrcRow> rows = builder.getLrcRows(currentLyric);
                   lrcView.setLrc(rows);
               }
               else if(msg.obj.toString().equals("SetMusicTitle")){
                   lrcView.setLrc(null);
                   NotificationCompat.Builder mBuilder =
                           (NotificationCompat.Builder) new NotificationCompat.Builder(getApplicationContext())
                                   .setSmallIcon(R.drawable.icon)
                                   .setContentTitle("Title:")
                                   .setContentText(currentPlayingTitle);
                                   // Creates an explicit intent for an Activity in your app
                   Intent resultIntent = new Intent(getApplicationContext(), FileActivity.class);
                   // The stack builder object will contain an artificial back stack for the
                   // start Activity.
                   // This ensures that navigating backward from the Activity leads out of
                   // your application to the Home screen.
                   TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
                   // Adds the back stack for the Intent (but not the Intent itself)
                   stackBuilder.addParentStack(FileActivity.class);
                   // Adds the Intent that starts the Activity to the top of the stack
                   stackBuilder.addNextIntent(resultIntent);
                   PendingIntent resultPendingIntent =
                           stackBuilder.getPendingIntent(
                                   0,
                                   PendingIntent.FLAG_UPDATE_CURRENT
                           );
                   mBuilder.setContentIntent(resultPendingIntent);
                   NotificationManager mNotificationManager =
                           (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.
                   mNotificationManager.notify(mId, mBuilder.build());
                   setTitle(currentPlayingTitle);
               }
               else if(msg.obj.toString().equals("PlayLrc")){
                   lrcView.seekLrcToTime(seekBar.getProgress()*1000);
               }
           }
       };
    }

    private void stopLrcPlay() {
        if(mTimer != null){
            mTimer.cancel();
            mTimer = null;
        }
    }

    private void pauseLrcPlay() {
        if(mTimer != null){
            pauseFlag = true;
        }
        //
    }


    private void initView() {
        this.main_backgroundImage = (ImageView) this.findViewById(R.id.main_backgroundImage);
        main_backgroundImage.setImageAlpha(140);
        this.myTitle = (TextView) this.findViewById(R.id.myTitle);
        this.lrcView = (LrcView) this.findViewById(R.id.lrcView);
        this.playButton = (ImageButton) this.findViewById(R.id.btn_play);
        this.playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playButton.setVisibility(View.GONE);
                pauseButton.setVisibility(View.VISIBLE);
                if(!currentMediaPlayer.isPlaying()){
                    currentMediaPlayer.start();
                }
                Message mes = handler.obtainMessage();
                mes.obj = "Play";
                handler.sendMessage(mes);
            }
        });
        this.pauseButton = (ImageButton) this.findViewById(R.id.btn_pause);
        this.pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pauseButton.setVisibility(View.GONE);
                playButton.setVisibility(View.VISIBLE);
                if(currentMediaPlayer.isPlaying()){
                    currentMediaPlayer.pause();
                }
                Message mes = handler.obtainMessage();
                mes.obj = "Pause";
                handler.sendMessage(mes);
            }
        });
        this.prevButton = (ImageButton) this.findViewById(R.id.btn_playPre);
        this.prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentMediaPlayer!=null&&currentMediaPlayer.isPlaying()) {
                    currentMediaPlayer.stop();
                }
                if(currentPlayList.indexOf(currentPlayingFile) > 0) {
                    currentMediaPlayer = MediaPlayer.create(FileActivity.this,
                            Uri.fromFile(currentPlayList.get(currentPlayList.indexOf(currentPlayingFile) - 1)));
                    seekBar.setMax(currentMediaPlayer.getDuration() / 1000);
                    seekBar.setProgress(0);
                    currentPlayingFile = currentPlayList.get(currentPlayList.indexOf(currentPlayingFile) - 1);
                    /*if(currentMediaPlayer.isPlaying()) {
                        while (seekBar.removeCallbacks(FileAdapter.musicRunnable));
                        seekBar.postDelayed(FileAdapter.musicRunnable, 1000);
                    }*/
                    //seekBar.removeCallbacks(FileAdapter.musicRunnable);
                    fileAdapter.sendCurrentLyric();
                    Message mes = handler.obtainMessage();
                    mes.obj = "Play";
                    handler.sendMessage(mes);
                    currentMediaPlayer.start();
                    fileAdapter.notifyDataSetChanged();
                }
            }
        });
        this.nextButton = (ImageButton) this.findViewById(R.id.btn_playNext);
        this.nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentMediaPlayer!=null&&currentMediaPlayer.isPlaying()) {
                    currentMediaPlayer.stop();
                }
                if(currentPlayList.indexOf(currentPlayingFile) < currentPlayList.size()-1) {
                    currentMediaPlayer = MediaPlayer.create(FileActivity.this,
                            Uri.fromFile(currentPlayList.get(currentPlayList.indexOf(currentPlayingFile) + 1)));
                    seekBar.setMax(currentMediaPlayer.getDuration() / 1000);
                    seekBar.setProgress(0);
                    currentPlayingFile = currentPlayList.get(currentPlayList.indexOf(currentPlayingFile) + 1);
                    /*if(currentMediaPlayer.isPlaying()) {
                        while (seekBar.removeCallbacks(FileAdapter.musicRunnable)) ;
                        seekBar.postDelayed(FileAdapter.musicRunnable, 1000);
                    }*/
                    fileAdapter.sendCurrentLyric();
                    Message mes = handler.obtainMessage();
                    mes.obj = "Play";
                    handler.sendMessage(mes);
                    currentMediaPlayer.start();
                    fileAdapter.notifyDataSetChanged();
                }
            }
        });
        this.total_Time = (TextView) this.findViewById(R.id.totalTime_tv);
        this.current_Time = (TextView) this.findViewById(R.id.currentTime_tv);
        seekBar = (SeekBar) this.findViewById(R.id.playback_seekbar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                current_Time.setText(String.format("%02d",seekBar.getProgress()/60)+":"+String.format("%02d", seekBar.getProgress() % 60));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if(currentMediaPlayer==null||currentPlayingFile==null||currentPlayList==null){
                    seekBar.setEnabled(false);
                }
                else{
                    seekBar.setEnabled(true);
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(currentMediaPlayer.isPlaying()){
                    currentMediaPlayer.seekTo(seekBar.getProgress()*1000);
                }

            }
        });
        this.listView = (ListView) this.findViewById(R.id.file_listView);
        fileAdapter = new FileAdapter(FileActivity.this);
        this.listView.setAdapter(fileAdapter);
        if(currentPlayingFile==null) {
            File path = new File(File.separator + "storage");
            FileAdapter.files = path.listFiles();
            Arrays.sort(FileAdapter.files);

            currentFile = path;
            currentDirectory = path.getAbsolutePath();
            this.setTitle(currentDirectory);
            fileAdapter.notifyDataSetChanged();
        }
        else{
            currentFile = currentPlayingFile.getParentFile();
            FileAdapter.files = currentPlayingFile.getParentFile().listFiles();
            Arrays.sort(FileAdapter.files);
            currentDirectory = currentPlayingFile.getParentFile().getAbsolutePath();
            this.setTitle(currentDirectory);
            fileAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void setTitle(CharSequence title){
        myTitle.setText(title);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            if(currentFile == null){
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("EXIT", true);
                startActivity(intent);
            }
            else if(currentFile.getAbsolutePath().equals(File.separator+"storage")){
                SharedPreferences.Editor editor = sharedPreferences.edit();
                if(currentPlayingFile!=null){
                    editor.putString("LastPlayingFile", currentPlayingFile.getAbsolutePath());
                }
                seekBar.removeCallbacks(FileAdapter.musicRunnable);
                if(currentMediaPlayer!=null){
                    editor.putInt("LastPlayingTime",seekBar.getProgress());
                    if(currentMediaPlayer.isPlaying()){
                        currentMediaPlayer.stop();
                    }
                    currentMediaPlayer.release();
                }
                editor.commit();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("EXIT", true);
                startActivity(intent);
            }
            else{
                FileAdapter.files = currentFile.getParentFile().listFiles();
                currentDirectory = currentDirectory.substring(0, currentDirectory.lastIndexOf(File.separator));
                currentFile = currentFile.getParentFile();
                Arrays.sort(FileAdapter.files);
                this.setTitle(currentDirectory);
                fileAdapter.notifyDataSetChanged();
            }
        }
        return false;
    }
    @Override
    protected void onResume(){
        super.onResume();
        initView();
        if(currentPlayingFile == null) {
            LastPlayingFile = sharedPreferences.getString("LastPlayingFile", "");
            LastPlayingTime = sharedPreferences.getInt("LastPlayingTime", 0);
            currentPlayingFile = new File(LastPlayingFile);
            FileAdapter.beforeMusicPlay(currentPlayingFile, currentPlayingFile.getParentFile().listFiles());
            currentMediaPlayer = MediaPlayer.create(this, Uri.fromFile(currentPlayingFile));
            currentMediaPlayer.seekTo(LastPlayingTime * 1000);
            seekBar.setMax(currentMediaPlayer.getDuration() / 1000);
            seekBar.setProgress(LastPlayingTime);
            seekBar.postDelayed(FileAdapter.musicRunnable,1000);
            current_Time.setText(String.format("%02d", seekBar.getProgress() / 60) + ":" + String.format("%02d", seekBar.getProgress() % 60));
            total_Time.setText(String.format("%02d", seekBar.getMax() / 60) + ":" + String.format("%02d", seekBar.getMax() % 60));
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
    }

    @Override
    protected void onStop(){
        super.onStop();
        if(!Screen_Off_Flag) {

        }
    }

    @Override
    protected void onStart(){
        super.onStart();
    }

    class LrcTask extends TimerTask{
        long beginTime = -1;
        long pausedBeginTime = 0;
        long pausedTime = 0;
        @Override
        public void run() {
            if(beginTime == -1) {
                beginTime = System.currentTimeMillis();
            }
            if(pauseFlag && pausedBeginTime==0){
                pausedBeginTime = System.currentTimeMillis();
            }
            else if(pauseFlag && pausedBeginTime!=0){
                pausedTime += System.currentTimeMillis() - pausedBeginTime;
            }
            else{
                pausedBeginTime = 0;
                pausedTime = 0;
            }
            final long timePassed = System.currentTimeMillis() - beginTime - pausedTime;
            FileActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    lrcView.seekLrcToTime(timePassed);
                }
            });
        }
    }
    private BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            if (Intent.ACTION_SCREEN_ON.equals(intent.getAction()) ) {//
                //SharedPreferences.Editor editor = sharedPreferences.edit();
                //editor.putInt("LastPlayingTime", seekBar.getProgress());
                //editor.commit();
            }
            if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction()) ) {//
                Screen_Off_Flag = true;
            }
            if (Intent.ACTION_USER_PRESENT.equals(intent.getAction()) ) {//

            }
            if(intent.getAction().equals("android.intent.action.PHONE_STATE")){
                SharedPreferences.Editor editor = sharedPreferences.edit();
                if (currentPlayingFile != null) {
                    editor.putString("LastPlayingFile", currentPlayingFile.getAbsolutePath());
                }
                seekBar.removeCallbacks(FileAdapter.musicRunnable);
                if (currentMediaPlayer != null) {
                    editor.putInt("LastPlayingTime", seekBar.getProgress());
                    if (currentMediaPlayer.isPlaying()) {
                        currentMediaPlayer.pause();
                    }
                }
                editor.commit();
                FileAdapter.sendMessage("Stop");
            }
        }
    };
    class MyPhoneStateListener extends PhoneStateListener{

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE:
                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    if (currentPlayingFile != null) {
                        editor.putString("LastPlayingFile", currentPlayingFile.getAbsolutePath());
                    }
                    seekBar.removeCallbacks(FileAdapter.musicRunnable);
                    if (currentMediaPlayer != null) {
                        editor.putInt("LastPlayingTime", seekBar.getProgress());
                        if (currentMediaPlayer.isPlaying()) {
                            currentMediaPlayer.pause();
                        }
                    }
                    editor.commit();
                    FileAdapter.sendMessage("Stop");
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    break;
                default:
                    break;
            }
            super.onCallStateChanged(state, incomingNumber);
        }

    }
}
