package ljl.com.homuraproject;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Administrator on 2015/8/31.
 */
public class MyApplication extends Application {
    private static Context context;

    public void onCreate() {
        super.onCreate();
        MyApplication.context = getApplicationContext();
        //Thread.setDefaultUncaughtExceptionHandler(new UnCaughtExceptionHandler());
    }

    public static Context getAppContext() {
        return MyApplication.context;
    }

    public class UnCaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            logToSdcard(thread,ex);
        }

        public void logToSdcard(Thread thread,Throwable ex) {
            StackTraceElement[] stackTraceElements = ex.getStackTrace();
            StringBuffer errorMessage = new StringBuffer("");
            errorMessage.append(ex.toString());
            errorMessage.append("\n");
            for(int i=0;i<stackTraceElements.length;i++)
            {
                errorMessage.append(String.format("%s\t%s\t%s\t", stackTraceElements[i].getClassName(), stackTraceElements[i].getLineNumber(), stackTraceElements[i].getMethodName()));
                errorMessage.append("\n");
            }
            if (ex.getCause() != null) {
                Throwable throwable = ex.getCause();
                stackTraceElements = throwable.getStackTrace();
                errorMessage.append(throwable.toString());
                errorMessage.append("\n");
                for (int i = 0; i < stackTraceElements.length; i++) {
                    errorMessage.append(String.format("%s\t%s\t%s\t", stackTraceElements[i].getClassName(), stackTraceElements[i].getLineNumber(), stackTraceElements[i].getMethodName()));
                    errorMessage.append("\n");
                }
            }
            Intent intent = new Intent ();
            intent.setAction("android.intent.action.BUG_REPORT");
            intent.putExtra("Error", errorMessage.toString());
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            System.exit(1);
        }
    }
}