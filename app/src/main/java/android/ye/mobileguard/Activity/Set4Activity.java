package android.ye.mobileguard.Activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.ye.mobileguard.Permit.PermissionCallBack;
import android.ye.mobileguard.Permit.PermissionManager;
import android.ye.mobileguard.R;
import android.ye.mobileguard.View.SetItemView;
import android.ye.mobileguard.utils.ConstantValue;
import android.ye.mobileguard.utils.SpUtil;
import android.ye.mobileguard.utils.ToastUtil;

/**
 *
 */
public class Set4Activity extends BaseSetActivity{


    private CheckBox openGuard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set4_activity);
        initUI();
        if(PermissionManager.getInstance().hasPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)){
            initBind();
        }else {
            if (PermissionManager.getInstance().shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_COARSE_LOCATION)){
                ToastUtil.show(this,"需要激活权限,进入设置-应用界面开启相应权限");
            }else {
                PermissionManager.getInstance().requestPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION, new PermissionCallBack() {
                    @Override
                    public void onGranted(String[] permissions, int[] grantResults) {
                        initBind();
                    }

                    @Override
                    public void onFailed(String[] permissions, int[] grantResults) {
                        ToastUtil.show(getApplicationContext(),"权限获取失败，进入设置-应用界面开启相应权限");
                    }
                });
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        PermissionManager.getInstance().onRequestPermissionResult(requestCode, permissions, grantResults);
    }


    private void initBind() {
        //获取已有的开关状态
        boolean open_Guard = SpUtil.getBoolean(getApplicationContext(), ConstantValue.OPEN_GUARD, false);
        openGuard.setChecked(open_Guard);
        //设置不同状态的文字显示
        if (open_Guard){
            openGuard.setText("您已开启防盗保护");
        }else {
            openGuard.setText("您已关闭防盗保护");
        }
        //监听状态改变
        openGuard.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //isCheck为点击后的状态，存储
                SpUtil.putBoolean(getApplicationContext(),ConstantValue.OPEN_GUARD,isChecked);
                //修改文字状态显示
                if (isChecked){
                openGuard.setText("您已开启防盗保护");
            }else {
                openGuard.setText("您已关闭防盗保护");
            }
            }
        });
    }

    private void initUI() {
        openGuard = (CheckBox) findViewById(R.id.open_guard);
    }


    @Override
    public void showPrePage() {
        Intent intent = new Intent(getApplicationContext(),Set3Activity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.pre_in_anim, R.anim.pre_out_anim);
    }

    @Override
    public void showNextPage() {
        boolean openGuard = SpUtil.getBoolean(getApplicationContext(),ConstantValue.OPEN_GUARD,false);
        if (openGuard){
            Intent intent = new Intent(getApplicationContext(),SetFinishActivity.class);
            startActivity(intent);
            finish();
            SpUtil.putBoolean(this, ConstantValue.SET_FINISH, true);
            overridePendingTransition(R.anim.next_in_anim, R.anim.next_out_anim);

        }else {
            ToastUtil.show(getApplicationContext(),"请点击开启防盗保护");
        }
    }

}
