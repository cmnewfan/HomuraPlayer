package ljl.com.homuraproject;

/**
 * Created by hzfd on 2016/8/1.
 */
public class Track_Time {
    private Integer minute;
    private Integer second;
    private Integer millisecond;

    public Track_Time(String timeString) {
        this.minute = Integer.valueOf(timeString.substring(0, 2));
        this.second = Integer.valueOf(timeString.substring(3, 5));
        this.millisecond = Integer.valueOf(timeString.substring(6, 8));
    }

    public int getTime() {
        return ((minute * 60) + second) * 1000;
    }
}
