package ljl.com.homuraproject;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import douzi.android.view.DefaultLrcBuilder;
import douzi.android.view.ILrcBuilder;
import douzi.android.view.LrcRow;
import douzi.android.view.LrcView;

/**
 * Created by Administrator on 2015/7/31.
 */
public class FileActivity extends Activity {
    public final static String LyricFolder = "/storage/emulated/0/Lyrics";
    private static final int mId = 1;
    public static MusicListAdapter mListAdapter;
    public static Handler handler;
    public static SeekBar seekBar;
    public static FileAdapter fileAdapter;
    public static File currentFile;
    public static String currentDirectory;
    public static ArrayList<File> currentPlayList;
    public static File currentPlayingFile;
    public static String currentLyric;
    public static String currentPlayingTitle;
    public static String LastPlayingFile;
    public static String currentArtist;
    private static EditText artistText;
    private static EditText titleText;
    private ListView listView;
    private TextView current_Time;
    private ImageButton playButton;
    private ImageButton pauseButton;
    private ImageButton prevButton;
    private ImageButton nextButton;
    private TextView total_Time;
    private LrcView lrcView;
    private TextView myTitle;
    private ImageView main_backgroundImage;
    private ViewPager viewPager;
    private PagerTabStrip pagerTabStrip;
    private SharedPreferences sharedPreferences;
    private int LastPlayingTime;
    private PowerManager.WakeLock wakeLock;
    private BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            if (intent.getAction().equals("android.intent.action.PHONE_STATE")) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                if (currentPlayingFile != null) {
                    editor.putString("LastPlayingFile", currentPlayingFile.getAbsolutePath());
                }
                seekBar.removeCallbacks(MusicRunnable.mRunnable);
                if (PlayService.exist()) {
                    editor.putInt("LastPlayingTime", seekBar.getProgress());
                    if (PlayService.getPlayerState().equals("Playing")) {
                        PlayService.pause();
                    }
                }
                editor.commit();
                FileAdapter.sendMessage("Stop");
            }
        }
    };

    public static String GetArtistText() {
        if (artistText != null) {
            return artistText.getText().toString();
        } else
            return "";
    }

    public static String GetTitleText() {
        if (artistText != null) {
            return titleText.getText().toString();
        } else
            return "";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        //Get screen width and height
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        float density = getResources().getDisplayMetrics().density;
        float dpHeight = dm.heightPixels / density;
        float dpWidth = dm.widthPixels / density;
        //
        Intent intent = new Intent("com.service.PlayService");
        intent.setPackage(getPackageName());
        startService(intent);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.file_explorer);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title_bar);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );
        //Inten
        Intent x = getIntent();
        int sc = x.getIntExtra("Command", 100);
        //WakeLock
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, FileActivity.class.getName());
        wakeLock.acquire();

        TelephonyManager manager = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
        manager.listen(new MyPhoneStateListener(), PhoneStateListener.LISTEN_CALL_STATE);
        registerReceiver(myReceiver, new IntentFilter("android.intent.action.PHONE_STATE"));
        //Fill View
        this.sharedPreferences = this.getSharedPreferences("music_player_info", Context.MODE_PRIVATE);
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.obj.toString().equals("Play")) {
                    pauseButton.setVisibility(View.VISIBLE);
                    playButton.setVisibility(View.GONE);
                    PlayService.ControlNotificationView(R.id.notification_pause, View.VISIBLE);
                    PlayService.ControlNotificationView(R.id.notification_play, View.GONE);
                    total_Time.setText(String.format("%02d", seekBar.getMax() / 60) + ":" + String.format("%02d", seekBar.getMax() % 60));
                    Intent intent = new Intent("com.service.PlayService");
                    intent.putExtras(msg.getData());
                    intent.setPackage(getPackageName());
                    startService(intent);
                } else if (msg.obj.toString().equals("Play2")) {
                    pauseButton.setVisibility(View.VISIBLE);
                    playButton.setVisibility(View.GONE);
                    total_Time.setText(String.format("%02d", seekBar.getMax() / 60) + ":" + String.format("%02d", seekBar.getMax() % 60));
                } else if (msg.obj.toString().equals("Stop")) {
                    playButton.setVisibility(View.VISIBLE);
                    pauseButton.setVisibility(View.GONE);
                } else if (msg.obj.toString().equals("SetTitle")) {
                    setTitle(currentDirectory);
                } else if (msg.obj.toString().equals("Pause")) {
                    pauseButton.setVisibility(View.GONE);
                    playButton.setVisibility(View.VISIBLE);
                    PlayService.ControlNotificationView(R.id.notification_play, View.VISIBLE);
                    PlayService.ControlNotificationView(R.id.notification_pause, View.GONE);
                } else if (msg.obj.toString().equals("1")) {
                    Toast.makeText(MyApplication.getAppContext(), "未找到目标", Toast.LENGTH_SHORT).show();
                } else if (msg.obj.toString().equals("2")) {
                    Toast.makeText(MyApplication.getAppContext(), "转换出错", Toast.LENGTH_SHORT).show();
                } else if (msg.obj.toString().equals("3")) {
                    Toast.makeText(MyApplication.getAppContext(), "转换成功", Toast.LENGTH_SHORT).show();
                } else if (msg.obj.toString().equals("UpdateLyric")) {
                    ILrcBuilder builder = new DefaultLrcBuilder();
                    List<LrcRow> rows = builder.getLrcRows(currentLyric);
                    lrcView.setLrc(rows);
                } else if (msg.obj.toString().equals("SetMusicTitle")) {
                    lrcView.setLrc(null);
                    setTitle(currentPlayingTitle);
                    PlayService.UpdateNotification(currentPlayingTitle, currentArtist);
                    /*NotificationCompat.Builder mBuilder =
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
                    mNotificationManager.notify(mId, mBuilder.build());*/

                } else if (msg.obj.toString().equals("PlayLrc")) {
                    lrcView.seekLrcToTime(seekBar.getProgress() * 1000);
                } else {
                    Object[] array = (Object[]) msg.obj;
                    final ArrayList<QueryResult> queryList = (ArrayList<QueryResult>) array[0];
                    final String[] list = (String[]) array[1];
                    new AlertDialog.Builder(FileActivity.this).setTitle("选择目标").setIcon(android.R.drawable.ic_dialog_info).setItems(list, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            final int item = i;
                            Thread download_thread = new Thread() {
                                public void run() {
                                    boolean flag = TTDownloader.download(queryList.get(item), FileActivity.currentPlayingTitle);
                                    if (flag) {
                                        FileAdapter.sendMessage("3");
                                    } else {
                                        FileAdapter.sendMessage("2");
                                    }
                                }
                            };
                            download_thread.start();
                        }
                    }).show();
                }
            }
        };
    }

    private void InitViewPagerAdapter() {
        this.viewPager = (ViewPager) this.findViewById(R.id.viewpager);
        final ArrayList<String> titleList = new ArrayList<String>();
        titleList.add("FileList");
        titleList.add("Lyric");
        LayoutInflater lf = getLayoutInflater().from(this);
        View FileView = lf.inflate(R.layout.fileview, null);
        this.main_backgroundImage = (ImageView) FileView.findViewById(R.id.main_backgroundImage);
        main_backgroundImage.setImageAlpha(140);
        this.listView = (ListView) FileView.findViewById(R.id.file_listView);
        fileAdapter = new FileAdapter(FileActivity.this);
        this.listView.setAdapter(fileAdapter);
        View LrcView = lf.inflate(R.layout.lrcview, null);
        this.lrcView = (LrcView) LrcView.findViewById(R.id.lrcView);
        final ArrayList<View> viewList = new ArrayList<View>();// 将要分页显示的View装入数组中
        viewList.add(FileView);
        viewList.add(LrcView);
        PagerAdapter pagerAdapter = new PagerAdapter() {
            @Override
            public boolean isViewFromObject(View arg0, Object arg1) {
                return arg0 == arg1;
            }

            @Override
            public int getCount() {
                return viewList.size();
            }

            @Override
            public void destroyItem(ViewGroup container, int position,
                                    Object object) {
                container.removeView(viewList.get(position));
            }

            @Override
            public int getItemPosition(Object object) {
                return super.getItemPosition(object);
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return titleList.get(position);
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                container.addView(viewList.get(position));
                return viewList.get(position);
            }
        };
        viewPager.setAdapter(pagerAdapter);
    }

    private void InitPagerTabStrip() {
        //pagerTitleStrip = (PagerTitleStrip) findViewById(R.id.pagertitle);
        pagerTabStrip = (PagerTabStrip) findViewById(R.id.pagertab);
        pagerTabStrip.setTabIndicatorColor(getResources().getColor(R.color.material_blue_grey_800));
        pagerTabStrip.setDrawFullUnderline(false);
        pagerTabStrip.setTextSpacing(50);

    }

    private void initView() {
        this.myTitle = (TextView) this.findViewById(R.id.myTitle);
        InitSeekBar();
        InitViewPagerAdapter();
        InitPagerTabStrip();
        this.playButton = (ImageButton) this.findViewById(R.id.btn_play);
        this.playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playButton.setVisibility(View.GONE);
                pauseButton.setVisibility(View.VISIBLE);
                if (!PlayService.getPlayerState().equals("Playing")) {
                    PlayService.play();
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
                if (PlayService.getPlayerState().equals("Playing")) {
                    PlayService.pause();
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
                if (PlayService.getPlayerState().equals("Playing")) {
                    PlayService.stop();
                }

                if (currentPlayList.indexOf(currentPlayingFile) > 0) {
                    //HomuraPlayer player = HomuraPlayer.getInstance(Uri.fromFile(currentPlayList.get(currentPlayList.indexOf(currentPlayingFile) - 1)), FileActivity.this);
                    currentPlayingFile = currentPlayList.get(currentPlayList.indexOf(currentPlayingFile) - 1);
                    fileAdapter.sendCurrentLyric();

                    //player.play();

                    Intent intent = new Intent("com.service.PlayService");
                    Bundle bundle = new Bundle();
                    bundle.putInt("op", 1);
                    bundle.putInt("LastTime", 0);
                    bundle.putString("file_path", currentPlayingFile.getAbsolutePath());
                    intent.putExtras(bundle);
                    intent.setPackage(getPackageName());
                    startService(intent);
                    fileAdapter.notifyDataSetChanged();
                }
            }
        });
        this.nextButton = (ImageButton) this.findViewById(R.id.btn_playNext);
        this.nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (PlayService.getPlayerState().equals("Playing")) {
                    PlayService.stop();
                }

                if (currentPlayList.indexOf(currentPlayingFile) < currentPlayList.size() - 1) {
                    //HomuraPlayer player = HomuraPlayer.getInstance(Uri.fromFile(currentPlayList.get(currentPlayList.indexOf(currentPlayingFile) + 1)), FileActivity.this);
                    currentPlayingFile = currentPlayList.get(currentPlayList.indexOf(currentPlayingFile) + 1);
                    fileAdapter.sendCurrentLyric();
                    Intent intent = new Intent("com.service.PlayService");
                    Bundle bundle = new Bundle();
                    bundle.putInt("op", 1);
                    bundle.putInt("LastTime", 0);
                    bundle.putString("file_path", currentPlayingFile.getAbsolutePath());
                    intent.putExtras(bundle);
                    intent.setPackage(getPackageName());
                    startService(intent);
                    fileAdapter.notifyDataSetChanged();
                }
            }
        });
        this.total_Time = (TextView) this.findViewById(R.id.totalTime_tv);
        this.current_Time = (TextView) this.findViewById(R.id.currentTime_tv);
        if (currentPlayingFile == null) {
            File path = new File(File.separator + "storage/emulated/0");
            FileAdapter.files = path.listFiles();
            Arrays.sort(FileAdapter.files);
            currentFile = path;
            currentDirectory = path.getAbsolutePath();
            this.setTitle(currentDirectory);
            fileAdapter.notifyDataSetChanged();
        } else {
            currentFile = currentPlayingFile.getParentFile();
            FileAdapter.files = currentPlayingFile.getParentFile().listFiles();
            Arrays.sort(FileAdapter.files);
            currentDirectory = currentPlayingFile.getParentFile().getAbsolutePath();
            this.setTitle(currentDirectory);
            fileAdapter.notifyDataSetChanged();
        }
    }

    private void InitSeekBar() {
        seekBar = (SeekBar) this.findViewById(R.id.playback_seekbar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                current_Time.setText(String.format("%02d", seekBar.getProgress() / 60) + ":" + String.format("%02d", seekBar.getProgress() % 60));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (currentPlayingFile == null || currentPlayList == null) {
                    seekBar.setEnabled(false);
                } else {
                    seekBar.setEnabled(true);
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (PlayService.getPlayerState().equals("Playing")) {
                    PlayService.seekTo(seekBar.getProgress() * 1000);
                }
            }
        });
    }

    @Override
    public void setTitle(CharSequence title) {
        myTitle.setText(title);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            if (currentFile == null) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("EXIT", true);
                startActivity(intent);
            } else if (currentFile.getAbsolutePath().equals(File.separator + "storage")) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                if (currentPlayingFile != null) {
                    editor.putString("LastPlayingFile", currentPlayingFile.getAbsolutePath());
                }
                seekBar.removeCallbacks(MusicRunnable.mRunnable);
                if (PlayService.exist()) {
                    editor.putInt("LastPlayingTime", seekBar.getProgress());
                    if (PlayService.getPlayerState().equals("Playing")) {
                        PlayService.stop();
                    }
                    PlayService.release();
                }

                //release wake lock
                if (wakeLock != null && wakeLock.isHeld()) {
                    wakeLock.release();
                    wakeLock = null;
                }

                editor.commit();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("EXIT", true);
                startActivity(intent);
            } else {
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
    protected void onResume() {
        super.onResume();
        initView();
        if (currentPlayingFile == null) {
            if (PlayService.exist()) {
                LastPlayingFile = PlayService.getLastFile();
                LastPlayingTime = PlayService.getLastProgress();
            }
            LastPlayingFile = sharedPreferences.getString("LastPlayingFile", "");
            LastPlayingTime = sharedPreferences.getInt("LastPlayingTime", 0);
            if (LastPlayingFile.equals("")) {
                return;
            }
            currentPlayingFile = new File(LastPlayingFile);
            FileAdapter.beforeMusicPlay(currentPlayingFile, currentPlayingFile.getParentFile().listFiles());
            //Test
            //HomuraPlayer player = HomuraPlayer.getInstance(Uri.fromFile(currentPlayingFile), this);
            //player.seekTo(LastPlayingTime * 1000);

            Intent intent = new Intent("com.service.PlayService");
            Bundle bundle = new Bundle();
            intent.setPackage(getPackageName());
            bundle.putInt("op", 1);
            bundle.putInt("LastTime", LastPlayingTime * 1000);
            bundle.putString("file_path", currentPlayingFile.getAbsolutePath());
            intent.putExtras(bundle);
            startService(intent);
            //
            pauseButton.setVisibility(View.VISIBLE);
            playButton.setVisibility(View.GONE);

            //seekBar.setProgress(LastPlayingTime);
            current_Time.setText(String.format("%02d", seekBar.getProgress() / 60) + ":" + String.format("%02d", seekBar.getProgress() % 60));
            //total_Time.setText(String.format("%02d", seekBar.getMax() / 60) + ":" + String.format("%02d", seekBar.getMax() % 60));
        } else {
            if (currentLyric != null) {
                ILrcBuilder builder = new DefaultLrcBuilder();
                List<LrcRow> rows = builder.getLrcRows(currentLyric);
                lrcView.setLrc(rows);
            }
            seekBar.setProgress(PlayService.GetProgress());
            current_Time.setText(String.format("%02d", PlayService.GetProgress() / 60) + ":" + String.format("%02d", PlayService.GetProgress() % 60));
            total_Time.setText(String.format("%02d", seekBar.getMax() / 60) + ":" + String.format("%02d", seekBar.getMax() % 60));
            if (PlayService.getPlayerState().equals("Playing")) {
                pauseButton.setVisibility(View.VISIBLE);
                playButton.setVisibility(View.GONE);
            } else if (PlayService.getPlayerState().equals("Pause")) {
                pauseButton.setVisibility(View.GONE);
                playButton.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onOptionsMenuClosed(Menu menu) {

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, 1, Menu.NONE, "搜索");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                if (FileActivity.currentPlayingFile == null) {
                    new AlertDialog.Builder(this).setTitle("当前不存在正在的文件").setIcon(android.R.drawable.ic_dialog_info).setNegativeButton("确定", null).show();
                } else {
                    LayoutInflater lf = getLayoutInflater().from(this);
                    View FileInfoView = lf.inflate(R.layout.file_info, null);
                    artistText = (EditText) FileInfoView.findViewById(R.id.ArtistText);
                    titleText = (EditText) FileInfoView.findViewById(R.id.TitleText);
                    artistText.setText(FileActivity.currentArtist);
                    titleText.setText(FileActivity.currentPlayingTitle);
                    new AlertDialog.Builder(this).setTitle("确认信息").setIcon(android.R.drawable.ic_dialog_info).setView(FileInfoView).setNegativeButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Thread LyricThread = new Thread() {
                                public void run() {
                                    String music_title = titleText.getText().toString();
                                    final String artist_name = artistText.getText().toString();
                                    LyricSearch ls = new LyricSearch(music_title, artist_name);
                                    ArrayList result = ls.fetchLyric();
                                    try {
                                        boolean flag = FileIO.SaveLyric(result, music_title);
                                        if (flag) {
                                            Toast.makeText(MyApplication.getAppContext(), "转换成功", Toast.LENGTH_SHORT);
                                        } else {
                                            Toast.makeText(MyApplication.getAppContext(), "转换出错", Toast.LENGTH_SHORT);
                                        }
                                    } catch (IOException e) {
                                        Toast.makeText(MyApplication.getAppContext(), "转换出错", Toast.LENGTH_SHORT);
                                    }
                                }
                            };
                            LyricThread.start();
                        }
                    }).show();
                }
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    class MyPhoneStateListener extends PhoneStateListener {

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
                    seekBar.removeCallbacks(MusicRunnable.mRunnable);
                    if (PlayService.exist()) {
                        editor.putInt("LastPlayingTime", seekBar.getProgress());
                        if (PlayService.getPlayerState().equals("Playing")) {
                            PlayService.pause();
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
