/**
 * douzifly @Aug 10, 2013
 * github.com/douzifly
 * douzifly@gmail.com
 */
package douzi.android.view;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * describe the lyric line
 * @author douzifly
 *
 */
public class LrcRow implements Comparable<LrcRow>, Parcelable {
    public final static String TAG = "LrcRow";
    
    /** begin time of this lrc row */
    public long time;
    /** content of this lrc */
    public String content;
    
    public String strTime;

    public boolean isChecked = false;
    
    public LrcRow(){}
    
    public LrcRow(String strTime,long time,String content){
        this.strTime = strTime;
        this.time = time;
        this.content = content;
        Log.d(TAG,"strTime:" + strTime + " time:" + time + " content:" + content);
    }
    
    /**
     *  create LrcRows by standard Lrc Line , if not standard lrc line,
     *  return false<br />
     *  [00:00:20] balabalabalabala
     */
    public static List<LrcRow> createRows(String standardLrcLine){
        try{
            if(standardLrcLine.indexOf("[") != 0 || standardLrcLine.indexOf("]") != 9 ){
                return null;
            }
            int lastIndexOfRightBracket = standardLrcLine.lastIndexOf("]");
            String content = standardLrcLine.substring(lastIndexOfRightBracket + 1, standardLrcLine.length());
            
            // times [mm:ss.SS][mm:ss.SS] -> *mm:ss.SS**mm:ss.SS*
            String times = standardLrcLine.substring(0,lastIndexOfRightBracket + 1).replace("[", "-").replace("]", "-");
            String arrTimes[] = times.split("-");
            List<LrcRow> listTimes = new ArrayList<LrcRow>();
            for(String temp : arrTimes){
                if(temp.trim().length() == 0){
                    continue;
                }
                LrcRow lrcRow = new LrcRow(temp, timeConvert(temp), content);
                listTimes.add(lrcRow);
            }
            return listTimes;
        }catch(Exception e){
            Log.e(TAG,"createRows exception:" + e.getMessage());
            return null;
        }
    }
    
    private static long timeConvert(String timeString){
        timeString = timeString.replace('.', ':');
        String[] times = timeString.split(":");
        // mm:ss:SS
        return Integer.valueOf(times[0]) * 60 * 1000 +
                Integer.valueOf(times[1]) * 1000 +
                Integer.valueOf(times[2]) ;
    }

    public int compareTo(LrcRow another) {
        return (int)(this.time - another.time);
    }


    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("isChecked", isChecked);
        bundle.putString("strTime", strTime);
        bundle.putLong("time", time);
        bundle.putString("content", content);
        dest.writeBundle(bundle);
    }

    public static final Parcelable.Creator<LrcRow> CREATOR = new Parcelable.Creator<LrcRow>() {
        public LrcRow createFromParcel(Parcel in) {
            Bundle bundle = in.readBundle();
            LrcRow lrcRow = new LrcRow(bundle.getString("strTime"), bundle.getLong("time"), bundle.getString("content"));
            return lrcRow;
        }

        public LrcRow[] newArray(int size) {
            return new LrcRow[size];
        }
    };
}