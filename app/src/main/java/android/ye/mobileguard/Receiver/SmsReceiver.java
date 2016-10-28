package android.ye.mobileguard.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsMessage;
import android.util.Log;
import android.ye.mobileguard.db.dao.BlackNumberDao;

/**
 * 拦截短信必备
 */
public class SmsReceiver extends BroadcastReceiver {
    private SmsMessage sms;
    //private BlackNumberDao mDao;

    @Override
    public void onReceive(Context context, Intent intent) {
        BlackNumberDao mDao =BlackNumberDao.getInstance(context);
        //获取短信内容,获取发送短信号码，如号码在黑名单中则拦截短信
        Object[] objects = (Object[]) intent.getSerializableExtra("pdus");
        String format = intent.getStringExtra("format");
        //循环遍历短信
        for (Object object : objects) {
            //获取短信对象
            sms = SmsMessage.createFromPdu((byte[]) object, format);

            //获取基本信息
            String originalAddr = sms.getOriginatingAddress();
            String messageBody = sms.getMessageBody();
            //短信接收后默认号码前+86,需要截取
            if (originalAddr.contains("+86")) {
                originalAddr = originalAddr.substring(3);
            }
            //  mDao = BlackNumberDao.getInstance(context);
            int mode = mDao.getMode(originalAddr);

             Log.d("MODE", String.valueOf(mode));
            if (mode == 1 || mode == 3){

                //拦截短信（中断广播）
                abortBroadcast();
                Log.d("IIIIII","已拦截");

            }
        }

    }
}
