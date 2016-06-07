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
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.File;
import java.util.Arrays;

import ljl.com.homuraproject.Activity.FileActivity;
import ljl.com.homuraproject.Constants;
import ljl.com.homuraproject.PlayService;
import ljl.com.homuraproject.PostMan;
import ljl.com.homuraproject.R;


/**
 * Created by Administrator on 2015/7/31.
 */
public class FileAdapter extends BaseAdapter {
    public static File[] files;
    private Context context;
    private LayoutInflater inflater;
    private File tempFile;
    private Boolean isFound = false;

    public FileAdapter(Context context) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }

    public void InitFindState() {
        isFound = false;
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
        final ImageButton fileButton = (ImageButton) view.findViewById(R.id.fileButton);
        fileName.setWidth(view.getWidth() - fileButton.getWidth());
        fileName.setText(files[i].getName());
        final String file_path = files[i].getAbsolutePath();
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
            if (Constants.AutomationControl && !isFound) {
                Bundle bundle = new Bundle();
                bundle.putInt("Target", i);
                PostMan.sendMessage(Constants.ViewControl, Constants.ViewControl_ScrollTo, bundle);
            }
            this.isFound = true;
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
            if (Constants.AutomationControl && !isFound && i < files.length - 1) {
                Bundle bundle = new Bundle();
                bundle.putInt("Target", i);
                PostMan.sendMessage(Constants.ViewControl, Constants.ViewControl_ScrollTo, bundle);
            } else {
                isFound = true;
            }
        }

        if (files[i].isDirectory()) {
            Drawable leftDrawable = context.getResources().getDrawable(R.drawable.abc_ic_menu_copy_mtrl_am_alpha);
            leftDrawable.setBounds(0, 0, leftDrawable.getMinimumWidth(), leftDrawable.getMinimumHeight());
            fileName.setCompoundDrawables(leftDrawable, fileName.getCompoundDrawables()[1], fileName.getCompoundDrawables()[2], fileName.getCompoundDrawables()[3]);
        }
        fileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PostMan.sendMessage(Constants.ViewControl, Constants.ViewControl_OpenOptionsMenu);
            }
        });
        fileName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isFound = false;
                tempFile = new File(FileActivity.currentDirectory + File.separator + fileName.getText().toString());
                if (tempFile.isDirectory()) {
                    files = tempFile.listFiles();
                    Arrays.sort(files);
                    FileActivity.currentFile = tempFile;
                    FileActivity.currentDirectory = FileActivity.currentDirectory + File.separator + fileName.getText();
                    PostMan.sendMessage(Constants.ViewControl, Constants.ViewControl_SetTitle);
                    FileActivity.fileAdapter.notifyDataSetChanged();
                } else if (tempFile.getName().substring(tempFile.getName().lastIndexOf(".")).equals(".mp3") ||
                        tempFile.getName().substring(tempFile.getName().lastIndexOf(".")).equals(".m4a") ||
                        tempFile.getName().substring(tempFile.getName().lastIndexOf(".")).equals(".flac") ||
                        tempFile.getName().substring(tempFile.getName().lastIndexOf(".")).equals(".MP3")) {
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
            }
        });
        fileName.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                PostMan.sendMessage(Constants.ViewControl, Constants.ViewControl_OpenOptionsMenu);
                ClipData clipData = ClipData.newPlainText("", "");
                clipData.addItem(new ClipData.Item(tempFile.getAbsolutePath()));
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
                view.startDrag(clipData, shadowBuilder, view, 0);
                return false;
            }
        });
        return view;
    }
}
