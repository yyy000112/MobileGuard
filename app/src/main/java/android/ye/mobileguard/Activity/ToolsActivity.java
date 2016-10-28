package android.ye.mobileguard.Activity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.ye.mobileguard.Permit.PermissionCallBack;
import android.ye.mobileguard.Permit.PermissionManager;
import android.ye.mobileguard.R;
import android.ye.mobileguard.engine.SmsBackup;
import android.ye.mobileguard.utils.ToastUtil;

import java.io.File;

/**
 *
 */
public class ToolsActivity extends Activity {

    private TextView queryadr;
    private TextView smsBackup;
    private TextView queryNumber;
    private TextView lock;
    private ProgressBar pgBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tools_activity);


        //获取权限
        requestPermit();
        onClick();
    }



    private void onClick() {
        //归属地查询
        queryadr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), QueryAdrActivity.class));
            }
        });

        //短信备份
        smsBackup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSmsBackUpDialog();
            }
        });
        //查询常用号码
        queryNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),CommonNumQueryActivity.class);
                startActivity(intent);
            }
        });

        //程序锁
        lock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),AppLockActivity.class);
                startActivity(intent);
            }
        });
    }

    private void showSmsBackUpDialog() {
        //创建进度条对话框
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setIcon(R.mipmap.app_icon);
        dialog.setTitle("短信备份");
        //设置进度条样式
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.show();
        //直接调用备份短信方法
        new Thread() {
            @Override
            public void run() {
                String expath = Environment.getExternalStorageDirectory().getAbsoluteFile() + File.separator+"sms.xml";
               SmsBackup.backUp(getApplication(), expath, new SmsBackup.CallBack() {
                   @Override
                   public void setMax(int max) {
                       dialog.setMax(max);
                       //pgBar.setMax(max);
                   }

                   @Override
                   public void setProgress(int index) {
                       dialog.setProgress(index);
                        //pgBar.setProgress(index);
                   }
               });
               dialog.dismiss();

            }
        }.start();


    }
    private void initUI() {

        queryadr = (TextView) findViewById(R.id.query_phone_address);
        smsBackup = (TextView) findViewById(R.id.sms_backup);
       // pgBar = (ProgressBar) findViewById(R.id.pr_bar);
        queryNumber = (TextView) findViewById(R.id.query_number);
        lock = (TextView) findViewById(R.id.lock);



    }


    private void requestPermit() {
        if (PermissionManager.getInstance().hasPermission(this, Manifest.permission.WRITE_CALL_LOG)) {
            initUI();
        } else {
            if (PermissionManager.getInstance().shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_CALL_LOG)) {
                ToastUtil.show(this, "需要激活权限,进入设置-应用界面开启相应权限");
            } else {
                PermissionManager.getInstance().requestPermission(this, Manifest.permission.WRITE_CALL_LOG, new PermissionCallBack() {

                    @Override
                    public void onGranted(String[] permissions, int[] grantResults) {
                        initUI();
                    }

                    @Override
                    public void onFailed(String[] permissions, int[] grantResults) {
                        ToastUtil.show(getApplicationContext(), "权限获取失败，进入设置-应用界面开启相应权限");
                    }
                });
            }
        }
            if (PermissionManager.getInstance().hasPermission(this, Manifest.permission.READ_CALL_LOG)) {

            } else {
                if (PermissionManager.getInstance().shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CALL_LOG)) {
                    ToastUtil.show(this, "需要激活权限,进入设置-应用界面开启相应权限");
                } else {
                    PermissionManager.getInstance().requestPermission(this, Manifest.permission.READ_CALL_LOG, new PermissionCallBack() {
                        @Override
                        public void onGranted(String[] permissions, int[] grantResults) {

                        }

                        @Override
                        public void onFailed(String[] permissions, int[] grantResults) {
                            ToastUtil.show(getApplicationContext(), "权限获取失败，进入设置-应用界面开启相应权限");
                        }
                    });
                }
            }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        PermissionManager.getInstance().onRequestPermissionResult(requestCode, permissions, grantResults);
    }
}