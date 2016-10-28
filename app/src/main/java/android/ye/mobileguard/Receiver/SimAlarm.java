package android.ye.mobileguard.Receiver;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.ye.mobileguard.utils.ConstantValue;
import android.ye.mobileguard.utils.SpUtil;
import android.ye.mobileguard.utils.ToastUtil;

/**
 * Sim卡变更广播接收器
 */
public class SimAlarm extends BroadcastReceiver {
    private static final int RC_SEND_SMS_PERM = 111;

    @Override
    public void onReceive(Context context, Intent intent) {
        String simSerial = SpUtil.getString(context, ConstantValue.SIM_SERIAL, "");
        TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        //获取当前的SIM卡序列号
        String currentSim = manager.getSimSerialNumber();
        //获取存储的安全号码
        String phone = SpUtil.getString(context, ConstantValue.PHONE_NUM, "");
        if (!currentSim.equals(simSerial)){
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phone, null, "SIM card is changed,new SIMSerials is"+currentSim, null, null);

        }
    }





}
