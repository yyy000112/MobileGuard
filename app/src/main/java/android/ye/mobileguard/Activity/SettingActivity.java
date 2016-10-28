package android.ye.mobileguard.Activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Telephony;
import android.renderscript.Sampler;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.ye.mobileguard.Permit.PermissionCallBack;
import android.ye.mobileguard.Permit.PermissionManager;
import android.ye.mobileguard.R;
import android.ye.mobileguard.Service.AddressService;
import android.ye.mobileguard.Service.AppLockService;
import android.ye.mobileguard.Service.BlackNumberService;
import android.ye.mobileguard.View.ClickItemView;
import android.ye.mobileguard.View.SetItemView;
import android.ye.mobileguard.utils.ConstantValue;
import android.ye.mobileguard.utils.ServiceUtil;
import android.ye.mobileguard.utils.SpUtil;
import android.ye.mobileguard.utils.ToastUtil;

/**
 *
 */
public class SettingActivity extends Activity{


    private SetItemView setupdate;
    private SetItemView disaddr;
    private ClickItemView dis_stytle;
    private String[] toastStyle;
    private int toast_stytle;
    private ClickItemView dis_location;
    private SetItemView setBlackNumber;
    private String defaultSmsApp;
    private SetItemView setAppLock;
    private SetItemView blockSms;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_activity);
        //获取默认App的包名并保存,便于恢复短信为默认应用
        defaultSmsApp = Telephony.Sms.getDefaultSmsPackage(this);
        //初始化控件
        initUI();
        //初始化settingItem
        initUpdate();
        //归属地显示
        if(Build.VERSION.SDK_INT>=23){
            if (! Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent,10);
            }
        }
      //初始化归属地吐司
        if(PermissionManager.getInstance().hasPermission(this, Manifest.permission.PROCESS_OUTGOING_CALLS)){
            //初始化数据
            initDisaddr();
        }else {
            if (PermissionManager.getInstance().shouldShowRequestPermissionRationale(this,Manifest.permission.PROCESS_OUTGOING_CALLS)){
                ToastUtil.show(this, "需要激活权限,进入设置-应用界面开启相应权限");
            }else {
                PermissionManager.getInstance().requestPermission(this, Manifest.permission.PROCESS_OUTGOING_CALLS, new PermissionCallBack() {
                    @Override
                    public void onGranted(String[] permissions, int[] grantResults) {
                        //初始化数据
                        initDisaddr();
                    }

                    @Override
                    public void onFailed(String[] permissions, int[] grantResults) {
                        ToastUtil.show(getApplicationContext(),"权限获取失败，进入设置-应用界面开启相应权限");
                    }
                });
            }
        }

        //初始化吐司样式选择
        initToastStyle();
        //初始化归属地吐司显示位置
        initLocation();
        //初始化黑名单服务管理并获取相应权限
        initBlackCatchblackpermission();

        //APP锁服务管理
        initAppLock();

        //拦截所有短信
        initBlockSMS();


    }

    private void initBlockSMS() {
        boolean isRunningService = SpUtil.getBoolean(getApplicationContext(),"android.ye.mobileguard.Service.HeadlessSmsSendService",false);
        blockSms = (SetItemView) findViewById(R.id.set_block_sms);
        blockSms.setCheck(isRunningService);
        blockSms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isCheck = blockSms.isCheck();
                blockSms.setCheck(!isCheck);
                if (!isCheck) {
                    //让用户修改该app为Default SMS app用于拦截短信广播
                    Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
                    intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, getApplication().getPackageName());
                    startActivity(intent);

                } else {

                    //让用户修改回Default SMS app
                    Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
                    intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, defaultSmsApp);
                    startActivity(intent);
                }
            }
        });

    }

    private void initAppLock() {
        setAppLock = (SetItemView) findViewById(R.id.set_app_lock);
        boolean isRunningLock = ServiceUtil.isRunning(this,"android.ye.mobileguard.Service.AppLockService");
        setAppLock.setCheck(isRunningLock);
        setAppLock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isCheck = setAppLock.isCheck();
                setAppLock.setCheck(!isCheck);
                if (!isCheck){
                    ToastUtil.show(getApplicationContext(),"该功能在5.0以上暂停使用");
                    startService(new Intent(getApplicationContext(), AppLockService.class));
                }else {
                    stopService(new Intent(getApplicationContext(),AppLockService.class));
                }
            }
        });


    }

    private void initBlackCatchblackpermission() {
        if(PermissionManager.getInstance().hasPermission(this, Manifest.permission.CALL_PHONE)){
            //初始化数据
            initBlackNumber();
        }else {
            if (PermissionManager.getInstance().shouldShowRequestPermissionRationale(this,Manifest.permission.CALL_PHONE)){
                ToastUtil.show(this, "需要激活权限,进入设置-应用界面开启相应权限");
            }else {
                PermissionManager.getInstance().requestPermission(this, Manifest.permission.CALL_PHONE, new PermissionCallBack() {
                    @Override
                    public void onGranted(String[] permissions, int[] grantResults) {
                        //初始化数据
                        initBlackNumber();
                    }

                    @Override
                    public void onFailed(String[] permissions, int[] grantResults) {
                        ToastUtil.show(getApplicationContext(),"权限获取失败，进入设置-应用界面开启相应权限");
                    }
                });
            }
        }
        if(PermissionManager.getInstance().hasPermission(this, Manifest.permission.READ_CALL_LOG)){
            //初始化数据
            initBlackNumber();
        }else {
            if (PermissionManager.getInstance().shouldShowRequestPermissionRationale(this,Manifest.permission.READ_CALL_LOG)){
                ToastUtil.show(this, "需要激活权限,进入设置-应用界面开启相应权限");
            }else {
                PermissionManager.getInstance().requestPermission(this, Manifest.permission.READ_CALL_LOG, new PermissionCallBack() {
                    @Override
                    public void onGranted(String[] permissions, int[] grantResults) {
                        //初始化数据
                        initBlackNumber();
                    }

                    @Override
                    public void onFailed(String[] permissions, int[] grantResults) {
                        ToastUtil.show(getApplicationContext(),"权限获取失败，进入设置-应用界面开启相应权限");
                    }
                });
            }
        }
        if(PermissionManager.getInstance().hasPermission(this, Manifest.permission.WRITE_CALL_LOG)){
            //初始化数据
            initBlackNumber();
        }else {
            if (PermissionManager.getInstance().shouldShowRequestPermissionRationale(this,Manifest.permission.WRITE_CALL_LOG)){
                ToastUtil.show(this, "需要激活权限,进入设置-应用界面开启相应权限");
            }else {
                PermissionManager.getInstance().requestPermission(this, Manifest.permission.WRITE_CALL_LOG, new PermissionCallBack() {
                    @Override
                    public void onGranted(String[] permissions, int[] grantResults) {
                        //初始化数据
                        initBlackNumber();
                    }

                    @Override
                    public void onFailed(String[] permissions, int[] grantResults) {
                        ToastUtil.show(getApplicationContext(),"权限获取失败，进入设置-应用界面开启相应权限");
                    }
                });
            }
        }
    }

    private void initBlackNumber() {
        boolean isRunning = ServiceUtil.isRunning(this,"android.ye.mobileguard.Service.BlackNumberService");
        setBlackNumber.setCheck(isRunning);
        setBlackNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isCheck = setBlackNumber.isCheck();
                setBlackNumber.setCheck(!isCheck);
                if (!isCheck) {
                    //开启服务
                    ToastUtil.show(getApplicationContext(),"4.4版本以上只能点击使用以下代替短信应用程序来实现短信拦截");
                    startService(new Intent(getApplicationContext(), BlackNumberService.class));
                } else {

                    //停止服务
                    stopService(new Intent(getApplicationContext(), BlackNumberService.class));
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        PermissionManager.getInstance().onRequestPermissionResult(requestCode,permissions,grantResults);
    }


    private void initLocation() {
        dis_location = (ClickItemView) findViewById(R.id.dis_location);
        dis_location.setTitle("归属地提示框位置");
        dis_location.setDes("设置归属地提示框位置");
        dis_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),ToastLocationActivity.class));
            }
        });
    }

    private void initToastStyle() {
        dis_stytle = (ClickItemView) findViewById(R.id.dis_style);
        dis_stytle.setTitle("设置归属地显示风格");
        //创建文字描述
        toastStyle = new String[]{"透明","橙色","蓝色","绿色","灰色"};
        toast_stytle = SpUtil.getInt(this, ConstantValue.TOAST_STYTLE, 0);
        //通过索引获取字符串数字的显示
        dis_stytle.setDes(toastStyle[toast_stytle]);
        //监听点击事件，弹出对话框
        dis_stytle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //显示吐司样式对话框
                showToastStytleDialog();
            }
        });
    }

    /**
     * 创建显示样式的对话框
     */
    private void showToastStytleDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.mipmap.app_icon);
        builder.setTitle("请选择归属地样式");
        //选择单个条目的事件监听(1.颜色描述，2，条目索引值，3，点击触发事件）
        builder.setSingleChoiceItems(toastStyle, toast_stytle, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //1.记录选中条目索引值
                SpUtil.putInt(getApplicationContext(), ConstantValue.TOAST_STYTLE, which);
                //2.关闭对话框
                dialog.dismiss();
                //3.显示色值的文字
                dis_stytle.setDes(toastStyle[which]);
            }
        });
        //添加消极按钮
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 10) {
            if (!Settings.canDrawOverlays(this)) {
                // SYSTEM_ALERT_WINDOW permission not granted...
                Toast.makeText(this,"not granted",Toast.LENGTH_SHORT);
            }
        }
    }

    private void initUpdate() {
        //获取已有的开关状态
        boolean auto_Update = SpUtil.getBoolean(getApplicationContext(),ConstantValue.AUTO_UPDATE,false);
        //根据上一次存储结果去做决定
        setupdate.setCheck(auto_Update);

        setupdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获取之前的选中状态
                boolean isCheck = setupdate.isCheck();
                //再次点击时，对原来的状态取反
                setupdate.setCheck(!isCheck);

                SpUtil.putBoolean(getApplicationContext(), ConstantValue.AUTO_UPDATE, !isCheck);
            }
        });
    }

    private void initDisaddr(){
        boolean isRunning = ServiceUtil.isRunning(this,"android.ye.mobileguard.Service.AddressService");
        disaddr.setCheck(isRunning);
        disaddr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isCheck = disaddr.isCheck();
                disaddr.setCheck(!isCheck);
                if (!isCheck){
                    startService(new Intent(getApplicationContext(), AddressService.class));
                }else {
                    stopService(new Intent(getApplicationContext(),AddressService.class));
                }
            }
        });
    }
    private void initUI() {
        setupdate = (SetItemView) findViewById(R.id.set_update);
        disaddr = (SetItemView) findViewById(R.id.dis_addr);
        setBlackNumber = (SetItemView) findViewById(R.id.set_black_number);

    }

}
