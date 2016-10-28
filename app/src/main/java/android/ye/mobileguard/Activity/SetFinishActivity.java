package android.ye.mobileguard.Activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.ye.mobileguard.Permit.PermissionCallBack;
import android.ye.mobileguard.Permit.PermissionManager;
import android.ye.mobileguard.R;
import android.ye.mobileguard.utils.ConstantValue;
import android.ye.mobileguard.utils.SpUtil;
import android.ye.mobileguard.utils.ToastUtil;

import java.util.ArrayList;


/**
 * 手机防盗界面
 */
public class SetFinishActivity extends Activity{


    private static final int RC_SEND_SMS_PERM = 121;
    private static final int RC_LOCATION_PERM = 122;

    private TextView safeNum;
    private TextView settingGuid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //判断是否已经设置完成，设置完成时停留在该页面，否则跳回第一个设置页面
        boolean setFinish = SpUtil.getBoolean(this, ConstantValue.SET_FINISH, false);
        if (setFinish) {
            if(PermissionManager.getInstance().hasPermission(this, Manifest.permission.SEND_SMS)){
                setContentView(R.layout.set_finish_activity);
                initUI();
            }else {
                if (PermissionManager.getInstance().shouldShowRequestPermissionRationale(this,Manifest.permission.SEND_SMS)){
                    ToastUtil.show(this,"需要激活权限,进入设置-应用界面开启相应权限");
                }else {
                    PermissionManager.getInstance().requestPermission(this, Manifest.permission.SEND_SMS, new PermissionCallBack() {
                        @Override
                        public void onGranted(String[] permissions, int[] grantResults) {
                            setContentView(R.layout.set_finish_activity);
                            initUI();
                        }

                        @Override
                        public void onFailed(String[] permissions, int[] grantResults) {
                            ToastUtil.show(getApplicationContext(),"权限获取失败，进入设置-应用界面开启相应权限");
                        }
                    });
                }
            }
            if(PermissionManager.getInstance().hasPermission(this, Manifest.permission.RECEIVE_SMS)){

            }else {
                if (PermissionManager.getInstance().shouldShowRequestPermissionRationale(this,Manifest.permission.RECEIVE_SMS)){
                    ToastUtil.show(this,"需要激活权限,进入设置-应用界面开启相应权限");
                }else {
                    PermissionManager.getInstance().requestPermission(this, Manifest.permission.RECEIVE_SMS, new PermissionCallBack() {
                        @Override
                        public void onGranted(String[] permissions, int[] grantResults) {

                        }

                        @Override
                        public void onFailed(String[] permissions, int[] grantResults) {
                            ToastUtil.show(getApplicationContext(),"权限获取失败，进入设置-应用界面开启相应权限");
                        }
                    });
                }
            }
            if(PermissionManager.getInstance().hasPermission(this, Manifest.permission.READ_SMS)){

            }else {
                if (PermissionManager.getInstance().shouldShowRequestPermissionRationale(this,Manifest.permission.READ_SMS)){
                    ToastUtil.show(this,"需要激活权限,进入设置-应用界面开启相应权限");
                }else {
                    PermissionManager.getInstance().requestPermission(this, Manifest.permission.READ_SMS, new PermissionCallBack() {
                        @Override
                        public void onGranted(String[] permissions, int[] grantResults) {

                        }

                        @Override
                        public void onFailed(String[] permissions, int[] grantResults) {
                            ToastUtil.show(getApplicationContext(),"权限获取失败，进入设置-应用界面开启相应权限");
                        }
                    });
                }
            }



        } else {
            Intent intent = new Intent(this, Set1Activity.class);
            startActivity(intent);
            finish();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        PermissionManager.getInstance().onRequestPermissionResult(requestCode, permissions, grantResults);
    }


    private void initUI() {
        safeNum = (TextView) findViewById(R.id.safe_num);
        settingGuid = (TextView) findViewById(R.id.setting_guid);
        //设置联系人号码
        String safeNumber = SpUtil.getString(getApplicationContext(), ConstantValue.PHONE_NUM,"");

        safeNum.setText(safeNumber);

        settingGuid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SetFinishActivity.this, Set1Activity.class);
                startActivity(intent);
                finish();
            }
        });
    }


}
