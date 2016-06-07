package ljl.com.homuraproject.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import ljl.com.homuraproject.Control.LyricControl;
import ljl.com.homuraproject.R;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent().getBooleanExtra("EXIT", false)) {
            this.finish();
            android.os.Process.killProcess(android.os.Process.myPid());
        }
        else {
            setContentView(R.layout.activity_main);
            LyricControl.Init();
            final Intent intent = new Intent();
            intent.setClass(this, FileActivity.class);
            Thread thread = new Thread() {
                @Override
                public void run() {
                    try {
                        this.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    startActivity(intent);
                }
            };
            thread.start();
        }
    }
}
