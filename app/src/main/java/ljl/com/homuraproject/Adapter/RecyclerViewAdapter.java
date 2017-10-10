package ljl.com.homuraproject.Adapter;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.File;

import ljl.com.homuraproject.Activity.FileActivity;
import ljl.com.homuraproject.Constants;
import ljl.com.homuraproject.Control.FileIO;
import ljl.com.homuraproject.MyApplication;
import ljl.com.homuraproject.PlayService;
import ljl.com.homuraproject.PostMan;
import ljl.com.homuraproject.R;

/**
 * Created by hzfd on 2016/9/14.
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter {
    private static File[] files;
    private Context context;
    private LayoutInflater inflater;
    private File tempFile;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView mTextView;

        public MyViewHolder(View itemView, ViewGroup parent) {
            super(itemView);
            mTextView = (TextView) itemView.findViewById(R.id.itmMessage);
            if (parent != null) {
                mTextView.getLayoutParams().width = parent.getWidth();
            }
        }
    }

    public RecyclerViewAdapter(Context context) {
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = View.inflate(this.context, R.layout.listview_item, null);
        MyViewHolder mViewHolder = new MyViewHolder(view, parent);
        return mViewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final MyViewHolder mViewHolder = new MyViewHolder(holder.itemView, null);
        mViewHolder.mTextView.setText(files[position].getName());
        highlightFolderAndPlayingFile(position, mViewHolder.mTextView);
        mViewHolder.mTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tempFile = new File(FileActivity.getCurrentDirectory().getAbsolutePath() + File.separator + mViewHolder.mTextView.getText().toString());
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
                } else {
                    PostMan.sendMessage(Constants.ViewControl, Constants.ViewControl_UnsupportdFormat);
                }
            }
        });
        mViewHolder.mTextView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                tempFile = new File(FileActivity.getCurrentDirectory().getAbsolutePath() + File.separator + mViewHolder.mTextView.getText().toString());
                PostMan.sendMessage(Constants.ViewControl, Constants.ViewControl_OpenOptionsMenu);
                ClipData clipData = ClipData.newPlainText("", "");
                clipData.addItem(new ClipData.Item(tempFile.getPath()));
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
                view.startDrag(clipData, shadowBuilder, view, 0);
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        if (files == null)
            return 0;
        else
            return files.length;
    }

    private void highlightFolderAndPlayingFile(int i, TextView fileName) {
        if (FileActivity.getCurrentPlayingFile() != null && FileActivity.getCurrentPlayingFile().getAbsolutePath().contains(files[i].getAbsolutePath())) {
            Drawable rightDrawable = MyApplication.getAppContext().getResources().getDrawable(R.drawable.play_icon);
            rightDrawable.setBounds(0, 0, rightDrawable.getMinimumWidth(), rightDrawable.getMinimumHeight());
            if (files[i].isDirectory()) {
                Drawable leftDrawable = MyApplication.getAppContext().getResources().getDrawable(R.drawable.abc_ic_menu_copy_mtrl_am_alpha);
                leftDrawable.setBounds(0, 0, leftDrawable.getMinimumWidth(), leftDrawable.getMinimumHeight());
                fileName.setCompoundDrawables(leftDrawable, null, rightDrawable, null);
                fileName.setTextColor(Color.YELLOW);
            } else {
                fileName.setCompoundDrawables(null, null, rightDrawable, null);
                fileName.setTextColor(Color.YELLOW);
            }
        } else {
            if (files[i].isDirectory()) {
                Drawable leftDrawable = MyApplication.getAppContext().getResources().getDrawable(R.drawable.abc_ic_menu_copy_mtrl_am_alpha);
                leftDrawable.setBounds(0, 0, leftDrawable.getMinimumWidth(), leftDrawable.getMinimumHeight());
                fileName.setCompoundDrawables(leftDrawable, null, null, null);
                fileName.setTextColor(Color.WHITE);
            } else {
                fileName.setCompoundDrawables(null, null, null, null);
                fileName.setTextColor(Color.WHITE);
            }
        }
        if (files[i].isDirectory()) {
            Drawable leftDrawable = MyApplication.getAppContext().getResources().getDrawable(R.drawable.abc_ic_menu_copy_mtrl_am_alpha);
            leftDrawable.setBounds(0, 0, leftDrawable.getMinimumWidth(), leftDrawable.getMinimumHeight());
            fileName.setCompoundDrawables(leftDrawable, fileName.getCompoundDrawables()[1], fileName.getCompoundDrawables()[2], fileName.getCompoundDrawables()[3]);
        }
    }

    public static void setFiles(File[] files) {
        RecyclerViewAdapter.files = files;
    }
}
