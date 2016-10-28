package android.ye.mobileguard.Activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.ye.mobileguard.R;
import android.ye.mobileguard.View.SetItemView;
import android.ye.mobileguard.utils.ConstantValue;
import android.ye.mobileguard.utils.SpUtil;
import android.ye.mobileguard.utils.ToastUtil;

/**
 *
 */
public class Set2Activity extends BaseSetActivity {

    private static final int RC_READ_PHONE_STATE_PERM = 123;
    private SetItemView simBind;
    final static String[] PERMISSIONS = new String[]{Manifest.permission.READ_PHONE_STATE};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set2_activity);
        //phoneState();

        initUI();
        if (Build.VERSION.SDK_INT>=23){
            int checkReadPhoneStatePermission = ContextCompat.checkSelfPermission(this,Manifest.permission.READ_PHONE_STATE);
            if (checkReadPhoneStatePermission != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(this, PERMISSIONS, RC_READ_PHONE_STATE_PERM);
                    return;
            }else {
                initBind();
            }
        }else {
            initBind();
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode){
            case RC_READ_PHONE_STATE_PERM:
                if (grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    initBind();
                }else {
                    ToastUtil.show(getApplicationContext(),"获取权限失败");
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void initBind() {
        //回显(读取已有的绑定状态,用作显示,sp中是否存储了sim卡的序列号)
        String simNum = SpUtil.getString(this,ConstantValue.SIM_SERIAL,"");
        //判断序列号是否为空
        if (TextUtils.isEmpty(simNum)){
            simBind.setCheck(false);
        }else {
            simBind.setCheck(true);
        }

        simBind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获取之前的选中状态
                boolean isCheck = simBind.isCheck();
                //再次点击时，对原来的状态取反
                simBind.setCheck(!isCheck);

                if (!isCheck) {

                    //获取手机管理系统服务
                    TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                    //得到SIM卡序列号
                    String simSerial = manager.getSimSerialNumber();

                    if (simSerial == null){
                        //showSimDialog();
                        ToastUtil.show(getApplicationContext(),"请检查SIM卡是否插入");
                        finish();
                    }
                    //存储序列号
                    SpUtil.putString(getApplicationContext(), ConstantValue.SIM_SERIAL, simSerial);



                } else {
                    //清楚根节点
                    SpUtil.remove(getApplicationContext(), ConstantValue.SIM_SERIAL);
                }
            }
        });
    }


    private void initUI() {
        simBind = (SetItemView) findViewById(R.id.SIM_Bind);
    }


    @Override
    public void showPrePage() {
        Intent intent = new Intent(getApplicationContext(),Set1Activity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.pre_in_anim, R.anim.pre_out_anim);
    }

    @Override
    public void showNextPage() {
        String serialNum = SpUtil.getString(this,ConstantValue.SIM_SERIAL,"");
        if (!TextUtils.isEmpty(serialNum)){
            Intent intent = new Intent(getApplicationContext(),Set3Activity.class);
            startActivity(intent);
            finish();
            overridePendingTransition(R.anim.next_in_anim, R.anim.next_out_anim);
        }else {
            ToastUtil.show(this,"请点击绑定SIM卡");
        }
    }


}
