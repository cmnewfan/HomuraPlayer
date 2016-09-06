package ljl.com.homuraproject;


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

    public static int getTime(String timeString) {
        return ((Integer.valueOf(timeString.substring(0, 2)) * 60) + Integer.valueOf(timeString.substring(3, 5))) * 1000;
    }
}
