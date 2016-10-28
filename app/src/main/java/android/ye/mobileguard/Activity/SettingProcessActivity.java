package android.ye.mobileguard.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.ye.mobileguard.R;
import android.ye.mobileguard.Service.LockScreenService;
import android.ye.mobileguard.db.domain.ProcessInfo;
import android.ye.mobileguard.engine.ProcessInfoProvider;
import android.ye.mobileguard.utils.ConstantValue;
import android.ye.mobileguard.utils.ServiceUtil;
import android.ye.mobileguard.utils.SpUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 进程设置
 */
public class SettingProcessActivity extends Activity{

    private CheckBox cbSys;
    private CheckBox cbLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.process_setting_activity);
        initHideProcess();
        initLockClear();
    }

    private void initLockClear() {
        cbLock = (CheckBox) findViewById(R.id.cb_lock_clear);
        //根据锁屏服务的是否开启来选中单选框
        boolean runningService = ServiceUtil.isRunning(this, "android.ye.mobileguard.Service.LockScreenService");
        cbLock.setChecked(runningService);
        if (runningService){
            cbLock.setText("锁屏清理已开启");
        }
        else {
            cbLock.setText("锁屏清理已关闭");
        }

        cbLock.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    cbLock.setText("锁屏清理已开启");
                    startService(new Intent(getApplicationContext(),LockScreenService.class));
                } else {
                    cbLock.setText("锁屏清理已关闭");
                    stopService(new Intent(getApplicationContext(), LockScreenService.class));
                }
                //存储点击boolean值
                SpUtil.putBoolean(getApplicationContext(), ConstantValue.LOCK_SCREEN_CLEAR, isChecked);
            }
        });
    }

    private void initHideProcess() {
        cbSys = (CheckBox) findViewById(R.id.cb_system_process);

        boolean showSys = SpUtil.getBoolean(getApplicationContext(),ConstantValue.SHOW_SYS,false);
        cbSys.setChecked(showSys);
        if (showSys){
            cbSys.setText("显示系统进程");
        }else {
            cbSys.setText("隐藏系统进程");
        }
        //设置状态点击
        cbSys.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    cbSys.setText("显示系统进程");
                } else {
                    cbSys.setText("隐藏系统进程");
                }
                //存储点击boolean值
                SpUtil.putBoolean(getApplicationContext(), ConstantValue.SHOW_SYS, isChecked);
            }
        });

    }

}
