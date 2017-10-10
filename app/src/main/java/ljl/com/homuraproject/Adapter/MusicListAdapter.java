package ljl.com.homuraproject.Adapter;

import android.content.ClipData;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.io.File;

import ljl.com.homuraproject.Activity.FileActivity;
import ljl.com.homuraproject.Constants;
import ljl.com.homuraproject.Control.FavouriteDatabase;
import ljl.com.homuraproject.Control.MusicDataControl;
import ljl.com.homuraproject.MusicData;
import ljl.com.homuraproject.PostMan;
import ljl.com.homuraproject.R;

/**
 * Created by Administrator on 2015/9/15.
 */
public class MusicListAdapter extends BaseAdapter {
    private final Context con;
    private LayoutInflater inflater;
    private MusicData[] mList;

    public MusicListAdapter(Context context, MusicData[] list) {
        this.con = context;
        this.mList = list;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        if (mList != null) {
            return mList.length;
        } else {
            return 0;
        }
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
        final TextView mListItem = (TextView) view.findViewById(R.id.itmMessage);
        mListItem.setText(mList[i].getArtist() + "-" + mList[i].getTitle());
        mListItem.setTextColor(Color.WHITE);
        if (mList[i].getSource().equals(FileActivity.getCurrentPlayingFile().getAbsolutePath())) {
            mListItem.setTextColor(Color.YELLOW);
        }
        final File tempFile = new File(mList[i].getSource());
        mListItem.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
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

    public void setData(MusicData[] list) {
        mList = list;
        notifyDataSetChanged();
    }
}
