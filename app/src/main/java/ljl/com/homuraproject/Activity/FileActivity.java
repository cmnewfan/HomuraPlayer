package ljl.com.homuraproject.Activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.PorterDuff;
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
public class FileActivity extends Activity implements View.OnTouchListener {
    private static Handler handler;
    private static SeekBar seekBar;
    private static FileAdapter fileAdapter;
    public static File currentFile;
    public static String currentDirectory;
    public static ArrayList<File> currentPlayList;
    public static File currentPlayingFile;
    public static String currentLyric;
    public static File currentLyricFile;
    public static String currentPlayingTitle;
    private static String LastPlayingFile;
    public static String currentArtist;
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
        //Fill View
        sharedPreferences = this.getSharedPreferences("music_player_info", Context.MODE_PRIVATE);
        Boolean isUsed = sharedPreferences.getBoolean("Used", false);
        if (!isUsed) {
            showInstruction();
        }
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
                            setTitle(currentDirectory);
                            break;
                        case Constants.ViewControl_SetMusicTitle:
                            setTitle(currentPlayingTitle);
                            PlayService.UpdateNotification(currentPlayingTitle, currentArtist);
                            break;
                        case Constants.ViewControl_UpdateLyric:
                            ILrcBuilder builder = new DefaultLrcBuilder();
                            List<LrcRow> rows = builder.getLrcRows(currentLyric);
                            lrcView.setLrc(rows);
                            break;
                        case Constants.ViewControl_PlayLrc:
                            lrcView.seekLrcToTime(seekBar.getProgress() * 1000);
                            break;
                        case Constants.ViewControl_ScrollTo:
                            listView.setSelection(msg.getData().getInt("Target"));
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
                            Toast.makeText(MyApplication.getAppContext(), "Need to update music database", Toast.LENGTH_SHORT).show();
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
                                    boolean flag = TTDownloader.download(queryList.get(item), queryList.get(item).mTitle);
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

    public static void resetLastPlayingState() {
        LastPlayingFile = "";
        LastPlayingTime = 0;
    }

    private void showInstruction() {
        //new AlertDialog.Builder(this).setTitle("拖拽功能说明").setMessage("App支持对拖拽文件进行删除、搜索歌词、设置铃声的操作，谢谢您的使用").setPositiveButton("确定", null).show();
        sharedPreferences.edit().putBoolean("Used", true).commit();
    }

    public static Message getMainLoopMessage() {
        return handler.obtainMessage();
    }

    public static void sendMessage(Message mes) {
        handler.sendMessage(mes);
    }

    public static void NotifyDataChangd() {
        fileAdapter.notifyDataSetChanged();
    }

    public static int GetSeekbarProgress() {
        return seekBar.getProgress();
    }

    public static int GetSeekBarMax() {
        return seekBar.getMax();
    }

    public static void SeekbarIncrement(int increment) {
        seekBar.incrementProgressBy(increment);
    }

    public static void SeekBarPost(Runnable runnable, long time) {
        seekBar.postDelayed(runnable, time);
    }

    public static void SetSeekbarProgress(int progress) {
        seekBar.setProgress(progress);
    }

    public static void SetSeekbarMax(int max) {
        seekBar.setMax(max);
    }

    public static void RemoveSekbarCallbacks(Runnable runnable) {
        seekBar.removeCallbacks(runnable);
    }

    private void scanSdCard() {
        mediaScan(FileActivity.currentPlayingFile);
    }

    public void mediaScan(File file) {
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
        pagerTabStrip = (PagerTabStrip) findViewById(R.id.pagertab);
        pagerTabStrip.setTabIndicatorColor(getResources().getColor(R.color.material_blue_grey_800));
        pagerTabStrip.setDrawFullUnderline(false);
        pagerTabStrip.setTextSpacing(50);
    }

