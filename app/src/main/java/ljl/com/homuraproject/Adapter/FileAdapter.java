package ljl.com.homuraproject.Adapter;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.io.File;

import ljl.com.homuraproject.Activity.FileActivity;
import ljl.com.homuraproject.Constants;
import ljl.com.homuraproject.Control.FileIO;
import ljl.com.homuraproject.PlayService;
import ljl.com.homuraproject.PostMan;
import ljl.com.homuraproject.R;


/**
 * Created by Administrator on 2015/7/31.
 */
public class FileAdapter extends BaseAdapter {
    public static File[] files;
    private static String[] SupportedCodec = new String[]{".ogg",".mp3",".m4a",".flac",".wmv"};
    private Context context;
    private LayoutInflater inflater;
    private File tempFile;
    private float touch_x;
    private float touch_y;

    public FileAdapter(Context context) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
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
        } else {
            final TextView fileName = (TextView) view.findViewById(R.id.itmMessage);
            fileName.setText(files[i].getName());
            HighlightFolderAndPlayingFile(i, fileName);
            return view;
        }
        final TextView fileName = (TextView) view.findViewById(R.id.itmMessage);
        fileName.setText(files[i].getName());
        HighlightFolderAndPlayingFile(i, fileName);
        fileName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tempFile = new File(FileActivity.currentDirectory + File.separator + fileName.getText().toString());
                if (tempFile.isDirectory()) {
                    //files = tempFile.listFiles();
                    files = FileIO.SortFiles(tempFile);
                    //Arrays.sort(files);
                    FileActivity.currentFile = tempFile;
                    FileActivity.currentDirectory = FileActivity.currentDirectory + File.separator + fileName.getText();
                    PostMan.sendMessage(Constants.ViewControl, Constants.ViewControl_SetTitle);
                    notifyDataSetChanged();
                } else if (isSupportedCodec(tempFile.getName().substring(tempFile.getName().lastIndexOf(".")).toLowerCase())) {
                    PlayService.generatePlayList(tempFile, files);
                    Bundle bundle = new Bundle();
                    bundle.putInt("op", 1);
                    bundle.putInt("LastTime", 0);
                    bundle.putString("file_path", tempFile.getAbsolutePath());
                    PostMan.sendMessage(Constants.PlayServiceCommand, Constants.PlayServiceCommand_Play, bundle);
                    notifyDataSetChanged();
                } else if (tempFile.getName().substring(tempFile.getName().lastIndexOf(".")).toLowerCase().equals(".jpg") ||
                        tempFile.getName().substring(tempFile.getName().lastIndexOf(".")).toLowerCase().equals(".png")) {
                    Uri data = Uri.fromFile(tempFile);
                    Intent sendIntent = new Intent(Intent.ACTION_VIEW, data).setDataAndType(data, "image/*");
                    context.startActivity(Intent.createChooser(sendIntent, ""));
                }
                else {
                    PostMan.sendMessage(Constants.ViewControl,Constants.ViewControl_UnsupportdFormat);
                }
            }
        });
        fileName.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                tempFile = new File(FileActivity.currentDirectory + File.separator + fileName.getText().toString());
                PostMan.sendMessage(Constants.ViewControl, Constants.ViewControl_OpenOptionsMenu);
                ClipData clipData = ClipData.newPlainText("", "");
                clipData.addItem(new ClipData.Item(tempFile.getPath()));
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
                view.startDrag(clipData, shadowBuilder, view, 0);
                return false;
            }
        });
        /*fileName.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        touch_x = event.getRawX();
                        touch_y = event.getRawY();
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        float now_x = event.getRawX();
                        float now_y = event.getY();
                        float raw_y = event.getRawY();
                        int width = fileName.getWidth();
                        int height = fileName.getHeight();
                        if ((now_x - touch_x > 40 && (Math.abs(raw_y - touch_y) < fileName.getHeight()))) {
                            PostMan.sendMessage(Constants.ViewControl, Constants.ViewControl_OnKeyDown);
                        }
                        break;
                }
                return false;
            }
        });*/
        return view;
    }

    private void HighlightFolderAndPlayingFile(int i, TextView fileName) {
        if (FileActivity.currentPlayingFile != null && FileActivity.currentPlayingFile.getAbsolutePath().contains(files[i].getAbsolutePath())) {
            Drawable rightDrawable = context.getResources().getDrawable(R.drawable.play_icon);
            rightDrawable.setBounds(0, 0, rightDrawable.getMinimumWidth(), rightDrawable.getMinimumHeight());
            if (files[i].isDirectory()) {
                Drawable leftDrawable = context.getResources().getDrawable(R.drawable.abc_ic_menu_copy_mtrl_am_alpha);
                leftDrawable.setBounds(0, 0, leftDrawable.getMinimumWidth(), leftDrawable.getMinimumHeight());
                fileName.setCompoundDrawables(leftDrawable, null, rightDrawable, null);
                fileName.setTextColor(Color.YELLOW);
            } else {
                fileName.setCompoundDrawables(null, null, rightDrawable, null);
                fileName.setTextColor(Color.YELLOW);
            }
        } else {
            if (files[i].isDirectory()) {
                Drawable leftDrawable = context.getResources().getDrawable(R.drawable.abc_ic_menu_copy_mtrl_am_alpha);
                leftDrawable.setBounds(0, 0, leftDrawable.getMinimumWidth(), leftDrawable.getMinimumHeight());
                fileName.setCompoundDrawables(leftDrawable, null, null, null);
                fileName.setTextColor(Color.WHITE);
            } else {
                fileName.setCompoundDrawables(null, null, null, null);
                fileName.setTextColor(Color.WHITE);
            }
        }
        if (files[i].isDirectory()) {
            Drawable leftDrawable = context.getResources().getDrawable(R.drawable.abc_ic_menu_copy_mtrl_am_alpha);
            leftDrawable.setBounds(0, 0, leftDrawable.getMinimumWidth(), leftDrawable.getMinimumHeight());
            fileName.setCompoundDrawables(leftDrawable, fileName.getCompoundDrawables()[1], fileName.getCompoundDrawables()[2], fileName.getCompoundDrawables()[3]);
        }
    }

    private Boolean isSupportedCodec(String targetCodec){
        for (String codec:SupportedCodec
             ) {
            if(codec.equals(targetCodec)){
                return true;
            }
        }
        return false;
    }
}
