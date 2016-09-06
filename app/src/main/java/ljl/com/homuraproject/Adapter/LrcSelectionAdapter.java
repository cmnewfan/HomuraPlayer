package ljl.com.homuraproject.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import douzi.android.view.LrcRow;
import ljl.com.homuraproject.R;

/**
 * Created by hzfd on 2016/8/15.
 */
public class LrcSelectionAdapter extends BaseAdapter {
    int count = 0;
    private LayoutInflater inflater;
    private CheckBox checkBox;
    private TextView textView;
    private List<LrcRow> mLrcRows;
    private Context mContext;
    private List<Integer> checkedList = new ArrayList<Integer>();

    public LrcSelectionAdapter(Context context, List<LrcRow> lrcRows) {
        this.mLrcRows = lrcRows;
        this.mContext = context;
        this.inflater = LayoutInflater.from(mContext);
        this.count = lrcRows.size();
    }

    public ArrayList<String> getLrcSelectionResultByArray(){
        ArrayList<String> result = new ArrayList<>();
        for (int i = 0; i < mLrcRows.size(); i++) {
            if (mLrcRows.get(i).isChecked) {
                result.add(mLrcRows.get(i).content);
            }
        }
        return result;
    }

    public String getLrcSelectionResult() {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < mLrcRows.size(); i++) {
            if (mLrcRows.get(i).isChecked) {
                result.append(mLrcRows.get(i).content);
                result.append("\r\n\r\n");
            }
        }
        return result.toString();
    }

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
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.lrc_selection, null);
        } else {
            this.checkBox = (CheckBox) convertView.findViewById(R.id.lrc_checkbox);
            if (checkedList.contains(position)) {
                this.checkBox.setChecked(true);
            } else {
                this.checkBox.setChecked(false);
            }
            this.textView = (TextView) convertView.findViewById(R.id.lrc_content);
            this.textView.setText(this.mLrcRows.get(position).content);
        }
        this.checkBox = (CheckBox) convertView.findViewById(R.id.lrc_checkbox);
        if (checkedList.contains(position)) {
            this.checkBox.setChecked(true);
        } else {
            this.checkBox.setChecked(false);
        }
        //use OnClick instead of OnCheckedChange
        this.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkedList.indexOf(position) != -1) {
                    checkBox.setChecked(false);
                    mLrcRows.get(position).isChecked = false;
                    checkedList.remove(checkedList.indexOf(position));
                } else {
                    checkBox.setChecked(true);
                    mLrcRows.get(position).isChecked = true;
                    checkedList.add(position);
                }
                notifyDataSetChanged();
            }
        });
        this.textView = (TextView) convertView.findViewById(R.id.lrc_content);
        this.textView.setText(this.mLrcRows.get(position).content);
        return convertView;
    }
}
