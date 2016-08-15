package ljl.com.homuraproject.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import ljl.com.homuraproject.R;

/**
 * Created by hzfd on 2016/8/15.
 */
public class LrcSelectionAdapter extends BaseAdapter {
    int count = 0;
    private LayoutInflater inflater;
    private CheckBox checkBox;
    private TextView textView;

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.lrc_selection, null);
            this.checkBox = (CheckBox) convertView.findViewById(R.id.lrc_checkbox);
            this.checkBox.setOn
            this.textView = (TextView) convertView.findViewById(R.id.lrc_content);
        }
        return convertView;
    }
}
