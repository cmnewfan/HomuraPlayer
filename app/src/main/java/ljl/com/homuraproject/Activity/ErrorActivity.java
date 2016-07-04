package ljl.com.homuraproject.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import ljl.com.homuraproject.R;

/**
 * Created by Administrator on 2015/8/31.
 */
public class ErrorActivity extends Activity {
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.error);
        showDialog("");
    }

    /**
     *
     * @param mess:dialog message
     */
    private void showDialog(String mess) {
        new AlertDialog.Builder(this).setTitle("发生错误，是否发送错误报告").setMessage(mess)
                    .setNegativeButton("确定", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (getIntent().getStringExtra("Error") != null) {
                                Intent data = new Intent(Intent.ACTION_SENDTO);
                                //data.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                data.setData(Uri.parse("mailto:906344660@qq.com"));
                                data.putExtra(Intent.EXTRA_SUBJECT, "App Crash");
                                data.putExtra(Intent.EXTRA_TEXT, getIntent().getStringExtra("Error"));
                                startActivity(Intent.createChooser(data, "Bug Report"));
                                finish();
                            }
                        }
                    }).setPositiveButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            }).show();
    }
}
