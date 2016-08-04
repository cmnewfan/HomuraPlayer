package ljl.com.homuraproject.Activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.DragEvent;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import douzi.android.view.DefaultLrcBuilder;
import douzi.android.view.ILrcBuilder;
import douzi.android.view.LrcRow;
import douzi.android.view.LrcView;
import ljl.com.homuraproject.Adapter.FileAdapter;
import ljl.com.homuraproject.Constants;
import ljl.com.homuraproject.Control.FileIO;
import ljl.com.homuraproject.Control.LyricControl;
import ljl.com.homuraproject.MusicRunnable;
import ljl.com.homuraproject.MyApplication;
import ljl.com.homuraproject.PlayService;
import ljl.com.homuraproject.PostMan;
import ljl.com.homuraproject.QueryResult;
import ljl.com.homuraproject.R;
import ljl.com.homuraproject.TTDownloader;

/**
 * Created by Administrator on 2015/7/31.
 */
@SuppressWarnings("unchecked")
public class FileActivity extends Activity implements View.OnTouchListener {
    private static Handler handler;
    private static SeekBar seekBar;
    private static FileAdapter fileAdapter;
    private static File currentDirectory;
    private static File currentPlayingFile;
    private static String LastPlayingFile;
    private ListView listView;
    private TextView current_Time;
    private ImageButton playButton;
    private ImageButton pauseButton;
    private ImageButton prevButton;
    private ImageButton nextButton;
    private ImageButton searchButton;
    private ImageButton deleteButton;
    private ImageButton phoneButton;
    private TextView total_Time;
    private LrcView lrcView;
    private TextView myTitle;
    private ImageView main_backgroundImage;
    private ViewPager viewPager;
    private PagerTabStrip pagerTabStrip;
    private ImageButton mImageButton;
    private LinearLayout linear_layout_normal;
    private LinearLayout linear_layout_onlongclick;
    private LinearLayout linear_layout_onlongclick_text;
    private LinearLayout progress_layout;
    private static SharedPreferences sharedPreferences;
    private static int LastPlayingTime;
    private PowerManager.WakeLock wakeLock;
    private int scrolledX = -1;
    private float touch_x = 0;
    private float touch_y = 0;
    private static final Object RecordLock = new Object();
    private static String LyricName;
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
                PostMan.sendMessage(Constants.PlayServiceCommand, Constants.PlayServiceCommand_Stop);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        //start play service
        Intent intent = new Intent("com.service.PlayService");
        //Service Intent must be explicit. from Android 5.0
        intent.setPackage(getPackageName());
        startService(intent);
        //set custom title
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.file_explorer);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title_bar);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );
        //WakeLock
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        //wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, FileActivity.class.getName());
        //wakeLock.acquire();
        //telephone state listener
        TelephonyManager manager = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
        manager.listen(new MyPhoneStateListener(), PhoneStateListener.LISTEN_CALL_STATE);
        registerReceiver(myReceiver, new IntentFilter("android.intent.action.PHONE_STATE"));
        //handle message
        sharedPreferences = this.getSharedPreferences("music_player_info", Context.MODE_PRIVATE);
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.obj.toString().equals(Constants.PlayServiceCommand)) {
                    switch (msg.what) {
                        case Constants.PlayServiceCommand_Play:
                            pauseButton.setVisibility(View.VISIBLE);
                            playButton.setVisibility(View.GONE);
                            PlayService.ControlNotificationView(R.id.notification_pause, View.VISIBLE);
                            PlayService.ControlNotificationView(R.id.notification_play, View.GONE);
                            total_Time.setText(String.format("%02d", seekBar.getMax() / 60) + ":" + String.format("%02d", seekBar.getMax() % 60));
                            Intent intent = new Intent("com.service.PlayService");
                            intent.putExtras(msg.getData());
                            intent.setPackage(getPackageName());
                            startService(intent);
                            break;
                        case Constants.PlayServiceCommand_PlayFromService:
                            pauseButton.setVisibility(View.VISIBLE);
                            playButton.setVisibility(View.GONE);
                            total_Time.setText(String.format("%02d", seekBar.getMax() / 60) + ":" + String.format("%02d", seekBar.getMax() % 60));
                            break;
                        case Constants.PlayServiceCommand_Stop:
                            playButton.setVisibility(View.VISIBLE);
                            pauseButton.setVisibility(View.GONE);
                            break;
                        case Constants.PlayServiceCommand_Pause:
                            pauseButton.setVisibility(View.GONE);
                            playButton.setVisibility(View.VISIBLE);
                            PlayService.ControlNotificationView(R.id.notification_play, View.VISIBLE);
                            PlayService.ControlNotificationView(R.id.notification_pause, View.GONE);
                            break;
                        case Constants.PlayServiceCommand_PlayFromNotification:
                            pauseButton.setVisibility(View.VISIBLE);
                            playButton.setVisibility(View.GONE);
                            break;
                        default:
                            break;
                    }
                } else if (msg.obj.toString().equals(Constants.ViewControl)) {
                    switch (msg.what) {
                        case Constants.ViewControl_SetTitle:
                            setTitle(currentDirectory.getAbsolutePath());
                            break;
                        case Constants.ViewControl_SetMusicTitle:
                            setTitle(LyricControl.getCurrentPlayingTitle());
                            PlayService.UpdateNotification(LyricControl.getCurrentPlayingTitle(), LyricControl.getCurrentArtist());
                            break;
                        case Constants.ViewControl_UpdateLyric:
                            ILrcBuilder builder = new DefaultLrcBuilder();
                            List<LrcRow> rows = builder.getLrcRows(LyricControl.getCurrentLyric());
                            lrcView.setLrc(rows);
                            break;
                        case Constants.ViewControl_PlayLrc:
                            lrcView.seekLrcToTime(seekBar.getProgress() * 1000);
                            break;
                        case Constants.ViewControl_PlayLrcFromCue:
                            lrcView.seekLrcToTime(msg.getData().getInt("LyricTime"));
                            break;
                        case Constants.ViewControl_UnsupportdFormat:
                            Toast.makeText(MyApplication.getAppContext(), "暂不支持目标文件格式", Toast.LENGTH_SHORT).show();
                            break;
                        case Constants.ViewControl_ToastMiss:
                            Toast.makeText(MyApplication.getAppContext(), "未找到目标", Toast.LENGTH_SHORT).show();
                            break;
                        case Constants.ViewControl_ToastError:
                            Toast.makeText(MyApplication.getAppContext(), "操作出错", Toast.LENGTH_SHORT).show();
                            break;
                        case Constants.ViewControl_ToastSuccess:
                            Toast.makeText(MyApplication.getAppContext(), "操作成功", Toast.LENGTH_SHORT).show();
                            break;
                        case Constants.ViewControl_ToastUpdate:
                            Toast.makeText(MyApplication.getAppContext(), "为找到歌曲信息，请在设置里更新数据库", Toast.LENGTH_SHORT).show();
                            break;
                        case Constants.ViewControl_OpenOptionsMenu:
                            linear_layout_normal.setVisibility(View.GONE);
                            linear_layout_onlongclick.setVisibility(View.VISIBLE);
                            progress_layout.setVisibility(View.GONE);
                            linear_layout_onlongclick_text.setVisibility(View.VISIBLE);
                            break;
                        case Constants.ViewControl_OnKeyDown:
                            OnKeyDown();
                            break;
                        case Constants.ViewControl_SetMusicTitleFromCue:
                            setTitle(msg.getData().getString("Title"));
                            PlayService.UpdateNotification(msg.getData().getString("Title"), msg.getData().getString("Performer"));
                            break;
                        default:
                            break;
                    }
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
                                    boolean flag = TTDownloader.download(queryList.get(item), LyricName);
                                    if (flag) {
                                        PostMan.sendMessage(Constants.ViewControl, Constants.ViewControl_ToastSuccess);
                                    } else {
                                        PostMan.sendMessage(Constants.ViewControl, Constants.ViewControl_ToastError);
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

    public static File getCurrentDirectory() {
        return currentDirectory;
    }

    public static void setCurrentDirectory(File currentDirectory) {
        FileActivity.currentDirectory = currentDirectory;
    }

    /**
     * reset playing state when delete the last file on the play list
     */
    public static void resetLastPlayingState() {
        LastPlayingFile = "";
        LastPlayingTime = 0;
        currentPlayingFile = null;
        seekBar.setProgress(0);
    }

    /**
     * get message of handler in FileActivity
     */
    public static Message getMainLoopMessage() {
        return handler.obtainMessage();
    }

    /**
     * send message to handler
     *
     * @param mes
     */
    public static void sendMessage(Message mes) {
        handler.sendMessage(mes);
    }

    /**
     * update file adapter when data changed
     */
    public static void NotifyDataChangd() {
        fileAdapter.notifyDataSetChanged();
    }

    /**
     * get progress of seekbar
     * @return progress of seekbar
     */
    public static int GetSeekbarProgress() {
        return seekBar.getProgress();
    }

    /**
     * get max value of seekbar
     * @return max value of seekbar
     */
    public static int GetSeekBarMax() {
        return seekBar.getMax();
    }

    /**
     * set increment of seekbar
     * @param increment increment progress
     */
    public static void SeekbarIncrement(int increment) {
        seekBar.incrementProgressBy(increment);
    }

    /**
     * init seekbar runnable
     */
    public static void initSeekbarRunnable() {
        FileActivity.RemoveSekbarCallbacks(MusicRunnable.mRunnable);
        FileActivity.SeekBarPost(MusicRunnable.mRunnable, 1000);
    }

    /**
     * set runnable of seekbar
     * @param runnable target runnable
     * @param time execution time
     */
    public static void SeekBarPost(Runnable runnable, long time) {
        seekBar.postDelayed(runnable, time);
    }

    /**
     * set progress of seekbar
     * @param progress target progress
     */
    public static void SetSeekbarProgress(int progress) {
        seekBar.setProgress(progress);
    }

    /**
     * set max value of seekbar
     * @param max target value
     */
    public static void SetSeekbarMax(int max) {
        seekBar.setMax(max);
    }

    /**
     * remove runnable of seekbar
     * @param runnable target runnable
     */
    public static void RemoveSekbarCallbacks(Runnable runnable) {
        seekBar.removeCallbacks(runnable);
    }

    /**
     * scan music on phone
     */
    private void scanSdCard() {
        mediaScan(FileActivity.currentPlayingFile);
    }

    /**
     * scan music on phone
     *
     * @param file
     */
    private void mediaScan(File file) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            file = Environment.getExternalStorageDirectory();
        } else {
            file = Environment.getDataDirectory();
        }
        MediaScannerConnection.scanFile(FileActivity.this,
                new String[]{file.getAbsolutePath()}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    @Override
                    public void onScanCompleted(String path, Uri uri) {
                        PostMan.sendMessage(Constants.ViewControl, Constants.ViewControl_ToastSuccess);
                    }
                });
    }

    /**
     * init view pager adater and views in view pager
     */
    private void InitViewPagerAdapter() {
        this.viewPager = (ViewPager) this.findViewById(R.id.viewpager);
        final ArrayList<String> titleList = new ArrayList<String>();
        titleList.add("FileList");
        titleList.add("Lyric");
        titleList.add("MyLrc");
        LayoutInflater lf = getLayoutInflater().from(this);
        View FileView = lf.inflate(R.layout.fileview, null);
        this.listView = (ListView) FileView.findViewById(R.id.file_listView);
        fileAdapter = new FileAdapter(FileActivity.this);
        this.listView.setAdapter(fileAdapter);
        //save the position when scroll action ended
        this.listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == SCROLL_STATE_IDLE) {
                    scrolledX = view.getFirstVisiblePosition();
                }
            }
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
        //change state when drag action ended
        this.listView.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                if (event.getAction() == DragEvent.ACTION_DROP) {
                    linear_layout_normal.setVisibility(View.VISIBLE);
                    linear_layout_onlongclick.setVisibility(View.GONE);
                    progress_layout.setVisibility(View.VISIBLE);
                    linear_layout_onlongclick_text.setVisibility(View.GONE);
                }
                return true;
            }
        });
        //back to previous scene when touch action ended
        this.listView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        touch_x = event.getX();
                        touch_y = event.getY();
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        float now_x = event.getX();
                        float now_y = event.getY();
                        if ((now_x - touch_x > 40 && (Math.abs(now_y - touch_y) < (listView.getHeight() / 4)))) {
                            OnKeyDown();
                        }
                        break;
                }
                return false;
            }
        });
        View LrcView = lf.inflate(R.layout.lrcview, null);
        this.lrcView = (LrcView) LrcView.findViewById(R.id.lrcView);
        final ArrayList<View> viewList = new ArrayList<View>();
        viewList.add(FileView);
        viewList.add(LrcView);
        //init view pager adapter
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

    /**
     * init pager tab strip
     */
    private void InitPagerTabStrip() {
        pagerTabStrip = (PagerTabStrip) findViewById(R.id.pagertab);
        pagerTabStrip.setTabIndicatorColor(getResources().getColor(R.color.material_blue_grey_800));
        pagerTabStrip.setDrawFullUnderline(false);
        pagerTabStrip.setTextSpacing(50);
    }

    /**
     * init buttons, layouts and labels in view
     */
    private void initView() {
        //init layouts on bottom
        this.linear_layout_normal = (LinearLayout) this.findViewById(R.id.linear_layout_normal);
        this.linear_layout_onlongclick = (LinearLayout) this.findViewById(R.id.linear_layout_onlongclick);
        this.linear_layout_onlongclick_text = (LinearLayout) this.findViewById(R.id.linear_layout_onlongclick_text);
        this.progress_layout = (LinearLayout) this.findViewById(R.id.progressLayout);
        //init search button
        initSearchButton();
        //init delete button
        initDeleteButton();
        //init phone button
        initPhoneButton();
        this.myTitle = (TextView) this.findViewById(R.id.myTitle);
        InitOptionButton();
        InitSeekBar();
        InitViewPagerAdapter();
        InitPagerTabStrip();
        //init play control buttons
        InitPlayControlButtons();
        this.total_Time = (TextView) this.findViewById(R.id.totalTime_tv);
        this.current_Time = (TextView) this.findViewById(R.id.currentTime_tv);
    }

    /**
     * init option button
     */
    private void InitOptionButton() {
        this.mImageButton = (ImageButton) this.findViewById(R.id.imageButton);
        this.mImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //init popup menu
                final PopupMenu popupMenu = new PopupMenu(FileActivity.this, v);
                popupMenu.inflate(R.menu.menu_main);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_settings:
                                scanSdCard();
                                break;
                            case R.id.share:
                                //begin when current playing title is not null
                                if (LyricControl.getCurrentPlayingTitle() != null && (!LyricControl.getCurrentPlayingTitle().equals(""))) {
                                    Intent sendIntent = new Intent(Intent.ACTION_SEND);
                                    //set intent type
                                    sendIntent.setType("image/*");
                                    Uri targetUri = FileIO.getImageUri(currentPlayingFile.getParentFile());
                                    if (targetUri != null) {
                                        sendIntent.putExtra(Intent.EXTRA_STREAM, targetUri);
                                    }
                                    sendIntent.putExtra(Intent.EXTRA_SUBJECT, "分享");
                                    sendIntent.putExtra(Intent.EXTRA_TEXT, LyricControl.getCurrentPlayingTitle());
                                    sendIntent.putExtra(Intent.EXTRA_TITLE, "From HomuraPlayer");
                                    sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(Intent.createChooser(sendIntent, "share"));
                                } else {
                                    Toast.makeText(FileActivity.this, "该功能需要在当前正在播放音乐的时候使用", Toast.LENGTH_SHORT).show();
                                }
                            default:
                                break;
                        }
                        return true;
                    }
                });
                popupMenu.show();
            }
        });
    }

    /**
     * init play, pause, previous and next button
     */
    private void InitPlayControlButtons() {
        this.playButton = (ImageButton) this.findViewById(R.id.btn_play);
        this.playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playButton.setVisibility(View.GONE);
                pauseButton.setVisibility(View.VISIBLE);
                if (PlayService.getPlayerState() != null && !PlayService.getPlayerState().equals("Playing")) {
                    PlayService.play();
                }
                PostMan.sendMessage(Constants.PlayServiceCommand, Constants.PlayServiceCommand_Play);
            }
        });
        this.pauseButton = (ImageButton) this.findViewById(R.id.btn_pause);
        this.pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (PlayService.getPlayerState() != null && PlayService.getPlayerState().equals("Playing")) {
                    PlayService.pause();
                } else {
                    return;
                }
                PostMan.sendMessage(Constants.PlayServiceCommand, Constants.PlayServiceCommand_Pause);
            }
        });
        this.prevButton = (ImageButton) this.findViewById(R.id.btn_playPre);
        this.prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (PlayService.getPlayerState() == null) {
                    return;
                }
                if (PlayService.getCurrentPlayList().indexOf(currentPlayingFile) > 0 || (PlayService.HasCueModel() && PlayService.getCueModel().IsEnableToMoveToNextOrPrev(-1))) {
                    if (PlayService.getPlayerState().equals("Playing")) {
                        PlayService.stop();
                    }
                    ControlPlayList(-1);
                } else {
                    Toast.makeText(FileActivity.this, "当前已是播放列表中的第一首歌曲", Toast.LENGTH_SHORT).show();
                }
            }
        });
        this.nextButton = (ImageButton) this.findViewById(R.id.btn_playNext);
        this.nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (PlayService.getPlayerState() == null) {
                    return;
                }
                if (PlayService.getCurrentPlayList().indexOf(currentPlayingFile) < PlayService.getCurrentPlayList().size() - 1 || (PlayService.HasCueModel() && PlayService.getCueModel().IsEnableToMoveToNextOrPrev(1))) {
                    if (PlayService.getPlayerState().equals("Playing")) {
                        PlayService.stop();
                    }
                    ControlPlayList(1);
                } else {
                    Toast.makeText(FileActivity.this, "当前已是播放列表中的最后一首歌曲", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public static File getCurrentPlayingFile() {
        return currentPlayingFile;
    }

    /**
     * back to the previous music on play list or advance to the next ControlPlayList
     *
     * @param index 1 means next, -1 means previous
     */
    private void ControlPlayList(int index) {
        if (!PlayService.HasCueModel()) {
            currentPlayingFile = PlayService.getCurrentPlayList().get(PlayService.getCurrentPlayList().indexOf(currentPlayingFile) + index);
            LyricControl.sendCurrentLyric();
            Intent intent = PlayService.CreateNewIntent(1, 0, currentPlayingFile.getAbsolutePath());
            intent.setPackage(getPackageName());
            startService(intent);
            fileAdapter.notifyDataSetChanged();
        } else {
            LyricControl.sendCurrentLyric();
            Intent intent = PlayService.CreateNewIntent(1, PlayService.getCueModel().getNextTrack(index).getStart_time(), currentPlayingFile.getAbsolutePath());
            intent.setPackage(getPackageName());
            startService(intent);
        }
    }

    /**
     * init phone button
     */
    private void initPhoneButton() {
        this.phoneButton = (ImageButton) this.findViewById(R.id.btn_set);
        this.phoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(FileActivity.this, "请拖拽所选文件至按钮处", Toast.LENGTH_SHORT).show();
            }
        });
        this.phoneButton.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                if (event.getAction() == DragEvent.ACTION_DROP) {
                    linear_layout_normal.setVisibility(View.VISIBLE);
                    linear_layout_onlongclick.setVisibility(View.GONE);
                    progress_layout.setVisibility(View.VISIBLE);
                    linear_layout_onlongclick_text.setVisibility(View.GONE);
                    phoneButton.invalidate();
                    File file = new File(event.getClipData().getItemAt(1).getText().toString());
                    if (file.isFile()) {
                        ContentValues values = new ContentValues();
                        Cursor c = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                new String[]{MediaStore.Video.Media.TITLE,
                                        MediaStore.Audio.Media.DURATION,
                                        MediaStore.Audio.Media.ARTIST,
                                        MediaStore.Audio.Media.DISPLAY_NAME,
                                        MediaStore.Audio.Media.SIZE,
                                        MediaStore.Video.Media._ID
                                },
                                "_display_name=?",
                                new String[]{file.getName()},
                                null);
                        try {
                            c.moveToFirst();
                        } catch (NullPointerException ex) {
                            new AlertDialog.Builder(FileActivity.this).setTitle("通知").setPositiveButton("确定", null).setMessage("数据库查询失败，请检查文件后更新数据库").show();
                            return true;
                        }
                        if (c.getCount() > 0) {
                            values.put(MediaStore.MediaColumns.DATA, file.getAbsolutePath());
                            values.put(MediaStore.Audio.Media.TITLE, c.getString(0));
                            values.put(MediaStore.MediaColumns.SIZE, c.getString(4));
                            values.put(MediaStore.Audio.Media.ARTIST, c.getString(2));
                            values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/*");
                            values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
                            values.put(MediaStore.Audio.Media.DURATION, c.getString(1));
                            values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
                            values.put(MediaStore.Audio.Media.IS_ALARM, false);
                            values.put(MediaStore.Audio.Media.IS_MUSIC, false);
                            c.close();
                            Uri uri = MediaStore.Audio.Media.getContentUriForPath(file.getAbsolutePath());
                            getContentResolver().delete(uri, MediaStore.MediaColumns.DATA
                                    + "=\"" + file.getAbsolutePath() + "\"", null);
                            Uri newUri = getContentResolver().insert(uri, values);
                            //getContentResolver().update(uri, values, MediaStore.MediaColumns.DATA, new String[]{file.getAbsolutePath()});
                            RingtoneManager.setActualDefaultRingtoneUri(FileActivity.this,
                                    RingtoneManager.TYPE_RINGTONE, newUri);
                            new AlertDialog.Builder(FileActivity.this).setTitle("通知").setPositiveButton("确定", null).setMessage("铃声设置成功").show();
                        } else {
                            new AlertDialog.Builder(FileActivity.this).setTitle("通知").setPositiveButton("确定", null).setMessage("铃声设置失败,文件类型错误").show();
                        }
                    } else {
                        new AlertDialog.Builder(FileActivity.this).setTitle("通知").setPositiveButton("确定", null).setMessage("铃声设置失败,文件类型错误").show();
                    }
                } else if (event.getAction() == DragEvent.ACTION_DRAG_ENTERED) {
                    phoneButton.invalidate();
                } else if (event.getAction() == DragEvent.ACTION_DRAG_EXITED) {
                    phoneButton.invalidate();
                }
                return true;
            }
        });
    }

    /**
     * init delete button
     */
    private void initDeleteButton() {
        this.deleteButton = (ImageButton) this.findViewById(R.id.btn_delete);
        this.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(FileActivity.this, "请拖拽所选文件至按钮处", Toast.LENGTH_SHORT).show();
            }
        });
        this.deleteButton.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                final ClipData data = event.getClipData();
                if (event.getAction() == DragEvent.ACTION_DROP) {
                    new AlertDialog.Builder(FileActivity.this).setTitle("确认").setMessage("确认删除吗?").setPositiveButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (data != null) {
                                FileIO.DeleteFile(data.getItemAt(1).getText().toString());
                            }
                        }
                    }).setNegativeButton("取消", null).show();
                    linear_layout_normal.setVisibility(View.VISIBLE);
                    linear_layout_onlongclick.setVisibility(View.GONE);
                    progress_layout.setVisibility(View.VISIBLE);
                    linear_layout_onlongclick_text.setVisibility(View.GONE);
                    deleteButton.invalidate();
                } else if (event.getAction() == DragEvent.ACTION_DRAG_ENTERED) {
                    deleteButton.invalidate();
                } else if (event.getAction() == DragEvent.ACTION_DRAG_EXITED) {
                    deleteButton.invalidate();
                }
                return true;
            }
        });
    }

    /**
     * init search button
     */
    private void initSearchButton() {
        this.searchButton = (ImageButton) this.findViewById(R.id.btn_search);
        this.searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(FileActivity.this, "请拖拽所选文件至按钮处", Toast.LENGTH_SHORT).show();
            }
        });
        this.searchButton.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                if (event.getAction() == DragEvent.ACTION_DROP) {
                    SearchButtonResponse(event);
                    linear_layout_normal.setVisibility(View.VISIBLE);
                    linear_layout_onlongclick.setVisibility(View.GONE);
                    progress_layout.setVisibility(View.VISIBLE);
                    linear_layout_onlongclick_text.setVisibility(View.GONE);
                    Toast.makeText(FileActivity.this, "正在搜索歌词,请稍候...", Toast.LENGTH_SHORT).show();
                    searchButton.invalidate();
                } else if (event.getAction() == DragEvent.ACTION_DRAG_ENTERED) {
                    searchButton.invalidate();
                } else if (event.getAction() == DragEvent.ACTION_DRAG_EXITED) {
                    searchButton.invalidate();
                }
                return true;
            }
        });
    }

    /**
     * when drag event ended in search button
     * @param event drag event which involve file information
     */
    private void SearchButtonResponse(DragEvent event) {
        File file = new File(event.getClipData().getItemAt(1).getText().toString());
        if (file.isFile()) {
            Cursor c = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Video.Media.TITLE,
                            MediaStore.Audio.Media.ARTIST
                    },
                    "_data=?",
                    new String[]{file.getAbsolutePath()},
                    null);
            try {
                c.moveToFirst();
            } catch (NullPointerException ex) {
                new AlertDialog.Builder(FileActivity.this).setTitle("通知").setPositiveButton("确定", null).setMessage("数据库查询失败，请检查文件后更新数据库").show();
                return;
            }
            if (c.getCount() > 0) {
                final String artist = c.getString(1);
                final String title = c.getString(0);
                LyricName = file.getName().substring(0,file.getName().lastIndexOf("."));
                c.close();
                StartLyricSearchThread(title, artist);
                fileAdapter.notifyDataSetChanged();
            } else if (PlayService.HasCueModel()) {
                final String artist = PlayService.getCueModel().getCurrentTrack(GetSeekbarProgress() * 1000).getPerformer();
                final String title = PlayService.getCueModel().getCurrentTrack(GetSeekbarProgress() * 1000).getTitle();
                LyricName = artist + "-" + title;
                StartLyricSearchThread(title, artist);
            } else {
                c.close();
                new AlertDialog.Builder(FileActivity.this).setTitle("通知").setPositiveButton("确定", null).setMessage("歌词搜索失败,文件类型错误").show();
            }
        } else {
            new AlertDialog.Builder(FileActivity.this).setTitle("通知").setPositiveButton("确定", null).setMessage("歌词搜索失败,文件类型错误").show();
        }
        fileAdapter.notifyDataSetChanged();
    }

    private void StartLyricSearchThread(final String title, final String artist) {
        Thread LyricThread = new Thread() {
            public void run() {
                final ArrayList<QueryResult> queryList = TTDownloader.query(artist, title);
                if (queryList != null && queryList.size() != 0) {
                    final String[] list = new String[queryList.size()];
                    for (int i = 0; i < list.length; i++) {
                        list[i] = queryList.get(i).mTitle;
                    }
                    Object[] obj = new Object[]{queryList, list};
                    PostMan.sendMessage(obj);
                } else {
                    PostMan.sendMessage(Constants.ViewControl, Constants.ViewControl_ToastMiss);
                }
            }
        };
        LyricThread.start();
    }

    /**
     * init seekbar
     */
    private void InitSeekBar() {
        seekBar = (SeekBar) this.findViewById(R.id.playback_seekbar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                current_Time.setText(String.format("%02d", seekBar.getProgress() / 60) + ":" + String.format("%02d", seekBar.getProgress() % 60));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (currentPlayingFile == null || PlayService.getCurrentPlayList() == null) {
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

    /**
     * set title of activity
     * @param title
     */
    @Override
    public void setTitle(CharSequence title) {
        myTitle.setText(title);
    }

    /**
     * when press keycode_back
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            OnKeyDown();
        }
        return false;
    }

    /**
     * when press KeyDown
     */
    private void OnKeyDown() {
        //if current file is null, clear activity.
        if (currentDirectory == null) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("EXIT", true);
            startActivity(intent);
            PlayService.release();
        } else if (currentDirectory.getAbsolutePath().equals(File.separator + "storage")) {
            seekBar.removeCallbacks(MusicRunnable.mRunnable);
            RecordPlayingInformation();
            //release wake lock
            if (wakeLock != null && wakeLock.isHeld()) {
                wakeLock.release();
                wakeLock = null;
            }
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("EXIT", true);
            startActivity(intent);
        } else {
            if(viewPager.getCurrentItem()==1){
                viewPager.setCurrentItem(0,true);
            }
            FileAdapter.setFiles(FileIO.SortFiles(currentDirectory.getParentFile()));
            currentDirectory = currentDirectory.getParentFile();
            //Arrays.sort(FileAdapter.files);
            this.setTitle(currentDirectory.getAbsolutePath());
            fileAdapter.notifyDataSetChanged();
            if (scrolledX != -1) {
                listView.setSelection(scrolledX);
            }
        }
    }

    /**
     * record play information
     */
    public static void RecordPlayingInformation() {
        synchronized (RecordLock) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            if (currentPlayingFile != null) {
                editor.putString("LastPlayingFile", currentPlayingFile.getAbsolutePath());
                editor.putInt("LastPlayingTime", seekBar.getProgress());
            } else {
                editor.putString("LastPlayingFile", "");
                editor.putInt("LastPlayingTime", 0);
            }
            editor.commit();
        }
    }

    public static void setCurrentPlayingFile(File currentPlayingFile) {
        FileActivity.currentPlayingFile = currentPlayingFile;
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
                ShowDefaultCatalog();
                return;
            }
            currentPlayingFile = new File(LastPlayingFile);
            /*PlayService.generatePlayList(currentPlayingFile, currentPlayingFile.getParentFile().listFiles());
            Intent intent = new Intent("com.service.PlayService");
            Bundle bundle = new Bundle();
            intent.setPackage(getPackageName());
            bundle.putInt("op", 1);
            bundle.putInt("LastTime", LastPlayingTime * 1000);
            bundle.putString("file_path", currentPlayingFile.getAbsolutePath());
            intent.putExtras(bundle);
            startService(intent);*/
            try {
                PlayService.generatePlayList(currentPlayingFile, FileIO.SortFiles(currentPlayingFile.getParentFile()));
            } catch (NullPointerException ex) {
                ShowDefaultCatalog();
                return;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            Intent intent = PlayService.CreateNewIntent(1, 0, currentPlayingFile.getAbsolutePath());
            intent.setPackage(getPackageName());
            startService(intent);
            //
            pauseButton.setVisibility(View.VISIBLE);
            playButton.setVisibility(View.GONE);
            current_Time.setText(String.format("%02d", seekBar.getProgress() / 60) + ":" + String.format("%02d", seekBar.getProgress() % 60));
            currentDirectory = currentPlayingFile.getParentFile();
            FileAdapter.setFiles(FileIO.SortFiles(currentPlayingFile.getParentFile()));
            this.setTitle(currentDirectory.getAbsolutePath());
            fileAdapter.notifyDataSetChanged();
        } else {
            //useless?
            if (LyricControl.getCurrentLyric() != null) {
                ILrcBuilder builder = new DefaultLrcBuilder();
                List<LrcRow> rows = builder.getLrcRows(LyricControl.getCurrentLyric());
                lrcView.setLrc(rows);
            }
            seekBar.setProgress(PlayService.GetProgress());
            current_Time.setText(String.format("%02d", PlayService.GetProgress() / 60) + ":" + String.format("%02d", PlayService.GetProgress() % 60));
            total_Time.setText(String.format("%02d", seekBar.getMax() / 60) + ":" + String.format("%02d", seekBar.getMax() % 60));
            if (PlayService.getPlayerState().equals("Playing")) {
                pauseButton.setVisibility(View.VISIBLE);
                playButton.setVisibility(View.GONE);
            } else if (PlayService.getPlayerState().equals("NOTIFICATION_PAUSE")) {
                pauseButton.setVisibility(View.GONE);
                playButton.setVisibility(View.VISIBLE);
            }
        }
    }

    private void ShowDefaultCatalog() {
        String status = Environment.getExternalStorageState();
        if (status.equals(Environment.MEDIA_MOUNTED)) {
            currentDirectory = Environment.getExternalStorageDirectory();
        } else {
            currentDirectory = Environment.getDataDirectory();
        }
        currentDirectory = Environment.getExternalStorageDirectory();
        //FileAdapter.files = currentDirectory.listFiles();
        FileAdapter.setFiles(FileIO.SortFiles(currentDirectory));
        //Arrays.sort(FileAdapter.files);
        this.setTitle(currentDirectory.getAbsolutePath());
        fileAdapter.notifyDataSetChanged();
        return;
    }

    @Override
    protected void onPause() {
        super.onPause();
        RecordPlayingInformation();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touch_x = event.getX();
                touch_y = event.getY();
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                float now_x = event.getX();
                float now_y = event.getY();
                int width = listView.getWidth();
                int height = listView.getHeight();
                if ((now_x - touch_x > (listView.getWidth() / 2) && (Math.abs(now_y - touch_y) < (listView.getHeight() / 4)))) {
                    OnKeyDown();
                }
                break;
        }
        return true;
    }

    //stop when phone call
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
                    PostMan.sendMessage(Constants.PlayServiceCommand, Constants.PlayServiceCommand_Stop);
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
