package ljl.com.homuraproject;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.io.File;

/**
 * Created by Administrator on 2015/9/15.
 */
public class MusicListAdapter extends BaseAdapter {
    private final Context con;
    private LayoutInflater inflater;


    @Override
    public int getCount() {
        if (FileActivity.currentPlayList != null) {
            return FileActivity.currentPlayList.size();
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
        mListItem.setText(FileActivity.currentPlayList.get(i).getName());
        mListItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File tempFile = new File(FileActivity.currentDirectory + File.separator + mListItem.getText().toString());
                File[] files = tempFile.getParentFile().listFiles();
                if (tempFile.getName().substring(tempFile.getName().lastIndexOf(".")).equals(".mp3") ||
                        tempFile.getName().substring(tempFile.getName().lastIndexOf(".")).equals(".m4a") ||
                        tempFile.getName().substring(tempFile.getName().lastIndexOf(".")).equals(".flac")) {
                    FileAdapter.beforeMusicPlay(tempFile, files);
                    HomuraPlayer player = HomuraPlayer.getInstance(Uri.fromFile(tempFile), con);
                    FileAdapter.sendMessage("Play");
                    player.play();
                    notifyDataSetChanged();
                }
            }
        });
        return view;
    }

    public MusicListAdapter(Context context) {
        this.con = context;
        this.inflater = LayoutInflater.from(context);
    }
}
