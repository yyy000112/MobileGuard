package android.ye.mobileguard.Receiver;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.telephony.SubscriptionManager;
import android.widget.Toast;
import android.ye.mobileguard.R;
import android.ye.mobileguard.Service.LocationService;
import android.ye.mobileguard.utils.ConstantValue;
import android.ye.mobileguard.utils.SpUtil;
import android.ye.mobileguard.utils.ToastUtil;

import java.util.Objects;

import static android.app.admin.DeviceAdminReceiver.ACTION_DEVICE_ADMIN_ENABLED;

/**
 * 开启短信接收服务
 */
public class SmsReceive extends BroadcastReceiver {

    //private static final int RC_RECEIVE_SMS_PERM = 131;
    private SmsMessage sms;
    private DevicePolicyManager mDPm;
    private ComponentName mDevice;


    @Override
    public void onReceive(Context context, Intent intent) {
        mDPm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        mDevice = new ComponentName(context, DeviceAdmin.class);
        boolean setFinish = SpUtil.getBoolean(context, ConstantValue.SET_FINISH, false);


        //判断设置完成
        if (setFinish) {
            //获取短信内容
            Object[] objects = (Object[]) intent.getExtras().get("pdus");
            //2,循环遍历短信过程
            for (Object object : objects) {
                //3,获取短信对象
                SmsMessage sms = SmsMessage.createFromPdu((byte[]) object);
                //4,获取短信对象的基本信息
                String originatingAddress = sms.getOriginatingAddress();
                String messageBody = sms.getMessageBody();
            /*Object[] objects = (Object[]) intent.getSerializableExtra("pdus");
            String format = intent.getStringExtra("format");
            //循环遍历短信
            for (Object object : objects) {
                //获取短信对象
                    sms = SmsMessage.createFromPdu((byte[]) object,format);

                //获取基本信息
                String originalAddr = sms.getOriginatingAddress();
                String messageBody = sms.getMessageBody();*/
                //判断是否包含播放音乐的信息
                if (messageBody.contains("#*alarm*#")) {
                    MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.ylzs);
                    mediaPlayer.setLooping(true);
                    mediaPlayer.start();
                }
                if (messageBody.contains("#*location*#")) {
                    context.startService(new Intent(context, LocationService.class));
                }
                if (messageBody.contains("#*lockcreen*#")) {
                    if (mDPm.isAdminActive(mDevice)) {
                        mDPm.lockNow();
                        mDPm.resetPassword("123", 0);
                    } else {
                        ToastUtil.show(context, "请先激活");
                    }

                    if (messageBody.contains("#*wipedata*#")) {
                        if (mDPm.isAdminActive(mDevice)) {
                            mDPm.wipeData(0);
                            mDPm.wipeData(DevicePolicyManager.WIPE_EXTERNAL_STORAGE);
                        } else {
                            ToastUtil.show(context, "请先激活");
                        }
                    }
                }
            }
        }
    }
}
