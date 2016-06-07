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
    private void showDialog(String mess) {
        if (mess.equals("无法登陆到服务器")) {
            new AlertDialog.Builder(this).setTitle("注意").setMessage("无法登录到服务器，请确认是否存在可用网络或者IP地址及端口是否填写正确。")
                    .setNegativeButton("确定", null).show();
        } else {
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

}
