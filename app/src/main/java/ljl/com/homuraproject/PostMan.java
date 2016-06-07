package ljl.com.homuraproject;

import android.os.Bundle;
import android.os.Message;

import ljl.com.homuraproject.Activity.FileActivity;

/**
 * Created by hzfd on 2016/5/4.
 */
public class PostMan {
    public static void sendMessage(String CommandType, int CommandCode) {
        Message mes = FileActivity.handler.obtainMessage();
        mes.obj = CommandType;
        mes.what = CommandCode;
        FileActivity.handler.sendMessage(mes);
    }

    public static void sendMessage(String CommandType, int CommandCode, Bundle bundle) {
        Message mes = FileActivity.handler.obtainMessage();
        mes.obj = CommandType;
        mes.what = CommandCode;
        mes.setData(bundle);
        FileActivity.handler.sendMessage(mes);
    }

    public static void sendMessage(Object obj) {
        Message mes = FileActivity.handler.obtainMessage();
        mes.obj = obj;
        FileActivity.handler.sendMessage(mes);
    }
}