    private void initView() {
        this.linear_layout_normal = (LinearLayout) this.findViewById(R.id.linear_layout_normal);
        this.linear_layout_onlongclick = (LinearLayout) this.findViewById(R.id.linear_layout_onlongclick);
        this.linear_layout_onlongclick_text = (LinearLayout) this.findViewById(R.id.linear_layout_onlongclick_text);
        this.progress_layout = (LinearLayout) this.findViewById(R.id.progressLayout);
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
                    searchButton.getBackground().clearColorFilter();
                    searchButton.invalidate();
                } else if (event.getAction() == DragEvent.ACTION_DRAG_ENTERED) {
                    searchButton.getBackground().setColorFilter(0x77000000, PorterDuff.Mode.SRC_ATOP);
                    searchButton.invalidate();
                } else if (event.getAction() == DragEvent.ACTION_DRAG_EXITED) {
                    searchButton.getBackground().clearColorFilter();
                    searchButton.invalidate();
                }
                return true;
            }
        });
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
                if (event.getAction() == DragEvent.ACTION_DROP) {
                    FileIO.DeleteFile(event.getClipData().getItemAt(1).getText().toString());
                    linear_layout_normal.setVisibility(View.VISIBLE);
                    linear_layout_onlongclick.setVisibility(View.GONE);
                    progress_layout.setVisibility(View.VISIBLE);
                    linear_layout_onlongclick_text.setVisibility(View.GONE);
                    deleteButton.getBackground().clearColorFilter();
                    deleteButton.invalidate();
                } else if (event.getAction() == DragEvent.ACTION_DRAG_ENTERED) {
                    deleteButton.getBackground().setColorFilter(0x77000000, PorterDuff.Mode.SRC_ATOP);
                    deleteButton.invalidate();
                } else if (event.getAction() == DragEvent.ACTION_DRAG_EXITED) {
                    deleteButton.getBackground().clearColorFilter();
                    deleteButton.invalidate();
                }
                return true;
            }
        });
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
                    phoneButton.getBackground().clearColorFilter();
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
                    phoneButton.getBackground().setColorFilter(0x77000000, PorterDuff.Mode.SRC_ATOP);
                    phoneButton.invalidate();
                } else if (event.getAction() == DragEvent.ACTION_DRAG_EXITED) {
                    phoneButton.getBackground().clearColorFilter();
                    phoneButton.invalidate();
                }
                return true;
            }
        });
        this.myTitle = (TextView) this.findViewById(R.id.myTitle);
        this.mImageButton = (ImageButton) this.findViewById(R.id.imageButton);
        this.mImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                                if (!FileActivity.currentPlayingTitle.equals("")) {
                                    Intent sendIntent = new Intent(Intent.ACTION_SEND);
                                    sendIntent.setType("image/*");
                                    Uri targetUri = FileIO.getImageUri(currentPlayingFile.getParentFile());
                                    if (targetUri != null) {
                                        sendIntent.putExtra(Intent.EXTRA_STREAM, targetUri);
                                    }
                                    ;
                                    sendIntent.putExtra(Intent.EXTRA_SUBJECT, "分享");
                                    sendIntent.putExtra(Intent.EXTRA_TEXT, FileActivity.currentPlayingTitle);
                                    sendIntent.putExtra(Intent.EXTRA_TITLE, "From HomuraPlayer");
                                    sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(Intent.createChooser(sendIntent, "share"));
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
        InitSeekBar();
        InitViewPagerAdapter();
        InitPagerTabStrip();
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
                if (PlayService.getPlayerState() != null && PlayService.getPlayerState().equals("Playing")) {
                    PlayService.stop();
                } else {
                    return;
                }
                if (currentPlayList.indexOf(currentPlayingFile) > 0) {
                    currentPlayingFile = currentPlayList.get(currentPlayList.indexOf(currentPlayingFile) - 1);
                    LyricControl.sendCurrentLyric();
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
                if (PlayService.getPlayerState() != null && PlayService.getPlayerState().equals("Playing")) {
                    PlayService.stop();
                } else {
                    return;
                }
                if (currentPlayList.indexOf(currentPlayingFile) < currentPlayList.size() - 1) {
                    currentPlayingFile = currentPlayList.get(currentPlayList.indexOf(currentPlayingFile) + 1);
                    LyricControl.sendCurrentLyric();
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
    }

    private void SearchButtonResponse(DragEvent event) {
        File file = new File(event.getClipData().getItemAt(1).getText().toString());
        if (file.isFile()) {
            Cursor c = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Video.Media.TITLE,
                            MediaStore.Audio.Media.ARTIST,
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
                c.close();
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
                fileAdapter.notifyDataSetChanged();
            } else {
                new AlertDialog.Builder(FileActivity.this).setTitle("通知").setPositiveButton("确定", null).setMessage("歌词搜索失败,文件类型错误").show();
            }
        } else {
            new AlertDialog.Builder(FileActivity.this).setTitle("通知").setPositiveButton("确定", null).setMessage("歌词搜错失败,文件类型错误").show();
        }
        /*LayoutInflater lf = LayoutInflater.from(FileActivity.this);
        View FileInfoView = lf.inflate(R.layout.file_info, null);
        final EditText artistText = (EditText) FileInfoView.findViewById(R.id.ArtistText);
        final EditText titleText = (EditText) FileInfoView.findViewById(R.id.TitleText);
        artistText.setText(FileActivity.currentArtist);
        if (FileActivity.currentPlayingTitle.contains("?")) {
            titleText.setText(FileActivity.currentPlayingFile.getName());
        } else {
            titleText.setText(FileActivity.currentPlayingTitle);
        }
        new AlertDialog.Builder(FileActivity.this).setTitle("确认信息").setIcon(android.R.drawable.ic_dialog_info).setView(FileInfoView).setNegativeButton("确定", new DialogInterface.OnClickListener() {
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
                            PostMan.sendMessage(obj);
                        } else {
                            PostMan.sendMessage(Constants.ViewControl, Constants.ViewControl_ToastMiss);
                        }
                    }
                };
                LyricThread.start();
            }
        }).show();*/
        fileAdapter.notifyDataSetChanged();
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
            OnKeyDown();
        }
        return false;
    }

    private void OnKeyDown() {
        if (currentFile == null) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("EXIT", true);
            startActivity(intent);
            PlayService.release();
        } else if (currentFile.getAbsolutePath().equals(File.separator + "storage")) {
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
            //FileAdapter.files = currentFile.getParentFile().listFiles();
            FileAdapter.files = FileIO.SortFiles(currentFile.getParentFile());
            currentDirectory = currentDirectory.substring(0, currentDirectory.lastIndexOf(File.separator));
            currentFile = currentFile.getParentFile();
            //Arrays.sort(FileAdapter.files);
            this.setTitle(currentDirectory);
            fileAdapter.notifyDataSetChanged();
            if (scrolledX != -1) {
                listView.setSelection(scrolledX);
            }
        }
    }


    public static void RecordPlayingInformation() {
        synchronized (RecordLock) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            if (currentPlayingFile != null) {
                editor.putString("LastPlayingFile", currentPlayingFile.getAbsolutePath());
                editor.putInt("LastPlayingTime", seekBar.getProgress());
            }
            editor.commit();
        }
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
                String status = Environment.getExternalStorageState();
                if (status.equals(Environment.MEDIA_MOUNTED)) {
                    currentFile = Environment.getExternalStorageDirectory();
                } else {
                    currentFile = Environment.getDataDirectory();
                }
                currentFile = Environment.getExternalStorageDirectory();
                //FileAdapter.files = currentFile.listFiles();
                FileAdapter.files = FileIO.SortFiles(currentFile);
                //Arrays.sort(FileAdapter.files);
                currentDirectory = currentFile.getAbsolutePath();
                this.setTitle(currentDirectory);
                fileAdapter.notifyDataSetChanged();
                return;
            }
            currentPlayingFile = new File(LastPlayingFile);
            PlayService.generatePlayList(currentPlayingFile, currentPlayingFile.getParentFile().listFiles());
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
            current_Time.setText(String.format("%02d", seekBar.getProgress() / 60) + ":" + String.format("%02d", seekBar.getProgress() % 60));
            currentFile = currentPlayingFile.getParentFile();
            FileAdapter.files = FileIO.SortFiles(currentPlayingFile.getParentFile());
            //FileAdapter.files = currentPlayingFile.getParentFile().listFiles();
            //Arrays.sort(FileAdapter.files);
            currentDirectory = currentPlayingFile.getParentFile().getAbsolutePath();
            this.setTitle(currentDirectory);
            fileAdapter.notifyDataSetChanged();
        } else {
            //useless?
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
            } else if (PlayService.getPlayerState().equals("NOTIFICATION_PAUSE")) {
                pauseButton.setVisibility(View.GONE);
                playButton.setVisibility(View.VISIBLE);
            }
        }
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
