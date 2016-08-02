package ljl.com.homuraproject;

import android.os.Environment;

/**
 * Created by hzfd on 2016/5/4.
 */
public class Constants {
    final public static String ViewControl = "ViewControl";
    final public static int ViewControl_SetTitle = 1;
    final public static int ViewControl_SetMusicTitle = 2;
    final public static int ViewControl_UpdateLyric = 3;
    final public static int ViewControl_PlayLrc = 4;
    final public static int ViewControl_UnsupportdFormat = 5;
    final public static int ViewControl_ToastMiss = 6;
    final public static int ViewControl_ToastError = 7;
    final public static int ViewControl_ToastSuccess = 8;
    final public static int ViewControl_ToastUpdate = 9;
    final public static int ViewControl_OpenOptionsMenu = 10;
    final public static int ViewControl_OnKeyDown = 11;
    final public static int ViewControl_OnPrevious = 12;
    final public static int ViewControl_OnNext = 13;
    final public static int ViewControl_SetMusicTitleFromCue = 14;
    final public static String PlayServiceCommand = "PlayServiceCommand";
    final public static int PlayServiceCommand_Play = 1;
    final public static int PlayServiceCommand_PlayFromService = 2;
    final public static int PlayServiceCommand_Stop = 3;
    final public static int PlayServiceCommand_Pause = 4;
    final public static int PlayServiceCommand_PlayFromNotification = 5;
    public static String LyricFolder = Environment.getExternalStorageDirectory() + "/Lyrics/";
}
