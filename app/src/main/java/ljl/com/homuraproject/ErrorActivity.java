package ljl.com.homuraproject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

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
        if (mess.equals("�޷���½��������")) {
            new AlertDialog.Builder(this).setTitle("ע��").setMessage("�޷���¼������������ȷ���Ƿ���ڿ����������IP��ַ���˿��Ƿ���д��ȷ��")
                    .setNegativeButton("ȷ��", null).show();
        } else {
            new AlertDialog.Builder(this).setTitle("���������Ƿ��ʹ��󱨸�").setMessage(mess)
                    .setNegativeButton("ȷ��", new DialogInterface.OnClickListener() {
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
                    }).setPositiveButton("ȡ��", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            }).show();
        }
    }

}
