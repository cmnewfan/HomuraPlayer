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
import ljl.com.homuraproject.Control.FavouriteDatabase;
import ljl.com.homuraproject.Control.FileIO;
import ljl.com.homuraproject.Control.MusicDataControl;
import ljl.com.homuraproject.MusicData;
import ljl.com.homuraproject.PlayService;
import ljl.com.homuraproject.PostMan;
import ljl.com.homuraproject.R;


/**
 * adapter of list view in FileActivity
 * Created by Administrator on 2015/7/31.
 */
public class FileAdapter extends BaseAdapter {
    private static File[] files;
    private Context context;
    private LayoutInflater inflater;
    private File tempFile;

    public FileAdapter(Context context) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        if (files == null)
            return 0;
        else
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
            highlightFolderAndPlayingFile(i, fileName);
            return view;
        }
        final TextView fileName = (TextView) view.findViewById(R.id.itmMessage);
        fileName.setText(files[i].getName());
        highlightFolderAndPlayingFile(i, fileName);
        fileName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tempFile = new File(FileActivity.getCurrentDirectory().getAbsolutePath() + File.separator + fileName.getText().toString());
                if (tempFile.isDirectory()) {
                    //target file is directory
                    files = FileIO.SortFiles(tempFile);
                    FileActivity.setCurrentDirectory(tempFile);
                    PostMan.sendMessage(Constants.ViewControl, Constants.ViewControl_SetTitle);
                    notifyDataSetChanged();
                } else if (tempFile.getName().lastIndexOf(".") != -1) {
                    if (PlayService.isSupportedCodec(tempFile.getName().substring(tempFile.getName().lastIndexOf(".")).toLowerCase())) {
                        //target file is supported music file
                        PlayService.generatePlayList(tempFile, files);
                        Bundle bundle = new Bundle();
                        bundle.putInt("op", 1);
                        bundle.putInt("LastTime", 0);
                        bundle.putString("file_path", tempFile.getAbsolutePath());
                        PostMan.sendMessage(Constants.PlayServiceCommand, Constants.PlayServiceCommand_Play, bundle);
                        notifyDataSetChanged();
                    } else if (tempFile.getName().substring(tempFile.getName().lastIndexOf(".")).toLowerCase().equals(".jpg") ||
                            tempFile.getName().substring(tempFile.getName().lastIndexOf(".")).toLowerCase().equals(".png")) {
                        //targer file is image file
                        Uri data = Uri.fromFile(tempFile);
                        Intent sendIntent = new Intent(Intent.ACTION_VIEW, data).setDataAndType(data, "image/*");
                        context.startActivity(Intent.createChooser(sendIntent, ""));
                    } else {
                        PostMan.sendMessage(Constants.ViewControl, Constants.ViewControl_UnsupportdFormat);
                    }
                }
                else {
                    PostMan.sendMessage(Constants.ViewControl,Constants.ViewControl_UnsupportdFormat);
                }
            }
        });
        fileName.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                tempFile = new File(FileActivity.getCurrentDirectory().getAbsolutePath() + File.separator + fileName.getText().toString());
                PostMan.sendMessage(Constants.ViewControl, Constants.ViewControl_OpenOptionsMenu);
                ClipData clipData = ClipData.newPlainText("", "");
                clipData.addItem(new ClipData.Item(tempFile.getPath()));
                MusicData data = MusicDataControl.getMusicDataFromFile(tempFile);
                String tag = FavouriteDatabase.getInstance().isTagged(data.getSource(), data.getTitle());
                clipData.addItem(new ClipData.Item(data.getTitle()));
                clipData.addItem(new ClipData.Item(data.getArtist()));
                clipData.addItem(new ClipData.Item(String.valueOf(data.getTrack())));
                clipData.addItem(new ClipData.Item(tag));
                clipData.addItem(new ClipData.Item(tempFile.getAbsolutePath()));
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
                view.startDrag(clipData, shadowBuilder, view, 0);
                if (tag.equals("Tagged")) {
                    PostMan.sendMessage(Constants.ViewControl, Constants.ViewControl_Tagged);
                } else {
                    PostMan.sendMessage(Constants.ViewControl, Constants.ViewControl_Untagged);
                }
                return false;
            }
        });
        return view;
    }

    /**
     * highlight playing folder and file
     *
     * @param i        index of target file in files
     * @param fileName target text view
     */
    private void highlightFolderAndPlayingFile(int i, TextView fileName) {
        if (FileActivity.getCurrentPlayingFile() != null && FileActivity.getCurrentPlayingFile().getAbsolutePath().contains(files[i].getAbsolutePath())) {
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

    public static void setFiles(File[] files) {
        FileAdapter.files = files;
    }

    public static File[] getFiles() {
        return files;
    }
}
